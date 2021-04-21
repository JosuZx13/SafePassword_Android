package pdm.safepassword.init;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import pdm.safepassword.R;
import pdm.safepassword.login.FragmentFloatingPassword;

public class PinCode extends FragmentActivity {

    private final int PINCODE_LENGTH = 4;
    private int num_digits = 0; // Número de digitos que se han escrito
    private String pin_enter = "";
    private int pin = -100;
    private int request;

    // Donde aparecerá el digito escrito
    private TextInputEditText digit0;
    private TextInputEditText digit1;
    private TextInputEditText digit2;
    private TextInputEditText digit3;

    private TextView numpad_0;
    private TextView numpad_1;
    private TextView numpad_2;
    private TextView numpad_3;
    private TextView numpad_4;
    private TextView numpad_5;
    private TextView numpad_6;
    private TextView numpad_7;
    private TextView numpad_8;
    private TextView numpad_9;

    private ImageView numpad_clear;
    private ImageView numpad_exit;

    private List<String> numpad_digits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.view_asking_pin);

        request = getIntent().getIntExtra("REQUEST", -100);

        digit0 = findViewById(R.id.digit0);
        digit1 = findViewById(R.id.digit1);
        digit2 = findViewById(R.id.digit2);
        digit3 = findViewById(R.id.digit3);

        numpad_0 = findViewById(R.id.numpad_0);
        numpad_1 = findViewById(R.id.numpad_1);
        numpad_2 = findViewById(R.id.numpad_2);
        numpad_3 = findViewById(R.id.numpad_3);
        numpad_4 = findViewById(R.id.numpad_4);
        numpad_5 = findViewById(R.id.numpad_5);
        numpad_6 = findViewById(R.id.numpad_6);
        numpad_7 = findViewById(R.id.numpad_7);
        numpad_8 = findViewById(R.id.numpad_8);
        numpad_9 = findViewById(R.id.numpad_9);

        numpad_digits = Arrays.asList(getResources().getStringArray(R.array.numpad_digits) );

        Collections.shuffle(numpad_digits); // Se reordenan los elementos del Array de forma aleatoria

        numpad_0.setText(numpad_digits.get(0));
        numpad_1.setText(numpad_digits.get(1));
        numpad_2.setText(numpad_digits.get(2));
        numpad_3.setText(numpad_digits.get(3));
        numpad_4.setText(numpad_digits.get(4));
        numpad_5.setText(numpad_digits.get(5));
        numpad_6.setText(numpad_digits.get(6));
        numpad_7.setText(numpad_digits.get(7));
        numpad_8.setText(numpad_digits.get(8));
        numpad_9.setText(numpad_digits.get(9));

        numpad_clear = findViewById(R.id.numpad_clear);
        numpad_exit = findViewById(R.id.numpad_exit);

        // Como los diez botones con número tienen la misma funcionalidad, se hace un Listener genérico
        View.OnClickListener numPressed = new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                TextView numpad_x = (TextView) v;

                switch (num_digits) {

                    case 0:
                        digit0.setText(numpad_x.getText());
                        break;

                    case 1:
                        digit1.setText(numpad_x.getText());
                        break;

                    case 2:
                        digit2.setText(numpad_x.getText());
                        break;

                    case 3:
                        digit3.setText(numpad_x.getText());
                        break;

                }
                pin_enter = pin_enter.concat(numpad_x.getText().toString());
                num_digits++;

                if(num_digits == PINCODE_LENGTH){
                    try {
                        pin = Integer.parseInt(pin_enter);

                        Intent intent = new Intent();
                        intent.putExtra("PIN-INTRODUCED", pin);
                        setResult(request, intent);
                        finish();

                    }catch(NumberFormatException nfe) {
                        System.err.println("Unable to Parse " + nfe);
                    }
                }

            };
        };

        numpad_0.setOnClickListener(numPressed);
        numpad_1.setOnClickListener(numPressed);
        numpad_2.setOnClickListener(numPressed);
        numpad_3.setOnClickListener(numPressed);
        numpad_4.setOnClickListener(numPressed);
        numpad_5.setOnClickListener(numPressed);
        numpad_6.setOnClickListener(numPressed);
        numpad_7.setOnClickListener(numPressed);
        numpad_8.setOnClickListener(numPressed);
        numpad_9.setOnClickListener(numPressed);

        // Elimina todos los números introducidos
        numpad_clear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                num_digits = 0;
                digit0.setText("");
                digit1.setText("");
                digit2.setText("");
                digit3.setText("");
            }
        });

        // Elimina todos los números introducidos
        numpad_exit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("PIN-INTRODUCED", -100);
                setResult(-100, intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent();
        intent.putExtra("PIN-INTRODUCED", -100);
        setResult(request, intent);
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

}

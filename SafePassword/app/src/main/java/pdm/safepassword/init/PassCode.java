package pdm.safepassword.init;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.textfield.TextInputEditText;

import pdm.safepassword.R;
import pdm.safepassword.login.ActivityLogin;
import pdm.safepassword.login.FragmentFloatingPassword;

public class PassCode extends FragmentActivity {

    private int request;
    private String pass_enter = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        request = getIntent().getIntExtra("REQUEST", -100);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getString(R.string.required_password_continue));
        alert.setIcon(R.drawable.ic_baseline_info_red);
        final EditText input = new EditText(this);
        // Se modifica el tipo para que sea de tipo password y solo números
        input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setMaxLines(1);
        // Se crea un filtro para que no sea posible añadir más de 4 números
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(24)});
        // Así aparece en el centro
        input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        input.setLayoutParams(new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.MATCH_PARENT));
        // Se define un edittext para aplicarlo al cuadro de diálogo
        alert.setView(input);
        alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                pass_enter = input.getText().toString();

                Intent intent = new Intent();
                intent.putExtra("PASS-INTRODUCED", pass_enter);
                setResult(request, intent);
                finish();

            }
        });
        alert.setNegativeButton(getString(R.string.cancelar), null);

        alert.show();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

}

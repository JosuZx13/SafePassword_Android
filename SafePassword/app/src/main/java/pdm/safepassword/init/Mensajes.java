package pdm.safepassword.init;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import java.util.Calendar;

import pdm.safepassword.R;
import pdm.safepassword.login.ActivityLogin;
import pdm.safepassword.login.FragmentFloatingPassword;

public class Mensajes extends FragmentActivity {

    Activity frontActivity;

    public Mensajes(Activity a){
        frontActivity = a;
    }

    public String getDate(){
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        // Se suma uno porque devuelve entre 0 y 11
        int month = c.get(Calendar.MONTH)+1;
        int year = c.get(Calendar.YEAR);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        return month + "" + day + "" + year + "_" + hour + "-" + minute + "-" + second;
    }

    public void toastAdvice(String t){
        LayoutInflater inflater = frontActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.layout_toast, frontActivity.findViewById(R.id.custom_toast_container));

        TextView text = layout.findViewById(R.id.text);
        text.setText(t);

        Toast toast = new Toast(frontActivity);
        toast.setGravity(Gravity.BOTTOM, 0, 250);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    public void alertNewPin(){
        AlertDialog.Builder alert = new AlertDialog.Builder(frontActivity);
        alert.setTitle(frontActivity.getString(R.string.pinconde_register));
        // Se define un edittext para aplicarlo al cuadro de diálogo
        final EditText input = new EditText(frontActivity);
        // Se modifica el tipo para que sea de tipo password y solo números
        input.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setMaxLines(1);
        // Se crea un filtro para que no sea posible añadir más de 4 números
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        // Así aparece en el centro
        input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        input.setLayoutParams(new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.MATCH_PARENT));
        // Se cambia el teclado que aparece para que sea solo numérico
        input.setRawInputType(Configuration.KEYBOARD_12KEY);
        alert.setView(input);
        alert.setPositiveButton(frontActivity.getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                String pin_texto = input.getText().toString();

                if(pin_texto.length() != 4){
                    toastAdvice(frontActivity.getString(R.string.pinconde_not_enough_digit));
                    alertNewPin();
                }else{
                    int pin_introduced = Integer.parseInt(input.getText().toString());

                    if(ActivityMain.database.UpdatePinUserDatabase(ActivityLogin.OWNER_SESSION, pin_introduced)){
                        ActivityLogin.OWNER_PIN = pin_introduced; //Se actualiza este valor
                        toastAdvice(frontActivity.getString(R.string.pinconde_save_correctly));
                    }else{
                        toastAdvice(frontActivity.getString(R.string.mensaje_generico_error_safepassword));
                    }
                }
            }
        });

        alert.setNegativeButton(frontActivity.getString(R.string.cancelar), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                toastAdvice(frontActivity.getString(R.string.pinconde_not_registered));
            }
        });
        alert.show();
    }
}

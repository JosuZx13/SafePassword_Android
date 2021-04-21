package pdm.safepassword.login;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import pdm.safepassword.R;
import pdm.safepassword.adapter.AdapterSpinnerIcon;
import pdm.safepassword.database.PasswordDatabase;
import pdm.safepassword.init.ActivityMain;
import pdm.safepassword.init.Mensajes;

public class FragmentFloatingNewPassword extends FragmentActivity {

    private Mensajes mensaje;

    View layout;

    // Se va a guardar el identificador de todos los drawable icon para las contraseñas
    protected static Integer[] IconDataset = {
            R.drawable.ic_spn_amazon,
            R.drawable.ic_spn_credit_card,
            R.drawable.ic_spn_email,
            R.drawable.ic_spn_facebook,
            R.drawable.ic_spn_gmail,
            R.drawable.ic_spn_instagram,
            R.drawable.ic_spn_microsoft_outlook,
            R.drawable.ic_spn_netflix,
            R.drawable.ic_spn_other,
            R.drawable.ic_spn_personal,
            R.drawable.ic_spn_reddit,
            R.drawable.ic_spn_steam,
            R.drawable.ic_spn_tiktok,
            R.drawable.ic_spn_twitch,
            R.drawable.ic_spn_twitter,
            R.drawable.ic_spn_wifi,
            R.drawable.ic_spn_work
    };

    Spinner password_new_logo;
    EditText password_new_name;
    EditText password_new_user;
    EditText password_new_pass;
    Spinner password_new_categ;
    EditText password_new_url;

    ImageView iv_add_new_pass;
    ImageView iv_none_pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_new_password);

        mensaje = new Mensajes(this);

        layout = findViewById(R.id.layout_new_pass);

        password_new_logo = findViewById(R.id.new_spinner_logo);
        password_new_name = findViewById(R.id.new_name);
        password_new_user = findViewById(R.id.new_user);
        password_new_pass = findViewById(R.id.new_pass);
        password_new_categ = findViewById(R.id.new_spinner_categ);
        password_new_url = findViewById(R.id.new_url);

        iv_add_new_pass = findViewById(R.id.fab_add_done_password);
        iv_none_pass = findViewById(R.id.fab_add_none_password);

        AdapterSpinnerIcon adapterSpinnerLogo = new AdapterSpinnerIcon(this, IconDataset);
        adapterSpinnerLogo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        password_new_logo.setAdapter(adapterSpinnerLogo);

        ArrayAdapter<CharSequence> adapterSpinnner = ArrayAdapter.createFromResource(this, R.array.category, android.R.layout.simple_spinner_item);
        adapterSpinnner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        password_new_categ.setAdapter(adapterSpinnner);

        iv_add_new_pass.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                int newLogo = -10;
                try {
                    newLogo = Integer.parseInt(password_new_logo.getSelectedItem().toString());

                }catch(NumberFormatException nfe) {
                    System.err.println("Unable to Parse " + nfe);
                    newLogo = -10;
                }

                String newName = password_new_name.getText().toString();
                String newUser = password_new_user.getText().toString();
                String newPass = password_new_pass.getText().toString();
                String newCateg = password_new_categ.getSelectedItem().toString();
                String newUrl = password_new_url.getText().toString();

                if(newPass.length() >= ActivityMain.MINIMUM_LENGTH_CHARACTERS){

                    PasswordDatabase exist = ActivityMain.database.SelectPasswordDatabase(newName);

                    // A la hora de añadir una contraseña nueva, solo puede ser nueva
                    // La busqueda hecha no puede devolver una contraseña existente
                    if (exist.getPswID() == -500) {

                        boolean correcto = ActivityMain.database.InsertDataPassword(newName, newUser, newPass, newCateg, newUrl, newLogo);

                        if (correcto) {
                            mensaje.toastAdvice(getString(R.string.mensaje_password_new_password_added));
                        } else {
                            mensaje.toastAdvice(getString(R.string.mensaje_generico_error_safepassword));
                        }

                        finish();

                    } else {
                        // En cualquier otro caso, el nombre asociado a la contraseña es incorrecto
                        // Ya que está siendo usado en otra contraseña
                        mensaje.toastAdvice(getString(R.string.mensaje_password_name_incorrecto));
                    }

                }else{
                    mensaje.toastAdvice(getString(R.string.mensaje_password_minimo_caracteres));
                }
            }
        });

        iv_none_pass.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
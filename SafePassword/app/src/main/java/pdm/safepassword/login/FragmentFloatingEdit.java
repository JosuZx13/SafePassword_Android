package pdm.safepassword.login;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import pdm.safepassword.adapter.AdapterSpinnerIcon;
import pdm.safepassword.init.ActivityMain;
import pdm.safepassword.R;
import pdm.safepassword.database.PasswordDatabase;

public class FragmentFloatingEdit extends FragmentActivity {



    PasswordDatabase editPass;

    View layout;

    Spinner sp_pass_edit_logo;
    EditText fg_tv_name_edit;
    EditText fg_tv_user_edit;
    EditText fg_tv_pass_edit;
    Spinner sp_pass_edit_categ;
    EditText fg_tv_url_edit;

    ImageView iv_done_pass;
    ImageView iv_none_pass;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_floating_edit);

        layout = findViewById(R.id.layout_floating_pass_edit);

        sp_pass_edit_logo = findViewById(R.id.edit_spinner_logo);
        fg_tv_name_edit = findViewById(R.id.edit_name);
        fg_tv_user_edit = findViewById(R.id.edit_user);
        fg_tv_pass_edit = findViewById(R.id.edit_pass);
        sp_pass_edit_categ = findViewById(R.id.edit_spinner_categ);
        fg_tv_url_edit = findViewById(R.id.edit_url);

        iv_done_pass = findViewById(R.id.floating_done_pass);
        iv_none_pass = findViewById(R.id.fab_none_password);

        AdapterSpinnerIcon adapterSpinnerLogo = new AdapterSpinnerIcon(this, FragmentFloatingNewPassword.IconDataset);
        adapterSpinnerLogo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_pass_edit_logo.setAdapter(adapterSpinnerLogo);

        ArrayAdapter<CharSequence> adapterSpinnner = ArrayAdapter.createFromResource(this, R.array.category, android.R.layout.simple_spinner_item);
        adapterSpinnner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_pass_edit_categ.setAdapter(adapterSpinnner);

        // Se recoge el objeto pasado en el Intent de ActivityLogin
        editPass = (PasswordDatabase) getIntent().getSerializableExtra("PASS-EDIT");

        //Para que aparezca el spinner con el item seleccionado correspondiente a su valor en la base de datos
        sp_pass_edit_logo.setSelection(adapterSpinnerLogo.getPosition(editPass.getPswLogo()));
        fg_tv_name_edit.setText(editPass.getPswName());
        fg_tv_user_edit.setText(editPass.getPswUser());
        fg_tv_pass_edit.setText(editPass.getPswPass());
        //Para que aparezca el spinner con el item seleccionado correspondiente a su valor en la base de datos
        sp_pass_edit_categ.setSelection(adapterSpinnner.getPosition(editPass.getPswCategory()));
        if(!editPass.getPswUrl().equals("-u")){
            fg_tv_url_edit.setText(editPass.getPswUrl());
        }else{
            fg_tv_url_edit.setText("");
        }


        iv_done_pass.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int newLogo = -10;
                try {
                    newLogo = Integer.parseInt(sp_pass_edit_logo.getSelectedItem().toString());

                }catch(NumberFormatException nfe) {
                    System.err.println("Unable to Parse " + nfe);
                    newLogo = -10;
                }
                String newName = fg_tv_name_edit.getText().toString();
                String newUser = fg_tv_user_edit.getText().toString();
                String newPass = fg_tv_pass_edit.getText().toString();
                String newCateg = sp_pass_edit_categ.getSelectedItem().toString();
                String newUrl = fg_tv_url_edit.getText().toString();

                PasswordDatabase exist = ActivityMain.database.SelectPasswordDatabase(newName);

                //Se está modificando la misma contraseña o el nombre asociado es nuevo
                if (exist.getPswID() == editPass.getPswID() || exist.getPswID() == -500){

                    if (newPass.length() >= ActivityMain.MINIMUM_LENGTH_CHARACTERS){
                        if(ActivityMain.database.UpdatePassDatabase(editPass.getPswID(), newName, newUser, newPass, newCateg, newUrl, newLogo)){
                            toastAdvice(getString(R.string.mensaje_password_edited));
                        }else{
                            toastAdvice(getString(R.string.mensaje_generico_error_safepassword));
                        }

                        finish();

                    }else{
                        toastAdvice(getString(R.string.mensaje_password_minimo_caracteres));
                    }

                }else{
                    // En cualquier otro caso, el nombre asociado a la contraseña es incorrecto
                    // Ya que está siendo usado en otra contraseña
                    toastAdvice(getString(R.string.mensaje_password_name_incorrecto));
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

    private void toastAdvice(String t){
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.layout_toast,
                (ViewGroup) findViewById(R.id.custom_toast_container));

        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(t);

        Toast toast = new Toast(FragmentFloatingEdit.this);
        toast.setGravity(Gravity.BOTTOM, 0, 250);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
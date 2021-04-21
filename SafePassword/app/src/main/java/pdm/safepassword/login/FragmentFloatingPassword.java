package pdm.safepassword.login;

import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.text.InputType;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import pdm.safepassword.init.ActivityMain;
import pdm.safepassword.R;
import pdm.safepassword.database.PasswordDatabase;
import pdm.safepassword.init.Mensajes;
import pdm.safepassword.init.PassCode;
import pdm.safepassword.init.PinCode;

public class FragmentFloatingPassword extends FragmentActivity {

    PasswordDatabase loadPass;
    ImageView im_hideshow_logo;
    TextView fg_tv_name;
    TextView fg_tv_user;
    TextInputLayout input_layout_password;
    TextInputEditText fg_tv_pass;
    TextView fg_tv_url;
    TextView fg_tv_categ;

    private boolean show;
    private int autoRequest;
    public static final int REQUEST_CODE_PIN_SHOWPASSWORD = 13;
    public static final int REQUEST_CODE_PIN_EDITPASSWORD = 14;
    public static final int REQUEST_CODE_PIN_DELETEPASSWORD = 15;

    public static final int REQUEST_CODE_PASS_SHOWPASSWORD = 23;
    public static final int REQUEST_CODE_PASS_EDITPASSWORD = 24;
    public static final int REQUEST_CODE_PASS_DELETEPASSWORD = 25;

    private Mensajes mensaje;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_floating_hideshow);

        mensaje = new Mensajes(this);

        show = false;

        im_hideshow_logo = findViewById(R.id.hideshow_logo);
        fg_tv_name = findViewById(R.id.hideshow_name);
        fg_tv_user = findViewById(R.id.hideshow_user);
        input_layout_password = findViewById(R.id.input_layout_hideshow_pass);
        fg_tv_pass = findViewById(R.id.hideshow_pass);
        fg_tv_url = findViewById(R.id.hideshow_url);
        fg_tv_categ = findViewById(R.id.hideshow_categ);

        input_layout_password.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!show){

                    new AlertDialog.Builder(FragmentFloatingPassword.this)
                        .setIcon(R.drawable.ic_baseline_domain_verification)
                        .setTitle(getResources().getString(R.string.app_verification))
                        .setPositiveButton(getResources().getString(R.string.prefer_password), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent askPass = new Intent(FragmentFloatingPassword.this, PassCode.class);
                                askPass.putExtra("REQUEST", REQUEST_CODE_PASS_SHOWPASSWORD);
                                startActivityForResult(askPass, REQUEST_CODE_PASS_SHOWPASSWORD);
                            }

                        })
                        .setNegativeButton(getResources().getString(R.string.prefer_pin_code), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if(ActivityLogin.OWNER_PIN != -1000){

                                    // YA existe pin, así que se pregunta por él
                                    Intent askPin = new Intent(FragmentFloatingPassword.this, PinCode.class);
                                    askPin.putExtra("REQUEST", REQUEST_CODE_PIN_SHOWPASSWORD);
                                    startActivityForResult(askPin, REQUEST_CODE_PIN_SHOWPASSWORD);

                                }else{
                                    // No hay pin, así que hay que pedirlo
                                    mensaje.alertNewPin();
                                }

                            }
                        })
                        .show();

                }else{
                    fg_tv_pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    show = false;
                }

            }
        });

        // Se recoge el objeto pasado en el Intent de ActivityPassword
        loadPass = (PasswordDatabase) getIntent().getSerializableExtra("ID-PASS");

        im_hideshow_logo.setImageResource(loadPass.getPswLogo());
        fg_tv_name.setText(loadPass.getPswName());
        fg_tv_user.setText(loadPass.getPswUser());
        fg_tv_pass.setText(loadPass.getPswPass());
        fg_tv_categ.setText(loadPass.getPswCategory());

        if(!loadPass.getPswUrl().equals("-u")){
            fg_tv_url.setText(loadPass.getPswUrl());
        }else{
            fg_tv_url.setText("");
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int code;
        String pass;

        switch (requestCode){

            case REQUEST_CODE_PIN_SHOWPASSWORD:

                code = data.getIntExtra("PIN-INTRODUCED", -100);

                if(code == ActivityLogin.OWNER_PIN) {

                    fg_tv_pass.setInputType(InputType.TYPE_CLASS_TEXT);
                    show = true;

                }else{
                    if(code != -100){
                        mensaje.toastAdvice(getString(R.string.pincode_error));
                    }
                }

                break;

            case REQUEST_CODE_PIN_EDITPASSWORD:

                code = data.getIntExtra("PIN-INTRODUCED", -100);

                if(code == ActivityLogin.OWNER_PIN) {

                    Intent editPass = new Intent(FragmentFloatingPassword.this, FragmentFloatingEdit.class);
                    // Se le aplica una animacion
                    ActivityOptions animacion = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.fade_in, R.anim.fade_out);
                    //Se añade un contenido extra al Intent
                    editPass.putExtra("PASS-EDIT", loadPass);
                    //Se inicializa
                    startActivity(editPass, animacion.toBundle());
                    finish();

                }else{
                    if(code != -100){
                        mensaje.toastAdvice(getString(R.string.pincode_error));
                    }
                }

                break;

            case REQUEST_CODE_PIN_DELETEPASSWORD:

                code = data.getIntExtra("PIN-INTRODUCED", -100);

                if(code == ActivityLogin.OWNER_PIN) {

                    new AlertDialog.Builder(FragmentFloatingPassword.this)
                            .setIcon(R.drawable.ic_baseline_info_red)
                            .setTitle(getResources().getString(R.string.app_borrar_password))
                            .setMessage(getResources().getString(R.string.borrar_password_mensaje))
                            .setPositiveButton(getResources().getString(R.string.aceptar), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    if(ActivityMain.database.DeletePasswordDatabase(loadPass.getPswID())){
                                        mensaje.toastAdvice(getString(R.string.mensaje_password_deleted));
                                    }else{
                                        mensaje.toastAdvice(getString(R.string.mensaje_generico_error_safepassword));
                                    }

                                    finish();
                                }

                            }).setNegativeButton(getResources().getString(R.string.cancelar), null).show();

                }else{
                    if(code != -100){
                        mensaje.toastAdvice(getString(R.string.pincode_error));
                    }
                }

                break;

            case REQUEST_CODE_PASS_SHOWPASSWORD:

                pass = data.getStringExtra("PASS-INTRODUCED");

                if(pass.equals(ActivityLogin.OWNER_PASS)){

                    fg_tv_pass.setInputType(InputType.TYPE_CLASS_TEXT);
                    show = true;

                }else{
                    mensaje.toastAdvice(getString(R.string.mensaje_usuario_password_incorrecta));
                }


                break;

            case REQUEST_CODE_PASS_EDITPASSWORD:

                pass = data.getStringExtra("PASS-INTRODUCED");

                if(pass.equals(ActivityLogin.OWNER_PASS)){

                    Intent editPass = new Intent(FragmentFloatingPassword.this, FragmentFloatingEdit.class);
                    // Se le aplica una animacion
                    ActivityOptions animacion = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.fade_in, R.anim.fade_out);
                    //Se añade un contenido extra al Intent
                    editPass.putExtra("PASS-EDIT", loadPass);
                    //Se inicializa
                    startActivity(editPass, animacion.toBundle());
                    finish();

                }else{
                    mensaje.toastAdvice(getString(R.string.mensaje_usuario_password_incorrecta));
                }

                break;

            case REQUEST_CODE_PASS_DELETEPASSWORD:

                pass = data.getStringExtra("PASS-INTRODUCED");

                if(pass.equals(ActivityLogin.OWNER_PASS)){

                    new AlertDialog.Builder(FragmentFloatingPassword.this)
                        .setIcon(R.drawable.ic_baseline_info_red)
                        .setTitle(getResources().getString(R.string.app_borrar_password))
                        .setMessage(getResources().getString(R.string.borrar_password_mensaje))
                        .setPositiveButton(getResources().getString(R.string.aceptar), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if(ActivityMain.database.DeletePasswordDatabase(loadPass.getPswID())){
                                    mensaje.toastAdvice(getString(R.string.mensaje_password_deleted));
                                }else{
                                    mensaje.toastAdvice(getString(R.string.mensaje_generico_error_safepassword));
                                }

                                finish();
                            }

                        }).setNegativeButton(getResources().getString(R.string.cancelar), null).show();

                }else{
                    mensaje.toastAdvice(getString(R.string.mensaje_usuario_password_incorrecta));
                }

                break;

            default:
                break;
        }

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    // Metodo para dotar de funcionalidad al pulsar el textview de la URL
    public void openURL(View view) {

        String url = fg_tv_url.getText().toString();

        if(!url.equals("")) {

            //En el caso de que el usuario la haya guardado sin su correspondiente protocolo HTTP
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }

            try {
                Uri uri = Uri.parse(url);
                Intent browser = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(browser);
                finish();

            } catch (ActivityNotFoundException e) {
                mensaje.toastAdvice("No application can handle this request."
                        + " Please install a webbrowser");
                e.printStackTrace();
            }
        }
    }
}
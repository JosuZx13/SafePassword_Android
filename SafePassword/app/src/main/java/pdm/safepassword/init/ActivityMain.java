package pdm.safepassword.init;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.Executor;

import pdm.safepassword.R;
import pdm.safepassword.database.DatabaseSafePassword;
import pdm.safepassword.database.UserDatabase;
import pdm.safepassword.login.ActivityLogin;

public class ActivityMain extends AppCompatActivity {

    // Se inicia la Base de Datos que contiene la información de la app
    // Static para poder usarla en cualquier Activity con los datos cargados
    public static DatabaseSafePassword database;

    public final static int MINIMUM_LENGTH_CHARACTERS = 4;
    SecreKeySafePassword SafeKey;

    View layout;

    EditText passwordEntry;
    Button loggingButton;
    Button registerButton;
    Button importButton;

    //Atributos necesarios para la autenticación biométrica
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    // Ventana en la que aparece la entrada de la huella
    private BiometricPrompt.PromptInfo promptInfo;

    // La Huella pese a estar, no está disponible
    public static final int BIOMETRIC_ERROR_HW_UNAVAILABLE = 1;
    // El movil no tiene una huella registrada en el sistema
    public static final int BIOMETRIC_ERROR_NONE_ENROLLED = 11;
    // El movil no dispone del hardware necesario (en este caso, será la huella)
    public static final int BIOMETRIC_ERROR_NO_HARDWARE = 12;
    // Es posible el uso de la huella como identificador
    public static final int BIOMETRIC_SUCCESS = 0;

    private Mensajes mensaje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mensaje = new Mensajes(this);

        //Se inicializa el objeto de la Base de Datos en SQLite que será usada por toda la aplicacion
        database = new DatabaseSafePassword(this);
        try {
            // Se crea una instancia de la clase que controla las Claves Publicas y Privadas
            SafeKey = new SecreKeySafePassword();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        ////////////////////////////////////////////////////////////////////////////////////////////

        // Se crean instancia de los atributos biométricos y se comprueba la disponibilidad
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(ActivityMain.this, executor, new BiometricPrompt.AuthenticationCallback() {

            // Mensaje de error, por ejemplo, si no hay disponible el hardware o huella o se elige usar la contraseña
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);

            }

            // Cuando la verificación de la huella es correcta
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {

                UserDatabase unique = database.SelectUniqueUserDatabase();

                Intent logged = new Intent(getApplicationContext(), ActivityLogin.class);
                logged.putExtra("SESION_ID", unique.getUsrID());
                logged.putExtra("SESION_PIN", unique.getUsrPin());
                logged.putExtra("SESION_PASS", unique.getUsrPass());
                logged.putExtra("SESION_CLOUD", unique.getUsrCloud());
                startActivity(logged);
                mensaje.toastAdvice(getString(R.string.biometric_sucess));
            }

            // Cuando la verificación de la huella ha fallado
            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                mensaje.toastAdvice(getString(R.string.biometric_fail));
            }
        });

        // Se crea una visualización que pide al usuario el usar su huella para iniciar sesión
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometric_login_title))
                .setSubtitle(getString(R.string.biometric_login_fingerprint))
                .setNegativeButtonText(getString(R.string.biometric_login_password))
                .build();



        ////////////////////////////////////////////////////////////////////////////////////////////

        if (database.CountUserDatabase()){ //Si devuelve True es que hay una cuenta

            // En este punto aparece el Prompt para introducir la huella
            biometricPrompt.authenticate(promptInfo);

            setContentView(R.layout.activity_welcome);
            layout = findViewById(R.id.ly_welcome);

            passwordEntry = findViewById(R.id.tv_password_entry); //Elemento comun entre ambos layout
            loggingButton = findViewById(R.id.bt_logging);

            /*
             * Si la contraseña no existe en la parte de PasswordDatabase es -500
             * */

            loggingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(database.CountUserDatabase()){

                        String password = passwordEntry.getText().toString();

                        UserDatabase usr = database.SelectUserDatabase(password);

                        // -400 es el valor que se ha asignado cuando se crea un Usuario por defecto
                        // Esto quiere decir que no ha encontrado ningun usuario para sustituirlo por los
                        // valores del ya existente en la tabla USUARIOS
                        if(usr.getUsrID() == -400){
                            // El usuario no existe, se muestra una ventana de error advirtiendo
                            mensaje.toastAdvice(getString(R.string.mensaje_usuario_password_incorrecta));
                        }else{
                            //Se comprueba que la contraseña es correcta
                            if(!usr.getUsrPass().equals(password)){
                                mensaje.toastAdvice(getString(R.string.mensaje_usuario_password_incorrecta));
                            }else{
                                Intent logged = new Intent(getApplicationContext(), ActivityLogin.class);
                                logged.putExtra("SESION_ID", usr.getUsrID());
                                startActivity(logged);
                                mensaje.toastAdvice(getString(R.string.mensaje_usuario_logged));
                                finish();
                            }

                        }

                    }else { // Quiere decir que no hay ninguna contraseña
                        mensaje.toastAdvice(getString(R.string.not_database_init));
                    }
                }
            });


        }else{
            setContentView(R.layout.activity_first_session);
            layout = findViewById(R.id.ly_first_session);

            passwordEntry = findViewById(R.id.tv_password_entry); //Aunque sea comun hay que recogerlo según el layout activo
            registerButton = findViewById(R.id.bt_register);
            importButton = findViewById(R.id.bt_import);

            registerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String password = passwordEntry.getText().toString();

                    if(!database.CountUserDatabase()){ //Si devuelve FALSE no hay database, se hace el registro
                        if (password.length() < MINIMUM_LENGTH_CHARACTERS) {
                            mensaje.toastAdvice(getString(R.string.mensaje_password_minimo_caracteres));

                        } else {

                            // Se carga el objeto SQLiteDatabase de la base de datos actual en modo escritura
                            if(database.InsertDataUsuarios(password)){
                                mensaje.toastAdvice(getString(R.string.mensaje_usuario_registrado));
                                onRefresh();
                            }else{
                                mensaje.toastAdvice(getString(R.string.mensaje_generico_error_safepassword));
                            }


                        }
                    }else{
                        mensaje.toastAdvice(getString(R.string.mensaje_usuario_existe));
                    }
                }
            });
        }

    }

    // El menú se gestionará en el Fragment que es donde se quiere usar
    @Override
    public void onBackPressed() {
        finishApp();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    private void onRefresh(){
        finish();
        ActivityOptions animacion = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.fade_in, R.anim.fade_out);

        startActivity(getIntent(), animacion.toBundle());
    }


    // Metodos auxiliares

    // Controlar el salir de la aplicación para que no quede abierta y asegurar que el usuario quería salir
    protected void finishApp(){
        new AlertDialog.Builder(ActivityMain.this)
                .setIcon(R.drawable.ic_baseline_exit_to_app)
                .setTitle(getResources().getString(R.string.app_cerrar))
                .setMessage(getResources().getString(R.string.cerrar_mensaje))
                .setPositiveButton(getResources().getString(R.string.aceptar), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent salir = new Intent(getApplicationContext(), ActivityExit.class);
                        salir.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(salir);
                        finish();
                    }

                }).setNegativeButton(getResources().getString(R.string.cancelar), null).show();
    }

}
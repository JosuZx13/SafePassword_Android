package pdm.safepassword.login;

import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.google.android.material.navigation.NavigationView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import pdm.safepassword.adapter.AdapterPassword;
import pdm.safepassword.backup.DriveServiceHelper;
import pdm.safepassword.backup.LocalBackup;
import pdm.safepassword.backup.RemoteBackup;
import pdm.safepassword.database.BackupUserDatabase;
import pdm.safepassword.database.UserDatabase;
import pdm.safepassword.init.ActivityMain;
import pdm.safepassword.R;
import pdm.safepassword.database.PasswordDatabase;
import pdm.safepassword.init.AndroidPermissions;
import pdm.safepassword.init.Mensajes;
import pdm.safepassword.init.PassCode;
import pdm.safepassword.init.PinCode;
import pdm.safepassword.init.SecreKeySafePassword;

public class ActivityLogin extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemSelectedListener {

    // Clase auxiliar creada para la encriptacion de las contraseñas almacenadas en la base de datos
    private SecreKeySafePassword SafeKey;

    // Contraseñas actuales mostradasa en la Activity
    ArrayList<PasswordDatabase> allPassword = new ArrayList<>();

    private Spinner spinner_filter;

    // Menu Lateral
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView naView;
    private View headerView;
    private TextView sync_log;
    private ImageView icon_sync_configuration;

    private MenuItem last_local;
    private MenuItem last_cloud;

    AdapterPassword adapter;
    private RecyclerView rviewListPass;
    private GridLayoutManager rviewGridLayoutManager;

    public static final int REQUEST_CODE_MODIFYPIN = 11;
    public static final int REQUEST_PASS_NEWPIN = 20;
    public static final int REQUEST_PASS_MODIFYPIN = 21;
    public static final int REQUEST_PASS_DELETEUSER = 22;

    private DriveServiceHelper mDriveServiceHelper;
    private static final int REQUEST_CODE_SIGN_IN = 30;
    private static final int REQUEST_DRIVE_PERMISSION = 40;

    // Estas variables estaticas facilitan el uso de estos valores que por lo general no van a cambiar una vez se inicia sesión
    private UserDatabase us;
    public static int OWNER_SESSION;
    public static int OWNER_PIN;
    public static String OWNER_PASS;
    public static String OWNER_CLOUD;

    private ImageView bt_delete_user;
    private ImageView bt_modify_pin;
    private ImageView bt_new_pass;

    public static LocalBackup localBackup;
    public RemoteBackup remoteBackup;

    private Mensajes mensaje;

    private PasswordDatabase passwordSwipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mensaje = new Mensajes(this);

        // Inicialización de la clase
        initialize();

        checkLastLocalBackup();

        // Se cargan las contraseñas del usuario en cuestión
        loadPassword();

        // Funcionalidad para eliminar al usuario actual que inicio sesión
        // Esto conlleva eliminar todas las contraseñas que tuviese asociadas
        bt_delete_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ActivityLogin.this)
                        .setIcon(R.drawable.ic_baseline_info_red)
                        .setTitle(getResources().getString(R.string.app_borrar_usuario))
                        .setMessage(getResources().getString(R.string.borrar_usuario_mensaje))
                        .setPositiveButton(getResources().getString(R.string.aceptar), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Intent askPass = new Intent(ActivityLogin.this, PassCode.class);
                                askPass.putExtra("REQUEST", REQUEST_PASS_DELETEUSER);
                                startActivityForResult(askPass, REQUEST_PASS_DELETEUSER);
                            }

                        }).setNegativeButton(getResources().getString(R.string.cancelar), null).show();
            }
        });

        // Permite modificar el PIN actual del usuario
        bt_modify_pin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(OWNER_PIN == -1000){
                    mensaje.toastAdvice(getString(R.string.pincode_not_exist));

                    // Como no hay un PIN aun registrado, debe meter la contraseña para continuar
                    Intent askPass = new Intent(ActivityLogin.this, PassCode.class);
                    askPass.putExtra("REQUEST", REQUEST_PASS_NEWPIN);
                    startActivityForResult(askPass, REQUEST_PASS_NEWPIN);

                }else{
                    new AlertDialog.Builder(ActivityLogin.this)
                            .setIcon(R.drawable.ic_baseline_domain_verification)
                            .setTitle(getResources().getString(R.string.app_verification))
                            .setPositiveButton(getResources().getString(R.string.prefer_password), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent askPass = new Intent(ActivityLogin.this, PassCode.class);
                                    askPass.putExtra("REQUEST", REQUEST_PASS_MODIFYPIN);
                                    startActivityForResult(askPass, REQUEST_PASS_MODIFYPIN);
                                }

                            })
                            .setNegativeButton(getResources().getString(R.string.prefer_pin_code), new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    // YA existe pin, así que se pregunta por él
                                    Intent askPin = new Intent(ActivityLogin.this, PinCode.class);
                                    askPin.putExtra("REQUEST", REQUEST_CODE_MODIFYPIN);
                                    startActivityForResult(askPin, REQUEST_CODE_MODIFYPIN);

                                }

                            })
                            .show();
                }
            }
        });


        // Funcionalidad para añadir una nueva contraseña
        bt_new_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addPass = new Intent (ActivityLogin.this, FragmentFloatingNewPassword.class);
                // Se le aplica una animacion
                ActivityOptions animacion = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.fade_in, R.anim.fade_out);
                //Se inicializa
                startActivity(addPass, animacion.toBundle());

            }
        });

    }

    // MÉTODO QUE INICIALIZA TODOS LOS ELEMENTOS DE LA ACTIVITY
    private void initialize(){

        localBackup = new LocalBackup(ActivityLogin.this);


        // Se recibe el usuario que inició sesión

        UserDatabase us = ActivityMain.database.SelectUniqueUserDatabase();
        OWNER_SESSION = us.getUsrID();
        OWNER_PIN = us.getUsrPin();
        OWNER_PASS = us.getUsrPass();
        OWNER_CLOUD = us.getUsrCloud();


        // MENU LATERAL (NAVIGATION DRAWER)

        // Aplica la Toolbar que reemplazará la ActionBar por defecto
        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        spinner_filter = findViewById(R.id.spinner_filter);
        ArrayAdapter<CharSequence> adapterSpinnner = ArrayAdapter.createFromResource(this, R.array.category_filter, android.R.layout.simple_spinner_item);
        adapterSpinnner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_filter.setAdapter(adapterSpinnner);
        spinner_filter.setOnItemSelectedListener(this);

        // Muestra un icono
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        drawerLayout = findViewById(R.id.drawer_layout);
        naView = findViewById(R.id.navigation_settings);
        // La clase debe implementar de
        // De esta forma se puede dar funcionalidad a las opciones del menu
        naView.setNavigationItemSelectedListener(this);
        headerView = naView.getHeaderView(0);

        last_local = naView.getMenu().findItem(R.id.last_local_backup);
        last_cloud = naView.getMenu().findItem(R.id.last_cloud_backup);
        // Se cambia por el correo usado para la copia de seguridad en la nube
        sync_log = headerView.findViewById(R.id.session_log_sync);

        // -g es el valor por defecto cuando no hay una sincronización en la nube
        updateCloudSync();

        // RECYCLER VIEW PARA LAS CONTRASEÑAS
        rviewListPass = findViewById(R.id.rview_list_pass);
        rviewGridLayoutManager = new GridLayoutManager(this, 2);

        bt_delete_user = findViewById(R.id.bt_delete_user);
        bt_modify_pin = findViewById(R.id.bt_modify_pin);
        bt_new_pass = findViewById(R.id.bt_new_pass);

        // Mejora de rendimiento
        // Se usa solo si se sabe que el contenido no afecta al tamaño del RV
        rviewListPass.setHasFixedSize(true);
    }

    // Cuando inicia sesión el usuario, comprueba si hay alguna copia en localBackup para indicarlo en el menú lateral
    private void checkLastLocalBackup(){
        ArrayList<BackupUserDatabase> allBk = ActivityMain.database.GetBackupUserDatabase();

        if(allBk.size() > 0){
            //El metodo las devuelve por el último ID asignado, la cual debe ser la ultima
            BackupUserDatabase bk = allBk.get(0);
            System.out.println(bk.toString());
            last_local.setTitle(getString(R.string.local_date_backup)+": "+bk.getBkDate());
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Se puede recoger el elemento pulsado de la siguiente forma
        String categ = parent.getItemAtPosition(position).toString();

        if(categ.equals("All") || categ.equals("Todo")){
            loadPassword();
        }else{
            loadPasswordByCategory(categ);
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    // El ultimo campo indica qué tipo de variable recibe la búsqueda
    private class PasswordLoadTask extends AsyncTask<Void, Void, ArrayList<PasswordDatabase>> {
        @Override
        protected ArrayList<PasswordDatabase> doInBackground(Void... voids){
            allPassword = ActivityMain.database.GetPasswordUserDatabase();
            return allPassword;
        }

        @Override
        protected void onPostExecute(ArrayList<PasswordDatabase> lista) {
            if (lista != null && lista.size() > 0) {
                adapter = new AdapterPassword(lista);

                adapter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Se recoge el objeto en cuestion en la posicion en la que se clickó
                        PasswordDatabase pd = lista.get(rviewListPass.getChildAdapterPosition(v));
                        // Crea un intent cargado con los datos de la password
                        Intent showPass = new Intent (ActivityLogin.this, FragmentFloatingPassword.class);
                        // Se le aplica una animacion
                        ActivityOptions animacion = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.fade_in, R.anim.fade_out);
                        //Se añade un contenido extra al Intent
                        showPass.putExtra("ID-PASS", pd);
                        //Se inicializa
                        startActivity(showPass, animacion.toBundle());
                    }
                });

                // Se le aplica la funcionalidad del ItemTouchHelper
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
                itemTouchHelper.attachToRecyclerView(rviewListPass);

                rviewListPass.setAdapter(adapter);
                rviewListPass.setLayoutManager(rviewGridLayoutManager);
                rviewListPass.setPadding(0,50,0,50);
            }
        }
    }

    // El ultimo campo indica qué tipo de variable recibe la búsqueda
    private class PasswordLoadByCategoryTask extends AsyncTask<String, Void, ArrayList<PasswordDatabase>> {

        @Override
        protected ArrayList<PasswordDatabase> doInBackground(String... strings) {
            allPassword = ActivityMain.database.GetPasswordUserDatabaseByCategory(strings[0]);
            return allPassword;
        }

        @Override
        protected void onPostExecute(ArrayList<PasswordDatabase> lista) {
            if (lista != null && lista.size() > 0) {
                adapter = new AdapterPassword(lista);

                adapter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Se recoge el objeto en cuestion en la posicion en la que se clickó
                        PasswordDatabase pd = lista.get(rviewListPass.getChildAdapterPosition(v));
                        // Crea un intent cargado con los datos de la password
                        Intent showPass = new Intent (ActivityLogin.this, FragmentFloatingPassword.class);
                        // Se le aplica una animacion
                        ActivityOptions animacion = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.fade_in, R.anim.fade_out);
                        //Se añade un contenido extra al Intent
                        showPass.putExtra("ID-PASS", pd);
                        //Se inicializa
                        startActivity(showPass, animacion.toBundle());
                    }
                });

                // Se le aplica la funcionalidad del ItemTouchHelper
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
                itemTouchHelper.attachToRecyclerView(rviewListPass);

                rviewListPass.setAdapter(adapter);
                rviewListPass.setLayoutManager(rviewGridLayoutManager);
                rviewListPass.setPadding(0,50,0,50);
            }
        }
    }

    private void loadPassword(){
        new PasswordLoadTask().execute();
    }

    private void loadPasswordByCategory(String categ){
        new PasswordLoadByCategoryTask().execute(categ);
    }

    private void updateCloudSync(){

        if(!OWNER_CLOUD.equals("-g")){
            sync_log.setText(OWNER_CLOUD);
            icon_sync_configuration = headerView.findViewById(R.id.icon_sync_configuration);
            icon_sync_configuration.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_cloud_done));
        }

    }

    private void modifyPin(){
        AlertDialog.Builder alert = new AlertDialog.Builder(ActivityLogin.this);
        alert.setTitle(getString(R.string.pinconde_register));
        final EditText input = new EditText(ActivityLogin.this);
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
        // Se define un edittext para aplicarlo al cuadro de diálogo
        alert.setView(input);
        alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                String pin_texto = input.getText().toString();

                if(pin_texto.length() != 4){
                    mensaje.toastAdvice(getString(R.string.pinconde_not_enough_digit));
                }else{
                    int pin_introduced = Integer.parseInt(input.getText().toString());

                    if(ActivityMain.database.UpdatePinUserDatabase(OWNER_SESSION, pin_introduced)){
                        OWNER_PIN = pin_introduced; //Se actualiza este valor
                        mensaje.toastAdvice(getString(R.string.pinconde_save_correctly));
                    }else{
                        mensaje.toastAdvice(getString(R.string.mensaje_generico_error_safepassword));
                    }

                }
            }
        });
        alert.setNegativeButton(getString(R.string.cancelar), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mensaje.toastAdvice(getString(R.string.pinconde_not_registered));
            }
        });

        alert.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // El boton home/up abre el menu

        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Sobreescribir este método permite manipular qué acciones ocurren cuando se pulsa un elemento
    // En el menú lateral de la app
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_upload_backup:
                new AlertDialog.Builder(ActivityLogin.this)
                        .setTitle(getResources().getString(R.string.app_backup))
                        .setMessage(getResources().getString(R.string.backup_mensaje))
                        .setPositiveButton(getResources().getString(R.string.local_type), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                AndroidPermissions.checkPermission(ActivityLogin.this);

                                if(localBackup.doBackup(ActivityMain.database)){
                                    // Se modifica el elemento del menu lateral
                                    last_local.setTitle(getString(R.string.local_date_backup)+": "+mensaje.getDate());
                                    mensaje.toastAdvice(getString(R.string.local_backup_create));
                                }else{
                                    mensaje.toastAdvice(getString(R.string.backup_not_complete));
                                }
                            }

                        }).setNegativeButton(getResources().getString(R.string.cloud_type), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if(checkLastGoogleSession()){

                                mDriveServiceHelper.createFile();

                            last_cloud.setTitle(getString(R.string.cloud_date_backup)+": "+mensaje.getDate());
                            mensaje.toastAdvice(getString(R.string.cloud_backup_create));
                        }else{
                            requestSignIn();
                            mensaje.toastAdvice(getString(R.string.not_google_session));
                        }


                    }

                }).show();

                break;

            case R.id.menu_download_cloud_backup:
                mensaje.toastAdvice("DE DESCARGA");
                break;

            case R.id.menu_admin_backup:

                if(ActivityMain.database.GetBackupUserDatabase().size() > 0){
                    Intent showBackup = new Intent (ActivityLogin.this, FragmentFloatingBackup.class);
                    // Se le aplica una animacion
                    ActivityOptions animacion = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.fade_in, R.anim.fade_out);
                    //Se inicializa
                    startActivity(showBackup, animacion.toBundle());
                }else{
                    mensaje.toastAdvice(getString(R.string.not_local_backup_yet));
                }

                break;

            case R.id.menu_account_cloud:
                requestSignIn();
                break;

            default:
                return false;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        int code;
        String pass;

        switch (requestCode){

            case REQUEST_CODE_MODIFYPIN:

                code = data.getIntExtra("PIN-INTRODUCED", -100);

                if (code == OWNER_PIN) {
                    modifyPin();

                } else {
                    if(code != -100){
                        mensaje.toastAdvice(getString(R.string.pincode_error));
                    }
                }

                break;

            case REQUEST_PASS_NEWPIN:

                pass = data.getStringExtra("PASS-INTRODUCED");

                if(pass.equals(OWNER_PASS)){
                    modifyPin();

                } else {
                    mensaje.toastAdvice(getString(R.string.pincode_error));
                }

                break;

            case REQUEST_PASS_MODIFYPIN:

                pass = data.getStringExtra("PASS-INTRODUCED");

                if(pass.equals(OWNER_PASS)){
                    modifyPin();
                }else{
                    mensaje.toastAdvice(getString(R.string.mensaje_usuario_password_incorrecta));
                }

                break;

            case REQUEST_PASS_DELETEUSER:

                pass = data.getStringExtra("PASS-INTRODUCED");
                if(pass.equals(OWNER_PASS)){
                    if(ActivityMain.database.DeleteUserDatabase(ActivityLogin.OWNER_SESSION)){
                        Intent borrar = new Intent(getApplicationContext(), ActivityMain.class);
                        borrar.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mensaje.toastAdvice(getString(R.string.mensaje_usuario_eliminado));
                        startActivity(borrar);
                        finish();
                    }else{
                        mensaje.toastAdvice(getString(R.string.mensaje_generico_error_safepassword));
                    }
                }else{
                    mensaje.toastAdvice(getString(R.string.mensaje_usuario_password_incorrecta));
                }

                break;

            case FragmentFloatingPassword.REQUEST_CODE_PIN_DELETEPASSWORD:

                code = data.getIntExtra("PIN-INTRODUCED", -100);

                if(code == ActivityLogin.OWNER_PIN) {

                    new AlertDialog.Builder(ActivityLogin.this)
                            .setIcon(R.drawable.ic_baseline_info_red)
                            .setTitle(getResources().getString(R.string.app_borrar_password))
                            .setMessage(getResources().getString(R.string.borrar_password_mensaje))
                            .setPositiveButton(getResources().getString(R.string.aceptar), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    if(ActivityMain.database.DeletePasswordDatabase(passwordSwipe.getPswID())){
                                        mensaje.toastAdvice(getString(R.string.mensaje_password_deleted));
                                    }else{
                                        mensaje.toastAdvice(getString(R.string.mensaje_generico_error_safepassword));
                                    }
                                    onRefresh();
                                }

                            }).setNegativeButton(getResources().getString(R.string.cancelar), null).show();

                }else{
                    if(code != -100){
                        mensaje.toastAdvice(getString(R.string.pincode_error));
                    }
                }

                break;

            case FragmentFloatingPassword.REQUEST_CODE_PIN_EDITPASSWORD:

                code = data.getIntExtra("PIN-INTRODUCED", -100);

                if(code == ActivityLogin.OWNER_PIN) {

                    Intent editPass = new Intent(ActivityLogin.this, FragmentFloatingEdit.class);
                    // Se le aplica una animacion
                    ActivityOptions animacion = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.fade_in, R.anim.fade_out);
                    //Se añade un contenido extra al Intent
                    editPass.putExtra("PASS-EDIT", passwordSwipe);
                    //Se inicializa
                    startActivity(editPass, animacion.toBundle());

                }else{
                    if(code != -100){
                        mensaje.toastAdvice(getString(R.string.pincode_error));
                    }
                }

                break;

            case FragmentFloatingPassword.REQUEST_CODE_PASS_EDITPASSWORD:

                pass = data.getStringExtra("PASS-INTRODUCED");

                if(pass.equals(ActivityLogin.OWNER_PASS)){

                    Intent editPass = new Intent(ActivityLogin.this, FragmentFloatingEdit.class);
                    // Se le aplica una animacion
                    ActivityOptions animacion = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.fade_in, R.anim.fade_out);
                    //Se añade un contenido extra al Intent
                    editPass.putExtra("PASS-EDIT", passwordSwipe);
                    //Se inicializa
                    startActivity(editPass, animacion.toBundle());

                }else{
                    mensaje.toastAdvice(getString(R.string.mensaje_usuario_password_incorrecta));
                }

                break;

            case FragmentFloatingPassword.REQUEST_CODE_PASS_DELETEPASSWORD:

                pass = data.getStringExtra("PASS-INTRODUCED");

                if(pass.equals(ActivityLogin.OWNER_PASS)){

                    new AlertDialog.Builder(ActivityLogin.this)
                            .setIcon(R.drawable.ic_baseline_info_red)
                            .setTitle(getResources().getString(R.string.app_borrar_password))
                            .setMessage(getResources().getString(R.string.borrar_password_mensaje))
                            .setPositiveButton(getResources().getString(R.string.aceptar), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    if(ActivityMain.database.DeletePasswordDatabase(passwordSwipe.getPswID())){
                                        mensaje.toastAdvice(getString(R.string.mensaje_password_deleted));
                                    }else{
                                        mensaje.toastAdvice(getString(R.string.mensaje_generico_error_safepassword));
                                    }

                                    onRefresh();
                                }

                            }).setNegativeButton(getResources().getString(R.string.cancelar), null).show();

                }else{
                    mensaje.toastAdvice(getString(R.string.mensaje_usuario_password_incorrecta));
                }

                break;

            case REQUEST_CODE_SIGN_IN:
                if (data != null) {
                    handleSignInResult(data);

                }

                break;

            default:
                break;
        }
    }

    // Este objeto va a permitir controlar el tipo de movimiento que se realiza sobre una vista
    // Del RecyclerView
    // El parámetro que se le pasa al constructor indica el tipo de movimiento que se va a recoger
    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            passwordSwipe = allPassword.get(viewHolder.getAdapterPosition());

            if(direction == ItemTouchHelper.RIGHT){ // DELETE

                new AlertDialog.Builder(ActivityLogin.this)
                        .setIcon(R.drawable.ic_baseline_domain_verification)
                        .setTitle(getResources().getString(R.string.app_verification))
                        .setPositiveButton(getResources().getString(R.string.prefer_password), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent askPass = new Intent(ActivityLogin.this, PassCode.class);
                                askPass.putExtra("REQUEST", FragmentFloatingPassword.REQUEST_CODE_PASS_DELETEPASSWORD);
                                startActivityForResult(askPass, FragmentFloatingPassword.REQUEST_CODE_PASS_DELETEPASSWORD);
                            }

                        })
                        .setNegativeButton(getResources().getString(R.string.prefer_pin_code), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if(ActivityLogin.OWNER_PIN != -1000){

                                    // Ya existe pin, así que se pregunta por él
                                    Intent askPin = new Intent(ActivityLogin.this, PinCode.class);
                                    askPin.putExtra("REQUEST", FragmentFloatingPassword.REQUEST_CODE_PIN_DELETEPASSWORD);
                                    startActivityForResult(askPin, FragmentFloatingPassword.REQUEST_CODE_PIN_DELETEPASSWORD);

                                }else{
                                    // No hay pin, así que hay que pedirlo
                                    mensaje.alertNewPin();
                                }
                            }
                        })
                        .show();
            }else{
                if(direction == ItemTouchHelper.LEFT){

                    new AlertDialog.Builder(ActivityLogin.this)
                            .setIcon(R.drawable.ic_baseline_domain_verification)
                            .setTitle(getResources().getString(R.string.app_verification))
                            .setPositiveButton(getResources().getString(R.string.prefer_password), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent askPass = new Intent(ActivityLogin.this, PassCode.class);
                                    askPass.putExtra("REQUEST", FragmentFloatingPassword.REQUEST_CODE_PASS_EDITPASSWORD);
                                    startActivityForResult(askPass, FragmentFloatingPassword.REQUEST_CODE_PASS_EDITPASSWORD);
                                }

                            })
                            .setNegativeButton(getResources().getString(R.string.prefer_pin_code), new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    // -1000 es el valor por defecto que se le da al crear el objeto del Usuario
                                    if(ActivityLogin.OWNER_PIN != -1000){

                                        // Ya existe pin, así que se pregunta por él
                                        Intent askPin = new Intent(ActivityLogin.this, PinCode.class);
                                        askPin.putExtra("REQUEST", FragmentFloatingPassword.REQUEST_CODE_PIN_EDITPASSWORD);
                                        startActivityForResult(askPin, FragmentFloatingPassword.REQUEST_CODE_PIN_EDITPASSWORD);

                                    }else{
                                        // No hay pin, así que hay que pedirlo
                                        mensaje.alertNewPin();
                                    }
                                }
                            })
                            .show();
                }
            }
            rviewListPass.removeViewAt(viewHolder.getAdapterPosition());
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

            Drawable icon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_item_delete);

            View itemView = viewHolder.itemView;

            int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
            int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
            int iconBottom = iconTop + icon.getIntrinsicHeight();

            // Desplazamiento desde la izquierda a la DERECHA -> RIGHT (DELETE)
            if (dX > 0) {

                int iconLeft = itemView.getLeft() + iconMargin;
                int iconRight = itemView.getLeft() + iconMargin + icon.getIntrinsicWidth();
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                icon.draw(c);

            // Desplazamiento de Derecha a IZQUIERDA -> LEFT (EDIT)
            } else if (dX < 0) {

                icon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_item_edit);

                int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
                int iconRight = itemView.getRight() - iconMargin;
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                icon.draw(c);

            }

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else{
            Intent inicio = new Intent (ActivityLogin.this, ActivityMain.class);
            ActivityOptions animacion = ActivityOptions.makeCustomAnimation(ActivityLogin.this, R.anim.fade_in, R.anim.fade_out);
            startActivity(inicio, animacion.toBundle());
            finish();
        }
    }

    private void onRefresh(){
        finish();
        ActivityOptions animacion = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.fade_in, R.anim.fade_out);
        startActivity(getIntent(), animacion.toBundle());
    }

    // Este se ejecuta antes del oncreate
    @Override
    protected void onStart() {
        super.onStart();
    }

    private boolean checkLastGoogleSession(){

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if (account != null) {

            if(checkDrivePermission()){
                mDriveServiceHelper = initDrive(account);
                remoteBackup = new RemoteBackup(ActivityLogin.this, mDriveServiceHelper);
                return true;
            }

        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPassword();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        loadPassword();
    }

    // AQUÍ IRA LA IMPLEMENTACIÓN DE LA CONEXIÓN A GOOGLE DRIVE
    //#################################################################################################################################################

    // Inicia una actividad para el inicio de sesión en el servicio google
    private void requestSignIn() {

        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);

        // The result of the sign-in Intent is handled in onActivityResult.
        startActivityForResult(client.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    // Recoge la solicitud realizada desde SignIn, la actividad para iniciar sesión
    // Si la operación es correcta, actualiza la base de datos con el correo usado
    // Ademas se crea con la API de Google Drive
    private void handleSignInResult(Intent result) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
            .addOnSuccessListener(googleAccount -> {

                if(ActivityMain.database.UpdateCloudUserDatabase(OWNER_SESSION, googleAccount.getEmail())){
                    OWNER_CLOUD = googleAccount.getEmail();
                    updateCloudSync();
                    mensaje.toastAdvice("Sesión Iniciada en Google");
                }

                // Use the authenticated account to sign in to the Drive service.
                GoogleAccountCredential credential =
                        GoogleAccountCredential.usingOAuth2(
                                this, Collections.singleton(DriveScopes.DRIVE));
                credential.setSelectedAccount(googleAccount.getAccount());
                Drive googleDriveService =
                        new Drive.Builder(
                                AndroidHttp.newCompatibleTransport(),
                                new GsonFactory(),
                                credential)
                                .setApplicationName("SafePassword")
                                .build();

                // The DriveServiceHelper encapsulates all REST API and SAF functionality.
                // Its instantiation is required before handling any onClick actions.
                mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
            })
            .addOnFailureListener(exception -> Log.e("ERROR-SIGN_IN", "Unable to sign in.", exception));
    }

    private DriveServiceHelper initDrive(GoogleSignInAccount google){
        // Use the authenticated account to sign in to the Drive service.
        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                        this, Collections.singleton(DriveScopes.DRIVE));
        credential.setSelectedAccount(google.getAccount());
        Drive googleDriveService =
                new Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(),
                        credential)
                        .setApplicationName("SafePassword")
                        .build();

        // The DriveServiceHelper encapsulates all REST API and SAF functionality.
        // Its instantiation is required before handling any onClick actions.
        return new DriveServiceHelper(googleDriveService);
    }

    private boolean checkDrivePermission(){

        if (!GoogleSignIn.hasPermissions(
                GoogleSignIn.getLastSignedInAccount(this),
                new Scope(DriveScopes.DRIVE))) {

            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_DRIVE_PERMISSION,
                    GoogleSignIn.getLastSignedInAccount(this),
                    new Scope(DriveScopes.DRIVE));

            return false;
        } else {
            return true;
        }

    }

}


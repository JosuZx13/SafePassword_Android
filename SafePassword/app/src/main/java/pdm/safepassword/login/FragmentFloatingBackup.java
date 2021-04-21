package pdm.safepassword.login;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.sql.SQLOutput;
import java.util.ArrayList;

import pdm.safepassword.R;
import pdm.safepassword.adapter.AdapterBackup;
import pdm.safepassword.database.BackupUserDatabase;
import pdm.safepassword.init.ActivityMain;
import pdm.safepassword.init.Mensajes;
import pdm.safepassword.init.PassCode;
import pdm.safepassword.init.PinCode;

public class FragmentFloatingBackup extends FragmentActivity {

    AdapterBackup adapter;
    private RecyclerView rviewListBackup;
    private LinearLayoutManager rviewLinearManager;

    ArrayList<BackupUserDatabase> lista; // Donde se almacenaran las contraseñas

    private FloatingActionButton fab_delete_all_backup;

    private String willDeleteBackup = "";
    public static final int REQUEST_CODE_PIN_DELETEBACKUP = 16;
    public static final int REQUEST_CODE_PIN_DELETEALLBACKUP = 17;
    public static final int REQUEST_CODE_PASS_DELETEBACKUP = 26;
    public static final int REQUEST_CODE_PASS_DELETEALLBACKUP = 27;

    private Mensajes mensaje;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_floating_backup);

        mensaje = new Mensajes(this);

        fab_delete_all_backup = findViewById(R.id.fab_delete_all_backup);

        // Acción para el icono central en la parte inferior. Borrará todas las copias de seguridad
        fab_delete_all_backup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(FragmentFloatingBackup.this)
                    .setIcon(R.drawable.ic_baseline_info_red)
                    .setTitle(getResources().getString(R.string.app_borrar_all_backup))
                    .setMessage(getResources().getString(R.string.borrar_all_backup_mensaje))
                    .setPositiveButton(getResources().getString(R.string.aceptar), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            new AlertDialog.Builder(FragmentFloatingBackup.this)
                                .setIcon(R.drawable.ic_baseline_domain_verification)
                                .setTitle(getResources().getString(R.string.app_verification))
                                .setPositiveButton(getResources().getString(R.string.prefer_password), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent askPass = new Intent(FragmentFloatingBackup.this, PassCode.class);
                                        askPass.putExtra("REQUEST", REQUEST_CODE_PASS_DELETEALLBACKUP);
                                        startActivityForResult(askPass, REQUEST_CODE_PASS_DELETEALLBACKUP);
                                    }

                                })
                                .setNegativeButton(getResources().getString(R.string.prefer_pin_code), new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        if(ActivityLogin.OWNER_PIN != -1000){

                                            // YA existe pin, así que se pregunta por él
                                            Intent askPin = new Intent(FragmentFloatingBackup.this, PinCode.class);
                                            askPin.putExtra("REQUEST", REQUEST_CODE_PIN_DELETEALLBACKUP);
                                            startActivityForResult(askPin, REQUEST_CODE_PIN_DELETEALLBACKUP);

                                        }else{
                                            // No hay pin, así que hay que pedirlo
                                            mensaje.alertNewPin();
                                        }

                                    }
                                })
                                .show();
                        }

                    }).setNegativeButton(getResources().getString(R.string.cancelar), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }

                }).show();
            }
        });

        // Se recoge el RecyclerView que contendrá todas las backup del usuario
        rviewListBackup = findViewById(R.id.rview_local_backup);
        rviewLinearManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        // Mejora de rendimiento
        // Se usa solo si se sabe que el contenido no afecta al tamaño del RV
        rviewListBackup.setHasFixedSize(true);

        // Al igual que se usó con las contraseñas, este metodo carga las backup disponibles
        // Segun el usuario que iniciase sesión
        loadBackup();

    }

    private void loadBackup(){

        lista = ActivityMain.database.GetBackupUserDatabase();
        adapter = new AdapterBackup(lista);

        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Se recoge el objeto en cuestion en la posicion en la que se clickó
                BackupUserDatabase bk = lista.get(rviewListBackup.getChildAdapterPosition(v));

                // Aparece un mensaje para preguntar al usuario sobre qué quiere hacer, si restaurar la base de datos o eliminarla
                new AlertDialog.Builder(FragmentFloatingBackup.this)
                    .setTitle(getResources().getString(R.string.app_backup))
                    .setMessage(bk.getBkName())
                    .setPositiveButton(getResources().getString(R.string.backup_delete_option), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            new AlertDialog.Builder(FragmentFloatingBackup.this)
                                .setIcon(R.drawable.ic_baseline_domain_verification)
                                .setTitle(getResources().getString(R.string.app_verification))
                                .setPositiveButton(getResources().getString(R.string.prefer_password), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent askPass = new Intent(FragmentFloatingBackup.this, PassCode.class);
                                        askPass.putExtra("REQUEST", REQUEST_CODE_PASS_DELETEBACKUP);
                                        willDeleteBackup = bk.getBkName();
                                        startActivityForResult(askPass, REQUEST_CODE_PASS_DELETEBACKUP);
                                    }

                                })
                                .setNegativeButton(getResources().getString(R.string.prefer_pin_code), new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        if(ActivityLogin.OWNER_PIN != -1000){

                                            // Ya existe pin, así que se pregunta por él
                                            Intent askPin = new Intent(FragmentFloatingBackup.this, PinCode.class);
                                            askPin.putExtra("REQUEST", REQUEST_CODE_PIN_DELETEBACKUP);
                                            willDeleteBackup = bk.getBkName();
                                            startActivityForResult(askPin, REQUEST_CODE_PIN_DELETEBACKUP);

                                        }else{
                                            // No hay pin, así que hay que registrarlo
                                            mensaje.alertNewPin();
                                        }
                                    }
                                })
                                .show();

                        }

                    }).setNegativeButton(getResources().getString(R.string.backup_restore_option), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if(ActivityLogin.localBackup.restoreBackup(ActivityMain.database, bk.getBkName())){
                                mensaje.toastAdvice(getString(R.string.local_backup_restored)+bk.getBkName());
                            }else{
                                mensaje.toastAdvice(getString(R.string.local_backup_not_restored));
                            }

                        }

                    }).show();
            }
        });

        rviewListBackup.setAdapter(adapter);
        rviewListBackup.setLayoutManager(rviewLinearManager);
        rviewListBackup.setPadding(0,50,0,50);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        int code;
        String pass;

        switch (requestCode) {

            case REQUEST_CODE_PIN_DELETEBACKUP:

                code = data.getIntExtra("PIN-INTRODUCED", -100);

                if (code == ActivityLogin.OWNER_PIN) {

                    if(ActivityLogin.localBackup.deleteBackup(ActivityMain.database, willDeleteBackup)){
                        mensaje.toastAdvice(getString(R.string.mensaje_backup_deleted));

                    }else{
                        mensaje.toastAdvice(getString(R.string.mensaje_generico_error_safepassword));
                    }

                }else{
                    mensaje.toastAdvice(getString(R.string.pincode_error));
                }

                break;

            case REQUEST_CODE_PIN_DELETEALLBACKUP:

                code = data.getIntExtra("PIN-INTRODUCED", -100);

                if (code == ActivityLogin.OWNER_PIN) {

                    ArrayList<BackupUserDatabase> allBackup = ActivityMain.database.GetBackupUserDatabase();

                    if(allBackup.size() > 0){
                        boolean deleted = true;
                        for(BackupUserDatabase bk : allBackup){

                            if(!ActivityLogin.localBackup.deleteBackup(ActivityMain.database, bk.getBkName())){
                                deleted = false;
                                break;
                            }

                        } //Fin del Foreach

                        if(deleted){
                            ActivityMain.database.ResetSequenceBackup();
                            mensaje.toastAdvice(getString(R.string.mensaje_all_backup_deleted));
                        }else{
                            mensaje.toastAdvice(getString(R.string.mensaje_generico_error_safepassword));
                        }
                    }

                }else{
                    mensaje.toastAdvice(getString(R.string.pincode_error));
                }

                break;

            case REQUEST_CODE_PASS_DELETEBACKUP:

                pass = data.getStringExtra("PASS-INTRODUCED");

                if(pass.equals(ActivityLogin.OWNER_PASS)){

                    if(ActivityLogin.localBackup.deleteBackup(ActivityMain.database, willDeleteBackup)){
                        mensaje.toastAdvice(getString(R.string.mensaje_backup_deleted));

                    }else{
                        mensaje.toastAdvice(getString(R.string.mensaje_generico_error_safepassword));
                    }

                }else{
                    mensaje.toastAdvice(getString(R.string.mensaje_usuario_password_incorrecta));
                }

                break;

            case REQUEST_CODE_PASS_DELETEALLBACKUP:

                ArrayList<BackupUserDatabase> allBackup = ActivityMain.database.GetBackupUserDatabase();

                if(allBackup.size() > 0){
                    boolean deleted = true;
                    for(BackupUserDatabase bk : allBackup){

                        if(!ActivityLogin.localBackup.deleteBackup(ActivityMain.database, bk.getBkName())){
                            deleted = false;
                            break;
                        }

                    } //Fin del Foreach

                    if(deleted){
                        ActivityMain.database.ResetSequenceBackup();
                        mensaje.toastAdvice(getString(R.string.mensaje_all_backup_deleted));
                    }else{
                        mensaje.toastAdvice(getString(R.string.mensaje_generico_error_safepassword));
                    }
                }

                break;

            default:
                break;
        }

        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}

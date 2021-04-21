package pdm.safepassword.init;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import pdm.safepassword.login.ActivityLogin;

public class AndroidPermissions {

    private final static int REQUEST_INTERNET_CODE_PERMISSIONS = 0;
    private final static String MANIFEST_INTERNET_PERMISSION = Manifest.permission.INTERNET;

    private final static int REQUEST_STORAGE_CODE_PERMISSIONS = 0;
    private final static String MANIFEST_READ_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;
    private final static String MANIFEST_WRITE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;


    public static void checkPermission(ActivityLogin actv){
        // No hay ningún permiso concedido

        // Comprueba si hay permisos para conexión a internet y de no ser así, los solicita
        CheckInternetPermission(actv);
        // Comprueba si hay permisos para lectura y escritura, y de no ser así, los solicita
        CheckStoragePermission(actv);

    }

    private static void CheckInternetPermission(ActivityLogin act){

        // Comprueba si los permisos fueron garantizados para la App
        if (ActivityCompat.checkSelfPermission(act, MANIFEST_INTERNET_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            // Solicita el aceptar los permisos
            ActivityCompat.requestPermissions(act,
                new String[]{MANIFEST_INTERNET_PERMISSION},
                REQUEST_INTERNET_CODE_PERMISSIONS);
        }
    }

    private static void CheckStoragePermission(ActivityLogin act){

        int readPerm = ActivityCompat.checkSelfPermission(act, MANIFEST_READ_PERMISSION);
        int writePerm = ActivityCompat.checkSelfPermission(act, MANIFEST_WRITE_PERMISSION);

        // Comprueba si los permisos fueron garantizados para la App
        if ( readPerm != PackageManager.PERMISSION_GRANTED || writePerm != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(act,
                new String[]{MANIFEST_WRITE_PERMISSION, MANIFEST_READ_PERMISSION},
                    REQUEST_STORAGE_CODE_PERMISSIONS);
        }
    }
}

package pdm.safepassword.backup;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import pdm.safepassword.R;
import pdm.safepassword.init.Mensajes;

import com.google.api.services.drive.Drive;

public class RemoteBackup {

    private DriveServiceHelper mDriveServiceHelper;

    private final String DATABASE_DIR;
    private File DATABASE_FILE;

    private Drive drive;


    // Como la copia se pediría desde el ActivityLogin, se carga
    Activity activity;
    Context context;
    Mensajes mensaje;

    // Simple constructor que recoge la actividad que lo ejecuta
    public RemoteBackup(Activity actv, DriveServiceHelper helper) {
        activity = actv;
        context = actv.getApplicationContext();
        mDriveServiceHelper = helper;
        mensaje = new Mensajes(actv);
        DATABASE_DIR = context.getDataDir().toString() + File.separator + "databases" + File.separator + context.getString(R.string.dir_local_backup);
        //DATABASE_FILE = new File(DATABASE_DIR, nameDB);

    }

}

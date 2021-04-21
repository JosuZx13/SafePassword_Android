package pdm.safepassword.backup;

import android.app.Activity;
import android.content.Context;

import java.io.File;
import java.util.Calendar;

import pdm.safepassword.R;
import pdm.safepassword.database.DatabaseSafePassword;
import pdm.safepassword.init.Mensajes;
import pdm.safepassword.login.ActivityLogin;

public class LocalBackup {

    // Como la copia se pediría desde el ActivityLogin, se carga
    Activity activity;
    Mensajes mensaje;

    // Simple constructor que recoge la actividad que lo ejecuta
    public LocalBackup(Activity actv){
        activity = actv;
        mensaje = new Mensajes(actv);

    }

    // SE CREA UNA COPIA LOCAL DE SEGURIDAD EN EL DISPOSITIVO
    public boolean doBackup(final DatabaseSafePassword db){
        Context c = activity.getApplicationContext();

        if (checkDirExist(c)) {
            String f = c.getString(R.string.file_local_backup);
            String dateBackup = mensaje.getDate();
            f = f.concat(dateBackup+".db");

            File copyDB = new File(c.getDataDir().toString() + File.separator + "databases" + File.separator + c.getString(R.string.dir_local_backup), f);

            return db.ExportBackup(c, copyDB, dateBackup, f);

        }else{
            return false;
        }
    }

    // Lo que ocurre es que se navega por las copias de seguridad existentes
    // Y se modifica la base de datos actual con las contraseñas asociadas al usuario
    // En la anterior copia
    // Primero se eliminan todas las contraseñas asociadas al usuario actual
    // Y después se insertan una a una que tuviera en la anterior base de datos
    public boolean restoreBackup(final DatabaseSafePassword db, String nameBackupDatabase){
        Context c = activity.getApplicationContext();

        if(checkDirExist(c)){

            File database = new File(c.getDataDir().toString() + File.separator + "databases" + File.separator + c.getString(R.string.dir_local_backup), nameBackupDatabase);

            if(database.exists()){
                // No es necesario checkar si existe el directorio y crearlo en caso contrario
                // Porque solo se puede restaurar si antes se ha creado una copia
                // Aun así, pudiera ocurrir que el usuario lo haya borrado manualmente

                return db.ImportBackup(c, nameBackupDatabase);
            }
        }

        return false;
    }

    // Borra uan copia de seguridad Local del dispositivo
    public boolean deleteBackup(final DatabaseSafePassword db, String nameBackup){

        Context c = activity.getApplicationContext();

        if(checkDirExist(c)){

            File backup = new File(c.getDataDir().toString() + File.separator + "databases" + File.separator + c.getString(R.string.dir_local_backup), nameBackup);

            // Si la copia sigue existiendo en el dispositivo entonces se pasa a eliminarlo
            if(backup.exists()){
                // Devuelve TRUE si ha sido posible eliminar el fichero
                if(backup.delete()){
                    return db.DeleteBackupDatabase(nameBackup);
                }
            }

        }

        return false;
    }

    // Comprueba que un directorio asociado a la aplicación existe
    private boolean checkDirExist(Context context) {

        String databasePath;
        databasePath = context.getDataDir().toString() + File.separator + "databases" + File.separator + context.getString(R.string.dir_local_backup);

        File folder = new File (databasePath);

        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }

        // Devuelve TRUE si el directorio existe o fue creado
        return success;

    }
}

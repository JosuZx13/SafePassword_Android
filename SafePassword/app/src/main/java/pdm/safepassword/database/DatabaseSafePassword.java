package pdm.safepassword.database;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.SQLOutput;
import java.util.ArrayList;

import pdm.safepassword.R;

//Extension SQLiteOpenHelper para administracion de base de datos
public class DatabaseSafePassword extends SQLiteOpenHelper {

    private final static String DATABASE = "DBSafePassword.db";

    private final static int version = 7; //Version de la base de datos (Si el valor cambia, se llama a actualizar, UPDGRADE)
    private String query_text; // Para formar sentencias SQL
    protected SQLiteDatabase dbOlder;
    private static SQLiteDatabase objectDB; //Base de Datos actual

    // Tabla USUARIOS - Ahora la tabla Usuario solo tendrá un único valor, id - contraseña y no más
    //#########################################
    private String TABLE_USUARIOS;
    private String KEY_USUARIOS_ID;
    private String KEY_USUARIOS_PIN;
    private String KEY_USUARIOS_PASS;
    private String KEY_USUARIOS_CLOUD;
    //#########################################

    // Tabla PASSWORD
    //#########################################
    private String TABLE_PASSWORD;
    private String KEY_PASSWORD_ID;
    private String KEY_PASSWORD_USER;
    private String KEY_PASSWORD_PASS;
    private String KEY_PASSWORD_NAME;
    private String KEY_PASSWORD_CATEGORY;
    private String KEY_PASSWORD_LOGO;
    private String KEY_PASSWORD_URL;
    //#########################################

    // Tabla COPIA DE SEGURIDAD
    //#########################################
    private String TABLE_BACKUP;
    private String KEY_BACKUP_ID;
    private String KEY_BACKUP_DATE;
    private String KEY_BACKUP_NAME;
    //#########################################

    // SENTENCIAS SQL
    //#########################################
    private String CREATE_TABLE_USUARIOS;
    private String CREATE_TABLE_PASSWORD;
    private String CREATE_TABLE_BACKUP;
    private String INSERT_USER;
    private String INSERT_PASS;
    private String INSERT_BACKUP;
    private String DROP_TABLE_USER;
    private String DROP_TABLE_PASSWORD;
    private String DROP_TABLE_BACKUP;
    private String UPDATE_SEQUENCE_USER;
    private String UPDATE_SEQUENCE_PASSWORD;
    private String UPDATE_SEQUENCE_BACKUP;
    private String DELETE_FROM_PASSWORD;
    //#########################################

    public String getNameDB () {return DATABASE;}

    /*Las busquedas tienen un CONSTRAINT donde el resultado debe estar controlado
    Por que la contraseña exista y pertenezca al usuario que hizo logging*/

    // INICIALIZACIÓN DE LOS NOMBRES DE LAS TABLAS Y CREACION DE TABLAS
    //##################################################################################################################################
    private void InicializarKey(Context context) {
        // TABLA USUARIOS
        TABLE_USUARIOS = context.getString(R.string.key_table_usuarios);
        KEY_USUARIOS_ID = context.getString(R.string.key_usuarios_id);
        KEY_USUARIOS_PIN = context.getString(R.string.key_usuarios_pin);
        KEY_USUARIOS_PASS = context.getString(R.string.key_usuarios_pass);
        KEY_USUARIOS_CLOUD = context.getString(R.string.key_usuarios_cloud);

        // TABLA PASSWORD
        TABLE_PASSWORD = context.getString(R.string.key_table_password);
        KEY_PASSWORD_ID = context.getString(R.string.key_password_id);
        KEY_PASSWORD_USER = context.getString(R.string.key_password_user);
        KEY_PASSWORD_PASS = context.getString(R.string.key_password_pass);
        KEY_PASSWORD_NAME = context.getString(R.string.key_password_name);
        KEY_PASSWORD_CATEGORY = context.getString(R.string.key_password_category);
        KEY_PASSWORD_URL = context.getString(R.string.key_password_url);
        KEY_PASSWORD_LOGO = context.getString(R.string.key_password_logo);

        // TABLA BACKUP
        TABLE_BACKUP = context.getString(R.string.key_table_backup);
        KEY_BACKUP_ID = context.getString(R.string.key_backup_id);
        KEY_BACKUP_DATE = context.getString(R.string.key_backup_date);
        KEY_BACKUP_NAME = context.getString(R.string.key_backup_name);

        //Los correspondientes DROP de cada tabla (esto elimina la tabla completa
        DROP_TABLE_USER = "DROP TABLE IF EXISTS "+TABLE_USUARIOS+";";
        DROP_TABLE_PASSWORD = "DROP TABLE IF EXISTS "+TABLE_PASSWORD+";";
        DROP_TABLE_BACKUP = "DROP TABLE IF EXISTS "+TABLE_BACKUP+";";

        // Esto elimina cada item de la tabla, pero la tabla sigue existiendo
        DELETE_FROM_PASSWORD = "DELETE FROM "+TABLE_PASSWORD+";";

        // Estas sentencias sirven para resetear el autoincremento cuando se ha eliminado el contenido de la tabla al completo
        UPDATE_SEQUENCE_USER = "UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='"+TABLE_USUARIOS+"';";
        UPDATE_SEQUENCE_PASSWORD = "UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='"+TABLE_PASSWORD+"';";
        UPDATE_SEQUENCE_BACKUP = "UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='"+TABLE_BACKUP+"';";
    }

    // SENTENCIAS CREATE TABLE
    private void CreateTables(){
        CREATE_TABLE_USUARIOS = "CREATE TABLE "+TABLE_USUARIOS+"("
                +KEY_USUARIOS_ID+" integer primary key AUTOINCREMENT, "+KEY_USUARIOS_PIN+" TEXT, "+KEY_USUARIOS_PASS+ " TEXT, "+KEY_USUARIOS_CLOUD+ " TEXT);";

        CREATE_TABLE_PASSWORD = "CREATE TABLE "+TABLE_PASSWORD+"("
                +KEY_PASSWORD_ID+" integer primary key AUTOINCREMENT, "+KEY_PASSWORD_NAME+ " TEXT, "+KEY_PASSWORD_USER+ " TEXT, "+KEY_PASSWORD_PASS+ " TEXT, "+KEY_PASSWORD_CATEGORY+ " TEXT, "+KEY_PASSWORD_URL+ " TEXT, "+KEY_PASSWORD_LOGO+ " integer);";

        CREATE_TABLE_BACKUP = "CREATE TABLE "+TABLE_BACKUP+"("
                +KEY_BACKUP_ID+" integer primary key AUTOINCREMENT, "+KEY_BACKUP_DATE+ " TEXT, "+KEY_BACKUP_NAME+ " TEXT);";

    }
    //##################################################################################################################################


    // SENTENCIAS SQL
    //##################################################################################################################################

    // Sentencia de inserción de datos en la Tabla Usuarios
    public boolean InsertDataUsuarios(String pass){
        boolean newUserAdded = false;

        try{

            // Sentencia SQL para insertar datos en la tabla USUARIOS
            INSERT_USER = "INSERT INTO "+ TABLE_USUARIOS+" ("+KEY_USUARIOS_ID+", "+KEY_USUARIOS_PIN+", "+KEY_USUARIOS_PASS+", "+KEY_USUARIOS_CLOUD+") " +
                    "VALUES (null, '"+-1000+"', '"+pass+"', '-g');";

            objectDB = getWritableDatabase();

            if(objectDB != null) {

                objectDB.execSQL(INSERT_USER);

                newUserAdded = true;

                objectDB.close();
            }

        } catch (SQLException sqe){
            newUserAdded = false;
            System.err.println("Something wrong with SQLiteDatabase: "+ sqe.toString());
        } catch (Exception e){
            newUserAdded = false;
            System.err.println("Error: " + e.toString());
        }

        return newUserAdded;

    }

    // Actualizar la contraseña de la tabla USUARIO
    public boolean UpdateUserDatabase(int id, String p) {
        boolean modified = true;

        try{

            // Se abre la conexión a la Base de Datos EN MODO ESCRITURA
            objectDB = getWritableDatabase();

            // Se ha inicializado correctamente
            if (objectDB != null) {
                query_text = "UPDATE " + TABLE_USUARIOS + " SET " + KEY_USUARIOS_PASS + " = '" + p + "' WHERE " + KEY_USUARIOS_ID + " = '" + id + "';";

                objectDB.execSQL(query_text);

                objectDB.close();
            }

        } catch (SQLException sqe){
            modified = false;
            System.err.println("Something wrong with SQLiteDatabase: "+ sqe.toString());
        } catch (Exception e){
            modified = false;
            System.err.println("Error: " + e.toString());
        }

        return modified;
    }

    // Actualizar el pin del usuario
    public boolean UpdatePinUserDatabase(int id, int pin) {
        boolean modified = true;

        try{
            // Se abre la conexión a la Base de Datos EN MODO ESCRITURA
            objectDB = getWritableDatabase();

            // Se ha inicializado correctamente
            if (objectDB != null) {
                query_text = "UPDATE " + TABLE_USUARIOS + " SET " + KEY_USUARIOS_PIN + " = '" + pin + "' WHERE " + KEY_USUARIOS_ID + " = '" + id + "';";

                objectDB.execSQL(query_text);

                objectDB.close();
            }

        } catch (SQLException sqe){
            modified = false;
            System.err.println("Something wrong with SQLiteDatabase: "+ sqe.toString());
        } catch (Exception e){
            modified = false;
            System.err.println("Error: " + e.toString());
        }

        return modified;
    }

    // Actualizar el pin del usuario
    public boolean UpdateCloudUserDatabase(int id, String cloud) {
        boolean modified = true;

        try{
            // Se abre la conexión a la Base de Datos EN MODO ESCRITURA
            objectDB = getWritableDatabase();

            // Se ha inicializado correctamente
            if (objectDB != null) {
                query_text = "UPDATE " + TABLE_USUARIOS + " SET " + KEY_USUARIOS_CLOUD + " = '" + cloud + "' WHERE " + KEY_USUARIOS_ID + " = '" + id + "';";

                objectDB.execSQL(query_text);

                objectDB.close();
            }

        } catch (SQLException sqe){
            modified = false;
            System.err.println("Something wrong with SQLiteDatabase: "+ sqe.toString());
        } catch (Exception e){
            modified = false;
            System.err.println("Error: " + e.toString());
        }

        return modified;
    }

    // Elimina un usuario de la tabla USUARIOS
    // Borrar un usuario conlleva eliminar todas las contraseñas asociadas
    public boolean DeleteUserDatabase(int id) {
        boolean deleted = true;

        try{
            // Lo primero es recoger todas las contraseñas asociadas
            ArrayList<PasswordDatabase> passAsociadas = GetPasswordUserDatabase();

            // Por cada contraseña que haya, se elimina
            for(PasswordDatabase pd : passAsociadas){
                DeletePasswordDatabase(pd.getPswID());
            }

            // Ahora que ya están todas eliminadas, se elimina el usuario

            // Se abre la conexión a la Base de Datos EN MODO ESCRITURA
            objectDB = getWritableDatabase();

            // Se ha inicializado correctamente
            if (objectDB != null) {
                query_text = "DELETE FROM " + TABLE_USUARIOS + " WHERE " + KEY_USUARIOS_ID + " = '" + id + "';";

                objectDB.execSQL(query_text);

                objectDB.execSQL(UPDATE_SEQUENCE_USER);

                objectDB.close();
            }

        } catch (SQLException sqe){
            deleted = false;
            System.err.println("Something wrong with SQLiteDatabase: "+ sqe.toString());
        } catch (Exception e){
            deleted = false;
            System.err.println("Error: " + e.toString());
        }

        return deleted;

    }

    // Cuando se inicia sesión con la huella, se recoge al único usuario que debería haber
    public UserDatabase SelectUniqueUserDatabase(){
        UserDatabase userFound = new UserDatabase();

        try{

            // Se abre la conexión a la Base de Datos
            objectDB = getReadableDatabase();

            // Se ha inicializado correctamente
            if(objectDB != null){
                query_text = "SELECT * FROM " + TABLE_USUARIOS + ";";

                Cursor usrDatos = objectDB.rawQuery(query_text, null);

                if (usrDatos.moveToFirst()) {
                    //Posicion 0 = id
                    //Posicion 1 = pin
                    //Posicion 2 = pass
                    userFound.setUsrID(usrDatos.getInt(0));
                    userFound.setUsrPin(usrDatos.getInt(1));
                    userFound.setUsrPass(usrDatos.getString(2));
                    userFound.setUsrCloud(usrDatos.getString(3));

                }

                usrDatos.close(); // Se cierra el Iterator
                objectDB.close(); // Se cierra la conexion a la base de datos
            }

        } catch (SQLException sqe){
            System.err.println("Something wrong with SQLiteDatabase: "+ sqe.toString());
        } catch (Exception e){
            System.err.println("Error: " + e.toString());
        }

        return userFound;
    }

    // Busca si existe un usuario y lo devuelve (Ahora se comprueba la contraseña solo)
    public UserDatabase SelectUserDatabase(String u){
        UserDatabase userFound = new UserDatabase();

        try{

            // Se abre la conexión a la Base de Datos
            objectDB = getReadableDatabase();

            // Se ha inicializado correctamente
            if(objectDB != null){
                query_text = "SELECT * FROM " + TABLE_USUARIOS + " WHERE "+ KEY_USUARIOS_PASS + " = '" + u + "';";
                // Cursor es un objeto que guarda el resultado de la consulta y permite navegar por ella
                // De la misma forma que se haría con un ITERATOR (ES un Iterator, resumiendo)
                Cursor usrDatos = objectDB.rawQuery(query_text, null);
                // Solo puede existir una única contraseña (por tanto, un único usuario)
                if (usrDatos.moveToFirst()) {
                    //Posicion 0 = id
                    //Posicion 1 = pin
                    //Posicion 2 = pass
                    //Posicion 3 = cloud
                    userFound.setUsrID(usrDatos.getInt(0));
                    userFound.setUsrPin(usrDatos.getInt(1));
                    userFound.setUsrPass(usrDatos.getString(2));
                    userFound.setUsrCloud(usrDatos.getString(3));
                }
                // El else no es necesario, ya que si no entra en el IF
                // El constructor por defecto ya le dio valor -400 al ID

                usrDatos.close(); // Se cierra el Iterator
                objectDB.close(); // Se cierra la conexion a la base de datos
            }

        } catch (SQLException sqe){
            System.err.println("Something wrong with SQLiteDatabase: "+ sqe.toString());
        } catch (Exception e){
            System.err.println("Error: " + e.toString());
        }

        return userFound;
    }

    // Si hay una contraseña almacenada, ya no se pueden registrar más usuarios
    public boolean CountUserDatabase(){ // True = UNA CUENTA
        boolean user = false;

        try{

            objectDB = getReadableDatabase();

            if(objectDB != null){
                query_text = "SELECT * FROM " + TABLE_USUARIOS + ";";

                Cursor usrDatos = objectDB.rawQuery(query_text, null);
                // Solo puede existir una única contraseña (por tanto, un único usuario)
                if (usrDatos.moveToFirst()) {
                    // Si entra aquí, es que al menos hay un elemento, por tanto, devuelve TRUE
                    user = true;
                }

                usrDatos.close(); // Se cierra el Iterator
                objectDB.close(); // Se cierra la conexion a la base de datos
            }

        } catch (SQLException sqe){
            user = false;
            System.err.println("Something wrong with SQLiteDatabase: "+ sqe.toString());
        } catch (Exception e){
            user = false;
            System.err.println("Error: " + e.toString());
        }

        return user;
    }


    // SENTENCIAS PARA LA TABLA PASSWORD
    //##################################################################################################################################
    //##################################################################################################################################

    // Sentencia de inserción de datos en la Tabla contraseñas
    public boolean InsertDataPassword(String nom, String us, String pass, String categ, String ur, int lo){
        boolean newPasswordAdded = false;

        if(ur.trim().equals("")){
            ur = "-u"; //Esto se traduce en la app como que no tiene URL asociada. Es más fácil de comprobar
        }

        try{
            // Sentencia SQL para insertar datos en la tabla PASSWORD
            INSERT_PASS = "INSERT INTO "+ TABLE_PASSWORD+" ("+KEY_PASSWORD_ID+", "+KEY_PASSWORD_NAME+", "+KEY_PASSWORD_USER+", "+KEY_PASSWORD_PASS+", "+KEY_PASSWORD_CATEGORY+", "+KEY_PASSWORD_URL+", "+KEY_PASSWORD_LOGO+") " +
                    "VALUES (null, '"+nom+"', '"+us+"', '"+pass+"', '"+categ+"', '"+ur+"', '"+lo+"');";

            objectDB = getWritableDatabase();

            if(objectDB != null){
                objectDB.execSQL(INSERT_PASS);
                newPasswordAdded = true;

                objectDB.close();
            }

        } catch (SQLException sqe){
            newPasswordAdded = false;
            System.err.println("Something wrong with SQLiteDatabase: "+ sqe.toString());
        } catch (Exception e){
            newPasswordAdded = false;
            System.err.println("Error: " + e.toString());
        }

        return newPasswordAdded;
    }

    // Actualizar un dato de la tabla USUARIOS
    public boolean UpdatePassDatabase(int id, String n, String u, String p, String c, String ur, int lo) {
        boolean updated = false;

        try{
            // Se abre la conexión a la Base de Datos EN MODO ESCRITURA
            objectDB = getWritableDatabase();

            // Se ha inicializado correctamente
            if (objectDB != null) {
                query_text = "UPDATE " + TABLE_PASSWORD +
                        " SET " + KEY_PASSWORD_NAME + " = '" + n + "', "+ KEY_PASSWORD_USER + " = '" + u + "', " + KEY_PASSWORD_PASS + " = '" + p +
                        "', " + KEY_PASSWORD_CATEGORY + " = '" + c + "', " + KEY_PASSWORD_URL + " = '" + ur + "', " + KEY_PASSWORD_LOGO + " = '" + lo +
                        "' WHERE " + KEY_PASSWORD_ID + " = '" + id + "';";

                objectDB.execSQL(query_text);

                objectDB.close();

                updated = true;
            }

        } catch (SQLException sqe){
            updated = false;
            System.err.println("Something wrong with SQLiteDatabase: "+ sqe.toString());
        } catch (Exception e){
            updated = false;
            System.err.println("Error: " + e.toString());
        }

        return updated;

    }

    // Elimina una contraseña de la tabla PASSWORD
    public boolean DeletePasswordDatabase(int id) {
        boolean deleted = false;

        try{
            // Se abre la conexión a la Base de Datos EN MODO ESCRITURA
            objectDB = getWritableDatabase();

            // Se ha inicializado correctamente
            if (objectDB != null) {
                query_text = "DELETE FROM " + TABLE_PASSWORD + " WHERE " + KEY_PASSWORD_ID + " = '" + id + "';";

                objectDB.execSQL(query_text);

                deleted = true;

                objectDB.close();
            }

        } catch (SQLException sqe){
            deleted = false;
            System.err.println("Something wrong with SQLiteDatabase: "+ sqe.toString());
        } catch (Exception e){
            deleted = false;
            System.err.println("Error: " + e.toString());
        }

        return deleted;

    }

    // Eliminar todos los Item de la Tabla Password
    public boolean DeleteAllPasswordDatabase() {
        boolean deleted = false;

        try{
            // Se abre la conexión a la Base de Datos EN MODO ESCRITURA
            objectDB = getWritableDatabase();

            // Se ha inicializado correctamente
            if (objectDB != null) {

                objectDB.execSQL(DELETE_FROM_PASSWORD);

                objectDB.execSQL(UPDATE_SEQUENCE_PASSWORD);

                deleted = true;

                objectDB.close();
            }

        } catch (SQLException sqe){
            deleted = false;
            System.err.println("Something wrong with SQLiteDatabase: "+ sqe.toString());
        } catch (Exception e){
            deleted = false;
            System.err.println("Error: " + e.toString());
        }

        return deleted;
    }

    // Busca si existe un objeto y lo devuelve (si no existe, devuelve una contraseña con valor ID -500)
    // En esta búsqueda sí recibe el identificador del ID ya que no se sabe cual es la contraseña
    // Pero distintos usuarios pueden tener contraseñas con mismo NAME
    public PasswordDatabase SelectPasswordDatabase(String nam){
        PasswordDatabase passFound = new PasswordDatabase();

        try{
            // Se abre la conexión a la Base de Datos
            objectDB = getReadableDatabase();

            // Se ha inicializado correctamente
            if(objectDB != null){

                query_text = "SELECT * FROM " + TABLE_PASSWORD + " WHERE " + KEY_PASSWORD_NAME + " = '"+nam+"';";

                Cursor pswDatos = objectDB.rawQuery(query_text, null);
                // Solo debería devolver uno, ya que no puede haber contraseñas con mismo name en la tabla PASSWORD
                if (pswDatos.moveToFirst()) {
                    //Posicion 0 = id
                    //Posicion 1 = name
                    //Posicion 2 = user
                    //Posicion 3 = pass
                    passFound.setPswID(pswDatos.getInt(0));
                    passFound.setPswName(pswDatos.getString(1));
                    passFound.setPswUser(pswDatos.getString(2));
                    passFound.setPswPass(pswDatos.getString(3));
                    passFound.setPswCategory(pswDatos.getString(4));
                    passFound.setPswUrl(pswDatos.getString(5));
                    passFound.setPswLogo(pswDatos.getInt(6));

                }
                // El else no es necesario, ya que si no entra en el IF
                // El constructor por defecto ya le dio valor -500 al ID

                pswDatos.close(); // Se cierra el Iterator
                objectDB.close(); // Se cierra la conexion a la base de datos
            }

        } catch (SQLException sqe){
            System.err.println("Something wrong with SQLiteDatabase: "+ sqe.toString());
        } catch (Exception e){
            System.err.println("Error: " + e.toString());
        }

        return passFound;
    }

    // Devuelve una colección de contraseñas que pertenecen a un usuario en concreto
    public ArrayList<PasswordDatabase> GetPasswordUserDatabase(){
        // Donde se guardaran todas las contraseñas recogidas que pertenecen al usuario
        ArrayList<PasswordDatabase> passList = new ArrayList<>();

        try{
            objectDB = getReadableDatabase();

            if(objectDB != null){
                query_text = "SELECT * FROM " + TABLE_PASSWORD + " ORDER BY " + KEY_PASSWORD_NAME + " ASC;";

                Cursor pswDatos = objectDB.rawQuery(query_text, null);

                if (pswDatos.moveToFirst()) { // Puede ser que tenga 0

                    // Si hay algo se procesa, y cuando se haga, puede haber más de una contraseña
                    do{
                        //Posicion 0 = id
                        //Posicion 1 = name
                        //Posicion 2 = user
                        //Posicion 3 = pass
                        //Posicion 4 = category
                        //Posicion 5 = url
                        //Posicion 6 = logo

                        passList.add(new PasswordDatabase(pswDatos.getInt(0), pswDatos.getString(1), pswDatos.getString(2), pswDatos.getString(3)
                        , pswDatos.getString(4), pswDatos.getString(5), pswDatos.getInt(6)));
                    }while(pswDatos.moveToNext());

                }

                pswDatos.close(); // Se cierra el Iterator
                objectDB.close(); // Se cierra la conexion a la base de datos
            }

        } catch (SQLException sqe){
            System.err.println("Something wrong with SQLiteDatabase: "+ sqe.toString());
        } catch (Exception e){
            System.err.println("Error: " + e.toString());
        }

        return passList;
    }

    // Devuelve una colección de contraseñas que pertenecen a un usuario en concreto
    public ArrayList<PasswordDatabase> GetPasswordUserDatabaseByCategory(String categ){
        // Donde se guardaran todas las contraseñas recogidas que pertenecen al usuario
        ArrayList<PasswordDatabase> passListByCategory = new ArrayList<>();

        try{
            objectDB = getReadableDatabase();

            if(objectDB != null){
                query_text = "SELECT * FROM " + TABLE_PASSWORD + " WHERE " + KEY_PASSWORD_CATEGORY + " = '" + categ + "' ORDER BY " + KEY_PASSWORD_NAME + " ASC;";

                Cursor pswDatos = objectDB.rawQuery(query_text, null);

                if (pswDatos.moveToFirst()) { // Puede ser que tenga 0

                    // Si hay algo se procesa, y cuando se haga, puede haber más de una contraseña
                    do{
                        //Posicion 0 = id
                        //Posicion 1 = name
                        //Posicion 2 = user
                        //Posicion 3 = pass
                        //Posicion 4 = category
                        //Posicion 5 = url
                        //Posicion 6 = logo

                        passListByCategory.add(new PasswordDatabase(pswDatos.getInt(0), pswDatos.getString(1), pswDatos.getString(2), pswDatos.getString(3)
                                , pswDatos.getString(4), pswDatos.getString(5), pswDatos.getInt(6)));
                    }while(pswDatos.moveToNext());

                }

                pswDatos.close(); // Se cierra el Iterator
                objectDB.close(); // Se cierra la conexion a la base de datos
            }

        } catch (SQLException sqe){
            System.err.println("Something wrong with SQLiteDatabase: "+ sqe.toString());
        } catch (Exception e){
            System.err.println("Error: " + e.toString());
        }

        return passListByCategory;
    }

    // SENTENCIAS PARA LA TABLA BACKUP
    //##################################################################################################################################
    //##################################################################################################################################

    // Sentencia de inserción de datos en la Tabla Backup
    public boolean InsertDataBackup(String da, String nam){
        boolean newBackupAdded = false;

        try{

            // Sentencia SQL para insertar datos en la tabla USUARIOS
            INSERT_BACKUP = "INSERT INTO "+ TABLE_BACKUP+" ("+KEY_BACKUP_ID+", "+KEY_BACKUP_DATE+", "+KEY_BACKUP_NAME+") " +
                    "VALUES (null, '"+da+"', '"+nam+"');";

            objectDB = getWritableDatabase();

            if(objectDB != null){

                objectDB.execSQL(INSERT_BACKUP);
                newBackupAdded = true;
                objectDB.close();
            }

        } catch (SQLException sqe){
            newBackupAdded = false;
            System.err.println("Something wrong with SQLiteDatabase: "+ sqe.toString());
        } catch (Exception e){
            newBackupAdded = false;
            System.err.println("Error: " + e.toString());
        }

        return newBackupAdded;

    }

    // Elimina un usuario de la tabla USUARIOS
    // Borrar un usuario conlleva eliminar todas las contraseñas asociadas
    public boolean DeleteBackupDatabase(String nam) {
        boolean deleted = false;

        try{
            // Se abre la conexión a la Base de Datos EN MODO ESCRITURA
            objectDB = getWritableDatabase();

            // Se ha inicializado correctamente
            if (objectDB != null) {
                query_text = "DELETE FROM " + TABLE_BACKUP + " WHERE " + KEY_BACKUP_NAME + " = '" + nam + "';";

                objectDB.execSQL(query_text);
                // Si entra en este IF quiere decir que ha eliminado el elemento y que el cursor tiene un dato
                // El cual es el dato borrado

                deleted = true;

                objectDB.close();
            }

        } catch (SQLException sqe){
            deleted = false;
            System.err.println("Something wrong with SQLiteDatabase: "+ sqe.toString());
        } catch (Exception e){
            deleted = false;
            System.err.println("Error: " + e.toString());
        }

        return deleted;
    }

    // Devuelve una colección de copias de seguridad localBackup que pertenecen al usuario
    public ArrayList<BackupUserDatabase> GetBackupUserDatabase(){
        // Donde se guardaran todas las contraseñas recogidas que pertenecen al usuario
        ArrayList<BackupUserDatabase> backupList = new ArrayList<>();

        try{
            objectDB = getReadableDatabase();

            if(objectDB != null){
                query_text = "SELECT * FROM " + TABLE_BACKUP + " ORDER BY " + KEY_BACKUP_NAME + " DESC;";

                Cursor bkDatos = objectDB.rawQuery(query_text, null);

                if (bkDatos.moveToFirst()) { // Puede ser que tenga 0

                    // Si hay algo se procesa, y cuando se haga, puede haber más de una contraseña
                    do{
                        //Posicion 0 = id
                        //Posicion 1 = date
                        //Posicion 2 = name

                        backupList.add(new BackupUserDatabase(bkDatos.getInt(0), bkDatos.getString(1), bkDatos.getString(2)));

                    }while(bkDatos.moveToNext());

                }

                bkDatos.close(); // Se cierra el Iterator
                objectDB.close(); // Se cierra la conexion a la base de datos
            }

        } catch (SQLException sqe){
            System.err.println("Something wrong with SQLiteDatabase: "+ sqe.toString());
        } catch (Exception e){
            System.err.println("Error: " + e.toString());
        }

        return backupList;
    }

    public void ResetSequenceBackup(){

        try{
            objectDB = getWritableDatabase();

            if(objectDB != null){

                objectDB.execSQL(UPDATE_SEQUENCE_BACKUP);

                objectDB.close(); // Se cierra la conexion a la base de datos
            }

        } catch (SQLException sqe){
            System.err.println("Something wrong with SQLiteDatabase: "+ sqe.toString());
        } catch (Exception e){
            System.err.println("Error: " + e.toString());
        }
    }

    //##################################################################################################################################
    //##################################################################################################################################
    //##################################################################################################################################

    // INICIALIZACIÓN DE LA BASE DE DATOS

    //Cuando se crea objeto de tipo Database llama a ese metodo
    public DatabaseSafePassword(Context context) {
        super(context, DATABASE, null, version);
        InicializarKey(context);
    }

    // Solo se ejecuta si la base de datos no está creada
    @Override
    public void onCreate(SQLiteDatabase db) {
        CreateTables();
        db.execSQL(CREATE_TABLE_USUARIOS);
        db.execSQL(CREATE_TABLE_PASSWORD);
        db.execSQL(CREATE_TABLE_BACKUP);

        String pass = "admin";

        db.execSQL(INSERT_USER = "INSERT INTO "+ TABLE_USUARIOS+" ("+KEY_USUARIOS_ID+", "+KEY_USUARIOS_PIN+", "+KEY_USUARIOS_PASS+", "+KEY_USUARIOS_CLOUD+") " +
                "VALUES (null, '"+-1000+"', '"+pass+"', '-g');");

        String nomb = "RouterCasa";
        String us = "TwiTTer@correo.es";
        String passw = "Twitter_Pass";
        String categ = "WiFi";
        String ur = "Twitter.com";
        int lo = 2131230900;

        db.execSQL(INSERT_PASS = "INSERT INTO "+ TABLE_PASSWORD+" ("+KEY_PASSWORD_ID+", "+KEY_PASSWORD_NAME+", "+KEY_PASSWORD_USER+", "+KEY_PASSWORD_PASS+", "+KEY_PASSWORD_CATEGORY+", "+KEY_PASSWORD_URL+", "+KEY_PASSWORD_LOGO+") " +
                "VALUES (null, '"+nomb+"', '"+us+"', '"+passw+"', '"+categ+"', '"+ur+"', '"+lo+"');");

        nomb = "InternetGranada";
        us = "insTa@correo.es";
        passw = "instagRAM_Pass";
        categ = "WiFi";
        ur = "Instagram.com";
        lo = 2131230900;

        db.execSQL(INSERT_PASS = "INSERT INTO "+ TABLE_PASSWORD+" ("+KEY_PASSWORD_ID+", "+KEY_PASSWORD_NAME+", "+KEY_PASSWORD_USER+", "+KEY_PASSWORD_PASS+", "+KEY_PASSWORD_CATEGORY+", "+KEY_PASSWORD_URL+", "+KEY_PASSWORD_LOGO+") " +
                "VALUES (null, '"+nomb+"', '"+us+"', '"+passw+"', '"+categ+"', '"+ur+"', '"+lo+"');");

        nomb = "Outlook";
        us = "insTa@correo.es";
        passw = "instagRAM_Pass";
        categ = "CorreoElectronico";
        ur = "Instagram.com";
        lo = 2131230900;

        db.execSQL(INSERT_PASS = "INSERT INTO "+ TABLE_PASSWORD+" ("+KEY_PASSWORD_ID+", "+KEY_PASSWORD_NAME+", "+KEY_PASSWORD_USER+", "+KEY_PASSWORD_PASS+", "+KEY_PASSWORD_CATEGORY+", "+KEY_PASSWORD_URL+", "+KEY_PASSWORD_LOGO+") " +
                "VALUES (null, '"+nomb+"', '"+us+"', '"+passw+"', '"+categ+"', '"+ur+"', '"+lo+"');");

        nomb = "Twitter1";
        us = "insTa@correo.es";
        passw = "instagRAM_Pass";
        categ = "RedSocial";
        ur = "Instagram.com";
        lo = 2131230900;

        db.execSQL(INSERT_PASS = "INSERT INTO "+ TABLE_PASSWORD+" ("+KEY_PASSWORD_ID+", "+KEY_PASSWORD_NAME+", "+KEY_PASSWORD_USER+", "+KEY_PASSWORD_PASS+", "+KEY_PASSWORD_CATEGORY+", "+KEY_PASSWORD_URL+", "+KEY_PASSWORD_LOGO+") " +
                "VALUES (null, '"+nomb+"', '"+us+"', '"+passw+"', '"+categ+"', '"+ur+"', '"+lo+"');");


        nomb = "Lenovo";
        us = "insTa@correo.es";
        passw = "instagRAM_Pass";
        categ = "Otro";
        ur = "Instagram.com";
        lo = 2131230900;

        db.execSQL(INSERT_PASS = "INSERT INTO "+ TABLE_PASSWORD+" ("+KEY_PASSWORD_ID+", "+KEY_PASSWORD_NAME+", "+KEY_PASSWORD_USER+", "+KEY_PASSWORD_PASS+", "+KEY_PASSWORD_CATEGORY+", "+KEY_PASSWORD_URL+", "+KEY_PASSWORD_LOGO+") " +
                "VALUES (null, '"+nomb+"', '"+us+"', '"+passw+"', '"+categ+"', '"+ur+"', '"+lo+"');");


        nomb = "Anilist";
        us = "insTa@correo.es";
        passw = "instagRAM_Pass";
        categ = "Foro";
        ur = "Instagram.com";
        lo = 2131230900;

        db.execSQL(INSERT_PASS = "INSERT INTO "+ TABLE_PASSWORD+" ("+KEY_PASSWORD_ID+", "+KEY_PASSWORD_NAME+", "+KEY_PASSWORD_USER+", "+KEY_PASSWORD_PASS+", "+KEY_PASSWORD_CATEGORY+", "+KEY_PASSWORD_URL+", "+KEY_PASSWORD_LOGO+") " +
                "VALUES (null, '"+nomb+"', '"+us+"', '"+passw+"', '"+categ+"', '"+ur+"', '"+lo+"');");


        nomb = "MyAnimeList";
        us = "insTa@correo.es";
        passw = "instagRAM_Pass";
        categ = "Foro";
        ur = "Instagram.com";
        lo = 2131230900;

        db.execSQL(INSERT_PASS = "INSERT INTO "+ TABLE_PASSWORD+" ("+KEY_PASSWORD_ID+", "+KEY_PASSWORD_NAME+", "+KEY_PASSWORD_USER+", "+KEY_PASSWORD_PASS+", "+KEY_PASSWORD_CATEGORY+", "+KEY_PASSWORD_URL+", "+KEY_PASSWORD_LOGO+") " +
                "VALUES (null, '"+nomb+"', '"+us+"', '"+passw+"', '"+categ+"', '"+ur+"', '"+lo+"');");

        nomb = "Netflix";
        us = "insTa@correo.es";
        passw = "instagRAM_Pass";
        categ = "Otro";
        ur = "Instagram.com";
        lo = 2131230900;

        db.execSQL(INSERT_PASS = "INSERT INTO "+ TABLE_PASSWORD+" ("+KEY_PASSWORD_ID+", "+KEY_PASSWORD_NAME+", "+KEY_PASSWORD_USER+", "+KEY_PASSWORD_PASS+", "+KEY_PASSWORD_CATEGORY+", "+KEY_PASSWORD_URL+", "+KEY_PASSWORD_LOGO+") " +
                "VALUES (null, '"+nomb+"', '"+us+"', '"+passw+"', '"+categ+"', '"+ur+"', '"+lo+"');");

        nomb = "Asus";
        us = "insTa@correo.es";
        passw = "instagRAM_Pass";
        categ = "Trabajo";
        ur = "Instagram.com";
        lo = 2131230900;

        db.execSQL(INSERT_PASS = "INSERT INTO "+ TABLE_PASSWORD+" ("+KEY_PASSWORD_ID+", "+KEY_PASSWORD_NAME+", "+KEY_PASSWORD_USER+", "+KEY_PASSWORD_PASS+", "+KEY_PASSWORD_CATEGORY+", "+KEY_PASSWORD_URL+", "+KEY_PASSWORD_LOGO+") " +
                "VALUES (null, '"+nomb+"', '"+us+"', '"+passw+"', '"+categ+"', '"+ur+"', '"+lo+"');");

        nomb = "Google";
        us = "insTa@correo.es";
        passw = "instagRAM_Pass";
        categ = "CorreoElectronico";
        ur = "Instagram.com";
        lo = 2131230900;

        db.execSQL(INSERT_PASS = "INSERT INTO "+ TABLE_PASSWORD+" ("+KEY_PASSWORD_ID+", "+KEY_PASSWORD_NAME+", "+KEY_PASSWORD_USER+", "+KEY_PASSWORD_PASS+", "+KEY_PASSWORD_CATEGORY+", "+KEY_PASSWORD_URL+", "+KEY_PASSWORD_LOGO+") " +
                "VALUES (null, '"+nomb+"', '"+us+"', '"+passw+"', '"+categ+"', '"+ur+"', '"+lo+"');");

        nomb = "Santander";
        us = "insTa@correo.es";
        passw = "instagRAM_Pass";
        categ = "Banco";
        ur = "Instagram.com";
        lo = 2131230900;

        db.execSQL(INSERT_PASS = "INSERT INTO "+ TABLE_PASSWORD+" ("+KEY_PASSWORD_ID+", "+KEY_PASSWORD_NAME+", "+KEY_PASSWORD_USER+", "+KEY_PASSWORD_PASS+", "+KEY_PASSWORD_CATEGORY+", "+KEY_PASSWORD_URL+", "+KEY_PASSWORD_LOGO+") " +
                "VALUES (null, '"+nomb+"', '"+us+"', '"+passw+"', '"+categ+"', '"+ur+"', '"+lo+"');");

        nomb = "CajaGranada";
        us = "insTa@correo.es";
        passw = "instagRAM_Pass";
        categ = "Banco";
        ur = "Instagram.com";
        lo = 2131230900;

        db.execSQL(INSERT_PASS = "INSERT INTO "+ TABLE_PASSWORD+" ("+KEY_PASSWORD_ID+", "+KEY_PASSWORD_NAME+", "+KEY_PASSWORD_USER+", "+KEY_PASSWORD_PASS+", "+KEY_PASSWORD_CATEGORY+", "+KEY_PASSWORD_URL+", "+KEY_PASSWORD_LOGO+") " +
                "VALUES (null, '"+nomb+"', '"+us+"', '"+passw+"', '"+categ+"', '"+ur+"', '"+lo+"');");

    }

    // Solo se ejecuta si la version de la Base de Datos es MAYOR que la version de cuando fue creada
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dbOlder = db;
        db.execSQL(DROP_TABLE_USER);
        db.execSQL(DROP_TABLE_PASSWORD);
        db.execSQL(DROP_TABLE_BACKUP);
        onCreate(db);
    }

    //##################################################################################################################################
    //##################################################################################################################################
    //##################################################################################################################################

    // ADMINISTRACIÓN DE LAS COPIAS DE SEGURIDAD

    // Metodo que crea una copia fisica en el telefono de la base de datos actual
    public boolean ExportBackup(Context c, File outBackup, String date, String name){

        // Ruta de la base de datos
        final String pathFileName = c.getDatabasePath(DATABASE).toString();

        try {

            // Se recoge el fichero database existente que creo la aplicación
            File dbFile = new File(pathFileName);
            FileInputStream fis = new FileInputStream(dbFile);

            // Se abre el flujo de salida para poder escribir la base de datos actual al nuevo fichero
            OutputStream output = new FileOutputStream(outBackup);

            // Copia los bytes desde el fichero abierto al fichero de salida
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            // Se cierran los flujos
            output.flush();
            output.close();
            fis.close();

            // Si llega aquí es que ha ido bien y se puede recoger en la TABLA
            return InsertDataBackup(date, name);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean restoreLocalDatabase(SQLiteDatabase copy){
        if(DeleteAllPasswordDatabase()) {
            // Con openDatabase en modo lectura se supone que ya no es null copy, pero por si acaso

            if (copy != null) {
                // Se abre una instancia de la Base de Datos actual
                objectDB = getWritableDatabase();

                if(objectDB != null){

                    query_text = "SELECT * FROM " + TABLE_PASSWORD + " ORDER BY " + KEY_PASSWORD_NAME + " ASC;";

                    Cursor pswCopy = copy.rawQuery(query_text, null);
                    if (pswCopy.moveToFirst()) { // Puede ser que tenga 0
                        // Si hay algo se procesa, y cuando se haga, puede haber más de una contraseña
                        do {
                            //Posicion 0 = id
                            //Posicion 1 = name
                            //Posicion 2 = user
                            //Posicion 3 = pass

                            // Cada iteración del cursor es un elemento Password
                            // Solo falta hacer los INSERT correspondientes

                            INSERT_PASS = "INSERT INTO "+ TABLE_PASSWORD+" ("+KEY_PASSWORD_ID+", "+KEY_PASSWORD_NAME+", "+KEY_PASSWORD_USER+", "+KEY_PASSWORD_PASS+") " +
                                    "VALUES (null, '"+pswCopy.getString(1)+"', '"+pswCopy.getString(2)+"', '"+pswCopy.getString(3)+"');";

                            objectDB.execSQL(INSERT_PASS);

                        } while (pswCopy.moveToNext());

                    }

                    pswCopy.close(); // Se cierra el Iterator
                    objectDB.close(); // Se cierra la conexion a la base de datos

                }

                return true;
            }

        }

        return false;
    }

    // Metodo que sobreescribe la base de datos actual con una copia de datos EXISTENTE
    public boolean ImportBackup(Context c, String nameBackup) {
        boolean imported = false;
        String databasePath;

        try{
            databasePath = c.getDataDir().toString() + File.separator + "databases" + File.separator + c.getString(R.string.dir_local_backup);
            File copyDB = new File(databasePath, nameBackup);

            // Se crea una instancia con la Base de Datos copia
            SQLiteDatabase copy = SQLiteDatabase.openDatabase(copyDB.getPath(), null, SQLiteDatabase.OPEN_READONLY);

            if(restoreLocalDatabase(copy)){

               imported = true;
            }

            copy.close();
            return imported;

        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

}

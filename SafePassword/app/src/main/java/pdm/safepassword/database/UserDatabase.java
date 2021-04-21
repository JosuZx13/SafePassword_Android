package pdm.safepassword.database;

import java.io.Serializable;

//Serializable, para poder hacer búsquedas y tratarlo como objeto identificativo
public class UserDatabase implements Serializable {
    //Atributos
    private int usrId;
    private int usrPin;
    private String usrPassword;
    private String usrCloud;

    //Constructor (quizas no sea necesario el por defecto)
    public UserDatabase(){
        usrId = -400;
        usrPin = -1000;
        usrPassword = "n";
        usrCloud = "-g";
    }

    /* Consturctor por Parametros (se usará en las búsquedas de la base de datos
    Es casi obligatorio si la clase es Serializable ya que podrán generarse
    Objetos automáticamente con los datos correspondientes */
    public UserDatabase(int i, int pin, String p, String g){
        usrId = i;
        usrPin = pin;
        usrPassword = p;
        usrCloud = g;
    }

    // Metodos GET
    public int getUsrID () {return usrId;}
    public int getUsrPin () {return usrPin;}
    public String getUsrPass () {return usrPassword;}
    public String getUsrCloud() {return usrCloud;}

    // Metodos SET
    public void setUsrID (int i) {usrId = i;}
    public void setUsrPin (int p) {usrPin = p;}
    public void setUsrPass (String p) {usrPassword = p;}
    public void setUsrCloud (String g) {usrCloud = g;}

    @Override
    public String toString() {
        return "UserDatabase{" +
                "usrId=" + usrId +
                ", usrPin=" + usrPin +
                ", usrPassword='" + usrPassword + '\'' +
                ", usrGoogleGmail='" + usrCloud + '\'' +
                '}';
    }
}
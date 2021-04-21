package pdm.safepassword.database;

import java.io.Serializable;

public class BackupUserDatabase implements Serializable {
    //Atributos
    private int bkId;
    private String bkDate;
    private String bkName;

    // Constructor por Defecto
    public BackupUserDatabase() {
        bkId = -300;
        bkDate = "-d";
        bkName = "null";
    }

    // Constructor por parámetros
    public BackupUserDatabase(int i, String d, String n) {
        bkId = i;
        bkDate = d;
        bkName = n;
    }

    // Metodos GET
    public int getBkID() {
        return bkId;
    }
    public String getBkDate() {return bkDate;}
    public String getBkName() {
        return bkName;
    }

    // Metodos SET
    public void setBkID(int i) {
        bkId = i;
    }
    public void setBkDate(String d){bkDate = d;}
    public void setBkName(String p) {
        bkName = p;
    }

    @Override
    public String toString() {
        return "BackupUserDatabase{" +
                "bkId=" + bkId +
                ", bkDate='" + bkDate + '\'' +
                ", bkName='" + bkName + '\'' +
                '}';
    }
}

package pdm.safepassword.database;

import java.io.Serializable;

public class PasswordDatabase implements Serializable {

    //Atributos
    private int pswId;
    //private int pswIdOwner; //Ya no es necesaria esta distinción, la contraseña pertenece al único usuario que habra
    private String pswName;
    private String pswUser;
    private String pswPassword;
    private String pswCategory;
    private String pswUrl;
    private int pswLogo;

    //Constructor (quizas no sea necesario el por defecto)
    public PasswordDatabase(){
        pswId = -500;
        pswName = "n";
        pswUser = "n";
        pswPassword = "n";
        pswCategory = "-c";
        pswUrl = "-u";
        pswLogo = -10;
    }

    // Constructor por parametros (recomendable para clases Serializables)
    public PasswordDatabase(int i, String nam, String u, String p, String categ, String ur, int lo){
        pswId = i;
        pswName = nam;
        pswUser = u;
        pswPassword = p;
        pswCategory = categ;
        pswUrl = ur;
        pswLogo = lo;
    }

    // Metodos GET
    public int getPswID () {return pswId;}
    public String getPswName () {return pswName;}
    public String getPswUser () {return pswUser;}
    public String getPswPass () {return pswPassword;}
    public String getPswCategory () {return pswCategory;}
    public String getPswUrl () {return pswUrl;}
    public int getPswLogo () {return pswLogo;}

    // Metodos SET
    public void setPswID (int i) {pswId = i;}
    public void setPswName(String nam) {pswName = nam;}
    public void setPswUser (String u) {pswUser = u;}
    public void setPswPass (String p) {pswPassword = p;}
    public void setPswCategory (String categ) {pswCategory = categ;}
    public void setPswUrl (String ur) {pswUrl = ur;}
    public void setPswLogo (int lo) {pswLogo = lo;}

    @Override
    public String toString() {
        return "PasswordDatabase{" +
                "pswId=" + pswId +
                ", pswName='" + pswName + '\'' +
                ", pswUser='" + pswUser + '\'' +
                ", pswPassword='" + pswPassword + '\'' +
                ", pswCategory='" + pswCategory + '\'' +
                ", pswUrl='" + pswUrl + '\'' +
                ", pswLogo=" + pswLogo +
                '}';
    }
}

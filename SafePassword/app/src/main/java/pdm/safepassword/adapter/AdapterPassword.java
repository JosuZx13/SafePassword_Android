package pdm.safepassword.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import pdm.safepassword.R;
import pdm.safepassword.database.PasswordDatabase;

public class AdapterPassword extends RecyclerView.Adapter<AdapterPassword.PasswordViewHolder> implements View.OnClickListener {

    // Contendrá las contraseñas
    ArrayList<PasswordDatabase> passwordDataSet;
    //Este listener se encargara de gestionar las acciones de cada vista
    private View.OnClickListener listener;

    public AdapterPassword(ArrayList<PasswordDatabase> lista){
        passwordDataSet = lista;
    }

    // Con esta clase interna se recogen las referencias
    // A los componentes del layout
    public static class PasswordViewHolder extends RecyclerView.ViewHolder {
        // Se añaden tantos elementos como tenga el layout item
        // Que se usará para rellenar la vista del RecyclerView
        // En este caso es Item_password

        ImageView iv_item_password_logo;
        TextView tv_item_password_name;


        public PasswordViewHolder(View itemPassView){
            super(itemPassView);
            // Se buscan los elementos dentro de la vista creada por el layout personalizado para la muestra de las contraseñas
            tv_item_password_name = itemPassView.findViewById(R.id.it_name);
            iv_item_password_logo = itemPassView.findViewById(R.id.it_logo);
        }
    } // Fin de la Clase Estatica ViewHolder


    // El Layout Manager llama a este método para renderizar el RecyclerView
    @Override
    public AdapterPassword.PasswordViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        // Aquí se coge el modelo por defecto creado para usarlo como contenedor de diseño
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_password, parent, false);

        PasswordViewHolder contenedor = new PasswordViewHolder(itemView);

        // Se controla que al hacer click se ejecute una accion personalizada
        itemView.setOnClickListener(this);

        return contenedor;
    }

    Por tant

    @Override
    public int getItemCount() {
        return passwordDataSet.size();
    }


    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        if(listener != null){
            listener.onClick(v);
        }
    }

}

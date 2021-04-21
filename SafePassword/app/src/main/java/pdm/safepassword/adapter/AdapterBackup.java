package pdm.safepassword.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import pdm.safepassword.R;
import pdm.safepassword.database.BackupUserDatabase;
import pdm.safepassword.database.PasswordDatabase;

public class AdapterBackup extends RecyclerView.Adapter<AdapterBackup.BackupViewHolder> implements View.OnClickListener {

    // Contendrá las contraseñas
    ArrayList<BackupUserDatabase> backupDataset;
    //Este listener se encargara de gestionar las acciones de cada vista
    private View.OnClickListener listener;

    public AdapterBackup(ArrayList<BackupUserDatabase> lista){
        backupDataset = lista;
    }

    // Con esta clase interna se recogen las referencias
    // A los componentes del layout
    public static class BackupViewHolder extends RecyclerView.ViewHolder {
        // Se añaden tantos elementos como tenga el layout item
        // Que se usará para rellenar la vista del RecyclerView
        // En este caso es Item_Backup

        TextView tv_backup_name;

        public BackupViewHolder(View itemPassView){
            super(itemPassView);
            // Se cogen los TextView del Layout ITEM creado para la vista de la copias
            tv_backup_name = itemPassView.findViewById(R.id.tv_backup_name);
        }
    } // Fin de la Clase Estatica ViewHolder


    // El Layout Manager llama a este método para renderizar el RecyclerView
    @Override
    public AdapterBackup.BackupViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        // Aquí se coge el modelo por defecto creado para usarlo como contenedor de diseño
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_backup, parent, false);

        AdapterBackup.BackupViewHolder contenedor = new AdapterBackup.BackupViewHolder(itemView);

        // Se controla que al hacer click se ejecute una accion personalizada
        itemView.setOnClickListener(this);

        return contenedor;
    }

    @Override
    public void onBindViewHolder(AdapterBackup.BackupViewHolder viewHolder, int pos) {
        BackupUserDatabase item = backupDataset.get(pos);

        viewHolder.tv_backup_name.setText(item.getBkName());
    }

    @Override
    public int getItemCount() {
        return backupDataset.size();
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

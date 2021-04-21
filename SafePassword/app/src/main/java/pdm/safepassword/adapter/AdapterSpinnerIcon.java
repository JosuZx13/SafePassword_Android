package pdm.safepassword.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import pdm.safepassword.R;

// Esta clase se va a usar como una modificación del adapter standard de ArrayString para el Spinner
//Se modifica el valor de los datos que recibe, de String a Integer
public class AdapterSpinnerIcon extends ArrayAdapter<Integer> {
    private Context ctx;
    private Integer[] images;

    public AdapterSpinnerIcon(Context context, Integer[] images) {
        super(context, android.R.layout.simple_spinner_item, images);
        this.ctx = context;
        this.images = images;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.item_spinner, null);
        ImageView im = row.findViewById(R.id.item_spinner_logo);

        im.setImageResource(images[position]);

        return row;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.item_spinner, null);
        ImageView im = row.findViewById(R.id.item_spinner_logo);

        im.setImageResource(images[position]);

        return row;
    }

    private View getImageForPosition(int position) {
        ImageView imageView = new ImageView(getContext());
        imageView.setBackgroundResource(images[position]);
        imageView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return imageView;
    }

}

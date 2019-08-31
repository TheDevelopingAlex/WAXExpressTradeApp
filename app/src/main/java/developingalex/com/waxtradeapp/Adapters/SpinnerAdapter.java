package developingalex.com.waxtradeapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import developingalex.com.waxtradeapp.objects.AppsData;
import developingalex.com.waxtradeapp.R;

public class SpinnerAdapter extends ArrayAdapter<AppsData> {

    private final int groupID;
    private final ArrayList<AppsData> list;
    private final LayoutInflater inflater;

    public SpinnerAdapter(Activity context, int groupID, int id, ArrayList<AppsData> list) {
        super(context, id, list);
        this.groupID = groupID;
        this.list = list;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent ) {

        final View itemView = convertView == null ? inflater.inflate(groupID, parent,false): convertView;
        final ImageView imageView = itemView.findViewById(R.id.appsImage);

        if (list.get(position).getImage() != null) {
            Picasso.get()
                    .load(list.get(position).getImage())
                    .error(R.drawable.opskins_logo_avatar)
                    .into(imageView);
        } else
            imageView.setImageResource(R.drawable.opskins_logo_avatar);


        final TextView textView = itemView.findViewById(R.id.appsText);
        textView.setText(list.get(position).getText());

        return itemView;
    }

    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}

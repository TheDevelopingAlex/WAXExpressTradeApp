package developingalex.com.waxtradeapp;

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

public class SpinnerAdapter extends ArrayAdapter<AppsData> {

    int groupID;
    Activity context;
    ArrayList<AppsData> list;
    LayoutInflater inflater;

    public SpinnerAdapter(Activity context, int groupID, int id, ArrayList<AppsData> list) {
        super(context, id, list);

        this.groupID = groupID;
        this.list = list;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent ){
        View itemView = inflater.inflate(groupID, parent,false);

        ImageView imageView = itemView.findViewById(R.id.appsImage);

        if (list.get(position).getImage() == null) {
            imageView.setImageResource(R.drawable.opskins_logo_avatar);
        } else {
            Picasso.get()
                    .load(list.get(position).getImage())
                    .error(R.drawable.opskins_logo_avatar)
                    .into(imageView);
        }

        TextView textView = itemView.findViewById(R.id.appsText);
        textView.setText(list.get(position).getText());

        return itemView;
    }

    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }

}

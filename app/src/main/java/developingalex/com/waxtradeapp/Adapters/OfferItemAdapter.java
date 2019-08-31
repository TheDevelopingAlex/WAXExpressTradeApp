package developingalex.com.waxtradeapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import developingalex.com.waxtradeapp.R;
import developingalex.com.waxtradeapp.objects.StandardItem;

public class OfferItemAdapter extends RecyclerView.Adapter<OfferItemAdapter.ViewHolder> {

    private Context context;
    private ArrayList<StandardItem> offerItems;

    public OfferItemAdapter(Context context, ArrayList<StandardItem> offerItems){
        this.context = context;
        this.offerItems = offerItems;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final RelativeLayout offer_item_layout;
        final TextView item_price;
        final TextView item_name;
        final TextView item_wear;
        final TextView item_wear_value;
        final ImageView item_image;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            offer_item_layout = itemView.findViewById(R.id.offer_item_layout);
            item_price = itemView.findViewById(R.id.offer_detail_item_price);
            item_name = itemView.findViewById(R.id.offer_detail_item_name);
            item_wear = itemView.findViewById(R.id.offer_detail_item_wear);
            item_wear_value = itemView.findViewById(R.id.offer_detail_item_wear_value);
            item_image = itemView.findViewById(R.id.offer_detail_item_image);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        final View v = LayoutInflater.from(context).inflate(R.layout.offer_item_card, viewGroup,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final StandardItem item = offerItems.get(position);

        Log.w("OfferItemAdapter", item.toString());

        holder.offer_item_layout.setBackgroundColor(Color.parseColor(item.isHighlighted() ? "#1000ff00" : "#80000000"));
        holder.item_price.setText(String.valueOf(item.getSuggested_price() / 100));
        holder.item_name.setText(item.getName());

        int color;
        try {
            color = Color.parseColor(item.getColor());
        } catch (Exception e) {
            color = Color.WHITE;
        }
        holder.item_name.setTextColor(color);

        holder.item_name.setSelected(true);
        holder.item_wear.setText(String.valueOf(item.getWear()));
        holder.item_wear.setTextColor(color);
        holder.item_wear_value.setText(String.valueOf(item.getWear()));

        if (item.getImage() != null) {
            Picasso.get()
                    .load(getValidImageURL((JSONObject) item.getImage()))
                    .error(context.getResources().getDrawable(R.drawable.opskins_logo_avatar))
                    .into(holder.item_image);
        } else
            holder.item_image.setImageDrawable(context.getResources().getDrawable(R.drawable.opskins_logo_avatar));
    }

    @Override
    public int getItemCount() {
        return offerItems.size();
    }


    private String getValidImageURL(JSONObject imageObject) {
        String imageURL = "";
        try {
            if (imageObject.has("300px")) {
                imageURL = imageObject.getString("300px");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.w("OfferItemAdapter", imageURL);
        return imageURL;
    }
}

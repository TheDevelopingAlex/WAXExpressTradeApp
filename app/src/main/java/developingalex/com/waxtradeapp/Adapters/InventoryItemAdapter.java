package developingalex.com.waxtradeapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import developingalex.com.waxtradeapp.R;
import developingalex.com.waxtradeapp.interfaces.ItemClickListener;
import developingalex.com.waxtradeapp.objects.StandardItem;

public class InventoryItemAdapter extends RecyclerView.Adapter<InventoryItemAdapter.ViewHolder> {

    private Context context;
    private List<StandardItem> offerItems;
    private ItemClickListener listener;

    public InventoryItemAdapter(Context context, List<StandardItem> offerItems){
        this.context = context;
        this.offerItems = offerItems;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        //each data item is just a string in this case
        final RelativeLayout offer_item_layout;
        final TextView item_price;
        final TextView item_name;
        final TextView item_wear;
        final TextView item_wear_value;
        final ImageView item_image;

        ViewHolder(@NonNull View itemView, final ItemClickListener listener) {
            super(itemView);
            offer_item_layout = itemView.findViewById(R.id.offer_item_layout);
            item_price = itemView.findViewById(R.id.offer_detail_item_price);
            item_name = itemView.findViewById(R.id.offer_detail_item_name);
            item_wear = itemView.findViewById(R.id.offer_detail_item_wear);
            item_wear_value = itemView.findViewById(R.id.offer_detail_item_wear_value);
            item_image = itemView.findViewById(R.id.offer_detail_item_image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        final int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION)
                            listener.onItemClick(position);
                    }
                }
            });
        }
    }

    //Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        final View v = LayoutInflater.from(context).inflate(R.layout.inventory_item_card, viewGroup,false);
        return new ViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final StandardItem offer = offerItems.get(position);

        holder.offer_item_layout.setBackgroundColor(Color.parseColor(offer.isHighlighted() ? "#1000ff00" : "#80000000")); // green
        holder.item_price.setText(String.valueOf(offer.getSuggested_price()));
        holder.item_name.setText(offer.getName());
        holder.item_name.setTextColor(Color.parseColor(offer.getColor()));
        holder.item_name.setSelected(true);
        holder.item_wear.setText(String.valueOf(offer.getWear()));
        holder.item_wear.setTextColor(Color.parseColor(offer.getColor()));
        holder.item_wear_value.setText(String.valueOf(offer.getWear()));

        if (offer.getImage() != null) {
            Picasso.get()
                    .load(getValidImageURL((JSONObject) offer.getImage()))
                    .error(context.getResources().getDrawable(R.drawable.opskins_logo_avatar))
                    .into(holder.item_image);
        } else
            holder.item_image.setImageDrawable(context.getResources().getDrawable(R.drawable.opskins_logo_avatar));
    }

    @Override
    public int getItemCount() {
        return offerItems.size();
    }

    public void setOnItemClickListener(ItemClickListener listener) {
        this.listener = listener;
    }

    public void toggleItemActive(int position) {
        offerItems.get(position).toggleHighlighted();
        notifyItemChanged(position);
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
        return imageURL;
    }
}

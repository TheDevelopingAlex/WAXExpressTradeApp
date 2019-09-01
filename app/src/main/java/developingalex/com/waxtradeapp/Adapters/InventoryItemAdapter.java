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
import java.util.Locale;

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
        final TextView item_category;
        final TextView item_wear_value;
        final ImageView item_image;

        ViewHolder(@NonNull View itemView, final ItemClickListener listener) {
            super(itemView);
            offer_item_layout = itemView.findViewById(R.id.offer_item_layout);
            item_price = itemView.findViewById(R.id.offer_detail_item_price);
            item_name = itemView.findViewById(R.id.offer_detail_item_name);
            item_category = itemView.findViewById(R.id.offer_detail_item_wear);
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

        final StandardItem item = offerItems.get(position);

        holder.offer_item_layout.setBackgroundColor(Color.parseColor(item.isHighlighted() ? "#1000ff00" : "#80000000")); // green
        holder.item_price.setText(String.format(Locale.US, "%.2f", item.getSuggested_price()) + "$");
        holder.item_name.setText(item.getName());

        int color;
        try {
            color = Color.parseColor(item.getColor());
        } catch (Exception e) {
            color = Color.WHITE;
        }
        holder.item_name.setTextColor(color);

        holder.item_name.setSelected(true);
        holder.item_category.setTextColor(color);

        if (!item.getCategory().isEmpty()) {
            holder.item_category.setText(item.getCategory());
        } else if (!item.getType().isEmpty()) {
            holder.item_category.setText(item.getType());
        } else if (!item.getRarity().isEmpty()) {
            holder.item_category.setText(item.getRarity());
        } else {

            try {
                JSONObject attributes = (JSONObject) item.getAttributes();

                if (attributes.has("item_type") && !attributes.getString("item_type").isEmpty()) {
                    holder.item_category.setText(attributes.getString("item_type"));
                } else if (attributes.has("collection") && !attributes.getString("collection").isEmpty()){
                    holder.item_category.setText(attributes.getString("collection"));
                } else {
                    holder.item_category.setText("");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (item.getWear() == 0.0) {
            holder.item_wear_value.setText("");
        } else {
            holder.item_wear_value.setText(String.format(Locale.US, "%.6f", item.getWear()));
        }

        if (item.getImage() != null) {
            Picasso.get()
                    .load(getValidImageURL(item.getImage()))
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

    private String getValidImageURL(Object image) {

        String imageURL = "";

        try {
            JSONObject imageObject = (JSONObject) image;

            try {
                if (imageObject.has("300px")) {
                    imageURL = imageObject.getString("300px");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            try {
                imageURL = (String) image;
            } catch (ClassCastException ex) {
                ex.printStackTrace();
            }
        }

        return imageURL;
    }
}

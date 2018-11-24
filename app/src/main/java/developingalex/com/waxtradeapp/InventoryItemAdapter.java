package developingalex.com.waxtradeapp;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class InventoryItemAdapter extends RecyclerView.Adapter<InventoryItemAdapter.ViewHolder> {

    private Context mContext;
    private List<OfferItem> offerItems;

    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public void toggleItemActive(int position) {
        offerItems.get(position).toggleHighlighted();
        notifyItemChanged(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //each data item is just a string in this case
        public RelativeLayout offer_item_layout;
        public TextView item_price, item_name, item_wear, item_wear_value;
        public ImageView item_image;

        public ViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
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
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    //Provide a suitable constructor
    public InventoryItemAdapter(Context ctx, List<OfferItem> offerItems){
        mContext = ctx;
        this.offerItems = offerItems;
    }


    //Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public InventoryItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        //Creating a new view
        View v = LayoutInflater.from(mContext).inflate(R.layout.inventory_item_card, viewGroup,false);
        return new ViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryItemAdapter.ViewHolder holder, int position) {

        OfferItem offer = offerItems.get(position);

        if (offer.isHighlighted()) {
            holder.offer_item_layout.setBackgroundColor(Color.parseColor("#1000ff00")); // green
        } else {
            holder.offer_item_layout.setBackgroundColor(Color.parseColor("#80000000")); // default
        }

        holder.item_price.setText(String.valueOf(offer.getPrice()));
        holder.item_name.setText(offer.getName());
        holder.item_name.setTextColor(Color.parseColor(offer.getItemColor()));
        holder.item_name.setSelected(true);
        holder.item_wear.setText(offer.getWearName());
        holder.item_wear.setTextColor(Color.parseColor(offer.getItemColor()));
        holder.item_wear_value.setText(String.valueOf(offer.getWear()));

        if (offer.getImage() == null) {
            holder.item_image.setImageDrawable(mContext.getResources().getDrawable(R.drawable.opskins_logo_avatar));
        } else {
            Picasso.get()
                    .load(offer.getImage())
                    .error(mContext.getResources().getDrawable(R.drawable.opskins_logo_avatar))
                    .into(holder.item_image);
        }
    }

    @Override
    public int getItemCount() {
        return offerItems.size();
    }
}

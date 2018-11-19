package developingalex.com.waxtradeapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class RecentTradePartnersAdapter extends RecyclerView.Adapter<RecentTradePartnersAdapter.ViewHolder> {

    private Context mContext;
    private List<RecentTradePartners> recentTradePartnersList;

    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //each data item is just a string in this case
        public TextView recentPartnerUsername;
        public ImageView recentPartnerPic;

        public ViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            recentPartnerUsername = itemView.findViewById(R.id.recentPartnerUsername);
            recentPartnerPic = itemView.findViewById(R.id.recentPartnerPic);

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

    public RecentTradePartnersAdapter(Context ctx, List<RecentTradePartners> recentTradePartnersList) {
        mContext = ctx;
        this.recentTradePartnersList = recentTradePartnersList;
    }

    //Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public RecentTradePartnersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        //Creating a new view
        View v = LayoutInflater.from(mContext).inflate(R.layout.layout_trade_partners, viewGroup,false);
        return new ViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentTradePartnersAdapter.ViewHolder holder, int position) {

        RecentTradePartners partner = recentTradePartnersList.get(position);

        holder.recentPartnerUsername.setText(String.valueOf(partner.getUsername()));

        if (partner.getAvatar() == null) {
            holder.recentPartnerPic.setImageDrawable(mContext.getResources().getDrawable(R.drawable.opskins_logo_avatar));
        } else {
            Picasso.get()
                    .load(partner.getAvatar())
                    .error(mContext.getResources().getDrawable(R.drawable.opskins_logo_avatar))
                    .placeholder(mContext.getResources().getDrawable(R.drawable.opskins_logo_avatar))
                    .into(holder.recentPartnerPic);
        }
    }

    @Override
    public int getItemCount() {
        return recentTradePartnersList.size();
    }
}

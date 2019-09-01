package developingalex.com.waxtradeapp.adapters;

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

import developingalex.com.waxtradeapp.R;
import developingalex.com.waxtradeapp.interfaces.ItemClickListener;
import developingalex.com.waxtradeapp.objects.RecentTradePartners;

public class RecentTradePartnersAdapter extends RecyclerView.Adapter<RecentTradePartnersAdapter.ViewHolder> {

    private Context context;
    private List<RecentTradePartners> recentTradePartnersList;
    private ItemClickListener listener;

    public RecentTradePartnersAdapter(Context context, List<RecentTradePartners> recentTradePartnersList) {
        this.context = context;
        this.recentTradePartnersList = recentTradePartnersList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        //each data item is just a string in this case
        final TextView recentPartnerUsername;
        final ImageView recentPartnerPic;

        ViewHolder(@NonNull View itemView, final ItemClickListener listener) {
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

    //Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public RecentTradePartnersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        final View v = LayoutInflater.from(context).inflate(R.layout.layout_trade_partners, viewGroup,false);
        return new ViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentTradePartnersAdapter.ViewHolder holder, int position) {

        final RecentTradePartners partner = recentTradePartnersList.get(position);

        holder.recentPartnerUsername.setText(String.valueOf(partner.getUsername()));

        if (partner.getAvatar() != null) {
            Picasso.get()
                    .load(partner.getAvatar())
                    .error(context.getResources().getDrawable(R.drawable.opskins_logo_avatar))
                    .placeholder(context.getResources().getDrawable(R.drawable.opskins_logo_avatar))
                    .into(holder.recentPartnerPic);
        } else
            holder.recentPartnerPic.setImageDrawable(context.getResources().getDrawable(R.drawable.opskins_logo_avatar));

    }

    @Override
    public int getItemCount() {
        return recentTradePartnersList.size();
    }

    public void setOnItemClickListener(ItemClickListener listener) {
        this.listener = listener;
    }
}

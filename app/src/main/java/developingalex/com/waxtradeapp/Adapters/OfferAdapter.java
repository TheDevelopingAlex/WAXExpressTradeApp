package developingalex.com.waxtradeapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import developingalex.com.waxtradeapp.R;
import developingalex.com.waxtradeapp.interfaces.ItemClickListener;
import developingalex.com.waxtradeapp.objects.Offer;


public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.OfferViewHolder> {

    private Context context;
    private ArrayList<Offer> offerList;
    private ItemClickListener listener;
    private int lastPosition = -1;

    private boolean acceptVisibility = true;
    private boolean declineVisibility = true;
    private boolean statusTextVisibility;

    public OfferAdapter(Context context, ArrayList<Offer> offerList) {
        this.context = context;
        this.offerList = offerList;
    }

    static class OfferViewHolder extends RecyclerView.ViewHolder {

        final CardView cardView;
        final ImageView mImageView;
        final TextView username;
        final TextView yourOffer;
        final TextView theirOffer;
        final ImageButton mAcceptButton;
        final ImageButton mDeclineButton;
        final TextView mStatusTextStatus;

        OfferViewHolder(@NonNull View itemView, final ItemClickListener listener) {
            super(itemView);

            cardView = itemView.findViewById(R.id.offerCardView);
            mImageView = itemView.findViewById(R.id.offerUserPic);
            username = itemView.findViewById(R.id.offerUsername);
            theirOffer = itemView.findViewById(R.id.offerTheir);
            yourOffer = itemView.findViewById(R.id.offerYour);
            mAcceptButton = itemView.findViewById(R.id.offerAccept);
            mDeclineButton = itemView.findViewById(R.id.offerDecline);
            mStatusTextStatus = itemView.findViewById(R.id.statusTextStatus);

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

            mAcceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        final int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION)
                            listener.onAcceptClick(position);
                    }
                }
            });

            mDeclineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        final int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION)
                            listener.onDeclineClick(position);
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final View v = LayoutInflater.from(context).inflate(R.layout.layout_offer, viewGroup, false);
        return new OfferViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder offerViewHolder, final int position) {

        final Offer currentOffer = offerList.get(position);

        offerViewHolder.mAcceptButton.setVisibility(acceptVisibility ? View.VISIBLE: View.INVISIBLE);
        offerViewHolder.mDeclineButton.setVisibility(declineVisibility ? View.VISIBLE: View.INVISIBLE);

        if (statusTextVisibility) {
            offerViewHolder.mStatusTextStatus.setVisibility(View.VISIBLE);
            offerViewHolder.mStatusTextStatus.setText(currentOffer.getStateName());
            switch (currentOffer.getStateName()) {
                case "Accepted":
                    offerViewHolder.cardView.setCardBackgroundColor(Color.parseColor("#1000ff00"));
                    break;
                case "Cancelled":
                case "Declined":
                case "Expired":
                    offerViewHolder.cardView.setCardBackgroundColor(Color.parseColor("#22ff0000"));
                    break;
                default:
                    offerViewHolder.cardView.setCardBackgroundColor(Color.parseColor("#33A9A9A9")); // default transparent grey
                    break;
            }
        } else {
            offerViewHolder.mStatusTextStatus.setVisibility(View.INVISIBLE);
        }

        offerViewHolder.username.setText(currentOffer.getUsername());
        offerViewHolder.yourOffer.setText(currentOffer.getYourOffer());
        offerViewHolder.theirOffer.setText(currentOffer.getTheirOffer());

        if (currentOffer.getImage() != null) {
            Picasso.get()
                    .load(currentOffer.getImage())
                    .error(context.getResources().getDrawable(R.drawable.opskins_logo_avatar))
                    .placeholder(context.getResources().getDrawable(R.drawable.opskins_logo_avatar))
                    .into(offerViewHolder.mImageView);
        } else
            offerViewHolder.mImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.opskins_logo_avatar));

        // Here you apply the animation when the view is bound
        setAnimation(offerViewHolder.itemView, position);
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            final Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return offerList.size();
    }

    public void setOnItemClickListener(ItemClickListener listener) {
        this.listener = listener;
    }

    public void acceptButtonVisibility(boolean visibility) {
        this.acceptVisibility = visibility;
        notifyDataSetChanged();
    }

    public void declineButtonVisibility(boolean visibility) {
        this.declineVisibility = visibility;
        notifyDataSetChanged();
    }

    public void statusTextVisibility(boolean visibility) {
        statusTextVisibility = visibility;
        notifyDataSetChanged();
    }
}

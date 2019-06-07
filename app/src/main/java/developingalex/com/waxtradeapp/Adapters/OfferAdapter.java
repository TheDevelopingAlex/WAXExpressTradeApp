package developingalex.com.waxtradeapp.Adapters;

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


public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.OfferViewHolder> {

    private Context mContext;
    private ArrayList<Offer> mOfferList;
    private OnItemClickListener mListener;
    private int lastPosition = -1;

    private boolean acceptVisibility = true;
    private boolean declineVisibility = true;
    private boolean statusTextVisibility;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onAcceptClick(int position);
        void onDeclineClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
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

    static class OfferViewHolder extends RecyclerView.ViewHolder {

        final CardView cardView;
        final ImageView mImageView;
        final TextView username;
        final TextView yourOffer;
        final TextView theirOffer;
        final ImageButton mAcceptButton;
        final ImageButton mDeclineButton;
        final TextView mStatusTextStatus;

        OfferViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);

            cardView = itemView.findViewById(R.id.offerCardView);
            mImageView = itemView.findViewById(R.id.offerUserPic);
            username = itemView.findViewById(R.id.offerUsername);
            yourOffer = itemView.findViewById(R.id.offerTheir);
            theirOffer = itemView.findViewById(R.id.offerYour);
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

    public OfferAdapter(Context mCtx, ArrayList<Offer> offerArrayList) {
        mContext = mCtx;
        mOfferList = offerArrayList;
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.layout_offer, viewGroup, false);
        return new OfferViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder offerViewHolder, final int position) {

        final Offer currentOffer = mOfferList.get(position);

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
        } else
            offerViewHolder.mStatusTextStatus.setVisibility(View.INVISIBLE);


        offerViewHolder.username.setText(currentOffer.getUsername());
        offerViewHolder.yourOffer.setText(currentOffer.getTheirOffer());
        offerViewHolder.theirOffer.setText(String.valueOf(currentOffer.getYourOffer()));

        if (currentOffer.getImage() == null) {
            offerViewHolder.mImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.opskins_logo_avatar));
        } else {
            Picasso.get()
                    .load(currentOffer.getImage())
                    .error(mContext.getResources().getDrawable(R.drawable.opskins_logo_avatar))
                    .placeholder(mContext.getResources().getDrawable(R.drawable.opskins_logo_avatar))
                    .into(offerViewHolder.mImageView);
        }

        // Here you apply the animation when the view is bound
        setAnimation(offerViewHolder.itemView, position);

    }

    private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.fade_in);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return mOfferList.size();
    }
}

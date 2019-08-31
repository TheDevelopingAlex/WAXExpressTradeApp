package developingalex.com.waxtradeapp.views.drawerViews;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;

import developingalex.com.waxtradeapp.interfaces.OfferListener;
import developingalex.com.waxtradeapp.lib.AsyncOfferLoader;
import developingalex.com.waxtradeapp.objects.Offer;
import developingalex.com.waxtradeapp.adapters.OfferAdapter;
import developingalex.com.waxtradeapp.interfaces.ItemClickListener;
import developingalex.com.waxtradeapp.objects.StandardItem;
import developingalex.com.waxtradeapp.objects.StandardTradeOffer;
import developingalex.com.waxtradeapp.views.OfferDetail;
import developingalex.com.waxtradeapp.R;
import developingalex.com.waxtradeapp.lib.TradeImplementation;
import developingalex.com.waxtradeapp.interfaces.TradeInterface;

public class DrawerSent extends Fragment {

    private TextView text;
    private SwipeRefreshLayout swipeRefreshLayout;

    private TradeInterface tradeInterface;

    private final ArrayList<Offer> offerList = new ArrayList<>();
    private OfferAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.drawer_sent, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        // find and initialize components
        tradeInterface = new TradeImplementation(view.getContext());
        text = view.findViewById(R.id.drawer_sender_text);

        swipeRefreshLayout = view.findViewById(R.id.drawer_sent_swipe);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadOffers();
            }
        });
        // Configure the refreshing colors
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_dark);

        loadOffers();

        final RecyclerView recyclerView = view.findViewById(R.id.drawer_sender_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // setting adapter
        adapter = new OfferAdapter(getActivity(), offerList);
        recyclerView.setAdapter(adapter);

        adapter.acceptButtonVisibility(false);

        adapter.setOnItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(getActivity(), OfferDetail.class);
                Bundle b = new Bundle();
                b.putInt("offerId", offerList.get(position).getId()); // Parameter for new Activity
                b.putBoolean("hideAccept", true);
                intent.putExtras(b);
                startActivity(intent);
            }

            @Override
            public void onAcceptClick(int position) { }

            @Override
            public void onDeclineClick(int position) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                final Integer id = offerList.get(position).getId();

                builder.setTitle("Confirm");
                builder.setMessage("Do you really want to 'CANCEL' the offer?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (tradeInterface.cancelOffer(id)) {
                                        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getContext(), "Offer Declined", Toast.LENGTH_SHORT).show();
                                                loadOffers();
                                            }
                                        });
                                    } else {
                                        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getContext(), "Error, Please Try Again.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

    }

    private void loadOffers() {
        // Start requesting offers from API in background
        swipeRefreshLayout.setRefreshing(true);

        new AsyncOfferLoader(getContext(), "sent", "2", "created",
                new OfferListener() {
                    @Override
                    public void onSuccess(ArrayList<StandardTradeOffer> offers) {

                        for (int i = 0; i < offers.size(); i++) {
                            StandardTradeOffer offer = offers.get(i);

                            offerList.add(
                                    new Offer(
                                            offer.getId(),
                                            offer.getRecipient().getDisplay_name(),
                                            "Your Items ("+offer.getSender().getItems().size()+"): "+ String.format(java.util.Locale.US,"%.2f", getItemsPrice(offer.getSender().getItems()))+ "$",
                                            "Their Items ("+offer.getRecipient().getItems().size()+"): "+ String.format(java.util.Locale.US,"%.2f", getItemsPrice(offer.getRecipient().getItems()))+ "$",
                                            offer.getRecipient().getAvatar(),
                                            offer.getState_name()
                                    )
                            );
                        }

                        adapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                        text.setText("");
                    }

                    @Override
                    public void onFailure(String error) {
                        if (!error.isEmpty()) {
                            Log.e("DrawerSent", error);
                        }
                        adapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                        text.setText(R.string.info_no_offer);
                    }
        }).execute();
    }

    public static double getItemsPrice(ArrayList<StandardItem> items) {
        double price = 0.00;

        for (int i = 0; i < items.size(); i++) {
            StandardItem item = items.get(i);
            price += ((double) item.getSuggested_price() / 100);
        }

        return price;
    }

}

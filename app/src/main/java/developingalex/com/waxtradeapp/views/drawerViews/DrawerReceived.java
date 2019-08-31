package developingalex.com.waxtradeapp.views.drawerViews;

import android.annotation.SuppressLint;
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
import android.widget.EditText;
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

public class DrawerReceived extends Fragment {

    private TextView text;
    private SwipeRefreshLayout swipeRefreshLayout;

    private TradeInterface tradeInterface;

    private ArrayList<Offer> offerList = new ArrayList<>();
    private OfferAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.drawer_received, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        // find and initialize components
        tradeInterface = new TradeImplementation(view.getContext());
        text = view.findViewById(R.id.drawer_receiver_text);

        swipeRefreshLayout = view.findViewById(R.id.drawer_received_swipe);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadOffers();
            }
        });
        // Configure the refreshing colors
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_dark);

        loadOffers();

        // init recyclerView
        final RecyclerView recyclerView = view.findViewById(R.id.drawer_receiver_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // setting adapter
        adapter = new OfferAdapter(getActivity(), offerList);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(getActivity(), OfferDetail.class);
                Bundle b = new Bundle();
                b.putInt("offerId", offerList.get(position).getId()); // Parameter for new Activity
                b.putBoolean("hideAccept", false);
                intent.putExtras(b);
                startActivity(intent);
            }

            @Override
            public void onAcceptClick(int position) {

                LayoutInflater li = LayoutInflater.from(getContext());
                @SuppressLint("InflateParams") View promptsView = li.inflate(R.layout.custom_accept_dialog, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                final Integer id = offerList.get(position).getId();

                builder.setTitle("Confirm");
                builder.setMessage("Please enter your 2-Factor Code below and click 'Accept' to complete the trade");
                builder.setView(promptsView);
                builder.setCancelable(false);

                final EditText userInput = promptsView.findViewById(R.id.twoFactorInput);

                builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        final DialogInterface temp_dialog = dialog;

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (userInput.getText().toString().isEmpty()) {
                                        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getContext(), "Please type in your 2FA code", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        try {
                                            if (tradeInterface.acceptOffer(id, userInput.getText().toString())) {
                                                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        temp_dialog.dismiss();
                                                        Toast.makeText(getContext(), "Offer Accepted", Toast.LENGTH_SHORT).show();
                                                        loadOffers();
                                                    }
                                                });
                                            } else {
                                                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        temp_dialog.dismiss();
                                                        Toast.makeText(getContext(), tradeInterface.getErrorMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();

            }

            @Override
            public void onDeclineClick(int position) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                final Integer id = offerList.get(position).getId();

                builder.setTitle("Confirm");
                builder.setMessage("Do you really want to 'DECLINE' the offer?");

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

        swipeRefreshLayout.setRefreshing(true);

        new AsyncOfferLoader(getContext(), "received", "2", "created",
                new OfferListener() {
                    @Override
                    public void onSuccess(ArrayList<StandardTradeOffer> offers) {

                        for (int i = 0; i <offers.size(); i++) {
                            StandardTradeOffer offer = offers.get(i);

                            offerList.add(
                                    new Offer(
                                            offer.getId(),
                                            offer.getSender().getDisplay_name(),
                                            "Your Items ("+offer.getRecipient().getItems().size()+"): "+ String.format(java.util.Locale.US,"%.2f", getItemsPrice(offer.getRecipient().getItems())) +"$",
                                            "Their Items ("+offer.getSender().getItems().size()+"): "+ String.format(java.util.Locale.US,"%.2f", getItemsPrice(offer.getSender().getItems())) +"$",
                                            offer.getSender().getAvatar(),
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
                            Log.e("DrawerReceived", error);
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

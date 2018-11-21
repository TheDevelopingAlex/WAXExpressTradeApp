package developingalex.com.waxtradeapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

public class DrawerReceived extends Fragment {

    private TextView text;
    private SwipeRefreshLayout swipeRefreshLayout;

    private TradeInterface tradeInterface;

    private ArrayList<Offer> offerList = new ArrayList<>();
    private RecyclerView recyclerView;
    private OfferAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.drawer_received, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        // find and initialize components
        tradeInterface = new TradeInterface(view.getContext());
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
        recyclerView = view.findViewById(R.id.drawer_receiver_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // setting adapter
        adapter = new OfferAdapter(getActivity(), offerList);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new OfferAdapter.OnItemClickListener() {
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
        // Start requesting offers from API in background
        swipeRefreshLayout.setRefreshing(true);
        new LongOperation(DrawerReceived.this, new OnEventListener() {
            @Override
            public void onSuccess() {
                adapter.notifyDataSetChanged();
                text.setText("");
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure() {
                adapter.notifyDataSetChanged();
                text.setText(R.string.info_no_offer);
                swipeRefreshLayout.setRefreshing(false);
            }
        }).execute();
    }

    public interface OnEventListener {
        void onSuccess();
        void onFailure();
    }

    private static class LongOperation extends AsyncTask<String, Void, JSONArray> {

        private WeakReference<DrawerReceived> activityWeakReference;
        private TradeInterface tradeInterface;
        private OnEventListener mCallBack;

        LongOperation(DrawerReceived activity, OnEventListener callback) {
            activityWeakReference = new WeakReference<>(activity);
            tradeInterface = new TradeInterface(activity.getContext());
            mCallBack = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONArray doInBackground(String... params) {
            JSONArray temp = null;

            try {
                JSONObject offers = tradeInterface.getOffers("received", "2", "created");

                if (offers != null && !offers.getString("total").equals("0"))
                    temp = (JSONArray) offers.get("offers");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return temp;
        }

        @Override
        protected void onPostExecute(JSONArray offers) {
            DrawerReceived activity = activityWeakReference.get();
            activity.offerList.clear();

            if (offers != null) {
                for (int i = 0; i < offers.length(); i++) {

                    try {
                        JSONObject jsonObject = offers.getJSONObject(i);
                        JSONObject recipient = (JSONObject) jsonObject.get("recipient");
                        JSONArray recipient_items = (JSONArray) recipient.get("items");
                        JSONObject sender = (JSONObject) jsonObject.get("sender");
                        JSONArray sender_items = (JSONArray) sender.get("items");

                        // Total Items in each offer
                        Integer sender_items_length = sender_items.length();
                        Integer recipient_items_length = recipient_items.length();

                        double sender_items_price = 0.00;
                        double recipient_items_price = 0.00;

                        for (int j = 0; j < sender_items.length(); j++) {
                            JSONObject sender_item = sender_items.getJSONObject(j);
                            sender_items_price += ((double) sender_item.getInt("suggested_price") / 100);
                        }

                        for (int j = 0; j < recipient_items.length(); j++) {
                            JSONObject recipient_item = recipient_items.getJSONObject(j);
                            recipient_items_price += ((double) recipient_item.getInt("suggested_price") / 100);
                        }

                        activity.offerList.add(new Offer(jsonObject.getInt("id"), sender.getString("display_name"),"Your Items ("+recipient_items_length.toString()+"): "+ String.format(java.util.Locale.US,"%.2f", recipient_items_price) +"$", "Their Items ("+sender_items_length.toString()+"): "+ String.format(java.util.Locale.US,"%.2f", sender_items_price) +"$", sender.getString("avatar"), jsonObject.getString("state_name")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                mCallBack.onSuccess();
            } else
                mCallBack.onFailure();
        }

    }

}

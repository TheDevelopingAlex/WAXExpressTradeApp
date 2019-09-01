package developingalex.com.waxtradeapp.views;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

import developingalex.com.waxtradeapp.R;
import developingalex.com.waxtradeapp.adapters.OfferItemAdapter;
import developingalex.com.waxtradeapp.interfaces.OfferListener;
import developingalex.com.waxtradeapp.lib.AsyncOfferLoader;
import developingalex.com.waxtradeapp.lib.TradeImplementation;
import developingalex.com.waxtradeapp.interfaces.TradeInterface;
import developingalex.com.waxtradeapp.objects.StandardItem;
import developingalex.com.waxtradeapp.objects.StandardTradeOffer;

public class OfferDetail extends AppCompatActivity {

    private int offerId;
    private TradeInterface tradeInterface;

    private ProgressDialog progressDialog;

    private ScrollView content;
    private LinearLayout message;
    private TextView username, status, their_info, their_info_value, your_info, your_info_value, message_text;
    private ImageView userpic;

    private RecyclerView recyclerViewTop, recyclerViewBot;
    private OfferItemAdapter itemAdapterTop, itemAdapterBot;
    private ArrayList<StandardItem> itemListTop = new ArrayList<>(), itemListBot = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_detail);

        Bundle b = getIntent().getExtras();
        assert b != null;
        offerId = b.getInt("offerId");

        tradeInterface = new TradeImplementation(this);

        Toolbar toolbar = findViewById(R.id.offer_detail_toolbar);
        toolbar.setTitle("Offer #" + offerId);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Offer ...");
        progressDialog.show();

        initComponents();
        initRecyclerViews();
    }

    private void initComponents() {

        content = findViewById(R.id.offer_detail_content);
        message = findViewById(R.id.offer_detail_message);
        message_text = findViewById(R.id.offer_detail_messagetext);
        username = findViewById(R.id.offer_detail_username) ;
        status = findViewById(R.id.offer_detail_status);
        userpic = findViewById(R.id.offer_detail_userpic);
        their_info = findViewById(R.id.offer_detail_their);
        their_info_value = findViewById(R.id.offer_detail_their_value);
        your_info = findViewById(R.id.offer_detail_your);
        your_info_value = findViewById(R.id.offer_detail_your_value);
        recyclerViewTop = findViewById(R.id.offer_detail_RVTop);
        recyclerViewBot = findViewById(R.id.offer_detail_RVBot);
    }

    private void initRecyclerViews() {

        recyclerViewTop.setHasFixedSize(true);
        recyclerViewTop.setLayoutManager(new GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false));

        recyclerViewBot.setHasFixedSize(true);
        recyclerViewBot.setLayoutManager(new GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false));

        initAdapters();
    }

    private void initAdapters() {

        itemAdapterTop = new OfferItemAdapter(this, itemListTop);
        recyclerViewTop.setAdapter(itemAdapterTop);

        itemAdapterBot = new OfferItemAdapter(this, itemListBot);
        recyclerViewBot.setAdapter(itemAdapterBot);

        loadOffer();
    }

    private void loadOffer() {

        new AsyncOfferLoader(this, offerId, new OfferListener() {
            @Override
            public void onSuccess(ArrayList<StandardTradeOffer> items) {

                StandardTradeOffer offer = items.get(0);

                itemListTop.clear();
                itemListBot.clear();

                status.setText(offer.getState_name());

                if (offer.isSent_by_you()) {

                    username.setText(offer.getRecipient().getDisplay_name());

                    if (offer.getRecipient().getAvatar() == null || offer.getRecipient().getAvatar().equals("/images/opskins-logo-avatar.png")) {
                        userpic.setImageDrawable(getResources().getDrawable(R.drawable.opskins_logo_avatar));
                    } else {
                        Picasso.get().load(offer.getRecipient().getAvatar()).into(userpic);
                    }

                    their_info.setText("Their Items (" + offer.getRecipient().getItems().size() + ")");
                    their_info_value.setText("Total Value: " + String.format(java.util.Locale.US, "%.2f", getItemsPrice(offer.getRecipient().getItems())) + "$");
                    itemListTop = offer.getRecipient().getItems();

                    your_info.setText("Your Items (" + offer.getSender().getItems().size() + ")");
                    your_info_value.setText("Total Value: " + String.format(java.util.Locale.US, "%.2f", getItemsPrice(offer.getSender().getItems())) + "$");
                    itemListBot = offer.getSender().getItems();

                } else {

                    username.setText(offer.getSender().getDisplay_name());

                    if (offer.getSender().getAvatar() == null || offer.getSender().getAvatar().equals("/images/opskins-logo-avatar.png")) {
                        userpic.setImageDrawable(getResources().getDrawable(R.drawable.opskins_logo_avatar));
                    } else {
                        Picasso.get().load(offer.getSender().getAvatar()).into(userpic);
                    }

                    their_info.setText("Their Items (" +offer.getSender().getItems().size() + ")");
                    their_info_value.setText("Total Value: " + String.format(java.util.Locale.US,"%.2f", getItemsPrice(offer.getSender().getItems())) + "$");
                    itemListTop = offer.getSender().getItems();

                    your_info.setText("Your Items (" +offer.getRecipient().getItems().size() + ")");
                    your_info_value.setText("Total Value: " + String.format(java.util.Locale.US,"%.2f", getItemsPrice(offer.getRecipient().getItems())) + "$");
                    itemListBot = offer.getRecipient().getItems();
                }

                if (!offer.getMessage().isEmpty()) {
                    message.setVisibility(View.VISIBLE);
                    message_text.setText("Message: " + offer.getMessage());
                }

                itemAdapterTop.notifyDataSetChanged();
                itemAdapterBot.notifyDataSetChanged();

                content.setVisibility(View.VISIBLE);

                progressDialog.dismiss();
            }

            @Override
            public void onFailure(String text) {
                progressDialog.dismiss();
                Toast.makeText(OfferDetail.this, text, Toast.LENGTH_SHORT).show();
            }
        }).execute();
    }

    public static double getItemsPrice(ArrayList<StandardItem> items) {
        double price = 0.00;

        for (int i = 0; i < items.size(); i++) {
            StandardItem item = items.get(i);
            price += item.getSuggested_price();
        }

        return price;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Bundle b = getIntent().getExtras();
        assert b != null;
        if (b.containsKey("hideAccept") && !b.getBoolean("hideAccept")) {
            getMenuInflater().inflate(R.menu.inventory_toolbar, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_check) {

            LayoutInflater li = LayoutInflater.from(this);
            @SuppressLint("InflateParams") View promptsView = li.inflate(R.layout.custom_accept_dialog, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            final int id = offerId;

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
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(OfferDetail.this, "Please type in your 2FA code", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    try {
                                        if (tradeInterface.acceptOffer(id, userInput.getText().toString())) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    temp_dialog.dismiss();
                                                    Toast.makeText(OfferDetail.this, "Offer Accepted", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                }
                                            });
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    temp_dialog.dismiss();
                                                    Toast.makeText(OfferDetail.this, tradeInterface.getErrorMessage(), Toast.LENGTH_SHORT).show();
                                                    finish();
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

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

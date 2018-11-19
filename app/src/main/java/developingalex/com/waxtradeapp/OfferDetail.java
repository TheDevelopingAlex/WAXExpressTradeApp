package developingalex.com.waxtradeapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

public class OfferDetail extends AppCompatActivity {

    private ScrollView content;
    private LinearLayout message;
    private TextView username, status, their_info, their_info_value, your_info, your_info_value, message_text;
    private ImageView userpic;

    private ProgressDialog progressDialog;

    private RecyclerView mRecyclerViewTop;
    private RecyclerView mRecyclerViewBot;
    private OfferItemAdapter itemAdapter1;
    private OfferItemAdapter itemAdapter2;

    private ArrayList<OfferItem> itemList1 = new ArrayList<>();
    private ArrayList<OfferItem> itemList2 = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_detail);

        // Get ID as parameter
        Bundle b = getIntent().getExtras();
        assert b != null;
        int id = b.getInt("offerId");

        // Init Toolbar
        Toolbar toolbar = findViewById(R.id.offer_detail_toolbar);
        toolbar.setTitle("Offer #" + id);

        // Adds Back-Arrow to Toolbar
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Show ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Offer ...");
        progressDialog.show();

        // Init other things
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

        // Init RecyclerViews
        mRecyclerViewTop = findViewById(R.id.offer_detail_RVTop);
        mRecyclerViewTop.setHasFixedSize(true);
        mRecyclerViewTop.setLayoutManager(new GridLayoutManager(OfferDetail.this, 1, GridLayoutManager.HORIZONTAL, false));

        mRecyclerViewBot = findViewById(R.id.offer_detail_RVBot);
        mRecyclerViewBot.setHasFixedSize(true);
        mRecyclerViewBot.setLayoutManager(new GridLayoutManager(OfferDetail.this, 1, GridLayoutManager.HORIZONTAL, false));

        //initializing adapter
        itemAdapter1 = new OfferItemAdapter(OfferDetail.this, itemList1);
        mRecyclerViewTop.setAdapter(itemAdapter1);

        //initializing adapter
        itemAdapter2 = new OfferItemAdapter(OfferDetail.this, itemList2);
        mRecyclerViewBot.setAdapter(itemAdapter2);

        LongOperation longOperation = new LongOperation(this, id, new OnEventListener() {
            @Override
            public void onSuccess(JSONObject offer) {
                DecimalFormat precision = new DecimalFormat("0.00000");
                DecimalFormat price_format = new DecimalFormat("0.00");
                try {
                    if(offer.getString("sent_by_you").equals("true")) {
                        // You are Sender
                        // Get+Set status text
                        status.setText(offer.getString("state_name"));
                        // get Sender object
                        JSONObject recipient = offer.getJSONObject("recipient");
                        // set username + profile picture
                        username.setText(recipient.getString("display_name"));
                        // check for wrong image url
                        if (recipient.getString("avatar").equals("null") || recipient.getString("avatar").equals("/images/opskins-logo-avatar.png"))
                            userpic.setImageDrawable(getResources().getDrawable(R.drawable.opskins_logo_avatar));
                        else
                            Picasso.get().load(recipient.getString("avatar")).into(userpic);

                        JSONArray items1 = recipient.getJSONArray("items");
                        their_info.setText("Their Items (" +items1.length() + ")");

                        Double totalValue = 0.00;

                        for (int i = 0; i < items1.length(); i++) {
                            JSONObject jsonObject = items1.getJSONObject(i);

                            // Split name into weapon_name and weapon_wear
                            String name = jsonObject.getString("name");
                            String mName;
                            String wear;

                            if (name.contains("(")) {
                                String[] parts = jsonObject.getString("name").split("\\(");
                                mName = parts[0];
                                wear = parts[1];
                                wear = wear.substring(0, wear.length() - 1);
                            } else {
                                mName = jsonObject.getString("name");
                                wear = jsonObject.getString("type");
                            }

                            String wear_value;
                            if (!jsonObject.getString("wear").equals("null")) {
                                wear_value = precision.format(jsonObject.getDouble("wear"));
                            } else {
                                wear_value = "-";
                            }

                            Double price = ((double) jsonObject.getInt("suggested_price") / 100);
                            totalValue+=price;

                            JSONObject images = jsonObject.getJSONObject("image");

                            itemList1.add(new OfferItem(jsonObject.getString("id"), mName, wear, wear_value, price.toString()+"$", images.getString("300px"), jsonObject.getString("color"),false));
                        }
                        itemAdapter1.notifyDataSetChanged();
                        their_info_value.setText("Total Value: " + price_format.format(totalValue) + "$");

                        JSONObject sender = offer.getJSONObject("sender");

                        JSONArray items2 = sender.getJSONArray("items");
                        your_info.setText("Your Items (" +items2.length() + ")");

                        totalValue = 0.00;

                        for (int i = 0; i < items2.length(); i++) {
                            JSONObject jsonObject = items2.getJSONObject(i);

                            // Split name into weapon_name and weapon_wear
                            String name = jsonObject.getString("name");
                            String mName;
                            String wear;

                            if (name.contains("(")) {
                                String[] parts = jsonObject.getString("name").split("\\(");
                                mName = parts[0];
                                wear = parts[1];
                                wear = wear.substring(0, wear.length() - 1);
                            } else {
                                mName = jsonObject.getString("name");
                                wear = jsonObject.getString("type");
                            }

                            String wear_value;
                            if (!jsonObject.getString("wear").equals("null")) {
                                wear_value = precision.format(jsonObject.getDouble("wear"));
                            } else {
                                wear_value = "-";
                            }

                            Double price = ((double) jsonObject.getInt("suggested_price") / 100);
                            totalValue+=price;

                            JSONObject images = jsonObject.getJSONObject("image");

                            itemList2.add(new OfferItem(jsonObject.getString("id"), mName, wear, wear_value, price.toString()+"$", images.getString("300px"), jsonObject.getString("color"),false));
                        }
                        itemAdapter2.notifyDataSetChanged();
                        your_info_value.setText("Total Value: " +price_format.format(totalValue) + "$");


                        if (!offer.getString("message").equals("") && offer.getString("message") != null) {
                            message.setVisibility(View.VISIBLE);
                            message_text.setText("Message: " + offer.getString("message"));
                        }
                    } else {
                        // Normal Sender
                        // Get+Set status text
                        status.setText(offer.getString("state_name"));
                        // get Sender object
                        JSONObject sender = offer.getJSONObject("sender");
                        // set username + profile picture
                        username.setText(sender.getString("display_name"));
                        // check for wrong image url
                        if (sender.getString("avatar").equals("null") || sender.getString("avatar").equals("/images/opskins-logo-avatar.png"))
                            userpic.setImageDrawable(getResources().getDrawable(R.drawable.opskins_logo_avatar));
                        else
                            Picasso.get().load(sender.getString("avatar")).into(userpic);

                        JSONArray items1 = sender.getJSONArray("items");
                        their_info.setText("Their Items (" +items1.length() + ")");

                        Double totalValue = 0.00;

                        for (int i = 0; i < items1.length(); i++) {
                            JSONObject jsonObject = items1.getJSONObject(i);

                            // Split name into weapon_name and weapon_wear
                            String name = jsonObject.getString("name");
                            String mName;
                            String wear;

                            if (name.contains("(")) {
                                String[] parts = jsonObject.getString("name").split("\\(");
                                mName = parts[0];
                                wear = parts[1];
                                wear = wear.substring(0, wear.length() - 1);
                            } else {
                                mName = jsonObject.getString("name");
                                wear = jsonObject.getString("type");
                            }

                            String wear_value;
                            if (!jsonObject.getString("wear").equals("null")) {
                                wear_value = precision.format(jsonObject.getDouble("wear"));
                            } else {
                                wear_value = "-";
                            }

                            Double price = ((double) jsonObject.getInt("suggested_price") / 100);
                            totalValue+=price;

                            JSONObject images = jsonObject.getJSONObject("image");

                            itemList1.add(new OfferItem(jsonObject.getString("id"), mName, wear, wear_value, price.toString()+"$", images.getString("300px"), jsonObject.getString("color"),false));
                        }
                        itemAdapter1.notifyDataSetChanged();
                        their_info_value.setText("Total Value: " +price_format.format(totalValue) + "$");


                        JSONObject recipient = offer.getJSONObject("recipient");
                        // set username + profile picture
                        // check for wrong image url

                        JSONArray items2 = recipient.getJSONArray("items");
                        your_info.setText("Your Items (" +items2.length() + ")");

                        totalValue = 0.00;

                        for (int i = 0; i < items2.length(); i++) {
                            JSONObject jsonObject = items2.getJSONObject(i);

                            // Split name into weapon_name and weapon_wear
                            String name = jsonObject.getString("name");
                            String mName;
                            String wear;

                            if (name.contains("(")) {
                                String[] parts = jsonObject.getString("name").split("\\(");
                                mName = parts[0];
                                wear = parts[1];
                                wear = wear.substring(0, wear.length() - 1);
                            } else {
                                mName = jsonObject.getString("name");
                                wear = jsonObject.getString("type");
                            }

                            String wear_value;
                            if (!jsonObject.getString("wear").equals("null")) {
                                wear_value = precision.format(jsonObject.getDouble("wear"));
                            } else {
                                wear_value = "-";
                            }

                            Double price = ((double) jsonObject.getInt("suggested_price") / 100);
                            totalValue+=price;

                            JSONObject images = jsonObject.getJSONObject("image");

                            itemList2.add(new OfferItem(jsonObject.getString("id"), mName, wear, wear_value, price.toString()+"$", images.getString("300px"), jsonObject.getString("color"),false));
                        }
                        itemAdapter2.notifyDataSetChanged();
                        your_info_value.setText("Total Value: " +price_format.format(totalValue) + "$");

                        if (!offer.getString("message").equals("") && offer.getString("message") != null) {
                            message.setVisibility(View.VISIBLE);
                            message_text.setText("Message: " + offer.getString("message"));
                        }
                    }

                    // Show content when everything is loaded
                    content.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(String text) {
                progressDialog.dismiss();
                Toast.makeText(OfferDetail.this, text, Toast.LENGTH_SHORT).show();
            }
        });
        longOperation.execute();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public interface OnEventListener {
        void onSuccess(JSONObject offer);
        void onFailure(String text);
    }

    // Long Operation ASYNC Task class
    private static class LongOperation extends AsyncTask<String, Void, JSONObject> {

        private OnEventListener mCallBack;
        private TradeInterface tradeInterface;

        private Integer offer_id;

        LongOperation(Activity activity, Integer offer_id, OnEventListener callback) {
            this.offer_id = offer_id;
            mCallBack = callback;
            tradeInterface = new TradeInterface(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... params) {

            JSONObject temp = null;

            try {
                JSONObject offers = tradeInterface.getOffer(this.offer_id);
                 temp = offers.getJSONObject("offer");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return temp;
        }

        @Override
        protected void onPostExecute(JSONObject offer) {
            try {
                if (mCallBack != null) {
                    if (offer != null)
                        mCallBack.onSuccess(offer);
                    else
                        mCallBack.onFailure("Invalid Offer");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}

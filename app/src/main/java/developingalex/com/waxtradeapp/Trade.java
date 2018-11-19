package developingalex.com.waxtradeapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class Trade extends AppCompatActivity {

    private static int ITEMS_TO_SEND = 1;
    private static int ITEMS_TO_RECEIVE = 2;

    private OAuth oAuth;
    private TradeInterface tradeInterface;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private ScrollView content;
    private ProgressBar progressBar;
    private ImageView userPicReceiver, userPicSender;
    private TextView usernameReceiver, usernameSender, receiver_info, receiver_info_value, sender_info, sender_info_value;
    private Button addItemsToSend, addItemsToReceive, makeOffer;
    private EditText tradeMessage, twoFactorCode;

    private String offer_url, user_id, partnerUsername, partnerAvatar;

    private ArrayList<String> itemsToSend = new ArrayList<>();
    private ArrayList<String> itemsToReceive = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade);

        Bundle b = getIntent().getExtras();
        assert b != null;
        user_id = b.getString("user_id");
        offer_url = b.getString("offer_url");

        oAuth = new OAuth(this);
        tradeInterface = new TradeInterface(this);
        sharedPreferences = this.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Init Toolbar
        Toolbar toolbar = findViewById(R.id.trade_toolbar);
        toolbar.setTitle("New Offer");

        // Adds Back-Arrow to Toolbar
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        content = findViewById(R.id.trade_content);
        progressBar = findViewById(R.id.activity_trade_progress);

        // Init apps dropdown
        userPicReceiver = findViewById(R.id.trade_userPicReceiver);
        userPicSender = findViewById(R.id.trade_userPicSender);
        // set picture of logged in user
        Picasso.get()
                .load(oAuth.getUserProfilePicture())
                .error(this.getResources().getDrawable(R.drawable.opskins_logo_avatar))
                .placeholder(this.getResources().getDrawable(R.drawable.opskins_logo_avatar))
                .into(userPicSender);

        usernameReceiver = findViewById(R.id.trade_usernameReceiver);
        usernameSender = findViewById(R.id.trade_usernameSender);
        // set text of logged in user
        usernameSender.setText(oAuth.getUserProfileUsername());

        receiver_info = findViewById(R.id.trade_their);
        receiver_info.setText("Their items (0)");
        receiver_info_value = findViewById(R.id.trade_their_value);
        receiver_info_value.setText("0.00$");

        sender_info = findViewById(R.id.trade_your);
        sender_info.setText("Your items (0)");
        sender_info_value = findViewById(R.id.trade_your_value);
        sender_info_value.setText("0.00$");

        addItemsToSend = findViewById(R.id.trade_add_itemsToSend);
        addItemsToSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Trade.this, TradeInventory.class);
                Bundle b = new Bundle();
                b.putString("user_id", oAuth.getUserID()); // Parameter for new Activity
                b.putStringArrayList("selectedItems", itemsToSend);
                intent.putExtras(b);
                startActivityForResult(intent, ITEMS_TO_SEND);
            }
        });

        addItemsToReceive = findViewById(R.id.trade_add_itemsToReceive);
        addItemsToReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Trade.this, TradeInventory.class);
                Bundle b = new Bundle();
                b.putString("user_id", user_id); // Parameter for new Activity
                b.putStringArrayList("selectedItems", itemsToReceive);
                intent.putExtras(b);
                startActivityForResult(intent, ITEMS_TO_RECEIVE);
            }
        });

        makeOffer = findViewById(R.id.trade_make_offer);
        tradeMessage = findViewById(R.id.trade_message);
        twoFactorCode = findViewById(R.id.trade_2FA);

        setReceiverInfo(user_id, 1);

        makeOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemsToReceive.isEmpty() && itemsToSend.isEmpty()) {
                    Toast.makeText(Trade.this,"Add at least one item you want to trade", Toast.LENGTH_SHORT).show();
                } else if (twoFactorCode.getText() == null || twoFactorCode.getText().toString().isEmpty()) {
                    Toast.makeText(Trade.this,"Enter a 2FA Code", Toast.LENGTH_SHORT).show();
                } else if (twoFactorCode.getText().toString().length() != 6) {
                    Toast.makeText(Trade.this,"Enter a valid 2FA Code", Toast.LENGTH_SHORT).show();
                } else if (itemsToSend.size() > 100) {
                    Toast.makeText(Trade.this, "Limit for Items to send is 100", Toast.LENGTH_SHORT).show();
                } else if (itemsToReceive.size() > 100) {
                    Toast.makeText(Trade.this, "Limit for Items to receive is 100", Toast.LENGTH_SHORT).show();
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final String status = tradeInterface.sendOffer(offer_url, twoFactorCode.getText().toString(), android.text.TextUtils.join(",", itemsToSend), android.text.TextUtils.join(",", itemsToReceive), tradeMessage.getText().toString());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(Trade.this, status, Toast.LENGTH_SHORT).show();
                                        if (status.equals("Offer Sent!")) {
                                            onBackPressed();
                                        }
                                    }
                                });

                                if (status.equals("Offer Sent!")) {

                                    JSONObject object = new JSONObject();
                                    object.put("username", partnerUsername);
                                    object.put("avatar", partnerAvatar);
                                    object.put("tradeURL", offer_url);

                                    JSONArray array;

                                    if (sharedPreferences.contains("recentTradePartners"))
                                        array = new JSONArray(sharedPreferences.getString("recentTradePartners", null));
                                    else
                                        array = new JSONArray();

                                    if (sharedPreferences.contains("recentTradePartners")) {
                                        JSONArray recentTradePartners = new JSONArray(sharedPreferences.getString("recentTradePartners", null));
                                        for (int i = 0; i < recentTradePartners.length(); i++) {
                                            JSONObject partner = recentTradePartners.getJSONObject(i);
                                            if (!partner.getString("tradeURL").equals(offer_url)) {
                                                editor.remove("recentTradePartners").apply();   // delete old recentPartners
                                                array.put(object);
                                                editor.putString("recentTradePartners", array.toString()).apply();
                                            }
                                        }
                                    } else {
                                        array.put(object);
                                        editor.putString("recentTradePartners", array.toString()).apply();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check which request we're responding to
        if (requestCode == ITEMS_TO_SEND && resultCode == RESULT_OK) {
            itemsToSend = (ArrayList<String>) data.getSerializableExtra("items");
            sender_info.setText("Your items (" + itemsToSend.size()+")");
            sender_info_value.setText(data.getStringExtra("totalValue") +"$");
        }

        if (requestCode == ITEMS_TO_RECEIVE && resultCode == RESULT_OK) {
            itemsToReceive = (ArrayList<String>) data.getSerializableExtra("items");
            receiver_info.setText("Their items (" + itemsToReceive.size() +")");
            receiver_info_value.setText(data.getStringExtra("totalValue") +"$");
        }
    }

    public void setReceiverInfo(String user_id, final Integer app_id) {
        LongOperation longOperation = new LongOperation(this, user_id, app_id, new Trade.OnEventListener() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    JSONObject userData = response.getJSONObject("user_data");
                    // set username
                    usernameReceiver.setText(userData.getString("username"));

                    // set userpic
                    Picasso.get()
                            .load(userData.getString("avatar"))
                            .error(getResources().getDrawable(R.drawable.opskins_logo_avatar))
                            .placeholder(getResources().getDrawable(R.drawable.opskins_logo_avatar))
                            .into(userPicReceiver);

                    partnerUsername = userData.getString("username");
                    partnerAvatar = userData.getString("avatar");

                    progressBar.setVisibility(View.GONE);
                    content.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(Trade.this, "Could not get recipient info", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Trade.this, CreateOffer.class);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_out_right, R.anim.slide_out_right);
                }
            }

            @Override
            public void onFailure(String text) {
                Toast.makeText(Trade.this, text, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Trade.this, CreateOffer.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_out_right, R.anim.slide_out_right);
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
        void onSuccess(JSONObject response);
        void onFailure(String text);
    }


    // Long Operation ASYNC Task class
    private static class LongOperation extends AsyncTask<String, Void, JSONObject> {

        private Trade.OnEventListener mCallBack;
        private TradeInterface tradeInterface;

        private String mUserID;
        private Integer mAppID;

        LongOperation(Activity activity, String user_id, Integer app_id, Trade.OnEventListener callback) {
            mCallBack = callback;
            mUserID = user_id;
            mAppID = app_id;
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
                temp = tradeInterface.getUserInventory(mUserID, mAppID);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return temp;
        }

        @Override
        protected void onPostExecute(JSONObject response) {
            if (mCallBack != null) {
                if (response != null)
                    mCallBack.onSuccess(response);
                else
                    mCallBack.onFailure("Could not get recipient info");
            }

        }

    }

}

package developingalex.com.waxtradeapp.views;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

import developingalex.com.waxtradeapp.R;
import developingalex.com.waxtradeapp.adapters.InventoryItemAdapter;
import developingalex.com.waxtradeapp.interfaces.ItemClickListener;
import developingalex.com.waxtradeapp.adapters.SpinnerAdapter;
import developingalex.com.waxtradeapp.lib.TradeImplementation;
import developingalex.com.waxtradeapp.interfaces.TradeInterface;
import developingalex.com.waxtradeapp.objects.AppsData;
import developingalex.com.waxtradeapp.objects.StandardItem;

public class TradeInventory extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String user_id;

    private ProgressBar progressBar;
    private TextView emptyInventory;
    private Spinner apps_dropdown;
    private RecyclerView recyclerView;
    private InventoryItemAdapter itemAdapter;
    private ArrayList<StandardItem> itemList = new ArrayList<>();

    private ArrayList<AppsData> apps_list = new ArrayList<>();
    private ArrayList<Integer> apps_list_id = new ArrayList<>();
    private ArrayList<Integer> selectedItems = new ArrayList<>();
    private double total_value = 0.00;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trade_inventory);

        final Bundle b = getIntent().getExtras();
        assert b != null;
        user_id = b.getString("user_id");
        selectedItems = b.getIntegerArrayList("selectedItems");

        // Init Toolbar
        final Toolbar toolbar = findViewById(R.id.trade_inventory_toolbar);
        toolbar.setTitle("Inventory");

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        progressBar = findViewById(R.id.trade_inventory_progress);
        emptyInventory = findViewById(R.id.trade_inventory_empty);

        apps_dropdown = findViewById(R.id.trade_apps_spinner);
        apps_dropdown.setOnItemSelectedListener(TradeInventory.this);

        recyclerView = findViewById(R.id.trade_inventory_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        itemAdapter = new InventoryItemAdapter(this, itemList);
        recyclerView.setAdapter(itemAdapter);

        itemAdapter.setOnItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(int position) {


                int currentItem = itemList.get(position).getId();
                int selectedItem = selectedItems.indexOf(currentItem);

                if (selectedItem != -1) {
                    selectedItems.remove(selectedItem);
                    total_value -= itemList.get(position).getSuggested_price();
                } else {
                    selectedItems.add(currentItem);
                    total_value += itemList.get(position).getSuggested_price();
                }

                Log.w("TradeInventory", selectedItems.toString());

                itemAdapter.toggleItemActive(position);
            }

            @Override
            public void onAcceptClick(int position) {
            }

            @Override
            public void onDeclineClick(int position) {
            }
        });


        new AppsFetcher(this, new OnEventListener() {
            @Override
            public void onSuccess(JSONArray apps) {
                try {
                    for (int i = 0; i < apps.length(); i++) {
                        JSONObject app = apps.getJSONObject(i);
                        apps_list.add(new AppsData(app.getString("name"), app.getString("img")));
                        apps_list_id.add(app.getInt("internal_app_id"));
                    }
                    SpinnerAdapter adapter = new SpinnerAdapter(TradeInventory.this, R.layout.spinner_layout, R.id.appsText, apps_list);
                    apps_dropdown.setAdapter(adapter);
                    apps_dropdown.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String text) {
                Toast.makeText(TradeInventory.this, text, Toast.LENGTH_SHORT).show();
            }
        }).execute();
    }


    public void getInventoryByAppID(String user_id, Integer app_id) {

        progressBar.setVisibility(View.VISIBLE);
        emptyInventory.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);

        new InventoryFetcher(this, user_id, app_id, new OnEventListener2() {
            @Override
            public void onSuccess(JSONObject response) {

                try {

                    JSONArray items = response.getJSONArray("items");
                    itemList.clear();

                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);

                        itemList.add(
                                new StandardItem(
                                        item.has("id") && !item.isNull("id") ? item.getInt("id") : -1,
                                        item.has("internal_app_id") && !item.isNull("internal_app_id") ? item.getInt("internal_app_id") : -1,
                                        item.has("def_id") && !item.isNull("def_id") ? item.getInt("def_id") : -1,
                                        item.has("sku") && !item.isNull("sku") ? item.getInt("sku") : -1,
                                        item.has("wear") && !item.isNull("wear") ? item.getDouble("wear") : 0.0,
                                        item.getBoolean("tradable"),
                                        item.getBoolean("is_trade_restricted"),
                                        item.has("trade_hold_expires") && !item.isNull("trade_hold_expires") ? item.getInt("trade_hold_expires") : 0,
                                        item.has("name") && !item.isNull("name") ? item.getString("name") : "",
                                        item.has("market_name") && !item.isNull("market_name") ? item.getString("market_name") : "",
                                        item.has("category") && !item.isNull("category") ? item.getString("category") : "",
                                        item.has("rarity") && !item.isNull("rarity") ? item.getString("rarity") : "",
                                        item.has("type") && !item.isNull("type") ? item.getString("type") : "",
                                        item.has("color") && !item.isNull("color") ? item.getString("color") : "",
                                        item.has("image") && !item.isNull("image") ? item.get("image") : null,
                                        item.has("suggested_price") && !item.isNull("suggested_price") ? item.getInt("suggested_price") : 0,
                                        item.has("suggested_price_floor") && !item.isNull("suggested_price_floor") ? item.getInt("suggested_price_floor") : 0,
                                        item.getBoolean("instant_sell_enabled"),
                                        item.has("preview_urls") && !item.isNull("preview_urls") ? item.get("preview_urls") : null,
                                        item.has("assets") && !item.isNull("assets") ? item.get("assets") : null,
                                        item.has("inspect") && !item.isNull("inspect") ? item.getString("inspect") : "",
                                        item.has("eth_inspect") && !item.isNull("eth_inspect") ? item.getString("eth_inspect") : "",
                                        item.has("pattern_index") && !item.isNull("pattern_index") ? item.getInt("pattern_index") : 0,
                                        item.has("paint_index") && !item.isNull("paint_index") ? item.getInt("paint_index") : 0,
                                        item.has("wear_tier_index") && !item.isNull("wear_tier_index") ? item.getInt("wear_tier_index") : 0,
                                        item.has("time_created") && !item.isNull("time_created") ? item.getInt("time_created") : 0,
                                        item.has("time_updated") && !item.isNull("time_updated") ? item.getInt("time_updated") : 0,
                                        item.has("attributes") && !item.isNull("attributes") ? item.get("attributes") : null
                                )
                        );

                    }

                    progressBar.setVisibility(View.GONE);
                    itemAdapter.notifyDataSetChanged();

                    if (items.length() <= 0) {
                        emptyInventory.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.INVISIBLE);
                    } else {
                        emptyInventory.setVisibility(View.INVISIBLE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String text) {
                Toast.makeText(TradeInventory.this, text, Toast.LENGTH_SHORT).show();
            }
        }).execute();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if (user_id != null) {
            getInventoryByAppID(user_id, apps_list_id.get(position));
        } else {
            Toast.makeText(TradeInventory.this, "Please Try Again!", Toast.LENGTH_SHORT).show();
        }
    }

    public void onNothingSelected(AdapterView<?> arg0) {

    }

    public interface OnEventListener {
        void onSuccess(JSONArray apps);
        void onFailure(String text);
    }

    public interface OnEventListener2 {
        void onSuccess(JSONObject response);
        void onFailure(String text);
    }

    // Long Operation ASYNC Task class
    private static class AppsFetcher extends AsyncTask<String, Void, JSONArray> {

        private TradeInventory.OnEventListener mCallBack;
        private TradeInterface tradeInterface;

        AppsFetcher(Activity activity, TradeInventory.OnEventListener callback) {
            mCallBack = callback;
            tradeInterface = new TradeImplementation(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONArray doInBackground(String... params) {

            JSONArray temp = null;

            try {
                JSONObject apps = tradeInterface.getApps();
                temp = apps.getJSONArray("apps");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return temp;
        }

        @Override
        protected void onPostExecute(JSONArray apps) {
            if (mCallBack != null) {
                if (apps != null)
                    mCallBack.onSuccess(apps);
                else
                    mCallBack.onFailure("Invalid Apps, please try again!");
            }

        }

    }

    // Long Operation ASYNC Task class
    private static class InventoryFetcher extends AsyncTask<String, Void, JSONObject> {

        private TradeInventory.OnEventListener2 mCallBack;
        private TradeInterface tradeInterface;

        private String mUserID;
        private Integer mAppID;

        InventoryFetcher(Activity activity, String user_id, Integer app_id, TradeInventory.OnEventListener2 callback) {
            mCallBack = callback;
            mUserID = user_id;
            mAppID = app_id;
            tradeInterface = new TradeImplementation(activity);
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
                    mCallBack.onFailure("Failed loading Inventory, please try again!");
            }

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.inventory_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_check) {
            DecimalFormat price_format = new DecimalFormat("0.00");

            Intent data = new Intent();
            data.putExtra ("items", selectedItems);
            data.putExtra("totalValue", price_format.format(total_value));
            setResult(RESULT_OK, data);
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}

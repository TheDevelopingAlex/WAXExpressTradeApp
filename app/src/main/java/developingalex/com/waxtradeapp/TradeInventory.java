package developingalex.com.waxtradeapp;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import java.util.List;
import java.util.Objects;

public class TradeInventory extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String user_id;

    private ProgressBar progressBar;
    private TextView emptyInventory;
    private Spinner apps_dropdown;
    private RecyclerView mRecyclerView;
    private InventoryItemAdapter itemAdapter;
    private ArrayList<OfferItem> itemList = new ArrayList<>();

    private ArrayList<AppsData> apps_list = new ArrayList<>();
    private List<Integer> apps_list_id = new ArrayList<>();
    private ArrayList<String> selectedItems = new ArrayList<>();
    private double total_value = 0.00;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trade_inventory);

        Bundle b = getIntent().getExtras();
        assert b != null;
        user_id = b.getString("user_id");
        selectedItems = b.getStringArrayList("selectedItems");

        // Init Toolbar
        Toolbar toolbar = findViewById(R.id.trade_inventory_toolbar);
        toolbar.setTitle("Inventory");

        // Adds Back-Arrow to Toolbar
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        progressBar = findViewById(R.id.trade_inventory_progress);
        emptyInventory = findViewById(R.id.trade_inventory_empty);

        apps_dropdown = findViewById(R.id.trade_apps_spinner);
        apps_dropdown.setOnItemSelectedListener(TradeInventory.this);

        mRecyclerView = findViewById(R.id.trade_inventory_recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        itemAdapter = new InventoryItemAdapter(this, itemList);
        mRecyclerView.setAdapter(itemAdapter);

        itemAdapter.setOnItemClickListener(new InventoryItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (selectedItems.contains(itemList.get(position).getItemID())) {
                    selectedItems.remove(itemList.get(position).getItemID());
                    itemAdapter.toggleItemActive(position);
                    String price = itemList.get(position).getPrice().replace("$","");
                    total_value -= Double.valueOf(price);
                } else {
                    selectedItems.add(itemList.get(position).getItemID());
                    itemAdapter.toggleItemActive(position);
                    String price = itemList.get(position).getPrice().replace("$","");
                    total_value += Double.valueOf(price);
                }
            }
        });


        // get all apps from opskins
        LongOperation longOperation = new LongOperation(this, new OnEventListener() {
            @Override
            public void onSuccess(JSONArray apps) {
                try {
                    for (int i = 0; i < apps.length(); i++) {
                        JSONObject app = apps.getJSONObject(i);
                        //apps_list.add(app.getString("name"));
                        apps_list.add(new AppsData(app.getString("name"), app.getString("img")));
                        apps_list_id.add(app.getInt("internal_app_id"));
                    }
                    SpinnerAdapter adapter = new SpinnerAdapter(TradeInventory.this, R.layout.spinner_layout, R.id.appsText, apps_list);
                    //set the spinners adapter to the previously created one.
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
        });
        longOperation.execute();
    }


    public void getInventoryByAppID(String user_id, Integer app_id) {

        progressBar.setVisibility(View.VISIBLE);
        emptyInventory.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);

        LongOperation2 longOperation2 = new LongOperation2(this, user_id, app_id, new OnEventListener2() {
            @Override
            public void onSuccess(JSONObject response) {
                DecimalFormat precision = new DecimalFormat("0.00000");

                try {

                    JSONArray items = response.getJSONArray("items");
                    itemList.clear();

                    for (int i = 0; i < items.length(); i++) {
                        JSONObject jsonObject = items.getJSONObject(i);

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

                        JSONObject images = jsonObject.getJSONObject("image");

                        if (selectedItems.contains(jsonObject.getString("id"))) {
                            itemList.add(new OfferItem(jsonObject.getString("id"), mName, wear, wear_value, price.toString() + "$", images.getString("300px"), jsonObject.getString("color"), true));
                            total_value+=price;
                        } else
                            itemList.add(new OfferItem(jsonObject.getString("id"), mName, wear, wear_value, price.toString()+"$", images.getString("300px"), jsonObject.getString("color"),false));
                    }

                    progressBar.setVisibility(View.GONE);
                    itemAdapter.notifyDataSetChanged();

                    if (items.length() <= 0) {
                        emptyInventory.setVisibility(View.VISIBLE);
                        mRecyclerView.setVisibility(View.INVISIBLE);
                    } else {
                        emptyInventory.setVisibility(View.INVISIBLE);
                        mRecyclerView.setVisibility(View.VISIBLE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String text) {
                Toast.makeText(TradeInventory.this, text, Toast.LENGTH_SHORT).show();
            }
        });
        longOperation2.execute();
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Showing selected spinner item
        if (user_id != null)
            getInventoryByAppID(user_id , apps_list_id.get(position));
        else
            Toast.makeText(TradeInventory.this, "Please Try Again!", Toast.LENGTH_SHORT).show();
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
    private static class LongOperation extends AsyncTask<String, Void, JSONArray> {

        private TradeInventory.OnEventListener mCallBack;
        private TradeInterface tradeInterface;

        LongOperation(Activity activity, TradeInventory.OnEventListener callback) {
            mCallBack = callback;
            tradeInterface = new TradeInterface(activity);
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
    private static class LongOperation2 extends AsyncTask<String, Void, JSONObject> {

        private TradeInventory.OnEventListener2 mCallBack;
        private TradeInterface tradeInterface;

        private String mUserID;
        private Integer mAppID;

        LongOperation2(Activity activity, String user_id, Integer app_id, TradeInventory.OnEventListener2 callback) {
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
                    mCallBack.onFailure("Invalid Apps, please try again!");
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

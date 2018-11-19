package developingalex.com.waxtradeapp;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DrawerHistory extends Fragment {

    // WeakReference for static class -> Activity
    private static WeakReference<DrawerHistory> mActivityRef;
    public static void updateActivity(DrawerHistory activity) {
        mActivityRef = new WeakReference<>(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DrawerHistory.updateActivity(this);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.drawer_history, container, false);
        // Setting ViewPager for each Tabs
        ViewPager viewPager = view.findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        // Set Tabs inside Toolbar
        TabLayout tabs = view.findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        return view;
    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getChildFragmentManager());

        // New Fragments
        adapter.addFragment(new ReceivedFragment(), "Received");
        adapter.addFragment(new SentFragment(), "Sent");
        viewPager.setAdapter(adapter);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        private Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        private void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


    // Fragment for Sent History
    public static class SentFragment extends Fragment {

        private OfferAdapter adapter;

        private SwipeRefreshLayout swipeRefreshLayout;
        public RecyclerView recyclerView;
        private TextView mText;

        public ArrayList<Offer> offerList = new ArrayList<>();

        public SentFragment() { }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.drawer_history_content, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

            mText = view.findViewById(R.id.drawer_history_text);

            swipeRefreshLayout = view.findViewById(R.id.drawer_history_swipe);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    loadOffers();
                }
            });
            // Configure the refreshing colors
            swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_dark);

            swipeRefreshLayout.setRefreshing(true);

            recyclerView = view.findViewById(R.id.drawer_history_recyclerView);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

            // settings adapter
            adapter = new OfferAdapter(getActivity(), offerList);
            recyclerView.setAdapter(adapter);

            adapter.acceptButtonVisibility(false);
            adapter.declineButtonVisibility(false);
            adapter.statusTextVisibility(true);

            loadOffers();

            adapter.setOnItemClickListener(new OfferAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    Intent intent = new Intent(getActivity(), OfferDetail.class);
                    Bundle b = new Bundle();
                    b.putInt("offerId", offerList.get(position).getId()); // Parameter for new Activity
                    b.putBoolean("hideAccept", true); // Parameter for new Activity
                    intent.putExtras(b);
                    startActivity(intent);
                }

                @Override
                public void onAcceptClick(int position) { }

                @Override
                public void onDeclineClick(int position) { }
            });
        }

        private void loadOffers() {
            offerList.clear();
            LongOperation longOperation = new LongOperation(mActivityRef.get().getActivity(),"sent", new OnEventListener() {
                @Override
                public void onSuccess(Offer offer) {
                    // Add offers to list and notify adapter to refresh it
                    offerList.add(offer);
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }

                @Override
                public void onFailure(String text) {
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                    mText.setText(R.string.info_no_offer);
                }
            });
            longOperation.execute();
        }
    }


    // Fragment for Sent History
    public static class ReceivedFragment extends Fragment {

        private OfferAdapter adapter;

        private SwipeRefreshLayout swipeRefreshLayout;
        public RecyclerView recyclerView;
        private TextView mText;

        public ArrayList<Offer> offerList = new ArrayList<>();

        public ReceivedFragment() { }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.drawer_history_content, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

            mText = view.findViewById(R.id.drawer_history_text);

            swipeRefreshLayout = view.findViewById(R.id.drawer_history_swipe);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    loadOffers();
                }
            });
            // Configure the refreshing colors
            swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_dark);

            swipeRefreshLayout.setRefreshing(true);

            recyclerView = view.findViewById(R.id.drawer_history_recyclerView);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

            // settings adapter
            adapter = new OfferAdapter(getActivity(), offerList);
            recyclerView.setAdapter(adapter);

            adapter.acceptButtonVisibility(false);
            adapter.declineButtonVisibility(false);
            adapter.statusTextVisibility(true);

            loadOffers();

            adapter.setOnItemClickListener(new OfferAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    Intent intent = new Intent(getActivity(), OfferDetail.class);
                    Bundle b = new Bundle();
                    b.putInt("offerId", offerList.get(position).getId()); // Parameter for new Activity
                    b.putBoolean("hideAccept", true); // Parameter for new Activity
                    intent.putExtras(b);
                    startActivity(intent);
                }

                @Override
                public void onAcceptClick(int position) { }

                @Override
                public void onDeclineClick(int position) { }
            });
        }

        private void loadOffers() {
            offerList.clear();
            LongOperation longOperation = new LongOperation(mActivityRef.get().getActivity(),"received", new OnEventListener() {
                @Override
                public void onSuccess(Offer offer) {
                    // Add offers to list and notify adapter to refresh it
                    offerList.add(offer);
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }

                @Override
                public void onFailure(String text) {
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                    mText.setText(R.string.info_no_offer);
                }
            });
            longOperation.execute();
        }
    }

    public interface OnEventListener {
        void onSuccess(Offer offer);
        void onFailure(String text);
    }

    // Long Operation ASYNC Task class
    private static class LongOperation extends AsyncTask<String, Void, JSONArray> {

        private OnEventListener mCallBack;
        private TradeInterface tradeInterface;

        private String type;

        LongOperation(Activity activity, String type, OnEventListener callback) {
            this.type = type;
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
                JSONObject offers = tradeInterface.getOffers(this.type, "3,5,6,7,8", "created");
                if (!offers.getString("total").equals("0"))
                    temp = (JSONArray) offers.get("offers");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return temp;
        }

        @Override
        protected void onPostExecute(JSONArray offers) {

            DecimalFormat price_format = new DecimalFormat("0.00");

            if (mCallBack != null) {
                if (offers != null) {

                    if (this.type.equals("sent")) {
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

                                Double sender_items_price = 0.00;
                                Double recipient_items_price = 0.00;

                                for (int j = 0; j < sender_items.length(); j++) {
                                    JSONObject sender_item = sender_items.getJSONObject(j);
                                    sender_items_price += ((double) sender_item.getInt("suggested_price") / 100);
                                }

                                for (int j = 0; j < recipient_items.length(); j++) {
                                    JSONObject recipient_item = recipient_items.getJSONObject(j);
                                    recipient_items_price += ((double) recipient_item.getInt("suggested_price") / 100);
                                }

                                mCallBack.onSuccess(new Offer(jsonObject.getInt("id"), recipient.getString("display_name"),"Your Items ("+sender_items_length.toString()+"): "+ price_format.format(sender_items_price)+ "$", "Their Items ("+recipient_items_length.toString()+"): "+ price_format.format(recipient_items_price)+ "$", recipient.getString("avatar"), jsonObject.getString("state_name")));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
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

                                Double sender_items_price = 0.00;
                                Double recipient_items_price = 0.00;

                                for (int j = 0; j < sender_items.length(); j++) {
                                    JSONObject sender_item = sender_items.getJSONObject(j);
                                    sender_items_price += ((double) sender_item.getInt("suggested_price") / 100);
                                }

                                for (int j = 0; j < recipient_items.length(); j++) {
                                    JSONObject recipient_item = recipient_items.getJSONObject(j);
                                    recipient_items_price += ((double) recipient_item.getInt("suggested_price") / 100);
                                }

                                mCallBack.onSuccess(new Offer(jsonObject.getInt("id"), sender.getString("display_name"),"Your Items ("+recipient_items_length.toString()+"): "+ price_format.format(recipient_items_price) +"$", "Their Items ("+sender_items_length.toString()+"): "+ sender_items_price+ "$", sender.getString("avatar"), jsonObject.getString("state_name")));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    mCallBack.onFailure("No Offers found");
                }
            }

        }

    }
}

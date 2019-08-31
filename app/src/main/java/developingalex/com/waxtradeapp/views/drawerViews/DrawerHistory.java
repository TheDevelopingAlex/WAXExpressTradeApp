package developingalex.com.waxtradeapp.views.drawerViews;

import android.content.Intent;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import developingalex.com.waxtradeapp.interfaces.OfferListener;
import developingalex.com.waxtradeapp.lib.AsyncOfferLoader;
import developingalex.com.waxtradeapp.objects.Offer;
import developingalex.com.waxtradeapp.adapters.OfferAdapter;
import developingalex.com.waxtradeapp.interfaces.ItemClickListener;
import developingalex.com.waxtradeapp.objects.StandardItem;
import developingalex.com.waxtradeapp.objects.StandardTradeOffer;
import developingalex.com.waxtradeapp.views.OfferDetail;
import developingalex.com.waxtradeapp.R;

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
        final View view = inflater.inflate(R.layout.drawer_history, container, false);
        // Setting ViewPager for each Tabs
        final ViewPager viewPager = view.findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        // Set Tabs inside Toolbar
        final TabLayout tabs = view.findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        return view;
    }

    private void setupViewPager(ViewPager viewPager) {
        final Adapter adapter = new Adapter(getChildFragmentManager());

        adapter.addFragment(new ReceivedFragment(), "Received");
        adapter.addFragment(new SentFragment(), "Sent");
        viewPager.setAdapter(adapter);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> fragmentTitleList = new ArrayList<>();

        private Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        private void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }


    // Fragment for Sent History
    public static class SentFragment extends Fragment {

        private OfferAdapter adapter;

        private SwipeRefreshLayout swipeRefreshLayout;
        private TextView mText;

        private final ArrayList<Offer> offerList = new ArrayList<>();

        public SentFragment() { }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

            RecyclerView recyclerView = view.findViewById(R.id.drawer_history_recyclerView);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

            // settings adapter
            adapter = new OfferAdapter(getActivity(), offerList);
            recyclerView.setAdapter(adapter);

            adapter.acceptButtonVisibility(false);
            adapter.declineButtonVisibility(false);
            adapter.statusTextVisibility(true);

            loadOffers();

            adapter.setOnItemClickListener(new ItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    final Intent intent = new Intent(getActivity(), OfferDetail.class);
                    final Bundle b = new Bundle();
                    b.putInt("offerId", offerList.get(position).getId()); // Parameter for new Activity
                    b.putBoolean("hideAccept", true); // Parameter for new Activity
                    intent.putExtras(b);
                    startActivity(intent);
                }

                @Override
                public void onAcceptClick(int position) {
                }

                @Override
                public void onDeclineClick(int position) {
                }

            });
        }

        private void loadOffers() {

            offerList.clear();

            new AsyncOfferLoader(mActivityRef.get().getActivity(),"sent", "3,5,6,7,8", "created",
                    new OfferListener() {
                        @Override
                        public void onSuccess(ArrayList<StandardTradeOffer> offers) {
                            // Add offers to list and notify adapter to refresh it
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
                            mText.setText("");
                        }

                        @Override
                        public void onFailure(String error) {
                            if (!error.isEmpty()) {
                                Log.e("DrawerReceived", error);
                            }
                            adapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
                            mText.setText(R.string.info_no_offer);
                        }
                    }).execute();
        }
    }


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

            adapter.setOnItemClickListener(new ItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    final Intent intent = new Intent(getActivity(), OfferDetail.class);
                    final Bundle b = new Bundle();
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

            new AsyncOfferLoader(mActivityRef.get().getActivity(),"received", "3,5,6,7,8", "created",
                    new OfferListener() {
                        @Override
                        public void onSuccess(ArrayList<StandardTradeOffer> offers) {
                            // Add offers to list and notify adapter to refresh it
                            for (int i = 0; i< offers.size(); i++) {
                                StandardTradeOffer offer = offers.get(i);

                                offerList.add(
                                        new Offer(
                                                offer.getId(),
                                                offer.getSender().getDisplay_name(),
                                                "Your Items ("+offer.getRecipient().getItems().size()+"): "+ String.format(java.util.Locale.US,"%.2f", getItemsPrice(offer.getRecipient().getItems()))+ "$",
                                                "Their Items ("+offer.getSender().getItems().size()+"): "+ String.format(java.util.Locale.US,"%.2f", getItemsPrice(offer.getSender().getItems()))+ "$",
                                                offer.getSender().getAvatar(),
                                                offer.getState_name()
                                        )
                                );
                            }

                            adapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
                            mText.setText("");
                        }

                        @Override
                        public void onFailure(String error) {
                            if (!error.isEmpty()) {
                                Log.e("DrawerHistory", error);
                            }
                            adapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
                            mText.setText(R.string.info_no_offer);
                        }
            }).execute();
        }
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

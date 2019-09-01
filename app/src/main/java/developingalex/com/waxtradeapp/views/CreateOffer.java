package developingalex.com.waxtradeapp.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Objects;

import developingalex.com.waxtradeapp.R;
import developingalex.com.waxtradeapp.interfaces.ItemClickListener;
import developingalex.com.waxtradeapp.objects.RecentTradePartners;
import developingalex.com.waxtradeapp.adapters.RecentTradePartnersAdapter;
import developingalex.com.waxtradeapp.lib.OAuthImplementation;

public class CreateOffer extends AppCompatActivity {

    private OAuthImplementation oAuthImplementation;

    private ProgressBar qrCodeInit;
    private TextView qrCodeText;
    private ImageView qrCodeIcon;
    private EditText tradeURL;

    private final ArrayList<RecentTradePartners> recentPartners = new ArrayList<>();

    private int QR_CAMERA_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_offer);

        oAuthImplementation = new OAuthImplementation(this);
        final SharedPreferences sharedPreferences = this.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);

        setTitle("New Offer");

        final Toolbar toolbar = findViewById(R.id.offer_create_toolbar);
        tradeURL = findViewById(R.id.tradeURLInput);

        // Recent trade partners
        final RecyclerView recentTradePartnersRecyclerView = findViewById(R.id.recentTradePartnersRecyclerView);
        recentTradePartnersRecyclerView.setHasFixedSize(true);
        recentTradePartnersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        final RecentTradePartnersAdapter adapter = new RecentTradePartnersAdapter(this, recentPartners);
        recentTradePartnersRecyclerView.setAdapter(adapter);

        if (sharedPreferences.contains("recentTradePartners")) {
            try {
                final JSONArray partners = new JSONArray(sharedPreferences.getString("recentTradePartners", null));
                for (int i = 0; i < partners.length(); i++) {
                    recentPartners.add(new RecentTradePartners(partners.getJSONObject(i).getString("username"), partners.getJSONObject(i).getString("avatar"), partners.getJSONObject(i).getString("tradeURL")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        adapter.notifyDataSetChanged();

        adapter.setOnItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                tradeURL.setText(recentPartners.get(position).getTradeURL());
            }

            @Override
            public void onAcceptClick(int ignore) { }

            @Override
            public void onDeclineClick(int ignore) { }
        });

        // Adds Back-Arrow to Toolbar
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        qrCodeInit = findViewById(R.id.qrCodeInitProgress);
        qrCodeText = findViewById(R.id.startQRCodeScannerText);
        qrCodeIcon = findViewById(R.id.startQRCodeScannerIcon);

        final CardView loginButton = findViewById(R.id.startQRCodeScanner);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrCodeInit.setVisibility(View.VISIBLE);
                qrCodeText.setVisibility(View.INVISIBLE);
                qrCodeIcon.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(CreateOffer.this, CreateOfferCamera.class);
                startActivityForResult(intent, QR_CAMERA_REQUEST_CODE);
            }
        });


        final CardView startTrading = findViewById(R.id.startTrading);
        startTrading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(CreateOffer.this, Trade.class);
                final String regex = "^(http://|https://)?(www.)?([a-zA-Z0-9]+).[a-zA-Z0-9]*.[a-z].?([a-z]+)?.(t).?([0-9])?.([0-9]){6}.([a-zA-Z0-9]){8}$";

                if (tradeURL.getText().toString().matches((regex))) {
                    try {
                        final String[] temp = tradeURL.getText().toString().split("/");
                        final String user_id = temp[4];

                        if (oAuthImplementation.getUserID().equals(user_id)) {
                            Toast.makeText(CreateOffer.this, "You can't trade with yourself!", Toast.LENGTH_SHORT).show();
                        } else {
                            final Bundle b = new Bundle();
                            b.putString("offer_url", tradeURL.getText().toString()); // Parameter for new Activity
                            b.putString("user_id", user_id); // Parameter for new Activity
                            intent.putExtras(b);
                            startActivity(intent);
                            finish();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(CreateOffer.this, "Trade URL invalid", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        qrCodeInit.setVisibility(View.INVISIBLE);
        qrCodeText.setVisibility(View.VISIBLE);
        qrCodeIcon.setVisibility(View.VISIBLE);

        if (requestCode == QR_CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    assert data != null;
                    final String link = Objects.requireNonNull(data.getExtras()).getString("tradeURL");
                    tradeURL.setText(link);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}

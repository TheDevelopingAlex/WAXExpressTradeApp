package developingalex.com.waxtradeapp.views;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.karan.churi.PermissionManager.PermissionManager;
import com.squareup.picasso.Picasso;

import developingalex.com.waxtradeapp.MainActivity;
import developingalex.com.waxtradeapp.R;
import developingalex.com.waxtradeapp.lib.OAuthImplementation;
import developingalex.com.waxtradeapp.views.drawerViews.DrawerHistory;
import developingalex.com.waxtradeapp.views.drawerViews.DrawerProfile;
import developingalex.com.waxtradeapp.views.drawerViews.DrawerReceived;
import developingalex.com.waxtradeapp.views.drawerViews.DrawerSent;

public class TradeArea extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private OAuthImplementation oAuthImplementation;
    private ProgressDialog progressDialog;
    private PermissionManager permissionManager;

    boolean doubleBackToExitPressedOnce = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade_area);

        permissionManager = new PermissionManager() {};
        permissionManager.checkAndRequestPermissions(this);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            final Intent intent = new Intent(TradeArea.this, CreateOffer.class);
            startActivity(intent);
            }
        });

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        setTitle("Received");
        final NavigationView navigationView = findViewById(R.id.nav_view);

        getSupportFragmentManager().beginTransaction().replace(R.id.content_trade_area, new DrawerReceived()).commit();
        navigationView.setCheckedItem(R.id.nav_received);
        navigationView.setNavigationItemSelectedListener(this);

        oAuthImplementation = new OAuthImplementation(this);
        progressDialog = new ProgressDialog(this);

        // SET USER PROFILE PICTURE
        final View headView = navigationView.getHeaderView(0);
        final ImageView profilePic = headView.findViewById(R.id.user_profile_picture);
        Picasso.get()
                .load(oAuthImplementation.getUserProfilePicture())
                .error(R.drawable.opskins_logo_avatar)
                .into(profilePic);

        // SET USER USERNAME
        final TextView profileUsername = headView.findViewById(R.id.user_profile_username);
        profileUsername.setText(oAuthImplementation.getUserProfileUsername());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionManager.checkResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        final int id = item.getItemId();

        if (id == R.id.nav_sent) {
            setTitle("Sent");
            getSupportFragmentManager().beginTransaction().replace(R.id.content_trade_area, new DrawerSent()).commit();
        } else if (id == R.id.nav_received) {
            setTitle("Received");
            getSupportFragmentManager().beginTransaction().replace(R.id.content_trade_area, new DrawerReceived()).commit();
        } else if (id == R.id.nav_history) {
            setTitle("History");
            getSupportFragmentManager().beginTransaction().replace(R.id.content_trade_area, new DrawerHistory()).commit();
        } else if (id == R.id.nav_profile) {
            setTitle("Profile");
            getSupportFragmentManager().beginTransaction().replace(R.id.content_trade_area, new DrawerProfile()).commit();
        } else if (id == R.id.nav_logout) {

            // CLOSE DRAWER
            DrawerLayout drawer =  findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            }

            progressDialog.setMessage("Logging Out ...");
            progressDialog.show();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // INITIATE LOGOUT
                        if (oAuthImplementation.logout()) {
                            Intent intent = new Intent(TradeArea.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // clears all previous activities task
                            startActivity(intent);
                            finish(); // destroy current activity..
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(TradeArea.this, "Please try again.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        progressDialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

}

package developingalex.com.waxtradeapp;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.WorkerThread;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import developingalex.com.waxtradeapp.lib.ChromeCustomTab;
import developingalex.com.waxtradeapp.lib.OAuth;


public class MainActivity extends AppCompatActivity {

    private OAuth oAuth;

    private ChromeCustomTab chromeCustomTab;
    private CardView loginButton;
    private ProgressBar progressBar;

    private String code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        oAuth = new OAuth(this);
        if (oAuth.checkAuthStatus()) {
            Intent intent = new Intent(MainActivity.this, TradeArea.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // clears all previous activities task
            startActivity(intent);
            finish(); // destroy current activity
        } else {
            chromeCustomTab = new ChromeCustomTab(this, oAuth.getURL());
            chromeCustomTab.warmup();
            chromeCustomTab.mayLaunch();

            progressBar = findViewById(R.id.loginProgress);

            loginButton = findViewById(R.id.loginButton);
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(chromeCustomTab.createCustomTab());
                }
            });
        }
    }

    @Override
    @WorkerThread
    protected void onResume() {
        super.onResume();

        final Uri uri = getIntent().getData();

        if (uri != null && uri.toString().startsWith(oAuth.getRedirectUri())) {

            code = uri.getQueryParameter("code");

            if (code != null) {

                loginButton.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (oAuth.accountSetup(code)) {
                                Intent intent = new Intent(MainActivity.this, TradeArea.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // clears all previous activities task
                                startActivity(intent);
                                finish(); // destroy current activity..
                            } else {
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "Login Failed. Please Try Again!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            } else {
                loginButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);

                String error = uri.getQueryParameter("error");

                if (error != null) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setTitle("Login Failed!");
                    alertDialogBuilder.setMessage("We need some permissions to request your data.");
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chromeCustomTab != null)
            chromeCustomTab.unbindCustomTabsService();
    }

}

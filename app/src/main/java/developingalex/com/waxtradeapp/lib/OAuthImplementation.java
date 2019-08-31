package developingalex.com.waxtradeapp.lib;

import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import developingalex.com.waxtradeapp.BuildConfig;
import developingalex.com.waxtradeapp.MainActivity;
import developingalex.com.waxtradeapp.interfaces.OAuthInterface;
import okhttp3.*;

import static android.content.Context.JOB_SCHEDULER_SERVICE;


public class OAuthImplementation implements OAuthInterface {

    private final static String REDIRECT_URI = "waxtradeapp://auth";
    private final static String URL_ACCESS_TOKEN = "https://oauth.opskins.com/v1/access_token";
    private final static String URL_GET_TRADE_URL = "https://api-trade.opskins.com/ITrade/GetTradeURL/v1/";
    private final static String URL_TEST_AUTH = "https://api.opskins.com/ITest/TestAuthed/v1/";
    private final static String URL_GET_PROFILE = "https://api.opskins.com/IUser/GetProfile/v1/";
    private final static String URL_REVOKE_TOKEN = "https://oauth.opskins.com/v1/revoke_token";

    private final OkHttpClient client = new OkHttpClient();
    private final SharedPreferences sharedPreferences;
    private final Context context;

    public OAuthImplementation(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
    }

    public boolean accountSetup(String code) throws Exception {

        final RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", code)
                .build();

        final Request request  = new Request.Builder()
                .header("Authorization", getAuthorization())
                .url(URL_ACCESS_TOKEN)
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IOException("[OAuthImplementation::AccountSetup] Unexpected code" + response);

            assert response.body() != null;

            try {
                final JSONObject jsonResponse = new JSONObject(response.body().string());

                if (jsonResponse.has("access_token") && jsonResponse.getString("token_type").equals("bearer"))
                    sharedPreferences.edit().putString("access_token", jsonResponse.getString("access_token")).apply();
                else
                    throw new IOException("[OAuthImplementation::AccountSetup] Token Error!");

                if (jsonResponse.has("refresh_token"))
                    sharedPreferences.edit().putString("refresh_token", jsonResponse.getString("refresh_token")).apply();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            setUserProfile();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }).start();

                return true;
            } catch (JSONException ex) {
                throw new IOException("[OAuthImplementation::AccountSetup]" + ex);
            }
        }
    }

    public boolean logout() {

        if (sharedPreferences.getString("refresh_token", "") != null) {

            final String refreshToken = sharedPreferences.getString("refresh_token", "");
            assert refreshToken != null;

            final RequestBody formBody = new FormBody.Builder()
                    .add("token_type", "refresh")
                    .add("token", refreshToken)
                    .build();

            final Request request = new Request.Builder()
                    .header("Authorization", getAuthorization())
                    .url(URL_REVOKE_TOKEN)
                    .post(formBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful())
                    throw new IOException("[OAuthImplementation::Logout] Unexpected code" + response);

                sharedPreferences.edit().remove("refresh_token").apply();
                sharedPreferences.edit().remove("access_token").apply();
                sharedPreferences.edit().remove("recentTradePartners").apply();

                final JobScheduler scheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
                scheduler.cancelAll();

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else
            return true;
    }

    public boolean checkAuthStatus() {
        return sharedPreferences.getString("access_token", null) != null;
    }

    public String getAuthURL() {
        return "https://oauth.opskins.com/v1/authorize?client_id="+ BuildConfig.oauth_clientId + "&state="+ getRandomNumber() +"&scope=identity+trades+items&response_type=code&duration=permanent";
    }

    public String getRedirectUri() {
        return REDIRECT_URI;
    }

    public String getUserID() {
        return String.valueOf(sharedPreferences.getInt("profile:user_id",0));
    }

    public String getUserProfileUsername() {
        return sharedPreferences.getString("profile:username", null);
    }

    public String getUserProfilePicture() {
        return sharedPreferences.getString("profile:avatar", null);
    }


    public String getUserTradeURL() throws Exception {

        final Request request  = new Request.Builder()
                .header("Authorization", getBearerToken())
                .url(URL_GET_TRADE_URL)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IOException("[OAuthImplementation::GetUserTradeURL] Unexpected code" + response);

            assert response.body() != null;
            final String myResponse = response.body().string();

            try {
                final JSONObject jsonResponse = new JSONObject(myResponse);
                final JSONObject urlResponse = (JSONObject) jsonResponse.get("response");
                return urlResponse.getString("short_url");

            } catch (JSONException ex) {
                throw new IOException("[OAuthImplementation::GetUserTradeURL] JSON Error");
            }
        }
    }


    String getBearerToken() throws Exception {

        final Request request  = new Request.Builder()
                .header("Authorization", "Bearer " + sharedPreferences.getString("access_token", null))
                .url(URL_TEST_AUTH)
                .build();

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                countDownLatch.countDown();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    assert response.body() != null;
                    final String myResponse = response.body().string();

                    if (response.code() == 401) {
                        final JSONObject jsonResponse = new JSONObject(myResponse);

                        if (jsonResponse.has("error")) {

                            switch (jsonResponse.getString("error")) {
                                case "invalid_token": {
                                    sharedPreferences.edit().remove("access_token").apply();

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {

                                            final String refreshToken = sharedPreferences.getString("refresh_token", "");
                                            assert refreshToken != null;

                                            final RequestBody formBody = new FormBody.Builder()
                                                    .add("grant_type", "refresh_token")
                                                    .add("refresh_token", refreshToken)
                                                    .build();

                                            String authorization = "";

                                            try {
                                                authorization = getAuthorization();
                                            } catch(Exception e) {
                                                e.printStackTrace();
                                            }

                                            final Request request  = new Request.Builder()
                                                    .header("Authorization", authorization)
                                                    .url(URL_ACCESS_TOKEN)
                                                    .post(formBody)
                                                    .build();

                                            final CountDownLatch countDownLatch2 = new CountDownLatch(1);

                                            client.newCall(request).enqueue(new Callback() {
                                                @Override
                                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                    e.printStackTrace();
                                                    countDownLatch2.countDown();
                                                }

                                                @Override
                                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                    assert response.body() != null;
                                                    final String myResponse = response.body().string();

                                                    if (response.isSuccessful()) {
                                                        try {
                                                            final JSONObject jsonResponse = new JSONObject(myResponse);
                                                            if (jsonResponse.getString("token_type").equals("bearer") && jsonResponse.has("access_token"))
                                                                sharedPreferences.edit().putString("access_token", jsonResponse.getString("access_token")).apply();
                                                            else
                                                                throw new IOException("Token Error!");

                                                            countDownLatch2.countDown();
                                                        } catch (JSONException ex) {
                                                            throw new IOException(ex);
                                                        }

                                                    } else {
                                                        sharedPreferences.edit().remove("refresh_token").apply();
                                                        sharedPreferences.edit().remove("access_token").apply();
                                                        sharedPreferences.edit().remove("recentTradePartners").apply();

                                                        final JobScheduler scheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
                                                        scheduler.cancelAll();

                                                        final Intent intent = new Intent(context, MainActivity.class);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        context.startActivity(intent);
                                                    }
                                                }
                                            });

                                            countDownLatch.countDown();
                                        }
                                    }).start();
                                    break;
                                }
                                case "invalid_grant": {
                                    Log.w("OAuthImplementation", jsonResponse.getString("error_description"));
                                    break;
                                }
                                default: { throw new IOException("[OAuthImplementation::GetUserTradeURL] Unknown OAuthImplementation Error"); }
                            }
                        }
                    } else countDownLatch.countDown();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        countDownLatch.await();
        return ("Bearer " + sharedPreferences.getString("access_token", null));
    }

    @WorkerThread
    private void setUserProfile() throws Exception {

        final Request request  = new Request.Builder()
                .header("Authorization", getBearerToken())
                .url(URL_GET_PROFILE)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new Exception("[OAuthImplementation::GetUserProfile] Unexpected code" + response);

            assert response.body() != null;

            try {
                final JSONObject jsonResponse = new JSONObject(response.body().string());
                final JSONObject profile_response = (JSONObject) jsonResponse.get("response");

                sharedPreferences.edit().putInt("profile:user_id", profile_response.getInt("id")).apply();
                sharedPreferences.edit().putString("profile:username", profile_response.getString("username")).apply();
                sharedPreferences.edit().putString("profile:avatar", profile_response.getString("avatar")).apply();
            } catch (JSONException ex) {
                throw new Exception("[OAuthImplementation::GetUserProfile] JSON Error");
            }
        }
    }

    private String getAuthorization() {
        // ENCODE oauth_clientId and oauth_clientSecret to Base64
        final String authToken = BuildConfig.oauth_clientId + ":" + BuildConfig.oauth_clientSecret;
        final byte[] data = authToken.getBytes(StandardCharsets.UTF_8);
        final String e_opskins_api_key = Base64.encodeToString(data, Base64.DEFAULT);

        return ("Basic " + e_opskins_api_key).trim();
    }

    private Integer getRandomNumber() {
        final Random random = new Random();
        return 100000 + random.nextInt(900000);
    }
}

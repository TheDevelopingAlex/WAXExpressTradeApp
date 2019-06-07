package developingalex.com.waxtradeapp.lib;

import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
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
import okhttp3.*;

import static android.content.Context.JOB_SCHEDULER_SERVICE;


public class OAuth {

    private final OkHttpClient client = new OkHttpClient();
    private final static String redirectUri = "waxtradeapp://auth";

    private final SharedPreferences sharedPreferences;
    private final Context mContext;

    public OAuth(Context context) {
        sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        mContext = context;
    }

    public String getURL() {
        return "https://oauth.opskins.com/v1/authorize?client_id="+ BuildConfig.oauth_clientId + "&state="+ getRandomNumber() +"&scope=identity+trades+items&response_type=code&duration=permanent";
    }

    public boolean checkAuthStatus() {
        return sharedPreferences.getString("access_token", null) != null;
    }

    public boolean accountSetup(String code) throws Exception {
        final String url = "https://oauth.opskins.com/v1/access_token";

        final RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", code)
                .build();

        final Request request  = new Request.Builder()
                .header("Authorization", getAuthorization())
                .url(url)
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("[OAuth::AccountSetup] Unexpected code" + response);

            assert response.body() != null;

            try {
                JSONObject jsonResponse = new JSONObject(response.body().string());

                if (jsonResponse.getString("token_type").equals("bearer") && jsonResponse.has("access_token"))
                    sharedPreferences.edit().putString("access_token", jsonResponse.getString("access_token")).apply();
                else
                    throw new IOException("[OAuth::AccountSetup] Token Error!");

                if (jsonResponse.has("refresh_token"))
                    sharedPreferences.edit().putString("refresh_token", jsonResponse.getString("refresh_token")).apply();

                return getUserProfile();
            } catch (JSONException ex) {
                throw new IOException("[OAuth::AccountSetup]" + ex);
            }
        }
    }

    private boolean getUserProfile() throws Exception {
        final String url = "https://api.opskins.com/IUser/GetProfile/v1/";

        final Request request  = new Request.Builder()
                .header("Authorization", getBearerToken())
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("[OAuth::GetUserProfile] Unexpected code" + response);

            assert response.body() != null;

            try {
                JSONObject jsonResponse = new JSONObject(response.body().string());
                JSONObject profile_response = (JSONObject) jsonResponse.get("response");

                sharedPreferences.edit().putInt("profile:user_id", profile_response.getInt("id")).apply();
                sharedPreferences.edit().putString("profile:username", profile_response.getString("username")).apply();
                sharedPreferences.edit().putString("profile:avatar", profile_response.getString("avatar")).apply();

                return true;
            } catch (JSONException ex) {
                throw new IOException("[OAuth::GetUserProfile] JSON Error");
            }
        }

    }


    public String getUserProfilePicture() {
        return sharedPreferences.getString("profile:avatar", null);
    }

    public String getUserProfileUsername() {
        return sharedPreferences.getString("profile:username", null);
    }

    public String getUserID() {
        return String.valueOf(sharedPreferences.getInt("profile:user_id",0));
    }

    public String getUserTradeURL() throws Exception {
        final String url = "https://api-trade.opskins.com/ITrade/GetTradeURL/v1/";

        final Request request  = new Request.Builder()
                .header("Authorization", getBearerToken())
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("[OAuth::GetUserTradeURL] Unexpected code" + response);

            assert response.body() != null;
            final String myResponse = response.body().string();

            try {
                JSONObject jsonResponse = new JSONObject(myResponse);
                JSONObject urlResponse = (JSONObject) jsonResponse.get("response");
                return urlResponse.getString("short_url");

            } catch (JSONException ex) {
                throw new IOException("[OAuth::GetUserTradeURL] JSON Error");
            }
        }

    }

    String getBearerToken() throws Exception {
        final String url = "https://api.opskins.com/ITest/TestAuthed/v1/";
        final Request request  = new Request.Builder()
                .header("Authorization", "Bearer " + sharedPreferences.getString("access_token", null))
                .url(url)
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
                    String myResponse = response.body().string();

                    if (response.code() == 401) {
                        JSONObject jsonResponse = new JSONObject(myResponse);

                        if (jsonResponse.has("error")) {
                            switch (jsonResponse.getString("error")) {
                                case "invalid_token": {
                                    sharedPreferences.edit().remove("access_token").apply();
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            String url = "https://oauth.opskins.com/v1/access_token";

                                            RequestBody formBody = new FormBody.Builder()
                                                    .add("grant_type", "refresh_token")
                                                    .add("refresh_token", sharedPreferences.getString("refresh_token", ""))
                                                    .build();

                                            String authorization = "";

                                            try {
                                                authorization = getAuthorization();
                                            } catch(Exception e) {
                                                e.printStackTrace();
                                            }

                                            Request request  = new Request.Builder()
                                                    .header("Authorization", authorization)
                                                    .url(url)
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
                                                    String myResponse = response.body().string();

                                                    if (response.isSuccessful()) {
                                                        try {
                                                            JSONObject jsonResponse = new JSONObject(myResponse);
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

                                                        JobScheduler scheduler = (JobScheduler) mContext.getSystemService(JOB_SCHEDULER_SERVICE);
                                                        scheduler.cancelAll();

                                                        Intent intent = new Intent(mContext, MainActivity.class);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        mContext.startActivity(intent);
                                                    }
                                                }
                                            });

                                            countDownLatch.countDown();
                                        }
                                    }).start();
                                    break;
                                }
                                case "invalid_grant": {
                                    Log.w("OAuth", jsonResponse.getString("error_description"));
                                    break;
                                }
                                default: { throw new IOException("[OAuth::GetUserTradeURL] Unknown OAuth Error"); }
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

    public String getRedirectUri() {
        return redirectUri;
    }

    public boolean logout() {

        if (sharedPreferences.getString("refresh_token", "") != null) {

            final String url = "https://oauth.opskins.com/v1/revoke_token";

            final RequestBody formBody = new FormBody.Builder()
                    .add("token_type", "refresh")
                    .add("token", sharedPreferences.getString("refresh_token", ""))
                    .build();

            final Request request = new Request.Builder()
                    .header("Authorization", getAuthorization())
                    .url(url)
                    .post(formBody)
                    .build();


            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("[OAuth::Logout] Unexpected code" + response);

                sharedPreferences.edit().remove("refresh_token").apply();
                sharedPreferences.edit().remove("access_token").apply();
                sharedPreferences.edit().remove("recentTradePartners").apply();

                JobScheduler scheduler = (JobScheduler) mContext.getSystemService(JOB_SCHEDULER_SERVICE);
                scheduler.cancelAll();

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else
            return true;
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

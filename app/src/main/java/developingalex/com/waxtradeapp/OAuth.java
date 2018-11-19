package developingalex.com.waxtradeapp;

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
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.JOB_SCHEDULER_SERVICE;


public class OAuth {

    private OkHttpClient client = new OkHttpClient();
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context mContext;

    private static String redirectUri = "waxtradeapp://auth";

    private static boolean ok = false;
    private static String tradeURL = null;

    public OAuth(Context context) {
        sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        mContext = context;
        editor = sharedPreferences.edit();
    }


    public String getURL() {
        return "https://oauth.opskins.com/v1/authorize?client_id="+ BuildConfig.oauth_clientId + "&state="+ getRandomNumber() +"&scope=identity+trades+items&response_type=code&duration=permanent";
    }

    public boolean checkAuthStatus() {
        return sharedPreferences.getString("access_token", null) != null;
    }

    public boolean accountSetup(String code, String state) throws Exception {
        String url = "https://oauth.opskins.com/v1/access_token";

        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", code)
                .build();

        Request request  = new Request.Builder()
                .header("Authorization", getAuthorization())
                .url(url)
                .post(formBody)
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
                assert response.body() != null;
                String myResponse = response.body().string();

                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonResponse = new JSONObject(myResponse);
                        if (jsonResponse.getString("token_type").equals("bearer") && jsonResponse.has("access_token")) {
                            editor.putString("access_token", jsonResponse.getString("access_token"));
                            editor.commit();
                        } else
                            throw new IOException("Token Error!");

                        if (jsonResponse.has("refresh_token")) {
                            editor.putString("refresh_token", jsonResponse.getString("refresh_token"));
                            editor.commit();
                        }

                        countDownLatch.countDown();
                    } catch (JSONException ex) {
                        throw new IOException(ex);
                    }

                } else {
                    countDownLatch.countDown();
                    throw new IOException("Unexpected Error, Please Try Again!");
                }
            }
        });

        countDownLatch.await();
        return getUserProfile();
    }

    private boolean getUserProfile() throws Exception {
        String url = "https://api.opskins.com/IUser/GetProfile/v1/";

        Request request  = new Request.Builder()
                .header("Authorization", getBearerToken())
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
                assert response.body() != null;
                String myResponse = response.body().string();

                try {
                    if (response.code() == 200) {
                        JSONObject jsonResponse = new JSONObject(myResponse);
                        JSONObject profile_response = (JSONObject) jsonResponse.get("response");

                        editor.putInt("profile:user_id", profile_response.getInt("id"));
                        editor.putString("profile:username", profile_response.getString("username"));
                        editor.putString("profile:avatar", profile_response.getString("avatar"));
                        editor.commit();

                        ok  = true;
                    }
                    countDownLatch.countDown();
                } catch (JSONException ex) {
                    throw new IOException("JSON Error");
                }

            }
        });

        countDownLatch.await();
        return ok;
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
        String url = "https://api-trade.opskins.com/ITrade/GetTradeURL/v1/";

        Request request  = new Request.Builder()
                .header("Authorization", getBearerToken())
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
                assert response.body() != null;
                String myResponse = response.body().string();

                try {
                    if (response.code() == 200) {
                        JSONObject jsonResponse = new JSONObject(myResponse);
                        JSONObject urlResponse = (JSONObject) jsonResponse.get("response");
                        tradeURL = urlResponse.getString("short_url");
                    }
                    countDownLatch.countDown();
                } catch (JSONException ex) {
                    throw new IOException("JSON Error");
                }

            }
        });

        countDownLatch.await();
        return tradeURL;
    }

    public String getBearerToken() throws Exception {
        String url = "https://api.opskins.com/ITest/TestAuthed/v1/";
        Request request  = new Request.Builder()
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
                                    editor.remove("access_token").commit();
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
                                                            if (jsonResponse.getString("token_type").equals("bearer") && jsonResponse.has("access_token")) {
                                                                editor.putString("access_token", jsonResponse.getString("access_token"));
                                                                editor.commit();
                                                            } else
                                                                throw new IOException("Token Error!");

                                                            countDownLatch2.countDown();
                                                        } catch (JSONException ex) {
                                                            throw new IOException(ex);
                                                        }

                                                    } else {
                                                        editor.remove("refresh_token").commit();
                                                        editor.remove("access_token").commit();
                                                        editor.remove("recentTradePartners").commit();
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
                                default: { throw new IOException("Unknown OAuth Error"); }
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

    public boolean logout() throws Exception {

        if (sharedPreferences.getString("refresh_token", null) != null) {

            String url = "https://oauth.opskins.com/v1/revoke_token";

            RequestBody formBody = new FormBody.Builder()
                    .add("token_type", "refresh")
                    .add("token", sharedPreferences.getString("refresh_token", ""))
                    .build();

            Request request  = new Request.Builder()
                    .header("Authorization", getAuthorization())
                    .url(url)
                    .post(formBody)
                    .build();

            final CountDownLatch countDownLatch = new CountDownLatch(1);

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                    countDownLatch.countDown();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    editor.remove("refresh_token").commit();
                    editor.remove("access_token").commit();
                    editor.remove("recentTradePartners").commit();
                    JobScheduler scheduler = (JobScheduler) mContext.getSystemService(JOB_SCHEDULER_SERVICE);
                    scheduler.cancelAll();
                    ok  = true;
                    countDownLatch.countDown();
                }
            });

            countDownLatch.await();
        }

        return ok;

    }

    private String getAuthorization() throws Exception {
        // ENCODE oauth_clientId and oauth_clientSecret to Base64
        String authToken = BuildConfig.oauth_clientId + ":" + BuildConfig.oauth_clientSecret;
        byte[] data = authToken.getBytes("UTF-8");
        String e_opskins_api_key = Base64.encodeToString(data, Base64.DEFAULT);

        return ("Basic " + e_opskins_api_key).trim();
    }

    private Integer getRandomNumber() {
        final Random random = new Random();
        return 100000 + random.nextInt(900000);
    }
}

package developingalex.com.waxtradeapp.lib;

import android.content.Context;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import developingalex.com.waxtradeapp.interfaces.TradeInterface;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TradeImplementation implements TradeInterface {

    private final OAuthImplementation oAuthImplementation;
    private final OkHttpClient client = new OkHttpClient();

    private JSONObject offers;
    private JSONObject offer = null;
    private JSONObject apps = null;
    private static boolean ok = false;
    private static String okStatus = "Default Error!";

    private String error_message = "Please Try Again!";

    public TradeImplementation(Context context) {
        oAuthImplementation = new OAuthImplementation(context);
    }

    public JSONObject getOffers(String type, String state, String sort, int page, int per_page) throws Exception {

        final String url = "https://api-trade.opskins.com/ITrade/GetOffers/v1?&state=" + state + "&type=" + type + "&sort=" + sort + "&page=" + page + "&per_page=" + per_page;
        final Request request = new Request.Builder()
                .header("Authorization", oAuthImplementation.getBearerToken())
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
                final String myResponse = response.body().string();

                try {
                    if (response.code() == 200) {
                        final JSONObject jsonResponse = new JSONObject(myResponse);
                        offers = (JSONObject) jsonResponse.get("response");
                    }
                    countDownLatch.countDown();
                } catch (JSONException ex) {
                    throw new IOException("JSON Error");
                }

            }
        });

        countDownLatch.await();
        return offers;
    }


    public JSONObject getOffer(Integer offer_id) throws Exception {

        final String url = "https://api-trade.opskins.com/ITrade/GetOffer/v1?offer_id=" +offer_id.toString();
        final Request request  = new Request.Builder()
                .header("Authorization", oAuthImplementation.getBearerToken())
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
                final String myResponse = response.body().string();

                try {
                    if (response.code() == 200) {
                        final JSONObject jsonResponse = new JSONObject(myResponse);
                        offer = (JSONObject) jsonResponse.get("response");
                    }
                    countDownLatch.countDown();
                } catch (JSONException ex) {
                    throw new IOException("JSON Error");
                }
            }
        });

        countDownLatch.await();
        return offer;
    }

    public boolean acceptOffer(Integer offer_id, String twofactor_code) throws Exception {

        final String url = "https://api-trade.opskins.com/ITrade/AcceptOffer/v1/";
        final RequestBody formBody = new FormBody.Builder()
                .add("twofactor_code", twofactor_code)
                .add("offer_id", offer_id.toString())
                .build();
        final Request request  = new Request.Builder()
                .header("Authorization", oAuthImplementation.getBearerToken())
                .url(url)
                .post(formBody)
                .build();
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                error_message = e.toString();
                e.printStackTrace();
                countDownLatch.countDown();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                assert response.body() != null;
                final String myResponse = response.body().string();

                try {
                    if (response.code() == 200) {
                        final JSONObject jsonResponse = new JSONObject(myResponse);
                        if (jsonResponse.getInt("status") == 122) {
                            error_message = jsonResponse.getString("message");
                            ok = false;
                        } else
                            ok  = true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                countDownLatch.countDown();
            }
        });

        countDownLatch.await();
        return ok;
    }

    public boolean cancelOffer(Integer offer_id) throws Exception {

        final String url = "https://api-trade.opskins.com/ITrade/CancelOffer/v1/";
        final RequestBody formBody = new FormBody.Builder()
                .add("offer_id", offer_id.toString())
                .build();
        final Request request  = new Request.Builder()
                .header("Authorization", oAuthImplementation.getBearerToken())
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
                if (response.code() == 200)
                    ok  = true;
                countDownLatch.countDown();
            }
        });

        countDownLatch.await();
        return ok;
    }

    public String sendOffer(String trade_url, String twofactor_code, String items_to_send, String items_to_receive, String message) throws Exception {

        final String url = "https://api-trade.opskins.com/ITrade/SendOffer/v1/";
        final RequestBody formBody = new FormBody.Builder()
                .add("trade_url", trade_url)
                .add("twofactor_code", twofactor_code)
                .add("items_to_send", items_to_send)
                .add("items_to_receive", items_to_receive)
                .add("message", message)
                .build();
        final Request request  = new Request.Builder()
                .header("Authorization", oAuthImplementation.getBearerToken())
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
                final String myResponse = response.body().string();

                try {
                    if (response.code() == 200) {
                        final JSONObject jsonResponse = new JSONObject(myResponse);
                        okStatus = jsonResponse.getInt("status") == 122 ? jsonResponse.getString("message") : "Offer Sent!";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                countDownLatch.countDown();
            }
        });

        countDownLatch.await();
        return okStatus;
    }

    public JSONObject getApps() throws Exception {

        final String url = "https://api-trade.opskins.com/ITrade/GetApps/v1/";
        final Request request  = new Request.Builder().url(url).build();
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
                final String myResponse = response.body().string();

                try {
                    if (response.code() == 200) {
                        final JSONObject jsonResponse = new JSONObject(myResponse);
                        apps = (JSONObject) jsonResponse.get("response");
                    }
                    countDownLatch.countDown();
                } catch (JSONException ex) {
                    throw new IOException("JSON Error");
                }
            }
        });

        countDownLatch.await();
        return apps;
    }

    public JSONObject getUserInventory(String user_id, Integer app_id) throws Exception {

        final String url = "https://api-trade.opskins.com/ITrade/GetUserInventory/v1?uid=" + user_id + "&app_id=" + app_id;
        final Request request  = new Request.Builder().url(url).build();
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
                final String myResponse = response.body().string();

                try {
                    if (response.code() == 200) {
                        final JSONObject jsonResponse = new JSONObject(myResponse);
                        apps = (JSONObject) jsonResponse.get("response");
                    }
                    countDownLatch.countDown();
                } catch (JSONException ex) {
                    throw new IOException("JSON Error");
                }
            }
        });

        countDownLatch.await();
        return apps;
    }

    public String getErrorMessage() {
        return error_message;
    }

}

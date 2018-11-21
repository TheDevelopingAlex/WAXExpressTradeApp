package developingalex.com.waxtradeapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;

public class myJobService extends JobService {

    private static final String CHANNEL_ID = "WAXTradeNotification";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    public boolean onStartJob(final JobParameters params) {
        sharedPreferences = myJobService.this.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        Log.w("myJobScheduler", "Job started");

        new myJobService.LongOperation(myJobService.this, new myJobService.OnEventListener() {
            @Override
            public void onSuccess() {
                editor.remove("lastCalled").apply();
                editor.putInt("lastCalled", (int) (System.currentTimeMillis() / 1000)).apply();
                jobFinished(params, false);
            }

            @Override
            public void onFailure() {
                editor.remove("lastCalled").apply();
                editor.putInt("lastCalled", (int) (System.currentTimeMillis() / 1000)).apply();
                jobFinished(params, false);
            }
        }).execute();

        return true;
    }

    public void doNotify(int id, String title, String text, int offerId, boolean hideAccept) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name ="Notification for new offers";
            String description = "You will receive a notification if you have new pending offers";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(this, OfferDetail.class);

        Bundle b = new Bundle();
        b.putInt("offerId", offerId); // Parameter for new Activity
        b.putBoolean("hideAccept", hideAccept);
        notificationIntent.putExtras(b);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, id, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.wax)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(id, mBuilder.build());
    }


    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }


    public interface OnEventListener {
        void onSuccess();
        void onFailure();
    }


    private static class LongOperation extends AsyncTask<String, Void, JSONArray> {

        private WeakReference<myJobService> activityWeakReference;
        private TradeInterface tradeInterface;
        private myJobService.OnEventListener mCallBack;

        LongOperation(myJobService activity, myJobService.OnEventListener callback) {
            activityWeakReference = new WeakReference<>(activity);
            tradeInterface = new TradeInterface(activity);
            mCallBack = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        private JSONArray concatArray(JSONArray... arrs)
                throws JSONException {
            JSONArray result = new JSONArray();
            for (JSONArray arr : arrs) {
                for (int i = 0; i < arr.length(); i++) {
                    result.put(arr.get(i));
                }
            }
            return result;
        }

        @Override
        protected JSONArray doInBackground(String... params) {
            JSONArray temp = new JSONArray();

            try {
                JSONObject offers_received = tradeInterface.getOffers("received", "2,6", "modified");
                JSONObject offer_sent = tradeInterface.getOffers("sent", "3,7", "modified");

                if (offer_sent != null && offers_received != null)
                    temp = concatArray(offers_received.getJSONArray("offers"), offer_sent.getJSONArray("offers"));
                else if (offers_received == null && offer_sent != null)
                    temp = offer_sent.getJSONArray("offers");
                else if (offers_received != null)
                    temp = offers_received.getJSONArray("offers");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return temp;
        }

        @Override
        protected void onPostExecute(JSONArray offers) {
            myJobService activity = activityWeakReference.get();
            DecimalFormat price_format = new DecimalFormat("0.00");

            if (offers != null) {

                for (int index = 0; index < offers.length(); index++) {

                    try {
                        JSONObject jsonObject = offers.getJSONObject(index);
                        int timeModified = jsonObject.getInt("time_updated");

                        if (timeModified >= activity.sharedPreferences.getInt("lastCalled", (int) (System.currentTimeMillis() / 1000))) {

                            switch(jsonObject.getInt("state")) {
                                case 2: {
                                    JSONObject recipient = (JSONObject) jsonObject.get("recipient");
                                    JSONArray recipient_items = (JSONArray) recipient.get("items");
                                    JSONObject sender = (JSONObject) jsonObject.get("sender");
                                    JSONArray sender_items = (JSONArray) sender.get("items");

                                    double sender_items_price = 0.00;
                                    double recipient_items_price = 0.00;

                                    for (int j = 0; j < sender_items.length(); j++) {
                                        JSONObject sender_item = sender_items.getJSONObject(j);
                                        sender_items_price += ((double) sender_item.getInt("suggested_price") / 100);
                                    }

                                    for (int j = 0; j < recipient_items.length(); j++) {
                                        JSONObject recipient_item = recipient_items.getJSONObject(j);
                                        recipient_items_price += ((double) recipient_item.getInt("suggested_price") / 100);
                                    }

                                    activity.doNotify(timeModified,"You have received a new offer",sender.getString("display_name") + " offers "+ price_format.format(sender_items_price) +"$ for your "+ price_format.format(recipient_items_price) + "$", jsonObject.getInt("id"), false);

                                    break;
                                }

                                case 3: {
                                    JSONObject recipient = (JSONObject) jsonObject.get("recipient");
                                    activity.doNotify(timeModified,"Offer #" + jsonObject.getInt("id"),recipient.getString("display_name") + " has ACCEPTED the offer", jsonObject.getInt("id"), true);
                                    break;
                                }

                                case 6: {
                                    JSONObject sender = (JSONObject) jsonObject.get("sender");
                                    activity.doNotify(timeModified,"Offer #" + jsonObject.getInt("id"),sender.getString("display_name") + " has CANCELED the offer", jsonObject.getInt("id"), true);
                                    break;
                                }

                                case 7: {
                                    JSONObject recipient = (JSONObject) jsonObject.get("recipient");
                                    activity.doNotify(timeModified,"Offer #" + jsonObject.getInt("id"),recipient.getString("display_name") + " has DECLINED the offer", jsonObject.getInt("id"), true);
                                    break;
                                }
                            }

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                mCallBack.onSuccess();
            } else
                mCallBack.onFailure();
        }

    }


}

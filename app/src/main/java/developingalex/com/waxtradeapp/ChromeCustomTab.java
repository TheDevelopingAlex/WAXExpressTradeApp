package developingalex.com.waxtradeapp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.util.Log;


public class ChromeCustomTab {

    public static final String CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome";

    private Activity activity;
    private String url;

    private CustomTabsSession customTabsSession;
    private CustomTabsClient client;
    private CustomTabsServiceConnection connection;

    private boolean warmupWhenReady = false;
    private boolean mayLaunchWhenReady = false;

    public ChromeCustomTab(Activity activity, String url) {
        this.activity = activity;
        this.url = url;
        bindCustomTabsService();
    }

    public void warmup() {
        if (client != null) {
            warmupWhenReady = false;
            client.warmup(0);
        }
        else {
            warmupWhenReady = true;
        }
    }

    public void mayLaunch() {
        CustomTabsSession session = getSession();
        if (client != null) {
            mayLaunchWhenReady = false;
            session.mayLaunchUrl(Uri.parse(url), null, null);
        }
        else {
            mayLaunchWhenReady = true;
        }
    }

    public Intent createCustomTab() {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(getSession());
        builder.setToolbarColor(-12303292).setShowTitle(true);
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.intent.setData(Uri.parse(url));
        return customTabsIntent.intent;
    }

    private CustomTabsSession getSession() {
        if (client == null) {
            customTabsSession = null;
        } else if (customTabsSession == null) {
            customTabsSession = client.newSession(new CustomTabsCallback() {
                @Override
                public void onNavigationEvent(int navigationEvent, Bundle extras) {
                    Log.w("CustomTabs", "onNavigationEvent: Code = " + navigationEvent);
                }
            });
        }
        return customTabsSession;
    }

    private void bindCustomTabsService() {
        if (client != null) {
            return;
        }
        connection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName name, CustomTabsClient client) {
                ChromeCustomTab.this.client = client;

                if (warmupWhenReady) {
                    warmup();
                }

                if (mayLaunchWhenReady) {
                    mayLaunch();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                client = null;
            }
        };
        boolean ok = CustomTabsClient.bindCustomTabsService(activity, CUSTOM_TAB_PACKAGE_NAME, connection);
        if (!ok) {
            connection = null;
        }
    }

    public void unbindCustomTabsService() {
        if (connection == null) {
            return;
        }
        activity.unbindService(connection);
        client = null;
        customTabsSession = null;
    }

}

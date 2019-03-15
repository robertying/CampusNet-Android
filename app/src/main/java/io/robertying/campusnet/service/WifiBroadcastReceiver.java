package io.robertying.campusnet.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import io.robertying.campusnet.helper.CredentialHelper;
import io.robertying.campusnet.helper.TunetHelper;
import io.robertying.campusnet.helper.TunetHelper.ResponseType;
import io.robertying.campusnet.helper.UseregHelper;

public class WifiBroadcastReceiver extends BroadcastReceiver {

    private static String lastTimeConnectedSsid;
    private static boolean lastTimeConnected = false;
    public final String CLASS_NAME = getClass().getSimpleName();

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        final String action = intent.getAction();

        if (action != null && action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

            if (info != null && info.isConnected()) {
                WifiManager wifiManager = (WifiManager)
                        context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                String ssid = wifiManager.getConnectionInfo().getSSID();

                if (!ssid.equals(lastTimeConnectedSsid) || !lastTimeConnected) {
                    Log.i(CLASS_NAME, "Connected to Wi-Fi " + ssid);
                    lastTimeConnectedSsid = ssid;
                    final String[] credentials = CredentialHelper.getCredentials(context);

                    if (ssid.equals("\"Tsinghua-5G\"") || ssid.equals("\"Tsinghua\"")) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                new NetLoginTask().execute(credentials);
                            }
                        }, 1000);

                        Log.i(CLASS_NAME, "NetLoginTask started for " + ssid);
                    }
                    if (ssid.equals("\"Tsinghua-IPv4\"")) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                new AuthLoginTask().execute(credentials);
                            }
                        }, 1000);

                        Log.i(CLASS_NAME, "AuthLoginTask started for " + ssid);
                    }
                }

                lastTimeConnected = true;
            } else {
                lastTimeConnectedSsid = "";
                lastTimeConnected = false;
            }
        }
    }

    private class NetLoginTask extends AsyncTask<String, Void, ResponseType> {
        @Override
        protected ResponseType doInBackground(String... credentials) {
            String username = credentials[0];
            String password = credentials[1];
            ResponseType response;

            if (username == null || password == null) {
                response = ResponseType.WRONG_CREDENTIAL;
            } else {
                response = TunetHelper.netLogin(username, password);
            }
            Log.i(CLASS_NAME, response.toString());
            return response;
        }
    }

    private class AuthLoginTask extends AsyncTask<String, Void, ResponseType> {
        @Override
        protected ResponseType doInBackground(String... credentials) {
            String username = credentials[0];
            String password = credentials[1];
            ResponseType response;

            if (username == null || password == null) {
                response = ResponseType.WRONG_CREDENTIAL;
            } else {
                response = UseregHelper.useregLogin(username, password);
            }
            Log.i(CLASS_NAME, response.toString());
            return response;
        }
    }
}

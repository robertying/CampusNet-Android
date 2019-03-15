package io.robertying.campusnet.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import io.robertying.campusnet.R;
import io.robertying.campusnet.activity.MainActivity;

public class AutoLoginService extends Service {

    public static final int AUTO_LOGIN_SERVICE_CHANNEL_SERVICE_ID = 101;
    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";
    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";
    public final String CLASS_NAME = getClass().getSimpleName();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();

            if (action != null) {
                switch (action) {
                    case ACTION_START_FOREGROUND_SERVICE:
                        startForegroundService();
                        break;
                    case ACTION_STOP_FOREGROUND_SERVICE:
                        stopForegroundService();
                        break;
                }
            }
        }

        return START_STICKY;
    }

    private void startForegroundService() {
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this,
                        0,
                        new Intent(this, MainActivity.class),
                        0);

        Notification notification =
                new NotificationCompat.Builder(this, MainActivity.AUTO_LOGIN_SERVICE_CHANNEL_ID)
                        .setContentTitle(getString(R.string.auto_login_notification_title))
                        .setContentText(getString(R.string.auto_login_notification_text))
                        .setContentIntent(pendingIntent)
                        .build();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(new WifiBroadcastReceiver(), intentFilter);

        startForeground(AUTO_LOGIN_SERVICE_CHANNEL_SERVICE_ID, notification);
        Log.i(CLASS_NAME, "Auto Login foreground service started");
    }

    private void stopForegroundService() {
        stopForeground(true);
        Log.i(CLASS_NAME, "Auto Login foreground service stopped");
    }
}

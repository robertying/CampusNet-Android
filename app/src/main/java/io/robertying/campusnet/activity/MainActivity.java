package io.robertying.campusnet.activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import io.fabric.sdk.android.Fabric;
import io.robertying.campusnet.R;
import io.robertying.campusnet.fragment.HomeFragment;
import io.robertying.campusnet.fragment.SettingsFragment;
import io.robertying.campusnet.helper.CAHelper;
import io.robertying.campusnet.helper.CredentialHelper;
import io.robertying.campusnet.service.AutoLoginService;
import io.robertying.campusnet.service.CAUpdateJobService;

public class MainActivity extends AppCompatActivity {

    public static final String AUTO_LOGIN_SERVICE_CHANNEL_ID = "AUTO_LOGIN_SERVICE_CHANNEL";
    public static final String APP_CHANNEL_ID = "APP_CHANNEL";
    public final String CLASS_NAME = getClass().getSimpleName();

    private final Fragment homeFragment = new HomeFragment();
    private final Fragment settingsFragment = new SettingsFragment();

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    switchFragment("Home");
                    return true;
                case R.id.navigation_settings:
                    switchFragment("Settings");
                    return true;
            }
            return false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fabric.with(this, new Crashlytics());

        firstRunConfigure();

        setContentView(R.layout.activity_main);
        initFragments();
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        setJobScheduler();
        setAutoLoginService();
    }

    private void firstRunConfigure() {
        boolean isFirstRun = getSharedPreferences("AppInfo", MODE_PRIVATE)
                .getBoolean("isFirstRun", true);
        String[] credentials = CredentialHelper.getCredentials(this);

        if (isFirstRun || credentials[0] == null || credentials[1] == null) {
            Log.i(CLASS_NAME, "App first run");

            CAHelper.copyFromAssetsToInternalStorage(this);

            createNotificationChannel();

            getSharedPreferences("AppInfo", MODE_PRIVATE)
                    .edit()
                    .putBoolean("isFirstRun", false)
                    .apply();

            startFirstRunActivity();
        }
    }

    private void startFirstRunActivity() {
        Log.i(CLASS_NAME, "Starting FirstRunActivity");

        Intent intent = new Intent(this, FirstRunActivity.class);
        startActivity(intent);
        finish();
    }

    private void initFragments() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, settingsFragment).hide(settingsFragment);
        fragmentTransaction.add(R.id.fragment_container, homeFragment);
        fragmentTransaction.commit();
    }

    private void switchFragment(String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (tag.equals("Settings")) {
            fragmentTransaction.hide(homeFragment).show(settingsFragment);
        } else {
            fragmentTransaction.hide(settingsFragment).show(homeFragment);
        }

        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
    }

    private void setJobScheduler() {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

        boolean isScheduled = false;
        for (JobInfo jobInfo : jobScheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == CAUpdateJobService.JOB_ID) {
                isScheduled = true;
                break;
            }
        }

        if (!isScheduled) {
            jobScheduler.schedule(new JobInfo.Builder(CAUpdateJobService.JOB_ID,
                    new ComponentName(this, CAUpdateJobService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPeriodic(7 * 24 * 60 * 60 * 1000)
                    .build());
            Log.i(CLASS_NAME, "Scheduled CAUpdateJobService");
        }
    }

    private void setAutoLoginService() {
        boolean autoLoginEnabled = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("AutoLogin", false);

        if (autoLoginEnabled) {
            Intent autoLoginServiceIntent = new Intent(this, AutoLoginService.class);
            autoLoginServiceIntent.setAction(AutoLoginService.ACTION_START_FOREGROUND_SERVICE);
            startService(autoLoginServiceIntent);
            Log.i(CLASS_NAME, "Started AutoLoginService");
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            CharSequence name = getString(R.string.service_channel_name);
            String description = getString(R.string.service_channel_description);
            int importance = NotificationManager.IMPORTANCE_NONE;
            NotificationChannel channel =
                    new NotificationChannel(AUTO_LOGIN_SERVICE_CHANNEL_ID,
                            name,
                            importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            name = getString(R.string.app_channel_name);
            description = getString(R.string.app_channel_description);
            importance = NotificationManager.IMPORTANCE_DEFAULT;
            channel =
                    new NotificationChannel(APP_CHANNEL_ID,
                            name,
                            importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);

            Log.i(CLASS_NAME, "Created notification channels");
        }
    }
}

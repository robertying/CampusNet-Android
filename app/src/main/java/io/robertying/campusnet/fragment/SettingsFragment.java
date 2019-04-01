package io.robertying.campusnet.fragment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.LinearLayout;

import com.github.javiersantos.appupdater.AppUpdaterUtils;
import com.github.javiersantos.appupdater.enums.AppUpdaterError;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.github.javiersantos.appupdater.objects.Update;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import io.robertying.campusnet.BuildConfig;
import io.robertying.campusnet.R;
import io.robertying.campusnet.activity.FirstRunActivity;
import io.robertying.campusnet.activity.MainActivity;
import io.robertying.campusnet.custom.MySnackbar;
import io.robertying.campusnet.helper.CredentialHelper;
import io.robertying.campusnet.service.AutoLoginService;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 200;
    private final String CLASS_NAME = getClass().getSimpleName();

    @Nullable
    private SwitchPreferenceCompat autoLoginSwitchPreference;
    private Preference checkUpdatePreference;
    private BottomNavigationView navigationView;
    @Nullable
    private FragmentActivity activity;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.init_preferences);

        activity = getActivity();
        if (activity.getClass().getSimpleName().equals("MainActivity"))
            addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        LinearLayout view = (LinearLayout) super.onCreateView(inflater, container, savedInstanceState);
        activity = getActivity();
        navigationView = activity.findViewById(R.id.navigation);

        if (view != null) {
            view.setBackgroundColor(getResources().getColor(android.R.color.white));
            if (activity.getClass().getSimpleName().equals("FirstRunActivity"))
                setDoneButton(view);

            autoLoginSwitchPreference = findPreference("AutoLogin");
            autoLoginSwitchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (!autoLoginSwitchPreference.isChecked() && !hasLocationPermission())
                        showLocationPermissionExplanation();
                    else if (!autoLoginSwitchPreference.isChecked())
                        startAutoLoginService();
                    else if (autoLoginSwitchPreference.isChecked())
                        stopAutoLoginService();

                    return true;
                }
            });

            if (activity.getClass().getSimpleName().equals("MainActivity")) {
                Preference logoutPreference = findPreference("Logout");
                logoutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        showLogoutConfirmation();
                        return true;
                    }
                });

                checkUpdatePreference = findPreference("CheckUpdate");
                int versionCode = BuildConfig.VERSION_CODE;
                String versionName = BuildConfig.VERSION_NAME;
                checkUpdatePreference.setSummary(String.format("%s %s (%d)",
                        getResources().getString(R.string.version_text),
                        versionName,
                        versionCode));
                checkUpdatePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        startCheckUpdate();
                        return true;
                    }
                });
            }
        }

        return view;
    }

    private void setDoneButton(@NonNull LinearLayout view) {
        final Context context = getActivity();
        if (context == null)
            return;

        MaterialButton doneButton = new MaterialButton(context);
        doneButton.setText(R.string.done_text);
        MarginLayoutParams doneButtonLayout = new MarginLayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        doneButtonLayout.bottomMargin = 88;
        doneButtonLayout.rightMargin = 48;
        doneButtonLayout.leftMargin = 48;
        doneButton.setLayoutParams(doneButtonLayout);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    Intent intent = new Intent(activity, MainActivity.class);
                    startActivity(intent);
                    activity.finish();
                }
            }
        });

        view.addView(doneButton);
    }

    private void startCheckUpdate() {
        final FragmentActivity activity = getActivity();
        if (activity != null) {
            MySnackbar.make(activity,
                    getView(),
                    navigationView,
                    getResources().getString(R.string.checking_update),
                    Snackbar.LENGTH_LONG)
                    .show();
            AppUpdaterUtils appUpdaterUtils = new AppUpdaterUtils(activity)
                    .setUpdateFrom(UpdateFrom.GITHUB)
                    .setGitHubUserAndRepo("robertying", "CampusNet-Android")
                    .withListener(new AppUpdaterUtils.UpdateListener() {
                        @Override
                        public void onSuccess(Update update, Boolean isUpdateAvailable) {
                            Log.i(CLASS_NAME, "Latest Version: " + update.getLatestVersion());

                            DialogFragment fragment = UpdateFragment.newInstance(isUpdateAvailable,
                                    update.getLatestVersion(),
                                    update.getUrlToDownload().toString());
                            fragment.show(getFragmentManager(), "CheckUpdate");
                        }

                        @Override
                        public void onFailed(AppUpdaterError error) {
                            Log.e(CLASS_NAME, error.toString());
                            MySnackbar.make(activity,
                                    getView(),
                                    navigationView,
                                    getResources().getString(R.string.checking_update_fail),
                                    Snackbar.LENGTH_LONG)
                                    .show();
                        }
                    });
            appUpdaterUtils.start();
        }
    }

    private void showLocationPermissionExplanation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(getResources().getString(R.string.request_location_permission_description))
                .setPositiveButton(getResources().getString(R.string.acknowledged_text),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                requestLocationPermission();
                            }
                        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        autoLoginSwitchPreference.setChecked(false);
                    }
                })
                .show();
    }

    private void showLogoutConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(getResources().getString(R.string.logout_confirmation))
                .setPositiveButton(getResources().getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                stopAutoLoginService();
                                CredentialHelper.removeCredentials(activity);

                                FragmentActivity activity = getActivity();
                                if (activity != null) {
                                    Intent intent = new Intent(activity, FirstRunActivity.class);
                                    startActivity(intent);
                                    activity.finish();
                                }
                            }
                        })
                .setNegativeButton(getResources().getString(R.string.no),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                .show();
    }

    private boolean hasLocationPermission() {
        boolean hasPermission = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        Log.i(CLASS_NAME,
                "Location permission " + (hasPermission ? "" : "not ") + "granted");
        return hasPermission;
    }

    private void requestLocationPermission() {
        Log.i(CLASS_NAME, "Requesting Location permission");

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.ACCESS_COARSE_LOCATION)) {
            showLocationPermissionExplanation();
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    autoLoginSwitchPreference.setChecked(true);
                    Log.i(CLASS_NAME, "Location permission granted");

                    startAutoLoginService();
                } else {
                    autoLoginSwitchPreference.setChecked(false);
                    Log.i(CLASS_NAME, "Location permission denied");
                }
            }
        }
    }

    private void startAutoLoginService() {
        Intent autoLoginServiceIntent = new Intent(activity, AutoLoginService.class);
        autoLoginServiceIntent.setAction(AutoLoginService.ACTION_START_FOREGROUND_SERVICE);
        activity.startService(autoLoginServiceIntent);
        Log.i(CLASS_NAME, "Started AutoLoginService");
    }

    private void stopAutoLoginService() {
        Intent autoLoginServiceIntent = new Intent(activity, AutoLoginService.class);
        autoLoginServiceIntent.setAction(AutoLoginService.ACTION_STOP_FOREGROUND_SERVICE);
        activity.startService(autoLoginServiceIntent);
        Log.i(CLASS_NAME, "Stopped AutoLoginService");
    }
}

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

import com.google.android.material.button.MaterialButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import io.robertying.campusnet.R;
import io.robertying.campusnet.activity.MainActivity;
import io.robertying.campusnet.service.AutoLoginService;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 200;
    private final String CLASS_NAME = getClass().getSimpleName();

    @Nullable
    private SwitchPreferenceCompat autoLoginSwitchPreference;
    @Nullable
    private FragmentActivity activity;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.init_preferences);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        LinearLayout view = (LinearLayout) super.onCreateView(inflater, container, savedInstanceState);
        activity = getActivity();

        if (view != null) {
            view.setBackgroundColor(getResources().getColor(android.R.color.white));
            if (activity.getClass().getSimpleName().equals("FirstRunActivity"))
                setDoneButton(view);

            autoLoginSwitchPreference = findPreference("AutoLogin");
            autoLoginSwitchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Log.i(CLASS_NAME,
                            "Location permission " + (hasLocationPermission() ? "" : "not ") + "granted");
                    if (!autoLoginSwitchPreference.isChecked() && !hasLocationPermission())
                        showLocationPermissionExplanation();

                    return true;
                }
            });
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
                .show();
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
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

                    Intent autoLoginServiceIntent = new Intent(activity, AutoLoginService.class);
                    autoLoginServiceIntent.setAction(AutoLoginService.ACTION_START_FOREGROUND_SERVICE);
                    activity.startService(autoLoginServiceIntent);
                    Log.i(CLASS_NAME, "Started AutoLoginService");
                } else {
                    autoLoginSwitchPreference.setChecked(false);
                    Log.i(CLASS_NAME, "Location permission denied");
                }
            }
        }
    }
}

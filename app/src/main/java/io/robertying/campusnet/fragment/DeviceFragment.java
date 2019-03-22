package io.robertying.campusnet.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import io.robertying.campusnet.R;
import io.robertying.campusnet.helper.CredentialHelper;
import io.robertying.campusnet.helper.UseregHelper;

public class DeviceFragment extends DialogFragment {

    private final String CLASS_NAME = getClass().getSimpleName();

    DeviceFragmentListener listener;
    private View view;

    public static DeviceFragment newInstance(String sessions) {
        DeviceFragment f = new DeviceFragment();

        Bundle args = new Bundle();
        args.putString("sessions", sessions);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (DeviceFragmentListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement DeviceFragmentListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String sessionsString = getArguments().getString("sessions");
        final JsonArray sessions = new JsonParser().parse(sessionsString).getAsJsonArray();
        final JsonArray returnedSessions = new JsonParser().parse(sessionsString).getAsJsonArray();
        List<String> deviceInfo = new ArrayList<>();
        for (int i = 0; i < sessions.size(); i++) {
            JsonObject session = sessions.get(i).getAsJsonObject();
            deviceInfo.add(String.format("%s %s %s", session.get("device").getAsString(),
                    "↑" + session.get("outTraffic").getAsString(),
                    "↓" + session.get("inTraffic").getAsString()));
        }

        final List<Integer> selectedDevices = new ArrayList<>();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.manage_online_devices)
                .setMultiChoiceItems(deviceInfo.toArray(new String[0]),
                        null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (isChecked) {
                                    selectedDevices.add(which);
                                } else if (selectedDevices.contains(which)) {
                                    selectedDevices.remove(Integer.valueOf(which));
                                }
                            }
                        })
                .setPositiveButton(R.string.drop_session, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        final String[] credentials = CredentialHelper.getCredentials(getContext());
                        for (int i = 0; i < selectedDevices.size(); ++i) {
                            int index = selectedDevices.get(i);
                            final String sessionId = sessions.get(index)
                                    .getAsJsonObject()
                                    .get("id")
                                    .getAsString();
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    UseregHelper.dropSession(credentials[0], credentials[1], sessionId);
                                    Log.i(CLASS_NAME, "Dropped session " + sessionId);
                                }
                            });
                        }

                        listener.onDialogClose(selectedDevices.size());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }

    public interface DeviceFragmentListener {
        void onDialogClose(int dropped);
    }
}

package io.robertying.campusnet.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import io.robertying.campusnet.R;

public class UpdateFragment extends DialogFragment {

    public static UpdateFragment newInstance(boolean updateAvailable, String newVersion, String url) {
        UpdateFragment f = new UpdateFragment();

        Bundle args = new Bundle();
        args.putBoolean("updateAvailable", updateAvailable);
        args.putString("newVersion", newVersion);
        args.putString("url", url);
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        boolean updateAvailable = getArguments().getBoolean("updateAvailable");
        final String newVersion = getArguments().getString("newVersion");
        final String url = getArguments().getString("url");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (updateAvailable) {
            builder
                    .setMessage(getResources().getString(R.string.update_found, newVersion))
                    .setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(browserIntent);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
        } else {
            builder.setMessage(R.string.update_not_found)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
        }

        return builder.create();
    }
}

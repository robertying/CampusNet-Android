package io.robertying.campusnet.custom;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.robertying.campusnet.R;

public class MySnackbar {
    @NonNull
    public static Snackbar make(@NonNull Context context,
                                @NonNull View view,
                                @Nullable View anchorView,
                                @NonNull CharSequence text,
                                int duration) {
        Snackbar snackbar = Snackbar.make(view, text, duration);

        if (anchorView == null)
            snackbar.setAnchorView(R.id.navigation);
        else
            snackbar.setAnchorView(anchorView);

        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(context.getResources()
                .getColor(R.color.design_default_color_background));
        TextView textView = snackbarView
                .findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));

        return snackbar;
    }
}

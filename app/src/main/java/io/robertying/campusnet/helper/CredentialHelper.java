package io.robertying.campusnet.helper;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import static android.content.Context.MODE_PRIVATE;

public class CredentialHelper {
    @Nullable
    public static String[] getCredentials(Context context) {
        SharedPreferences preferences = context
                .getApplicationContext()
                .getSharedPreferences("Credentials", MODE_PRIVATE);

        String username = preferences.getString("username", null);
        String password = preferences.getString("password", null);

        String[] credentials = {username, password};
        return credentials;
    }

    public static void removeCredentials(Context context) {
        SharedPreferences preferences = context
                .getApplicationContext().getSharedPreferences("Credentials", MODE_PRIVATE);
        preferences.edit().clear().apply();
    }
}

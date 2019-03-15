package io.robertying.campusnet.helper;

import android.content.Context;

import androidx.annotation.NonNull;

public class UseregHelper {
    public static void init(@NonNull Context context) {
        TunetHelper.init(context);
    }

    public static void cleanup() {
        TunetHelper.cleanup();
    }

    @NonNull
    public native static TunetHelper.ResponseType useregLogin(String username, String password);

    @NonNull
    public native static String getSessions(String username, String password);

    public native static void dropSession(String username, String password, String sessionId);

    public native static float getUsage();

    public native static float getUsageDetail(String username, String password, String startTime, String endTime);
}

package io.robertying.campusnet.helper;

import android.content.Context;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

public class TunetHelper {

    private native static void tunetInit(String caBundlePath);

    private native static void tunetCleanup();

    @NonNull
    public native static ResponseType netLogin(String username, String password);

    @NonNull
    public native static ResponseType auth4Login(String username, String password);

    @NonNull
    public native static ResponseType auth6Login(String username, String password);

    @NonNull
    public native static ResponseType netLogout();

    @NonNull
    public native static ResponseType auth4Logout();

    @NonNull
    public native static ResponseType auth6Logout();

    public static void init(@NonNull Context context) {
        System.loadLibrary("tunet");
        tunetInit(CAHelper.getCABundlePath(context));
    }

    public static void cleanup() {
        tunetCleanup();
    }

    @Keep
    public enum ResponseType {
        SUCCESS,
        UNKNOWN_ERR,
        EMPTY_CHALLENGE,
        WRONG_CREDENTIAL,   // E2553 E2531 E5992
        OUT_OF_BALANCE,     // E3004 E2616
        TOO_SHORT_INTERVAL, // E2532
        TOO_MANY_ATTEMPTS,  // E2533
        ALREADY_ONLINE,     // E2620
        INVALID_IP,         // E2833
    }
}

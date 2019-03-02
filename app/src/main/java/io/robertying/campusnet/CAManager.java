package io.robertying.campusnet;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

class CAManager {
    private static String caFilename = "cacert.pem";

    public static void copyFromAssetsToInternalStorage(Context context) {
        AssetManager assetManager = context.getAssets();
        InputStream in;
        OutputStream out;

        try {
            in = assetManager.open(caFilename);
            File caFile = new File(context.getFilesDir(), caFilename);
            out = new FileOutputStream(caFile, false);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Log.e("CAManager", "copyFromAssetsToInternalStorage " + e.getMessage());
        }
    }

    public static void update(Context context) {
        // TODO
    }

    public static String getCABundlePath(Context context) {
        return new File(context.getFilesDir(), caFilename).getAbsolutePath();
    }
}

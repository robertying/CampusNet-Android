package io.robertying.campusnet.helper;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class CAHelper {

    public final static String caFilename = "cacert.pem";

    public static void copyFromAssetsToInternalStorage(Context context) {
        AssetManager assetManager = context.getApplicationContext().getAssets();
        InputStream in;
        OutputStream out;

        try {
            in = assetManager.open(caFilename);
            File caFile = new File(context.getApplicationContext().getFilesDir(), caFilename);
            out = new FileOutputStream(caFile, false);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            in.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            Log.e("CAHelper", e.getMessage());
        }

        Log.i("CAHelper", "CA bundle copied to internal storage");
    }

    static String getCABundlePath(Context context) {
        return new File(context.getApplicationContext().getFilesDir(), caFilename).getAbsolutePath();
    }
}

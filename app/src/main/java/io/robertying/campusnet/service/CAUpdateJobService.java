package io.robertying.campusnet.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import io.robertying.campusnet.helper.CAHelper;

public class CAUpdateJobService extends JobService {

    static final public int JOB_ID = 100;
    public final String CLASS_NAME = getClass().getSimpleName();

    @Override
    public boolean onStartJob(final JobParameters params) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(CLASS_NAME, "Started CA update");
                boolean success = downloadCA();
                jobFinished(params, false);
            }
        }).start();

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

    private boolean downloadCA() {
        String caURL = "https://curl.haxx.se/ca/cacert.pem";
        InputStream in = null;
        OutputStream out = null;
        HttpsURLConnection connection = null;
        boolean success = false;

        try {
            URL url = new URL(caURL);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setReadTimeout(3000);
            connection.setConnectTimeout(3000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpsURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }

            in = connection.getInputStream();
            File newFile = new File(getFilesDir(), CAHelper.caFilename + ".tmp");
            out = new FileOutputStream(newFile, false);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();

            File oldFile = new File(getFilesDir(), CAHelper.caFilename);
            success = newFile.renameTo(oldFile);
        } catch (IOException e) {
            Log.e(CLASS_NAME, e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(CLASS_NAME, e.getMessage());
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Log.e(CLASS_NAME, e.getMessage());
                }
            }

            if (connection != null) {
                connection.disconnect();
            }
        }

        return success;
    }
}

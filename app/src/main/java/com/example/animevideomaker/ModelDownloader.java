package com.example.animevideomaker;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ModelDownloader {

    private static final String TAG = "ModelDownloader";

    // ✅ GitHub Release model file URL (update if version or name changes)
    private static final String MODEL_URL = "https://github.com/Mr-H9542/AnimeVideoMaker/releases/download/v1.0/model.onnx";

    // ✅ Model filename when saved locally
    private static final String MODEL_FILENAME = "model.onnx";

    public interface DownloadListener {
        void onDownloadSuccess(File file);
        void onDownloadFailed(String error);
    }

    public static void downloadModelIfNeeded(Context context, DownloadListener listener) {
        File modelFile = new File(context.getFilesDir(), MODEL_FILENAME);
        if (modelFile.exists()) {
            Log.i(TAG, "Model already exists: " + modelFile.getAbsolutePath());
            if (listener != null) listener.onDownloadSuccess(modelFile);
        } else {
            new DownloadTask(context.getApplicationContext(), modelFile, listener).execute();
        }
    }

    private static class DownloadTask extends AsyncTask<Void, Void, Boolean> {

        private final Context context;
        private final File destFile;
        private final DownloadListener listener;
        private String errorMsg = null;

        DownloadTask(Context context, File destFile, DownloadListener listener) {
            this.context = context;
            this.destFile = destFile;
            this.listener = listener;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                URL url = new URL(MODEL_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.connect();

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    errorMsg = "Server returned HTTP " + conn.getResponseCode();
                    return false;
                }

                try (InputStream input = conn.getInputStream();
                     FileOutputStream output = new FileOutputStream(destFile)) {

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = input.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }
                    output.flush();
                }

                conn.disconnect();

                if (!destFile.exists() || destFile.length() == 0) {
                    errorMsg = "Downloaded file is empty or missing";
                    return false;
                }

                Log.i(TAG, "Model downloaded successfully: " + destFile.getAbsolutePath());
                return true;

            } catch (Exception e) {
                Log.e(TAG, "Download failed", e);
                errorMsg = e.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                if (listener != null) listener.onDownloadSuccess(destFile);
            } else {
                if (listener != null) listener.onDownloadFailed(errorMsg != null ? errorMsg : "Unknown error");
            }
        }
    }
                        }

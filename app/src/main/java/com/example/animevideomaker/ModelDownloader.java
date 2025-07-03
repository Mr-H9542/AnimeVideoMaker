package com.example.animevideomaker;  // use your actual package name

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
    private static final String MODEL_FILENAME = "model.onnx";
    private static final String MODEL_URL = "https://drive.google.com/uc?export=download&id=1YNmra-wLt-VFdTfcg2BRyc9aBXQbNL8G";

    public static void downloadModelIfNeeded(Context context) {
        File modelFile = new File(context.getFilesDir(), MODEL_FILENAME);

        if (!modelFile.exists()) {
            new DownloadTask(context, modelFile).execute(MODEL_URL);
        } else {
            Log.i(TAG, "Model already exists, skipping download");
        }
    }

    private static class DownloadTask extends AsyncTask<String, Void, Boolean> {

        private Context context;
        private File file;

        public DownloadTask(Context context, File file) {
            this.context = context.getApplicationContext();
            this.file = file;
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            String urlToDownload = urls[0];
            try {
                URL url = new URL(urlToDownload);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "Server returned HTTP " + connection.getResponseCode());
                    return false;
                }

                InputStream input = connection.getInputStream();
                FileOutputStream output = new FileOutputStream(file);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }

                output.close();
                input.close();

                Log.i(TAG, "Model downloaded successfully");
                return true;

            } catch (Exception e) {
                Log.e(TAG, "Failed to download model", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) {
                Log.e(TAG, "Download failed");
                // Optionally, notify user or retry
            }
        }
    }
}

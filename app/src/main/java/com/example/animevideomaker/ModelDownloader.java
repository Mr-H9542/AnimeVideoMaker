package com.example.animevideomaker;  // Use your actual package name

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class ModelDownloader {

    private static final String TAG = "ModelDownloader";

    // Google Drive file ID for your model file
    private static final String FILE_ID = "1YNmra-wLt-VFdTfcg2BRyc9aBXQbNL8G";

    // URL to download file from Google Drive
    private static final String BASE_URL = "https://drive.google.com/uc?export=download";

    private static final String MODEL_FILENAME = "model.onnx";

    public interface DownloadListener {
        void onDownloadSuccess(File file);
        void onDownloadFailed(String error);
    }

    /**
     * Call this method to download the model if it doesn't already exist.
     *
     * @param context Android context
     * @param listener callback for success/failure
     */
    public static void downloadModelIfNeeded(Context context, DownloadListener listener) {
        File modelFile = new File(context.getFilesDir(), MODEL_FILENAME);
        if (modelFile.exists()) {
            Log.i(TAG, "Model already exists at " + modelFile.getAbsolutePath());
            if (listener != null) listener.onDownloadSuccess(modelFile);
            return;
        }
        // Start download async task
        new DownloadTask(context, modelFile, listener).execute();
    }

    private static class DownloadTask extends AsyncTask<Void, Void, Boolean> {

        private final Context context;
        private final File destFile;
        private final DownloadListener listener;
        private String errorMsg = null;

        DownloadTask(Context context, File destFile, DownloadListener listener) {
            this.context = context.getApplicationContext();
            this.destFile = destFile;
            this.listener = listener;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                return downloadFileWithConfirmation(FILE_ID, destFile);
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

        /**
         * Downloads a file from Google Drive handling confirmation token if needed.
         *
         * @param fileId Google Drive file ID
         * @param destination File to save the downloaded content
         * @return true if successful, false otherwise
         * @throws Exception on download errors
         */
        private boolean downloadFileWithConfirmation(String fileId, File destination) throws Exception {
            String url = BASE_URL + "&id=" + fileId;
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setInstanceFollowRedirects(false);
            conn.connect();

            String confirmToken = getConfirmTokenFromCookies(conn);

            if (confirmToken != null) {
                // Need to confirm download due to large file
                conn.disconnect();
                String confirmedUrl = BASE_URL + "&confirm=" + confirmToken + "&id=" + fileId;
                conn = (HttpURLConnection) new URL(confirmedUrl).openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.connect();
            } else if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new Exception("Server returned HTTP " + conn.getResponseCode());
            }

            try (InputStream input = conn.getInputStream();
                 FileOutputStream output = new FileOutputStream(destination)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
                output.flush();
            }

            conn.disconnect();

            if (!destination.exists() || destination.length() == 0) {
                throw new Exception("Downloaded file is empty");
            }

            Log.i(TAG, "Model downloaded successfully to " + destination.getAbsolutePath());
            return true;
        }

        /**
         * Extracts the confirmation token from the "Set-Cookie" header if it exists.
         *
         * @param conn HttpURLConnection
         * @return confirmation token string or null if none found
         */
        private String getConfirmTokenFromCookies(HttpURLConnection conn) {
            Map<String, List<String>> headers = conn.getHeaderFields();
            List<String> cookies = headers.get("Set-Cookie");
            if (cookies == null) return null;

            for (String cookie : cookies) {
                if (cookie.contains("download_warning")) {
                    String[] parts = cookie.split(";");
                    for (String part : parts) {
                        if (part.startsWith("download_warning")) {
                            String[] tokenPair = part.split("=");
                            if (tokenPair.length > 1) {
                                return tokenPair[1];
                            }
                        }
                    }
                }
            }
            return null;
        }
    }
                }

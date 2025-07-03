package com.example.animevideomaker;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ModelDownloader {

    private static final String TAG = "ModelDownloader";

    // ✅ Change this URL to your real ONNX model URL
    private static final String MODEL_URL = "https://example.com/path-to-your-model.onnx";
    private static final String MODEL_FILENAME = "text_encoder_model.onnx";

    public interface DownloadListener {
        void onDownloadSuccess(File modelFile);
        void onDownloadFailed(String error);
    }

    /**
     * Checks if model exists. If not, downloads it.
     */
    public static void downloadModelIfNeeded(Context context, DownloadListener listener) {
        File modelFile = new File(getModelStorageDir(context), MODEL_FILENAME);

        if (modelFile.exists() && modelFile.length() > 0) {
            Log.i(TAG, "Model already exists at: " + modelFile.getAbsolutePath());
            listener.onDownloadSuccess(modelFile);
            return;
        }

        downloadModel(context, modelFile, listener);
    }

    private static void downloadModel(Context context, File destinationFile, DownloadListener listener) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(MODEL_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(30000);

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    listener.onDownloadFailed("Server returned HTTP " + responseCode);
                    return;
                }

                try (BufferedInputStream input = new BufferedInputStream(connection.getInputStream());
                     FileOutputStream output = new FileOutputStream(destinationFile)) {

                    byte[] buffer = new byte[8192];
                    int bytesRead;

                    while ((bytesRead = input.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }

                    output.flush();
                    Log.i(TAG, "Model downloaded to: " + destinationFile.getAbsolutePath());
                    listener.onDownloadSuccess(destinationFile);

                } catch (Exception ioEx) {
                    listener.onDownloadFailed("Failed to save model: " + ioEx.getMessage());
                }

            } catch (Exception e) {
                Log.e(TAG, "Download failed", e);
                listener.onDownloadFailed("Model download error: " + e.getMessage());
            } finally {
                if (connection != null) connection.disconnect();
                executor.shutdown();
            }
        });
    }

    private static File getModelStorageDir(Context context) {
        File dir = new File(context.getFilesDir(), "models");
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) Log.e(TAG, "Failed to create model storage directory");
        }
        return dir;
    }
}

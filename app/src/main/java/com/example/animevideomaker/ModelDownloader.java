package com.example.animevideomaker;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ModelDownloader {

    private static final String TAG = "ModelDownloader";

    // ✅ GitHub release zip file URL
    private static final String ZIP_URL = "https://github.com/Mr-H9542/AnimeVideoMaker/releases/download/v1.0/assets.zip";
    private static final String ZIP_FILENAME = "assets.zip";

    public interface DownloadListener {
        void onDownloadSuccess(File modelDir);
        void onDownloadFailed(String error);
    }

    /**
     * Checks if the models folder exists and contains at least some model files.
     */
    public static void downloadModelsIfNeeded(Context context, DownloadListener listener) {
        File modelDir = getModelStorageDir(context);
        File textEncoder = new File(modelDir, "text_encoder/text_encoder_model.onnx");

        if (textEncoder.exists() && textEncoder.length() > 0) {
            Log.i(TAG, "Models already exist at: " + modelDir.getAbsolutePath());
            listener.onDownloadSuccess(modelDir);
            return;
        }

        downloadAndExtractModels(context, modelDir, listener);
    }

    private static void downloadAndExtractModels(Context context, File outputDir, DownloadListener listener) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            HttpURLConnection connection = null;
            File zipFile = new File(outputDir, ZIP_FILENAME);

            try {
                // Download the zip
                URL url = new URL(ZIP_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(30000);

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    listener.onDownloadFailed("Server returned HTTP " + responseCode);
                    return;
                }

                try (BufferedInputStream input = new BufferedInputStream(connection.getInputStream());
                     FileOutputStream output = new FileOutputStream(zipFile)) {

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = input.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }
                    output.flush();
                    Log.i(TAG, "Downloaded zip to: " + zipFile.getAbsolutePath());
                }

                // Extract all files
                try (ZipInputStream zipInput = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {
                    ZipEntry entry;
                    while ((entry = zipInput.getNextEntry()) != null) {
                        File outFile = new File(outputDir, entry.getName());

                        if (entry.isDirectory()) {
                            if (!outFile.exists() && !outFile.mkdirs()) {
                                Log.e(TAG, "Failed to create directory: " + outFile.getAbsolutePath());
                            }
                        } else {
                            File parent = outFile.getParentFile();
                            if (parent != null && !parent.exists()) parent.mkdirs();

                            try (FileOutputStream outStream = new FileOutputStream(outFile)) {
                                byte[] buffer = new byte[8192];
                                int bytesRead;
                                while ((bytesRead = zipInput.read(buffer)) != -1) {
                                    outStream.write(buffer, 0, bytesRead);
                                }
                            }
                        }
                        zipInput.closeEntry();
                    }
                }

                Log.i(TAG, "Extracted all models to: " + outputDir.getAbsolutePath());
                listener.onDownloadSuccess(outputDir);
            } catch (Exception e) {
                Log.e(TAG, "Download failed", e);
                listener.onDownloadFailed("Error: " + e.getMessage());
            } finally {
                if (connection != null) connection.disconnect();
                if (zipFile.exists()) zipFile.delete(); // cleanup
                executor.shutdown();
            }
        });
    }

    private static File getModelStorageDir(Context context) {
        File dir = new File(context.getFilesDir(), "models");
        if (!dir.exists() && !dir.mkdirs()) {
            Log.e(TAG, "Failed to create model storage directory");
        }
        return dir;
    }
                        }

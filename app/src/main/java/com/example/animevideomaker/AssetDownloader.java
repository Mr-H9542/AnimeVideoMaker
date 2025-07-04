package com.example.animevideomaker;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.animevideomaker.utils.ZipUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Downloads and extracts assets.zip from GitHub Releases if not already present.
 */
public class AssetDownloader {

    private static final String TAG = "AssetDownloader";
    private static final String ZIP_URL = "https://github.com/Mr-H9542/AnimeVideoMaker/releases/download/v1.0/assets.zip";
    private static final String ZIP_NAME = "assets.zip";
    private static final String TARGET_FOLDER = "onnx_model";

    public interface OnAssetsReadyCallback {
        void onReady();
        void onFailed(Exception e);
    }

    public static void ensureAssetsAvailable(Context context, OnAssetsReadyCallback callback) {
        File targetDir = new File(context.getFilesDir(), TARGET_FOLDER);

        if (targetDir.exists() && targetDir.isDirectory() && targetDir.list().length > 0) {
            Log.d(TAG, "Assets already available.");
            callback.onReady();
        } else {
            new DownloadAndExtractTask(context, callback).execute();
        }
    }

    private static class DownloadAndExtractTask extends AsyncTask<Void, Void, Boolean> {
        private final Context context;
        private final OnAssetsReadyCallback callback;
        private Exception error;

        public DownloadAndExtractTask(Context context, OnAssetsReadyCallback callback) {
            this.context = context.getApplicationContext();
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                File zipFile = new File(context.getCacheDir(), ZIP_NAME);
                File outputDir = new File(context.getFilesDir(), TARGET_FOLDER);

                // Step 1: Download ZIP
                downloadZipFile(ZIP_URL, zipFile);

                // Step 2: Extract
                ZipUtils.unzip(zipFile, outputDir);

                // Step 3: Validate expected models
                File textEncoder = new File(outputDir, "text_encoder/text_encoder_model.onnx");
                File unet = new File(outputDir, "unet/model.onnx");

                if (!textEncoder.exists()) {
                    throw new Exception("Missing model: " + textEncoder.getAbsolutePath());
                }
                if (!unet.exists()) {
                    throw new Exception("Missing model: " + unet.getAbsolutePath());
                }

                return true;

            } catch (Exception e) {
                Log.e(TAG, "Asset setup failed", e);
                error = e;
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                callback.onReady();
            } else {
                callback.onFailed(error);
            }
        }
    }

    private static void downloadZipFile(String zipUrl, File destination) throws Exception {
        Log.d(TAG, "Downloading: " + zipUrl);
        HttpURLConnection connection = (HttpURLConnection) new URL(zipUrl).openConnection();
        connection.connect();

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new Exception("Server returned HTTP " + connection.getResponseCode());
        }

        try (
            InputStream input = new BufferedInputStream(connection.getInputStream());
            FileOutputStream output = new FileOutputStream(destination)
        ) {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
            output.flush();
        } finally {
            connection.disconnect();
        }

        Log.d(TAG, "Downloaded to: " + destination.getAbsolutePath());
    }
        }

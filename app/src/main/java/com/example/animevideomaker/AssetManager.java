package com.example.animevideomaker;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
            return;
        }

        new DownloadAndExtractTask(context, callback).execute();
    }

    private static class DownloadAndExtractTask extends AsyncTask<Void, Void, Boolean> {

        private final Context context;
        private final OnAssetsReadyCallback callback;
        private Exception failure;

        DownloadAndExtractTask(Context context, OnAssetsReadyCallback callback) {
            this.context = context.getApplicationContext();
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                File zipFile = new File(context.getCacheDir(), ZIP_NAME);
                downloadZipFile(ZIP_URL, zipFile);
                unzip(zipFile.getAbsolutePath(), context.getFilesDir().getAbsolutePath());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                failure = e;
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                callback.onReady();
            } else {
                callback.onFailed(failure);
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

        try (InputStream input = new BufferedInputStream(connection.getInputStream());
             FileOutputStream output = new FileOutputStream(destination)) {

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

    private static void unzip(String zipFilePath, String targetDirectoryPath) throws Exception {
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFilePath)))) {
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectoryPath, ze.getName());

                if (ze.isDirectory()) {
                    if (!file.exists() && !file.mkdirs()) {
                        throw new Exception("Failed to create directory: " + file.getAbsolutePath());
                    }
                } else {
                    File parent = file.getParentFile();
                    if (!parent.exists() && !parent.mkdirs()) {
                        throw new Exception("Failed to create directory: " + parent.getAbsolutePath());
                    }

                    try (FileOutputStream fout = new FileOutputStream(file)) {
                        byte[] buffer = new byte[4096];
                        int count;
                        while ((count = zis.read(buffer)) != -1) {
                            fout.write(buffer, 0, count);
                        }
                    }
                }
                zis.closeEntry();
            }
        }

        Log.d(TAG, "Unzipped to: " + targetDirectoryPath);
    }
                   }

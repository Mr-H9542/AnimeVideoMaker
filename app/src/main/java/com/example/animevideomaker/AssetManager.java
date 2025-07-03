package com.example.animevideomaker;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AssetManager {

    private static final String ZIP_URL = "https://github.com/YourOrg/AnimeVideoMaker/releases/download/v1.0/assets.zip";
    private static final String ZIP_FILENAME = "model_assets.zip";
    private static final String CHECK_FILE = "onnx_model/text_encoder/text_encoder_model.onnx";

    public interface AssetCallback {
        void onAssetsReady();
        void onError(String error);
    }

    public static void ensureAssetsReady(Context context, AssetCallback callback) {
        File modelFile = new File(context.getFilesDir(), CHECK_FILE);

        if (modelFile.exists()) {
            callback.onAssetsReady();
            return;
        }

        new Thread(() -> {
            try {
                // 1. Download ZIP
                File zipFile = new File(context.getFilesDir(), ZIP_FILENAME);
                URL url = new URL(ZIP_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();

                try (InputStream in = conn.getInputStream();
                     FileOutputStream out = new FileOutputStream(zipFile)) {
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                }

                // 2. Extract
                unzip(zipFile.getAbsolutePath(), context.getFilesDir().getAbsolutePath());

                // 3. Confirm result
                File check = new File(context.getFilesDir(), CHECK_FILE);
                if (check.exists()) {
                    runOnMain(() -> callback.onAssetsReady());
                } else {
                    runOnMain(() -> callback.onError("Model file missing after unzip"));
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnMain(() -> callback.onError("Asset download failed: " + e.getMessage()));
            }
        }).start();
    }

    private static void unzip(String zipPath, String destDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath))) {
            ZipEntry entry;
            byte[] buffer = new byte[1024];

            while ((entry = zis.getNextEntry()) != null) {
                File newFile = new File(destDir, entry.getName());
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                    continue;
                } else {
                    new File(newFile.getParent()).mkdirs();
                }

                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
        }
    }

    private static void runOnMain(Runnable r) {
        new Handler(Looper.getMainLooper()).post(r);
    }
                            }

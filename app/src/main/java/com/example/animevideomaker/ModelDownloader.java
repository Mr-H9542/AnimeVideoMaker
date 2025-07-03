package com.example.animevideomaker;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Utility class to manage model downloads and setup.
 */
public class ModelDownloader {
    private static final String TAG = "ModelDownloader";
    private static final String MODEL_FOLDER_NAME = "onnx_models";

    /**
     * Interface for download callback events.
     */
    public interface DownloadListener {
        void onDownloadSuccess(File modelDir);
        void onDownloadFailed(String error);
    }

    /**
     * Downloads models if not already available.
     */
    public static void downloadModelsIfNeeded(Context context, DownloadListener listener) {
        File modelDir = new File(context.getFilesDir(), MODEL_FOLDER_NAME);
        File textEncoderModel = new File(modelDir, "text_encoder/text_encoder_model.onnx");

        if (textEncoderModel.exists()) {
            listener.onDownloadSuccess(modelDir);
            return;
        }

        try {
            copyAssetFolder(context, "onnx_models", modelDir);
            new Handler(Looper.getMainLooper()).post(() -> listener.onDownloadSuccess(modelDir));
        } catch (Exception e) {
            Log.e(TAG, "Model download failed", e);
            new Handler(Looper.getMainLooper()).post(() -> listener.onDownloadFailed(e.getMessage()));
        }
    }

    /**
     * Recursively copies an asset folder to internal storage.
     */
    private static void copyAssetFolder(Context context, String assetPath, File destDir) throws Exception {
        String[] files = context.getAssets().list(assetPath);
        if (files == null || files.length == 0) return;

        if (!destDir.exists()) destDir.mkdirs();

        for (String fileName : files) {
            String assetFilePath = assetPath + "/" + fileName;
            File outFile = new File(destDir, fileName);

            String[] nestedFiles = context.getAssets().list(assetFilePath);
            if (nestedFiles != null && nestedFiles.length > 0) {
                copyAssetFolder(context, assetFilePath, outFile);
            } else {
                try (InputStream in = context.getAssets().open(assetFilePath);
                     FileOutputStream out = new FileOutputStream(outFile)) {
                    byte[] buffer = new byte[4096];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                }
            }
        }
    }
            }

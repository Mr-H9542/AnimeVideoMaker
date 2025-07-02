package com.example.animevideomaker;

import android.content.Context;
import android.content.res.AssetManager;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

import java.io.IOException;
import java.io.InputStream;

public class OnnxUtils {

    /**
     * Loads an ONNX model from the assets folder into an OrtSession.
     *
     * @param context   Android context to access assets
     * @param env       OrtEnvironment instance
     * @param assetPath Path to the ONNX model file inside assets (e.g. "animagine-xl/text_encoder.onnx")
     * @return OrtSession instance ready for inference
     * @throws IOException    If reading the asset fails
     * @throws OrtException   If ONNX Runtime fails to create a session
     */
    public static OrtSession loadModelFromAssets(Context context, OrtEnvironment env, String assetPath) throws IOException, OrtException {
        AssetManager assetManager = context.getAssets();
        try (InputStream inputStream = assetManager.open(assetPath)) {
            byte[] modelBytes = new byte[inputStream.available()];
            int read = inputStream.read(modelBytes);
            if (read != modelBytes.length) {
                throw new IOException("Failed to read entire model file");
            }
            return env.createSession(modelBytes, new OrtSession.SessionOptions());
        }
    }
}

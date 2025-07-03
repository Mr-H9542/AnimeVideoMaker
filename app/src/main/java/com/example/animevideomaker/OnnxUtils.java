package com.example.animevideomaker;

import android.util.Log;

import java.io.File;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

public class OnnxUtils {

    private static final String TAG = "OnnxUtils";

    /**
     * Loads an ONNX model into an OrtSession.
     *
     * @param env           The OrtEnvironment to use (non-null).
     * @param modelFilePath Path to ONNX model file (non-null, absolute).
     * @return OrtSession instance ready for inference.
     * @throws OrtException           If loading the model fails.
     * @throws IllegalArgumentException If environment or file is invalid.
     */
    public static OrtSession loadModelFromFile(OrtEnvironment env, String modelFilePath) throws OrtException {
        if (env == null) {
            throw new IllegalArgumentException("OrtEnvironment cannot be null.");
        }

        if (modelFilePath == null || modelFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Model file path is null or empty.");
        }

        File modelFile = new File(modelFilePath);
        if (!modelFile.exists() || !modelFile.isFile()) {
            throw new IllegalArgumentException("Model file does not exist or is not a file: " + modelFilePath);
        }

        Log.d(TAG, "Initializing ONNX session for model: " + modelFile.getAbsolutePath());

        OrtSession.SessionOptions options = new OrtSession.SessionOptions();

        // Optional: Set optimization or execution providers
        // options.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.BASIC_OPT);

        OrtSession session = env.createSession(modelFilePath, options);

        Log.i(TAG, "ONNX model loaded successfully.");
        return session;
    }
}

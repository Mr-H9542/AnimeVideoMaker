package com.example.animevideomaker;

import android.util.Log;

import java.io.File;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

public class OnnxUtils {

    private static final String TAG = "OnnxUtils";

    /**
     * Loads an ONNX model into an OrtSession with optional optimizations.
     *
     * @param env           OrtEnvironment instance (must not be null)
     * @param modelFilePath Absolute path to the ONNX model file
     * @return OrtSession instance for inference
     * @throws OrtException If ONNX loading fails
     */
    public static OrtSession loadModelFromFile(OrtEnvironment env, String modelFilePath) throws OrtException {
        validateInputs(env, modelFilePath);

        File modelFile = new File(modelFilePath);
        Log.d(TAG, "Initializing ONNX session for model: " + modelFile.getAbsolutePath());

        OrtSession.SessionOptions options = new OrtSession.SessionOptions();

        // Optional: Enable basic graph optimization
        options.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.BASIC_OPT);

        // Optional: If using GPU (add GPU EP)
        // options.addCUDA();

        OrtSession session = env.createSession(modelFilePath, options);

        Log.i(TAG, "ONNX model loaded successfully.");
        return session;
    }

    /**
     * Validates inputs before model loading.
     */
    private static void validateInputs(OrtEnvironment env, String modelFilePath) {
        if (env == null) {
            throw new IllegalArgumentException("OrtEnvironment cannot be null.");
        }

        if (modelFilePath == null || modelFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Model file path is null or empty.");
        }

        File modelFile = new File(modelFilePath);
        if (!modelFile.exists() || !modelFile.isFile()) {
            throw new IllegalArgumentException("Model file does not exist: " + modelFilePath);
        }
    }
}

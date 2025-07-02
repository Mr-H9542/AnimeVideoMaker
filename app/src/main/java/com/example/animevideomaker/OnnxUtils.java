package com.example.animevideomaker;

import android.util.Log;

import java.io.File;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

public class OnnxUtils {

    private static final String TAG = "OnnxUtils";

    /**
     * Loads an ONNX model from a local file path into an OrtSession.
     *
     * @param env           OrtEnvironment instance. Must not be null.
     * @param modelFilePath Absolute path to the ONNX model file on device storage.
     * @return OrtSession ready for inference.
     * @throws OrtException           if session creation fails.
     * @throws IllegalArgumentException if parameters are invalid or file doesn't exist.
     */
    public static OrtSession loadModelFromFile(OrtEnvironment env, String modelFilePath) throws OrtException {
        if (env == null) {
            throw new IllegalArgumentException("OrtEnvironment is null.");
        }
        if (modelFilePath == null || modelFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Model file path is null or empty.");
        }

        File modelFile = new File(modelFilePath);
        if (!modelFile.exists() || !modelFile.isFile()) {
            throw new IllegalArgumentException("Model file does not exist: " + modelFilePath);
        }

        Log.i(TAG, "Loading ONNX model from file: " + modelFilePath);

        OrtSession.SessionOptions options = new OrtSession.SessionOptions();
        OrtSession session = env.createSession(modelFilePath, options);

        Log.i(TAG, "ONNX model loaded successfully.");
        return session;
    }
}

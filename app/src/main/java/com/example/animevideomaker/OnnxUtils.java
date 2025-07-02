package com.example.animevideomaker;

import android.util.Log;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

public class OnnxUtils {

    private static final String TAG = "OnnxUtils";

    /**
     * Loads an ONNX model from a local file path into an OrtSession.
     *
     * @param env           OrtEnvironment instance.
     * @param modelFilePath Absolute path to ONNX model file on device storage.
     * @return OrtSession ready for inference.
     * @throws OrtException if session creation fails.
     */
    public static OrtSession loadModelFromFile(OrtEnvironment env, String modelFilePath) throws OrtException {
        Log.i(TAG, "Loading ONNX model from file: " + modelFilePath);
        return env.createSession(modelFilePath, new OrtSession.SessionOptions());
    }
}

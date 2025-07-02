package com.example.animevideomaker;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.OnnxValue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.Map;

/**
 * Utility class for loading ONNX models and running inference with ONNX Runtime.
 */
public class OnnxUtils {

    private static final String TAG = "OnnxUtils";

    /**
     * Loads an ONNX model from app assets into an OrtSession.
     *
     * @param context   Android Context to access assets.
     * @param env       OrtEnvironment instance.
     * @param assetPath Path to ONNX model inside assets (e.g. "animagine-xl/text_encoder/model.onnx").
     * @return OrtSession ready for inference.
     * @throws IOException  if reading the model fails.
     * @throws OrtException if creating session fails.
     */
    public static OrtSession loadModelFromAssets(Context context, OrtEnvironment env, String assetPath)
            throws IOException, OrtException {
        AssetManager assetManager = context.getAssets();
        try (InputStream inputStream = assetManager.open(assetPath)) {
            int available = inputStream.available();
            byte[] modelBytes = new byte[available];
            int bytesRead = inputStream.read(modelBytes);
            if (bytesRead != available) {
                throw new IOException("Failed to read the complete ONNX model file.");
            }
            return env.createSession(modelBytes, new OrtSession.SessionOptions());
        }
    }

    /**
     * Runs a dummy inference on the given ONNX Runtime session.
     * This is useful for testing model integration.
     *
     * @param env     OrtEnvironment instance.
     * @param session OrtSession loaded with your model.
     * @throws OrtException if inference fails.
     */
    public static void runDummyInference(OrtEnvironment env, OrtSession session) throws OrtException {
        // Example input shape: [1, 3, 224, 224] - adjust as per your model's input
        long[] inputShape = new long[]{1, 3, 224, 224};
        int inputSize = 1;
        for (long dim : inputShape) inputSize *= dim;

        float[] inputData = new float[inputSize];
        // Fill with dummy data (e.g., 1.0f)
        for (int i = 0; i < inputSize; i++) {
            inputData[i] = 1.0f;
        }

        try (OnnxTensor inputTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(inputData), inputShape)) {
            // Get the first input name (some models have multiple inputs; adjust if needed)
            String inputName = session.getInputNames().iterator().next();

            // Prepare inputs map
            Map<String, OnnxTensor> inputs = Collections.singletonMap(inputName, inputTensor);

            // Run inference
            try (OrtSession.Result results = session.run(inputs)) {
                for (String outputName : session.getOutputNames()) {
                    OnnxValue outputValue = results.get(outputName);
                    if (outputValue != null) {
                        long[] shape = outputValue.getInfo().getShape();
                        Log.i(TAG, "Output name: " + outputName + ", shape: " + java.util.Arrays.toString(shape));
                    } else {
                        Log.w(TAG, "Output missing for name: " + outputName);
                    }
                }
            }
        }
    }
}

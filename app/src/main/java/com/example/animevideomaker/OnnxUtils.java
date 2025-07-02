package com.example.animevideomaker;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.Map;

/**
 * Utility class for loading ONNX models and running inference using ONNX Runtime.
 */
public class OnnxUtils {

    private static final String TAG = "OnnxUtils";

    /**
     * Loads an ONNX model from the app assets into an OrtSession.
     *
     * @param context   Android Context to access assets.
     * @param env       OrtEnvironment instance.
     * @param assetPath Path to ONNX model inside assets folder (e.g., "animagine-xl/text_encoder.onnx").
     * @return Initialized OrtSession ready for inference.
     * @throws IOException    If the model file cannot be read.
     * @throws OrtException   If ONNX Runtime fails to create a session.
     */
    public static OrtSession loadModelFromAssets(Context context, OrtEnvironment env, String assetPath)
            throws IOException, OrtException {
        AssetManager assetManager = context.getAssets();
        try (InputStream inputStream = assetManager.open(assetPath)) {
            byte[] modelBytes = new byte[inputStream.available()];
            int bytesRead = inputStream.read(modelBytes);
            if (bytesRead != modelBytes.length) {
                throw new IOException("Failed to read the complete ONNX model file.");
            }
            return env.createSession(modelBytes, new OrtSession.SessionOptions());
        }
    }

    /**
     * Runs a dummy inference on the given ONNX Runtime session.
     * Adjust the input tensor shape and data to match your model's requirements.
     *
     * @param env     OrtEnvironment instance.
     * @param session OrtSession loaded with your model.
     * @throws OrtException If inference fails.
     */
    public static void runDummyInference(OrtEnvironment env, OrtSession session) throws OrtException {
        // Example input shape: [1, 3, 224, 224] (modify as needed)
        long[] inputShape = new long[]{1, 3, 224, 224};
        float[] inputData = new float[(int) (inputShape[0] * inputShape[1] * inputShape[2] * inputShape[3])];

        // Fill dummy data with 1.0f
        for (int i = 0; i < inputData.length; i++) {
            inputData[i] = 1.0f;
        }

        // Create input tensor from float buffer
        try (OnnxTensor inputTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(inputData), inputShape)) {

            // Get the first input name from the model
            String inputName = session.getInputNames().iterator().next();

            // Prepare input map for inference
            Map<String, OnnxTensor> inputs = Collections.singletonMap(inputName, inputTensor);

            // Run the model inference
            try (OrtSession.Result results = session.run(inputs)) {
                // Log output names and shapes
                results.forEach((name, value) -> {
                    long[] shape = value.getInfo().getShape();
                    Log.i(TAG, "Output name: " + name + ", shape: " + java.util.Arrays.toString(shape));
                });
            }
        }
    }
}

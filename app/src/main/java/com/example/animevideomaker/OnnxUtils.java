package com.example.animevideomaker;

import android.content.Context;
import android.content.res.AssetManager;

import ai.onnxruntime.*;
import java.io.IOException;
import java.io.InputStream;

public class OnnxUtils {

    public static OrtSession loadModelFromAssets(Context context, OrtEnvironment env, String assetPath) throws IOException, OrtException {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open(assetPath);
        byte[] modelBytes = new byte[inputStream.available()];
        inputStream.read(modelBytes);
        inputStream.close();
        return env.createSession(modelBytes, new OrtSession.SessionOptions());
    }

}

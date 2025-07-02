package com.example.animevideomaker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int DEFAULT_DURATION_SECONDS = 5;
    private static final int BACKGROUND_WIDTH = 640;
    private static final int BACKGROUND_HEIGHT = 480;

    private static final String MODEL_URL = "https://drive.google.com/uc?export=download&id=1YI72jDRoZraSZjWlgKuMxNY8mFZrZm9P";
    private static final String ZIP_NAME = "models.zip";
    private static final String MODEL_DIR = "ai_model";

    private TextView welcomeText;
    private EditText promptInput;
    private Button btnRender;
    private ProgressBar progressBar;

    private OrtEnvironment ortEnv;
    private OrtSession textEncoderSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        // Start download in background, then load models & enable UI
        new Thread(() -> {
            ensureModelsDownloaded();
            runOnUiThread(() -> {
                checkPermissionsAndLoadModels();
            });
        }).start();

        btnRender.setOnClickListener(v -> {
            btnRender.setEnabled(false);
            welcomeText.setText("");
            progressBar.setVisibility(View.VISIBLE);

            if (hasRequiredPermissions()) {
                processRenderRequest();
            } else {
                requestNecessaryPermissions();
            }
        });
    }

    private void initViews() {
        welcomeText = findViewById(R.id.welcomeText);
        promptInput = findViewById(R.id.promptInput);
        btnRender = findViewById(R.id.btnRender);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        welcomeText.setText("Welcome to Anime Video Maker");
        btnRender.setEnabled(false);  // Disabled until models are ready
    }

    private void checkPermissionsAndLoadModels() {
        if (hasRequiredPermissions()) {
            loadOnnxModelsAsync();
        } else {
            requestNecessaryPermissions();
        }
    }

    private boolean hasRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For simplicity, assume permissions granted for Android 13+
            return true;
        }
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestNecessaryPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            welcomeText.setText("Storage permission is needed to save rendered videos.");
        }
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = grantResults.length > 0;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                loadOnnxModelsAsync();
            } else {
                welcomeText.setText("Permission denied. Cannot render without storage access.");
                btnRender.setEnabled(true);
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    private void loadOnnxModelsAsync() {
        new Thread(() -> {
            try {
                ortEnv = OrtEnvironment.getEnvironment();

                // Load ONNX models from internal storage (ai_model folder)
                File modelDir = new File(getFilesDir(), MODEL_DIR);
                File textEncoderFile = new File(modelDir, "animagine-xl/text_encoder/model.onnx");

                if (!textEncoderFile.exists()) {
                    throw new RuntimeException("Model file not found: " + textEncoderFile.getAbsolutePath());
                }

                textEncoderSession = OnnxUtils.loadModelFromFile(ortEnv, textEncoderFile.getAbsolutePath());

                runOnUiThread(() -> {
                    welcomeText.setText("AI models loaded successfully.");
                    btnRender.setEnabled(true);
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to load ONNX model.", e);
                runOnUiThread(() -> {
                    welcomeText.setText("Failed to initialize AI models.");
                    btnRender.setEnabled(false);
                });
            }
        }).start();
    }

    private void processRenderRequest() {
        new Thread(() -> {
            String prompt = "";
            if (promptInput.getText() != null) {
                prompt = promptInput.getText().toString().trim();
            }

            if (prompt.isEmpty()) {
                runOnUiThread(() -> {
                    welcomeText.setText("Please enter a prompt.");
                    btnRender.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                });
                return;
            }

            AnimationRequest req = AITextParser.parse(prompt);
            if (req == null) {
                runOnUiThread(() -> {
                    welcomeText.setText("Failed to parse prompt. Try again.");
                    btnRender.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                });
                return;
            }

            String characterType = req.characterType != null ? req.characterType : "star";
            String characterColor = req.characterColor != null ? req.characterColor : "blue";
            String action = req.action != null ? req.action : "idle";
            int duration = req.duration > 0 ? req.duration : DEFAULT_DURATION_SECONDS;
            String bgColorName = req.background != null ? req.background.toLowerCase() : "black";

            Character character = new Character();
            character.setType(characterType);
            character.setColor(characterColor);
            character.setAction(action);

            Bitmap bgBitmap = Bitmap.createBitmap(BACKGROUND_WIDTH, BACKGROUND_HEIGHT, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bgBitmap);

            int bgColor = switch (bgColorName) {
                case "white" -> 0xFFFFFFFF;
                case "red" -> 0xFFFF0000;
                case "blue" -> 0xFF0000FF;
                case "gray" -> 0xFF888888;
                default -> 0xFF000000;
            };
            canvas.drawColor(bgColor);

            Scene scene = new Scene();
            scene.setBackground(bgBitmap);
            scene.setDuration(duration);
            scene.setCharacter(character);

            SceneHolder.scene = scene;

            runOnUiThread(() -> {
                welcomeText.setText("Rendering started...");
                progressBar.setVisibility(View.GONE);
                btnRender.setEnabled(true);
                startActivity(new Intent(MainActivity.this, CharacterPreviewActivity.class));
            });
        }).start();
    }

    // --- Model download & unzip methods ---

    private void ensureModelsDownloaded() {
        File modelDir = new File(getFilesDir(), MODEL_DIR);
        if (modelDir.exists() && modelDir.isDirectory() && modelDir.listFiles().length > 0) {
            Log.d(TAG, "Model files already present.");
            return;
        }

        try {
            Log.d(TAG, "Downloading model zip...");
            File zipFile = new File(getFilesDir(), ZIP_NAME);
            downloadFile(MODEL_URL, zipFile);

            Log.d(TAG, "Extracting...");
            unzip(zipFile.getAbsolutePath(), modelDir.getAbsolutePath());

            zipFile.delete();
            Log.d(TAG, "Models downloaded and extracted.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to download or extract models", e);
            runOnUiThread(() -> {
                welcomeText.setText("Error downloading models: " + e.getMessage());
            });
        }
    }

    private void downloadFile(String fileURL, File destination) throws Exception {
        URL url = new URL(fileURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();

        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Server returned HTTP " + conn.getResponseCode()
                    + " " + conn.getResponseMessage());
        }

        InputStream input = conn.getInputStream();
        FileOutputStream output = new FileOutputStream(destination);

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }

        output.close();
        input.close();
    }

    private void unzip(String zipFilePath, String destDirectory) throws Exception {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) destDir.mkdirs();

        ZipInputStream zipIn = new ZipInputStream(new java.io.FileInputStream(zipFilePath));
        ZipEntry entry;
        while ((entry = zipIn.getNextEntry()) != null) {
            File outFile = new File(destDir, entry.getName());
            if (entry.isDirectory()) {
                outFile.mkdirs();
            } else {
                outFile.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(outFile);
                byte[] buffer = new byte[4096];
                int len;
                while ((len = zipIn.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipIn.closeEntry();
        }
        zipIn.close();
    }
                   }

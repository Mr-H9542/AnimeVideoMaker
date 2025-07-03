package com.example.animevideomaker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
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

    // Google Drive file ID for your model ZIP
    private static final String DRIVE_FILE_ID = "1YNmra-wLt-VFdTfcg2BRyc9aBXQbNL8G";
    private static final String MODEL_URL = "https://drive.google.com/uc?export=download&id=" + DRIVE_FILE_ID;
    private static final String ZIP_NAME = "models.zip";
    private static final String MODEL_DIR = "ai_model";

    private TextView welcomeText;
    private EditText promptInput;
    private Button btnRender;
    private ProgressBar progressBar;

    private OrtEnvironment ortEnv;
    private OrtSession textEncoderSession;

    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        // Start model download and load process on background thread
        new Thread(() -> {
            try {
                ensureModelsDownloaded();
                mainHandler.post(this::checkPermissionsAndLoadModels);
            } catch (Exception e) {
                Log.e(TAG, "Error during model download/init", e);
                mainHandler.post(() -> {
                    welcomeText.setText("Error downloading models: " + e.getMessage());
                    btnRender.setEnabled(false);
                    progressBar.setVisibility(View.GONE);
                });
            }
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
        btnRender.setEnabled(false); // Disable until models load
    }

    private void checkPermissionsAndLoadModels() {
        if (hasRequiredPermissions()) {
            loadOnnxModelsAsync();
        } else {
            requestNecessaryPermissions();
        }
    }

    private boolean hasRequiredPermissions() {
        // Since we only save models internally, no storage permission needed for Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true;
        }
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestNecessaryPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    PERMISSION_REQUEST_CODE);
        } else {
            // No permissions needed for internal storage on Android 10+
            loadOnnxModelsAsync();
        }
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

                File modelDir = new File(getFilesDir(), MODEL_DIR);
                File textEncoderFile = new File(modelDir, "animagine-xl/text_encoder/model.onnx");

                if (!textEncoderFile.exists()) {
                    throw new RuntimeException("Model file not found: " + textEncoderFile.getAbsolutePath());
                }

                textEncoderSession = OnnxUtils.loadModelFromFile(ortEnv, textEncoderFile.getAbsolutePath());

                mainHandler.post(() -> {
                    welcomeText.setText("AI models loaded successfully.");
                    btnRender.setEnabled(true);
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to load ONNX model.", e);
                mainHandler.post(() -> {
                    welcomeText.setText("Failed to initialize AI models.");
                    btnRender.setEnabled(false);
                });
            }
        }).start();
    }

    private void processRenderRequest() {
        new Thread(() -> {
            String prompt = promptInput.getText() != null ? promptInput.getText().toString().trim() : "";

            if (prompt.isEmpty()) {
                mainHandler.post(() -> {
                    welcomeText.setText("Please enter a prompt.");
                    btnRender.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                });
                return;
            }

            AnimationRequest req = AITextParser.parse(prompt);
            if (req == null) {
                mainHandler.post(() -> {
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

            mainHandler.post(() -> {
                welcomeText.setText("Rendering started...");
                progressBar.setVisibility(View.GONE);
                btnRender.setEnabled(true);
                startActivity(new Intent(MainActivity.this, CharacterPreviewActivity.class));
            });
        }).start();
    }

    // --- Model download & unzip with Google Drive confirmation token handling ---

    private void ensureModelsDownloaded() throws Exception {
        File modelDir = new File(getFilesDir(), MODEL_DIR);
        if (modelDir.exists() && modelDir.isDirectory() && modelDir.listFiles().length > 0) {
            Log.d(TAG, "Model files already present.");
            return;
        }

        Log.d(TAG, "Downloading model zip...");
        File zipFile = new File(getFilesDir(), ZIP_NAME);
        downloadFileFromGoogleDrive(DRIVE_FILE_ID, zipFile);

        Log.d(TAG, "Extracting model files...");
        unzip(zipFile.getAbsolutePath(), modelDir.getAbsolutePath());

        boolean deleted = zipFile.delete();
        if (!deleted) {
            Log.w(TAG, "Could not delete temporary zip file.");
        }

        Log.d(TAG, "Models downloaded and extracted.");
    }

    /**
     * Downloads a file from Google Drive with confirmation token handling for large files.
     * @param fileId Google Drive file ID
     * @param destination Local file destination
     * @throws Exception
     */
    private void downloadFileFromGoogleDrive(String fileId, File destination) throws Exception {
        String baseUrl = "https://drive.google.com/uc?export=download";

        // First request to get confirmation token if needed
        URL url = new URL(baseUrl + "&id=" + fileId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(false);

        String confirmToken = null;
        Map<String, List<String>> headers = conn.getHeaderFields();

        // Look for "Set-Cookie" headers containing confirmation token
        List<String> cookies = headers.get("Set-Cookie");
        if (cookies != null) {
            for (String cookie : cookies) {
                if (cookie.contains("download_warning")) {
                    int start = cookie.indexOf("download_warning");
                    int end = cookie.indexOf(";", start);
                    confirmToken = cookie.substring(start, end);
                    break;
                }
            }
        }

        // If confirmToken not found in cookies, try to parse from response body
        if (confirmToken == null) {
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line

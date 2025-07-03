package com.example.animevideomaker;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.Toast;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private static final int MAX_ZIP_SIZE = 1024 * 1024 * 500; // 500 MB max
    private static final int MAX_RETRIES = 3;
    private static final String DRIVE_FILE_ID = "1YNmra-wLt-VFdTfcg2BRyc9aBXQbNL8G"; // From provided link
    private static final String MODEL_URL = "https://drive.google.com/uc?export=download&id=" + DRIVE_FILE_ID;
    private static final String ZIP_NAME = "models.zip";
    private static final String MODEL_DIR = "ai_model";

    private TextView welcomeText;
    private EditText promptInput;
    private Button btnRender;
    private ProgressBar progressBar;
    private AlertDialog loadingDialog;

    private OrtEnvironment ortEnv;
    private OrtSession textEncoderSession;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupLoadingDialog();

        // Start model download and initialization
        executor.execute(() -> {
            try {
                ensureModelsDownloaded();
                mainHandler.post(this::checkPermissionsAndLoadModels);
            } catch (Exception e) {
                Log.e(TAG, "Error during model download/init", e);
                mainHandler.post(() -> showError("Error downloading models: " + e.getMessage()));
            }
        });

        btnRender.setOnClickListener(v -> {
            btnRender.setEnabled(false);
            showLoadingDialog("Processing request...");
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
        btnRender.setEnabled(false);
    }

    private void setupLoadingDialog() {
        loadingDialog = new AlertDialog.Builder(this)
                .setView(R.layout.dialog_loading) // Assumes a layout with a ProgressBar and TextView
                .setCancelable(false)
                .create();
    }

    private void showLoadingDialog(String message) {
        if (loadingDialog != null) {
            loadingDialog.setMessage(message);
            loadingDialog.show();
        }
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private boolean hasRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true; // Scoped storage, no permissions needed for internal storage
        }
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestNecessaryPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this)
                        .setTitle("Storage Permission Needed")
                        .setMessage("This app needs storage access to save and load AI models.")
                        .setPositiveButton("Grant", (dialog, which) -> ActivityCompat.requestPermissions(
                                MainActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PERMISSION_REQUEST_CODE))
                        .setNegativeButton("Deny", (dialog, which) -> showError("Permission denied. Cannot proceed without storage access."))
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            }
        } else {
            loadOnnxModelsAsync();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
                showError("Permission denied. Cannot render without storage access.");
            }
        }
    }

    private void loadOnnxModelsAsync() {
        executor.execute(() -> {
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
                    hideLoadingDialog();
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to load ONNX model", e);
                mainHandler.post(() -> showError("Failed to initialize AI models: " + e.getMessage()));
            }
        });
    }

    private void processRenderRequest() {
        executor.execute(() -> {
            String prompt = promptInput.getText() != null ? promptInput.getText().toString().trim() : "";
            if (prompt.isEmpty()) {
                mainHandler.post(() -> showError("Please enter a valid prompt."));
                return;
            }

            AnimationRequest req = AITextParser.parse(prompt);
            if (req == null) {
                mainHandler.post(() -> showError("Failed to parse prompt. Try again."));
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
                hideLoadingDialog();
                btnRender.setEnabled(true);
                startActivity(new Intent(MainActivity.this, CharacterPreviewActivity.class));
            });
        });
    }

    private void ensureModelsDownloaded() throws Exception {
        File modelDir = new File(getFilesDir(), MODEL_DIR);
        if (modelDir.exists() && modelDir.isDirectory() && modelDir.listFiles().length > 0) {
            Log.d(TAG, "Model files already present.");
            return;
        }

        mainHandler.post(() -> showLoadingDialog("Downloading AI models..."));
        File zipFile = new File(getFilesDir(), ZIP_NAME);
        downloadFileFromGoogleDrive(DRIVE_FILE_ID, zipFile);

        mainHandler.post(() -> showLoadingDialog("Extracting AI models..."));
        unzip(zipFile.getAbsolutePath(), modelDir.getAbsolutePath());

        if (!zipFile.delete()) {
            Log.w(TAG, "Could not delete temporary zip file.");
        }
        Log.d(TAG, "Models downloaded and extracted.");
    }

    private void downloadFileFromGoogleDrive(String fileId, File destination) throws Exception {
        String baseUrl = "https://drive.google.com/uc?export=download";
        int attempt = 0;

        while (attempt < MAX_RETRIES) {
            try {
                URL url = new URL(baseUrl + "&id=" + fileId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(false);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                conn.connect();

                String confirmToken = getConfirmToken(conn);
                InputStream inputStream;

                if (confirmToken != null) {
                    String downloadUrl = baseUrl + "&confirm=" + confirmToken + "&id=" + fileId;
                    URL confirmedUrl = new URL(downloadUrl);
                    HttpURLConnection downloadConn = (HttpURLConnection) confirmedUrl.openConnection();
                    downloadConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                    downloadConn.connect();
                    inputStream = downloadConn.getInputStream();
                } else {
                    inputStream = conn.getInputStream();
                }

                try (FileOutputStream output = new FileOutputStream(destination)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    long totalBytes = 0;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        totalBytes += bytesRead;
                        if (totalBytes > MAX_ZIP_SIZE) {
                            throw new SecurityException("ZIP file exceeds maximum size limit.");
                        }
                        output.write(buffer, 0, bytesRead);
                    }
                } finally {
                    inputStream.close();
                }
                Log.d(TAG, "Downloaded ZIP file size: " + destination.length() + " bytes");
                return; // Success, exit retry loop
            } catch (Exception e) {
                attempt++;
                if (attempt >= MAX_RETRIES) {
                    throw new Exception("Failed to download file after " + MAX_RETRIES + " attempts: " + e.getMessage(), e);
                }
                Thread.sleep(1000 * attempt); // Exponential backoff
            }
        }
    }

    private String getConfirmToken(HttpURLConnection conn) throws Exception {
        Map<String, List<String>> headers = conn.getHeaderFields();
        List<String> cookies = headers.get("Set-Cookie");
        if (cookies != null) {
            for (String cookie : cookies) {
                if (cookie.contains("download_warning")) {
                    String[] parts = cookie.split(";");
                    for (String part : parts) {
                        if (part.startsWith("download_warning")) {
                            return part.split("=")[1].trim();
                        }
                    }
                }
            }
        }

        try (InputStream is = conn.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("confirm=")) {
                    int start = line.indexOf("confirm=") + 8;
                    int end = line.indexOf("&", start);
                    if (end == -1) end = line.indexOf("\"", start);
                    if (end != -1) {
                        return line.substring(start, end);
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to parse confirmation token from HTML", e);
        }
        return null;
    }

    private void unzip(String zipFilePath, String destDir) throws Exception {
        File destDirFile = new File(destDir);
        if (!destDirFile.exists()) destDirFile.mkdirs();
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File newFile = new File(destDirFile, entry.getName());
                if (!newFile.getCanonicalPath().startsWith(destDirFile.getCanonicalPath())) {
                    throw new SecurityException("Invalid ZIP entry: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[4096];
                        int len;
                        long totalBytes = 0;
                        while ((len = zis.read(buffer)) > 0) {
                            totalBytes += len;
                            if (totalBytes > MAX_ZIP_SIZE) {
                                throw new SecurityException("ZIP entry exceeds maximum size limit.");
                            }
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }

    private void showError(String message) {
        welcomeText.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        btnRender.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        hideLoadingDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textEncoderSession != null) {
            try {
                textEncoderSession.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing OrtSession", e);
            }
        }
        if (ortEnv != null) {
            try {
                ortEnv.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing OrtEnvironment", e);
            }
        }
        executor.shutdownNow();
    }
                         }

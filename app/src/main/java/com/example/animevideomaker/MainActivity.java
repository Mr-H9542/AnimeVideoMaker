package com.example.animevideomaker;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int MAX_PROMPT_LENGTH = 100;

    private TextView welcomeText;
    private EditText promptInput;
    private Button btnRender;
    private ProgressBar progressBar;
    private AlertDialog loadingDialog;

    private OrtEnvironment ortEnv;
    private OrtSession textEncoderSession;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static final List<String> VALID_COLORS = Arrays.asList("red", "blue", "green", "yellow");
    private static final List<String> VALID_TYPES = Arrays.asList("star", "ball", "cat");
    private static final List<String> VALID_ACTIONS = Arrays.asList("bounce", "rotate", "walk", "jump", "idle");
    private static final List<String> VALID_BACKGROUNDS = Arrays.asList("white", "red", "blue", "gray", "black");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setupLoadingDialog();
        setupPromptValidation();
        checkAndRequestPermissions();
        btnRender.setOnClickListener(v -> onRenderClicked());
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
                .setView(R.layout.dialog_loading)
                .setCancelable(false)
                .create();
    }

    private void setupPromptValidation() {
        promptInput.setHint("E.g., 'red star bounce on blue background for 5 seconds'");
        promptInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                validatePrompt(s.toString());
            }
        });
    }

    private void validatePrompt(String input) {
        String trimmed = input.trim().replace("\n", " ");
        AnimationRequest req = AITextParser.parse(trimmed);

        boolean isValid = !trimmed.isEmpty() && trimmed.length() <= MAX_PROMPT_LENGTH &&
                req != null && req.characterType != null && req.characterColor != null && req.action != null;

        if (isValid) {
            promptInput.setError(null);
            btnRender.setEnabled(textEncoderSession != null);
        } else {
            promptInput.setError("Invalid prompt. Try: 'color type action on background for duration seconds'");
            btnRender.setEnabled(false);
        }
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // No need for storage permissions on Android 10+
            startModelDownload();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            } else {
                startModelDownload();
            }
        }
    }

    private void startModelDownload() {
        showLoadingDialog("Downloading AI model...");
        ModelDownloader.downloadModelIfNeeded(this, new ModelDownloader.DownloadListener() {
            @Override
            public void onDownloadSuccess(File modelFile) {
                Log.i(TAG, "Model download success: " + modelFile.getAbsolutePath());
                mainHandler.post(() -> {
                    showLoadingDialog("Loading AI model...");
                    loadOnnxModelAsync(modelFile);
                });
            }

            @Override
            public void onDownloadFailed(String error) {
                Log.e(TAG, "Model download failed: " + error);
                mainHandler.post(() -> {
                    hideLoadingDialog();
                    showError("Failed to download AI model: " + error);
                });
            }
        });
    }

    private void loadOnnxModelAsync(File modelFile) {
        executor.execute(() -> {
            try {
                ortEnv = OrtEnvironment.getEnvironment();
                if (!modelFile.exists()) {
                    throw new RuntimeException("Model file missing: " + modelFile.getAbsolutePath());
                }
                textEncoderSession = OnnxUtils.loadModelFromFile(ortEnv, modelFile.getAbsolutePath());
                mainHandler.post(() -> {
                    welcomeText.setText("AI model loaded successfully.");
                    btnRender.setEnabled(promptInput.getError() == null && !promptInput.getText().toString().trim().isEmpty());
                    hideLoadingDialog();
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed loading ONNX model", e);
                mainHandler.post(() -> {
                    hideLoadingDialog();
                    showError("AI model loading failed: " + e.getMessage());
                });
            }
        });
    }

    private void onRenderClicked() {
        btnRender.setEnabled(false);
        showLoadingDialog("Processing your prompt...");

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            // Ask for permission again if missing
            checkAndRequestPermissions();
            return;
        }

        processRenderRequest();
    }

    private void processRenderRequest() {
        executor.execute(() -> {
            String input = promptInput.getText().toString().trim().replace("\n", " ");
            AnimationRequest req = AITextParser.parse(input);

            if (req == null || input.isEmpty() || input.length() > MAX_PROMPT_LENGTH) {
                mainHandler.post(() -> showError("Invalid or incomplete prompt."));
                return;
            }

            if (!VALID_COLORS.contains(req.characterColor.toLowerCase()) ||
                !VALID_TYPES.contains(req.characterType.toLowerCase()) ||
                !VALID_ACTIONS.contains(req.action.toLowerCase()) ||
                !VALID_BACKGROUNDS.contains(req.background.toLowerCase())) {
                mainHandler.post(() -> showError("Unsupported values in prompt. Please follow the examples."));
                return;
            }

            try {
                Scene scene = new Scene();
                scene.configureFromRequest(req);

                File sceneFile = new File(getCacheDir(), "scene_" + System.currentTimeMillis() + ".ser");
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(sceneFile))) {
                    oos.writeObject(scene);
                }

                Intent previewIntent = new Intent(MainActivity.this, CharacterPreviewActivity.class);
                previewIntent.putExtra("scene_file_path", sceneFile.getAbsolutePath());

                mainHandler.post(() -> {
                    welcomeText.setText("Rendering scene...");
                    hideLoadingDialog();
                    btnRender.setEnabled(true);
                    startActivity(previewIntent);
                });

            } catch (Exception e) {
                Log.e(TAG, "Scene rendering failed", e);
                mainHandler.post(() -> showError("Scene generation failed: " + e.getMessage()));
            }
        });
    }

    private void showLoadingDialog(String message) {
        if (loadingDialog != null && !loadingDialog.isShowing()) {
            loadingDialog.show();
        }
        // The dialog uses a custom layout, so update text via this method:
        View view = loadingDialog.findViewById(R.id.loadingMessage);
        if (view instanceof TextView) {
            ((TextView) view).setText(message);
        }
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void showError(String message) {
        welcomeText.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        btnRender.setEnabled(promptInput.getError() == null);
        progressBar.setVisibility(View.GONE);
        hideLoadingDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (textEncoderSession != null) textEncoderSession.close();
            if (ortEnv != null) ortEnv.close();
        } catch (Exception e) {
            Log.e(TAG, "Error closing ONNX resources", e);
        }
        executor.shutdownNow();
    }

    @Override
    public void onRequestPermissionsResult(int code, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(code, permissions, grantResults);
        if (code == PERMISSION_REQUEST_CODE) {
            boolean granted = true;
            for (int r : grantResults) {
                if (r != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }
            if (granted) {
                startModelDownload();
            } else {
                showError("Storage permission denied. Cannot continue.");
            }
        }
    }
            }

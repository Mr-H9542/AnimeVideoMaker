package com.example.animevideomaker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

public class MainActivity extends AppCompatActivity {
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
        checkPermissionsAndStart();
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
        promptInput.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(CharSequence s) {
                validatePrompt(s.toString());
            }
        });
    }

    private void validatePrompt(String input) {
        String trimmed = input.trim().replace("\n", " ");
        AnimationRequest req = AITextParser.parse(trimmed);

        boolean isValid = !trimmed.isEmpty()
                && trimmed.length() <= MAX_PROMPT_LENGTH
                && req != null
                && VALID_COLORS.contains(req.characterColor.toLowerCase())
                && VALID_TYPES.contains(req.characterType.toLowerCase())
                && VALID_ACTIONS.contains(req.action.toLowerCase())
                && VALID_BACKGROUNDS.contains(req.background.toLowerCase());

        if (isValid && textEncoderSession != null) {
            promptInput.setError(null);
            btnRender.setEnabled(true);
        } else {
            promptInput.setError("Try: 'red star bounce on blue background for 5 seconds'");
            btnRender.setEnabled(false);
        }
    }

    private void checkPermissionsAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startModelDownload();
        } else {
            if (!hasStoragePermissions()) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            } else {
                startModelDownload();
            }
        }
    }

    private boolean hasStoragePermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void startModelDownload() {
        showLoadingDialog("Downloading AI models...");
        ModelDownloader.downloadModelsIfNeeded(this, new ModelDownloader.DownloadListener() {
            @Override
            public void onDownloadSuccess(File modelDir) {
                mainHandler.post(() -> {
                    showLoadingDialog("Loading ONNX models...");
                    loadTextEncoderModel(new File(modelDir, "text_encoder/text_encoder_model.onnx"));
                });
            }

            @Override
            public void onDownloadFailed(String error) {
                Log.e(TAG, "Model download failed: " + error);
                mainHandler.post(() -> {
                    hideLoadingDialog();
                    showError("Download failed: " + error);
                });
            }
        });
    }

    private void loadTextEncoderModel(File modelFile) {
        executor.execute(() -> {
            try {
                ortEnv = OrtEnvironment.getEnvironment();
                textEncoderSession = OnnxUtils.loadModelFromFile(ortEnv, modelFile.getAbsolutePath());
                mainHandler.post(() -> {
                    welcomeText.setText("ONNX models loaded!");
                    validatePrompt(promptInput.getText().toString());
                    hideLoadingDialog();
                });
            } catch (Exception e){
                Log.e(TAG, "ONNX load error", e);
                mainHandler.post(() -> {
                    hideLoadingDialog();
                    showError("Model load failed: " + e.getMessage());
                });
            }
        });
    }

    private void onRenderClicked() {
        btnRender.setEnabled(false);
        showLoadingDialog("Processing prompt...");
        processRenderRequest();
    }

    private void processRenderRequest() {
        executor.execute(() -> {
            String input = promptInput.getText().toString().trim().replace("\n", " ");
            AnimationRequest req = AITextParser.parse(input);
            if (req == null) {
                mainHandler.post(() -> showError("Invalid prompt."));
                return;
            }
            // build scene and serialize
            try {
                Scene scene = new Scene();
                scene.configureFromRequest(req);
                File sceneFile = new File(getCacheDir(), "scene_" + System.currentTimeMillis() + ".ser");
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(sceneFile))) {
                    oos.writeObject(scene);
                }
                Intent intent = new Intent(MainActivity.this, CharacterPreviewActivity.class);
                intent.putExtra("scene_file_path", sceneFile.getAbsolutePath());
                mainHandler.post(() -> {
                    hideLoadingDialog();
                    btnRender.setEnabled(true);
                    startActivity(intent);
                });
            } catch (Exception e) {
                Log.e(TAG, "Scene render error", e);
                mainHandler.post(() -> showError("Scene generation failed."));
            }
        });
    }

    private void showLoadingDialog(String message) {
        if (loadingDialog != null && !loadingDialog.isShowing()) {
            loadingDialog.show();
        }
        TextView txt = loadingDialog.findViewById(R.id.loadingMessage);
        if (txt != null) txt.setText(message);
    }

    private void hideLoadingDialog(){
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void showError(String msg){
        welcomeText.setText(msg);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        btnRender.setEnabled(promptInput.getError() == null);
        hideLoadingDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (textEncoderSession != null) textEncoderSession.close();
            if (ortEnv != null) ortEnv.close();
        } catch(Exception e){
            Log.e(TAG, "Error closing ONNX", e);
        }
        executor.shutdownNow();
    }

    @Override
    public void onRequestPermissionsResult(int code, @NonNull String[] perms, @NonNull int[] results){
        super.onRequestPermissionsResult(code, perms, results);
        if (code == PERMISSION_REQUEST_CODE) {
            if (Arrays.stream(results).allMatch(r -> r == PackageManager.PERMISSION_GRANTED)) {
                startModelDownload();
            } else {
                showError("Storage permissions denied.");
            }
        }
    }
            }

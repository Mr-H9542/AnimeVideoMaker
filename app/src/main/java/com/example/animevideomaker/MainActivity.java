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

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;

public class MainActivity extends Activity {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int DEFAULT_DURATION_SECONDS = 5;
    private static final int BACKGROUND_WIDTH = 640;
    private static final int BACKGROUND_HEIGHT = 480;

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
        checkPermissionsAndLoadModels();

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
            // Android 13+ uses different permissions, assume granted or manage accordingly
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
                textEncoderSession = OnnxUtils.loadModelFromAssets(this, ortEnv, "animagine-xl/text_encoder/model.onnx");
                runOnUiThread(() -> {
                    welcomeText.setText("AI models loaded successfully.");
                    btnRender.setEnabled(true);
                });
            } catch (Exception e) {
                Log.e("ONNX", "Failed to load ONNX model.", e);
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
}

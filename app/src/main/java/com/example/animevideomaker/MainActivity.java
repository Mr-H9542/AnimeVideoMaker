package com.example.animevideomaker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends Activity {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int DEFAULT_DURATION_SECONDS = 5;
    private static final int BACKGROUND_WIDTH = 640;
    private static final int BACKGROUND_HEIGHT = 480;

    private TextView welcomeText;
    private EditText promptInput;
    private Button btnRender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        welcomeText = findViewById(R.id.welcomeText);
        promptInput = findViewById(R.id.promptInput);
        btnRender = findViewById(R.id.btnRender);

        welcomeText.setText("Welcome to Anime Video Maker");

        btnRender.setOnClickListener(v -> {
            btnRender.setEnabled(false); // Prevent multiple clicks
            if (hasRequiredPermissions()) {
                processRenderRequest();
                btnRender.setEnabled(true);
            } else {
                requestNecessaryPermissions();
            }
        });
    }

    private boolean hasRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Scoped storage on Android 13+ no longer requires storage permission for this use-case
            return true;
        }
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestNecessaryPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            if (grantResults.length == 0) {
                allGranted = false;
            } else {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }
            }

            if (allGranted) {
                processRenderRequest();
            } else {
                welcomeText.setText("Permission denied. Cannot render without storage access.");
            }
            btnRender.setEnabled(true);
        }
    }

    private void processRenderRequest() {
        String prompt = promptInput.getText() != null ? promptInput.getText().toString().trim() : "";

        if (prompt.isEmpty()) {
            welcomeText.setText("Please enter a prompt.");
            btnRender.setEnabled(true);
            return;
        }

        AnimationRequest req = AITextParser.parse(prompt);
        if (req == null) {
            welcomeText.setText("Failed to parse prompt. Try again.");
            btnRender.setEnabled(true);
            return;
        }

        // Use defaults if null or invalid
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
            default -> 0xFF000000; // black fallback
        };
        canvas.drawColor(bgColor);

        Scene scene = new Scene();
        scene.setBackground(bgBitmap);
        scene.setDuration(duration);
        scene.setCharacter(character);

        SceneHolder.scene = scene;

        welcomeText.setText("Rendering started...");
        startActivity(new Intent(MainActivity.this, CharacterPreviewActivity.class));
    }
}

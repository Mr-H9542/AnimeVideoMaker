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
import java.util.List;

public class MainActivity extends Activity {

    private static final int PERMISSION_REQUEST_CODE = 1001;

    private TextView welcomeText;
    private EditText promptInput;
    private Button btnRender;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        welcomeText = findViewById(R.id.welcomeText);
        promptInput = findViewById(R.id.promptInput);
        btnRender = findViewById(R.id.btnRender);

        welcomeText.setText("Welcome to Anime Video Maker");

        btnRender.setOnClickListener(v -> {
            if (checkAndRequestPermissions()) {
                handleRender();
            }
        });
    }

    private boolean checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ does not require storage permissions for scoped access
            return true;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                PERMISSION_REQUEST_CODE
            );
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int res : grantResults) {
                if (res != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                handleRender();
            } else {
                welcomeText.setText("Permission denied. Cannot render without storage access.");
            }
        }
    }

    private void handleRender() {
        String prompt = promptInput.getText().toString().trim();
        if (prompt.isEmpty()) {
            welcomeText.setText("Please enter a prompt");
            return;
        }

        AnimationRequest req = AITextParser.parse(prompt);

        Character character = new Character();
        character.setType(req.characterType);
        character.setColor(req.characterColor);
        character.setAction(req.action);

        Bitmap bg = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bg);
        int bgColor = req.background.equals("white") ? 0xFFFFFFFF : 0xFF000000;
        canvas.drawColor(bgColor);

        Scene scene = new Scene(bg);
        scene.setDuration(req.duration);
        scene.addCharacter(character);

        SceneHolder.scene = scene;
        startActivity(new Intent(MainActivity.this, CharacterPreviewActivity.class));
    }
}

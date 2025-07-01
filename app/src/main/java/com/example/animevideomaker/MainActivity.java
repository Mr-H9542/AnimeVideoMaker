package com.example.animevideomaker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

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
            String prompt = promptInput.getText().toString().trim();

            if (prompt.isEmpty()) {
                welcomeText.setText("Please enter a prompt");
                return;
            }

            // Parse input
            AnimationRequest req = AITextParser.parse(prompt);

            // Build character
            Character character = new Character();
            character.setType(req.characterType);
            character.setColor(req.characterColor);
            character.setAction(req.action);

            // Create background
            Bitmap bg = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bg);
            int bgColor = req.background.equals("white") ? 0xFFFFFFFF : 0xFF000000;
            canvas.drawColor(bgColor);

            // Build scene
            Scene scene = new Scene(bg);
            scene.setDuration(req.duration);
            scene.addCharacter(character);

            // Pass scene to preview
            SceneHolder.scene = scene;
            startActivity(new Intent(MainActivity.this, CharacterPreviewActivity.class));
        });
    }
}

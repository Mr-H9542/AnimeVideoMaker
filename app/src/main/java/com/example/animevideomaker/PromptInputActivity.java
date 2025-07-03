package com.example.animevideomaker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PromptInputActivity extends Activity {

    private EditText promptInput;
    private Button btnGenerate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prompt_input);

        promptInput = findViewById(R.id.promptInput);
        btnGenerate = findViewById(R.id.btnGenerate);

        btnGenerate.setOnClickListener(view -> handlePrompt());
    }

    private void handlePrompt() {
        String prompt = promptInput.getText().toString().trim();

        if (prompt.isEmpty()) {
            Toast.makeText(this, "Please enter a prompt.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            AnimationRequest request = AITextParser.parse(prompt);

            // Build character from request
            Character character = new Character();
            character.setType(request.characterType);
            character.setColor(request.characterColor);
            character.setAction(request.action);

            // Create a blank background
            Bitmap bg = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bg);
            int bgColor = request.background.equalsIgnoreCase("white") ? 0xFFFFFFFF : 0xFF000000;
            canvas.drawColor(bgColor);

            // Create scene
            Scene scene = new Scene(bg);  // Ensure Scene has a Bitmap constructor
            scene.setDuration(request.duration);
            scene.addCharacter(character);

            // Pass scene via holder
            SceneHolder.scene = scene;

            // Launch preview
            Intent intent = new Intent(this, CharacterPreviewActivity.class);
            startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to generate animation from prompt.", Toast.LENGTH_SHORT).show();
        }
    }
}

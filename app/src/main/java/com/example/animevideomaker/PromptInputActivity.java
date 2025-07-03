package com.example.animevideomaker;

import android.app.Activity;
import android.content.Intent;
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

        btnGenerate.setOnClickListener(v -> handlePrompt());
    }

    private void handlePrompt() {
        String prompt = promptInput.getText().toString().trim();

        if (prompt.isEmpty()) {
            Toast.makeText(this, "Please enter a prompt.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Parse input into an AnimationRequest
            AnimationRequest request = AITextParser.parse(prompt);

            // Configure scene from request
            Scene scene = new Scene();
            scene.configureFromRequest(request);

            // Store scene globally for preview
            SceneHolder.scene = scene;

            // Launch the preview activity
            startActivity(new Intent(this, CharacterPreviewActivity.class));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to generate animation from prompt.", Toast.LENGTH_LONG).show();
        }
    }
}

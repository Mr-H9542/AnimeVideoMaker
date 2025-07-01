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

    EditText promptInput;
    Button btnGenerate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prompt_input);

        promptInput = findViewById(R.id.promptInput);
        btnGenerate = findViewById(R.id.btnGenerate);

        btnGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String prompt = promptInput.getText().toString().trim();

                if (prompt.isEmpty()) {
                    Toast.makeText(PromptInputActivity.this, "Please enter a prompt.", Toast.LENGTH_SHORT).show();
                    return;
                }

                AnimationRequest request = AITextParser.parse(prompt);

                // Build character
                Character character = new Character();
                character.setType(request.characterType);
                character.setColor(request.characterColor);
                character.setAction(request.action);

                // Create background
                Bitmap bg = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bg);
                int bgColor = request.background.equals("white") ? 0xFFFFFFFF : 0xFF000000;
                canvas.drawColor(bgColor);

                // Create scene
                Scene scene = new Scene(bg);
                scene.setDuration(request.duration);
                scene.addCharacter(character);

                // Pass scene using holder
                SceneHolder.scene = scene;

                // Go to preview
                Intent intent = new Intent(PromptInputActivity.this, CharacterPreviewActivity.class);
                startActivity(intent);
            }
        });
    }
}

package com.example.animevideomaker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class PromptInputActivity extends Activity {

    EditText promptInput;
    Button generateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);

        promptInput = new EditText(this);
        promptInput.setHint("Enter animation prompt...");
        layout.addView(promptInput);

        generateButton = new Button(this);
        generateButton.setText("Generate Animation");
        layout.addView(generateButton);

        setContentView(layout);

        generateButton.setOnClickListener(v -> {
            String prompt = promptInput.getText().toString();
            AnimationRequest request = AITextParser.parse(prompt);

            // Build character from request
            Character character = new Character();
            character.setType(request.characterType);
            character.setColor(request.characterColor);
            character.setAction(request.action);

            // Build background bitmap
            Bitmap background = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(background);
            int bgColor = Color.parseColor(request.background.equals("white") ? "#FFFFFF" : "#000000");
            canvas.drawColor(bgColor);

            // Build scene
            Scene scene = new Scene(background);
            scene.setDuration(request.duration);
            scene.addCharacter(character);

            // Pass to preview activity
            SceneHolder.scene = scene;  // static memory holder

            Intent intent = new Intent(PromptInputActivity.this, CharacterPreviewActivity.class);
            startActivity(intent);
        });
    }
}

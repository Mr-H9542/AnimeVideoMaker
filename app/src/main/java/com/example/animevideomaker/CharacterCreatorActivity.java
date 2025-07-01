package com.example.animevideomaker;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class CharacterCreatorActivity extends Activity {

    private ImageView characterPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        characterPreview = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        );
        characterPreview.setLayoutParams(params);

        setContentView(characterPreview);

        // Create a sample character
        Character character = new Character();
        character.setType("star");
        character.setColor("blue");

        // Render a single frame to preview
        CharacterRenderer renderer = new CharacterRenderer();
        Bitmap bitmap = renderer.renderCharacterFrame(character, 400, 400, 0, 1);

        // Show preview image
        characterPreview.setImageBitmap(bitmap);
    }
}

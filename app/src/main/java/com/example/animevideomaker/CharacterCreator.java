package com.example.animevideomaker;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CharacterCreator extends AppCompatActivity {

    private LinearLayout characterList;
    private AssetManager assetManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_creator);

        characterList = findViewById(R.id.characterList);
        assetManager = getAssets();

        List<Character> characters = loadCharacters();
        showCharacterPreviews(characters);
    }

    private List<Character> loadCharacters() {
        List<Character> characters = new ArrayList<>();
        try {
            String[] folders = assetManager.list("characters");
            if (folders != null) {
                for (String folder : folders) {
                    Character character = new Character();
                    
                    // Infer properties from folder name if possible
                    String[] parts = folder.split("_");
                    if (parts.length >= 2) {
                        character.setColor(parts[0]);
                        character.setType(parts[1]);
                    } else {
                        character.setType("star");
                        character.setColor("blue");
                    }

                    character.setAction("idle");
                    characters.add(character);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return characters;
    }

    private void showCharacterPreviews(List<Character> characters) {
        for (Character character : characters) {
            try {
                String path = character.getAssetPath(512); // FIXED METHOD CALL
                InputStream is = assetManager.open(path);
                Bitmap bitmap = BitmapFactory.decodeStream(is);

                ImageView imageView = new ImageView(this);
                imageView.setImageBitmap(bitmap);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 16, 0, 16);
                imageView.setLayoutParams(params);

                characterList.addView(imageView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

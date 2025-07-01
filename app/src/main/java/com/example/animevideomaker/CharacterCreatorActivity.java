package com.example.animevideomaker;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.io.InputStream;

public class CharacterCreatorActivity extends Activity {

    private LinearLayout characterList;
    private AssetManager assetManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_creator);

        characterList = findViewById(R.id.characterList);
        assetManager = getAssets();

        loadAndDisplayCharacters();
    }

    private void loadAndDisplayCharacters() {
        try {
            String[] folders = assetManager.list("characters");
            if (folders != null) {
                for (String folder : folders) {
                    String path = "characters/" + folder + "/idle_512.png";
                    try (InputStream is = assetManager.open(path)) {
                        Bitmap bmp = BitmapFactory.decodeStream(is);

                        ImageView imageView = new ImageView(this);
                        imageView.setImageBitmap(bmp);

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(0, 16, 0, 16);
                        imageView.setLayoutParams(params);

                        // Optional: On click, show character name (folder)
                        imageView.setOnClickListener(v ->
                                Toast.makeText(this, "Selected: " + folder, Toast.LENGTH_SHORT).show());

                        characterList.addView(imageView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

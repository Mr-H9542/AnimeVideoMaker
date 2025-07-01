package com.example.animevideomaker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    TextView welcomeText;
    Button btnCharacterCreator, btnSceneComposer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        welcomeText = findViewById(R.id.welcomeText);
        btnCharacterCreator = findViewById(R.id.btnCharacterCreator);
        btnSceneComposer = findViewById(R.id.btnSceneComposer);

        welcomeText.setText("Welcome to Anime Video Maker");

        btnCharacterCreator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Launch CharacterCreatorActivity
                welcomeText.setText("Character Creator clicked!");
            }
        });

        btnSceneComposer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Launch SceneComposerActivity
                welcomeText.setText("Scene Composer clicked!");
            }
        });
    }
}

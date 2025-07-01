package com.example.animevideomaker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnCharacterCreator;
    private Button btnSceneComposer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find buttons by ID from layout
        btnCharacterCreator = findViewById(R.id.btnCharacterCreator);
        btnSceneComposer = findViewById(R.id.btnSceneComposer);

        // Set click listener to open CharacterCreator activity
        btnCharacterCreator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CharacterCreator.class);
                startActivity(intent);
            }
        });

        // Set click listener to open SceneComposer activity
        btnSceneComposer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SceneComposer.class);
                startActivity(intent);
            }
        });
    }
            }

package com.example.animevideomaker;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView welcomeText = findViewById(R.id.welcomeText);
        welcomeText.setText("Rendering...");

        // Example prompt
        String prompt = "Red ball bouncing on blue background for 5 seconds";

        // Step 1: Parse prompt into animation request
        AnimationRequest request = AITextParser.parse(prompt);

        // Step 2: Generate scene
        Scene scene = new Scene();
        scene.configureFromRequest(request); // <-- You may need to implement this method

        // Step 3: Generate video frames
        List<VideoFrame> frames = FrameGenerator.generate(scene);

        // Step 4: Save first frame as test (to Movies folder)
        File file = TextureStreamer.saveFirstFrame(frames);
        welcomeText.setText("First frame saved to:\n" + file.getAbsolutePath());
    }
}

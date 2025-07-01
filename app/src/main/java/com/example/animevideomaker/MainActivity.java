package com.example.animevideomaker;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.util.List;

public class MainActivity extends Activity {
    private TextView welcomeText;
    private EditText promptInput;
    private Button btnRender;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        welcomeText = findViewById(R.id.welcomeText);
        promptInput = findViewById(R.id.promptInput);
        btnRender = findViewById(R.id.btnRender);

        btnRender.setOnClickListener(v -> {
            String prompt = promptInput.getText().toString().trim();
            if (prompt.isEmpty()) {
                welcomeText.setText("Please enter a prompt");
                return;
            }

            AnimationRequest req = AITextParser.parse(prompt);
            Scene scene = new Scene();
            scene.configureFromRequest(req);

            List<VideoFrame> frames = FrameGenerator.generate(this, scene);
            VideoEncoder.save(frames);

            welcomeText.setText("Rendered: " + req.toString());
        });
    }
}

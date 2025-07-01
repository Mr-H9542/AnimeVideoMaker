package com.example.animevideomaker;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.util.List;

public class CharacterPreviewActivity extends Activity {

    private ImageView characterView;
    private Handler handler = new Handler();
    private List<VideoFrame> frames;
    private int currentFrame = 0;
    private static final int FRAME_DELAY_MS = 100; // 10 FPS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Simple fullscreen container
        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setBackgroundColor(0xFF000000); // black background
        rootLayout.setGravity(Gravity.CENTER);

        characterView = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        characterView.setLayoutParams(params);
        rootLayout.addView(characterView);

        setContentView(rootLayout);

        // Get scene from SceneHolder
        Scene scene = SceneHolder.scene;

        if (scene == null) {
            Toast.makeText(this, "No scene provided!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Generate frames
        frames = FrameGenerator.generate(this, scene);

        if (frames == null || frames.isEmpty()) {
            Toast.makeText(this, "No frames to display!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Start animation loop
        handler.post(frameRunnable);
    }

    private final Runnable frameRunnable = new Runnable() {
        @Override
        public void run() {
            if (frames == null || frames.isEmpty()) return;

            Bitmap bmp = frames.get(currentFrame).getBitmap();
            characterView.setImageBitmap(bmp);

            currentFrame = (currentFrame + 1) % frames.size();
            handler.postDelayed(this, FRAME_DELAY_MS);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(frameRunnable);

        // Free memory
        if (frames != null) {
            for (VideoFrame frame : frames) {
                frame.recycle();
            }
        }
    }
}

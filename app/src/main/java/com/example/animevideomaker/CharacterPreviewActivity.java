package com.example.animevideomaker;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.util.List;

public class CharacterPreviewActivity extends Activity {

    private ImageView characterView;
    private Handler handler = new Handler();
    private List<VideoFrame> frames;
    private int currentFrame = 0;
    private final int FRAME_DELAY_MS = 100;  // 10 FPS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        characterView = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT);
        characterView.setLayoutParams(params);
        setContentView(characterView);

        // Create a sample Character (or load from Intent extras)
        Character character = new Character();
        character.setColor("blue");
        character.setType("star");

        // Create a dummy Scene with a blank background
        Bitmap bg = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
        Scene scene = new Scene(bg);
        scene.addCharacter(character);
        scene.setDuration(5); // 5 seconds animation

        // Generate frames
        frames = FrameGenerator.generate(this, scene);

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

        // Recycle bitmaps to free memory
        if (frames != null) {
            for (VideoFrame frame : frames) {
                frame.recycle();
            }
        }
    }
}

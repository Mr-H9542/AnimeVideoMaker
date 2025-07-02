package com.example.animevideomaker;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.util.List;

public class CharacterPreviewActivity extends Activity {

    private ImageView characterView;
    private final Handler handler = new Handler();
    private List<VideoFrame> frames;
    private int currentFrame = 0;
    private static final int FRAME_DELAY_MS = 100; // 10 FPS

    private final Runnable frameRunnable = new Runnable() {
        @Override
        public void run() {
            if (frames == null || frames.isEmpty()) return;

            Bitmap bmp = frames.get(currentFrame).getBitmap();
            if (bmp != null && !bmp.isRecycled()) {
                characterView.setImageBitmap(bmp);
            }

            currentFrame = (currentFrame + 1) % frames.size();
            handler.postDelayed(this, FRAME_DELAY_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout rootLayout = setupLayout();

        Scene scene = SceneHolder.scene;
        if (scene == null) {
            Toast.makeText(this, "No scene provided!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            frames = FrameGenerator.generate(this, scene);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to generate frames: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (frames == null || frames.isEmpty()) {
            Toast.makeText(this, "No frames to display!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        handler.post(frameRunnable);

        // Save button
        Button saveButton = new Button(this);
        saveButton.setText("Save as Video");
        saveButton.setOnClickListener(v -> {
            Toast.makeText(this, "Saving video...", Toast.LENGTH_SHORT).show();
            new Thread(() -> {
                File savedFile = VideoEncoder.save(frames);
                if (savedFile != null) {
                    // Refresh gallery
                    MediaScannerConnection.scanFile(this,
                            new String[]{savedFile.getAbsolutePath()},
                            new String[]{"video/mp4"}, null);

                    runOnUiThread(() -> Toast.makeText(this, "Video saved:\n" + savedFile.getName(), Toast.LENGTH_LONG).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to save video", Toast.LENGTH_SHORT).show());
                }
            }).start();
        });

        rootLayout.addView(saveButton);
    }

    private LinearLayout setupLayout() {
        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(0xFF000000); // black background
        rootLayout.setGravity(Gravity.CENTER);

        characterView = new ImageView(this);
        characterView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        characterView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));

        rootLayout.addView(characterView);
        setContentView(rootLayout);
        return rootLayout;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(frameRunnable);
        recycleFrames();
    }

    private void recycleFrames() {
        if (frames != null) {
            for (VideoFrame frame : frames) {
                frame.recycle();
            }
            frames.clear();
        }
    }
                                  }

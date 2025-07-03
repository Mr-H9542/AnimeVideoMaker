package com.example.animevideomaker;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.util.List;

public class CharacterPreviewActivity extends Activity {

    private static final int FRAME_DELAY_MS = 100; // 10 FPS
    private final Handler handler = new Handler(Looper.getMainLooper());

    private ImageView characterView;
    private List<VideoFrame> frames;
    private int currentFrame = 0;

    private final Runnable frameUpdater = new Runnable() {
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
        LinearLayout rootLayout = createLayout();

        String sceneFilePath = getIntent().getStringExtra("scene_file_path");
        Scene scene = null;
        if (sceneFilePath != null) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(sceneFilePath))) {
                scene = (Scene) ois.readObject();
                // Restore transient background
                scene.setBackgroundColor(scene.getBackground() == null ? "black" : scene.getBackground().toString());
                new File(sceneFilePath).delete(); // Clean up
            } catch (Exception e) {
                showToast("Failed to load scene: " + e.getMessage());
                finish();
                return;
            }
        }

        if (scene == null) {
            showToast("No scene provided!");
            finish();
            return;
        }

        try {
            frames = FrameGenerator.generate(this, scene);
        } catch (Exception e) {
            showToast("Failed to generate frames: " + e.getMessage());
            finish();
            return;
        }

        if (frames == null || frames.isEmpty()) {
            showToast("No frames to display!");
            finish();
            return;
        }

        handler.post(frameUpdater);
        addSaveButton(rootLayout);
    }

    private LinearLayout createLayout() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(0xFF000000);
        layout.setGravity(Gravity.CENTER);

        characterView = new ImageView(this);
        characterView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        characterView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));

        layout.addView(characterView);
        setContentView(layout);
        return layout;
    }

    private void addSaveButton(LinearLayout parentLayout) {
        Button saveButton = new Button(this);
        saveButton.setText("Save as Video");

        saveButton.setOnClickListener(v -> {
            showToast("Saving video...");
            new Thread(() -> {
                File outputFile = VideoEncoder.save(frames);
                if (outputFile != null) {
                    MediaScannerConnection.scanFile(this,
                            new String[]{outputFile.getAbsolutePath()},
                            new String[]{"video/mp4"}, null);
                    runOnUiThread(() -> showToast("Video saved: " + outputFile.getName()));
                } else {
                    runOnUiThread(() -> showToast("Failed to save video"));
                }
            }).start();
        });

        parentLayout.addView(saveButton);
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(frameUpdater);
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

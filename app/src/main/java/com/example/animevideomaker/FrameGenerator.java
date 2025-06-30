package com.example.animevideomaker;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import java.util.List;

public class FrameGenerator {
    private final CharacterRenderer renderer;

    public FrameGenerator(CharacterRenderer renderer) {
        this.renderer = renderer;
    }

    public VideoFrame generateFrame(Scene scene, int frameIndex) {
        Bitmap frame = Bitmap.createBitmap(720, 1280, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(frame);

        // Draw background
        Bitmap bg = scene.getBackground();
        canvas.drawBitmap(bg, 0, 0, null);

        // Draw characters (depth sorted)
        List<Character> characters = scene.getCharactersByDepth();
        for (Character c : characters) {
            Bitmap charBmp = renderer.renderCharacterFrame(c, 720, 1280);
            canvas.drawBitmap(charBmp, c.getPosition().x, c.getPosition().y, null);
            charBmp.recycle();
        }

        return new VideoFrame(frame, frameIndex);
    }
              }

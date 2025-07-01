package com.example.animevideomaker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates animation frames for a given Scene.
 */
public class FrameGenerator {
    private static final int FPS = 10;

    /**
     * Generates a list of VideoFrame for the Scene animation.
     *
     * @param ctx   Application context (if needed for rendering)
     * @param scene The animation Scene to render
     * @return List of generated VideoFrames
     * @throws IllegalArgumentException if scene or its background or characters are missing
     */
    public static List<VideoFrame> generate(Context ctx, Scene scene) {
        if (scene == null) {
            throw new IllegalArgumentException("Scene cannot be null");
        }
        Bitmap bg = scene.getBackground();
        if (bg == null) {
            throw new IllegalArgumentException("Scene background cannot be null");
        }
        List<Character> chars = scene.getCharactersByDepth();
        if (chars == null || chars.isEmpty()) {
            throw new IllegalArgumentException("Scene must have at least one Character");
        }

        List<VideoFrame> frames = new ArrayList<>();
        CharacterRenderer renderer = new CharacterRenderer(ctx);
        Character mainChar = chars.get(0);

        int totalFrames = scene.getDuration() * FPS;

        for (int i = 0; i < totalFrames; i++) {
            // Create a mutable bitmap for this frame
            Bitmap frameBitmap = Bitmap.createBitmap(bg.getWidth(), bg.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(frameBitmap);

            // Draw background
            canvas.drawBitmap(bg, 0, 0, null);

            // Render character frame for current frame index i
            Bitmap charBitmap = renderer.renderCharacterFrame(mainChar, bg.getWidth(), bg.getHeight(), i, totalFrames);
            canvas.drawBitmap(charBitmap, 0, 0, null);

            // Add to list with index
            frames.add(new VideoFrame(frameBitmap, i));

            // Recycle character bitmap if not needed anymore to save memory
            if (charBitmap != frameBitmap && !charBitmap.isRecycled()) {
                charBitmap.recycle();
            }
        }

        return frames;
    }
}

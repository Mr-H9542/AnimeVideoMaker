package com.example.animevideomaker;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Renders character frames as simple shapes.
 * You can later extend this to real sprites or animations.
 */
public class CharacterRenderer {

    /**
     * Renders a single frame of the given character.
     *
     * @param character   The character to render (must not be null)
     * @param width       Width of the frame bitmap
     * @param height      Height of the frame bitmap
     * @param frameIndex  Current frame index (for animation)
     * @param totalFrames Total frames in animation
     * @return Bitmap representing the rendered frame
     * @throws IllegalArgumentException if character is null
     */
    public Bitmap renderCharacterFrame(Character character, int width, int height, int frameIndex, int totalFrames) {
        if (character == null) {
            throw new IllegalArgumentException("Character cannot be null");
        }

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);

        // Determine color based on character color string
        int color = 0xFFCCCCCC; // default gray fallback
        if (character.getColor() != null) {
            switch (character.getColor().toLowerCase()) {
                case "red" -> color = 0xFFFF4444;
                case "blue" -> color = 0xFF4488FF;
                case "green" -> color = 0xFF44FF44;
            }
        }
        paint.setColor(color);

        float cx = width / 2f;
        float cy = height / 2f;

        // Simple pulsing radius animation based on frameIndex
        float baseRadius = 80f;
        float pulse = (float) (5 * Math.sin(2 * Math.PI * frameIndex / totalFrames));

        String type = character.getType() != null ? character.getType().toLowerCase() : "";

        switch (type) {
            case "ball" -> canvas.drawCircle(cx, cy, 120 + pulse, paint);
            case "star" -> drawStar(canvas, paint, cx, cy, baseRadius + pulse);
            default -> {
                float size = 100f + pulse;
                canvas.drawRect(cx - size / 2, cy - size / 2, cx + size / 2, cy + size / 2, paint);
            }
        }

        return bmp;
    }

    /**
     * Draws a simple star shape centered at (cx, cy).
     * This is a placeholder; you can replace with a real star sprite later.
     *
     * @param canvas Canvas to draw on
     * @param paint  Paint used for drawing
     * @param cx     Center X coordinate
     * @param cy     Center Y coordinate
     * @param radius Radius of the star shape
     */
    private void drawStar(Canvas canvas, Paint paint, float cx, float cy, float radius) {
        // For simplicity, draw 5 circles to simulate a star shape.
        for (int i = 0; i < 5; i++) {
            double angle = Math.toRadians(i * 72); // 360/5 = 72 degrees per point
            float x = cx + (float) (radius * Math.cos(angle));
            float y = cy + (float) (radius * Math.sin(angle));
            canvas.drawCircle(x, y, radius / 5, paint);
        }
    }
}

package com.example.animevideomaker;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class CharacterRenderer {
    public Bitmap renderCharacterFrame(Character character, int width, int height, int frameIndex, int totalFrames) {
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();

        int color = switch (character.getColor()) {
            case "red" -> 0xFFFF4444;
            case "blue" -> 0xFF4488FF;
            case "green" -> 0xFF44FF44;
            default -> 0xFFCCCCCC;
        };
        paint.setColor(color);

        float cx = width / 2f, cy = height / 2f;
        switch (character.getType()) {
            case "star", "ball" -> canvas.drawCircle(cx, cy, character.getType().equals("ball") ? 120 : 80, paint);
        }

        return bmp;
    }
}

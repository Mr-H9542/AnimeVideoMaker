package com.example.animevideomaker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import java.util.ArrayList;
import java.util.List;

public class FrameGenerator {
    private static final int FPS = 10;

    public static List<VideoFrame> generate(Context ctx, Scene scene) {
        List<VideoFrame> frames = new ArrayList<>();
        CharacterRenderer renderer = new CharacterRenderer();
        List<Character> chars = scene.getCharactersByDepth();
        Character ch = chars.get(0);
        Bitmap bg = scene.getBackground();

        int total = scene.getDuration() * FPS;
        for (int i = 0; i < total; i++) {
            Bitmap frameBmp = Bitmap.createBitmap(bg.getWidth(), bg.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(frameBmp);
            canvas.drawBitmap(bg, 0, 0, null);
            Bitmap charBmp = renderer.renderCharacterFrame(ch, bg.getWidth(), bg.getHeight(), i, total);
            canvas.drawBitmap(charBmp, 0, 0, null);
            frames.add(new VideoFrame(frameBmp));
        }
        return frames;
    }
}

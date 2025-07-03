package com.example.animevideomaker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class FrameGenerator {
    private static final String TAG = "FrameGenerator";

    public static List<VideoFrame> generate(Context context, Scene scene) throws Exception {
        List<VideoFrame> frames = new ArrayList<>();
        int fps = 10;
        int frameCount = scene.getDuration() * fps;
        Bitmap background = scene.getBackground();
        List<Character> characters = scene.getCharactersByDepth();

        for (int i = 0; i < frameCount; i++) {
            Bitmap frameBitmap = Bitmap.createBitmap(background.getWidth(), background.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(frameBitmap);
            canvas.drawBitmap(background, 0, 0, null);

            for (Character character : characters) {
                try {
                    // Try to load asset
                    String assetPath = character.getAssetPath(character.getAction(), 64);
                    Bitmap characterBitmap = BitmapFactory.decodeStream(context.getAssets().open(assetPath));
                    float x = character.getPosition() != null ? character.getPosition().x : 100;
                    float y = character.getPosition() != null ? character.getPosition().y : 100;
                    canvas.drawBitmap(characterBitmap, x, y, null);
                    characterBitmap.recycle();
                } catch (Exception e) {
                    Log.w(TAG, "Failed to load asset for " + character.getType() + ", using fallback", e);
                    // Fallback to drawing a colored shape
                    Paint paint = new Paint();
                    paint.setColor(switch (character.getColor().toLowerCase()) {
                        case "red" -> 0xFFFF0000;
                        case "blue" -> 0xFF0000FF;
                        case "green" -> 0xFF00FF00;
                        case "yellow" -> 0xFFFFFF00;
                        default -> 0xFF0000FF;
                    });
                    float x = character.getPosition() != null ? character.getPosition().x : 100;
                    float y = character.getPosition() != null ? character.getPosition().y : 100;
                    if (character.getType().equals("star")) {
                        canvas.drawCircle(x, y, 50, paint);
                    } else {
                        canvas.drawRect(x, y, x + 50, y + 50, paint);
                    }
                }
            }

            frames.add(new VideoFrame(frameBitmap, i * 1000 / fps));
        }
        return frames;
    }
                        }

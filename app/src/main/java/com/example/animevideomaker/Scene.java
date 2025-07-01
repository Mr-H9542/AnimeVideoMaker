package com.example.animevideomaker;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import java.util.Collections;
import java.util.List;

/**
 * Represents an animated scene with background and a single character.
 */
public class Scene {
    private Bitmap background;
    private Character character;
    private int durationSeconds = 5;

    public Scene() {
        setBackgroundColor("black");  // Default background
    }

    /**
     * Optional constructor to set background bitmap directly.
     */
    public Scene(Bitmap background) {
        this.background = background;
    }

    /**
     * Sets background color by name, creates a bitmap with that color.
     */
    public void setBackgroundColor(String colorName) {
        int w = 720, h = 1280;  // You can parameterize this if needed
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        int c = switch (colorName.toLowerCase()) {
            case "white" -> Color.WHITE;
            case "red" -> Color.RED;
            case "blue" -> Color.BLUE;
            case "gray" -> Color.GRAY;
            default -> Color.BLACK;
        };
        canvas.drawColor(c);
        this.background = bmp;
    }

    public Bitmap getBackground() {
        return background;
    }

    public void setBackground(Bitmap background) {
        this.background = background;
    }

    public void setCharacter(Character character) {
        this.character = character;
    }

    public List<Character> getCharactersByDepth() {
        // Currently supports only one character
        return Collections.singletonList(character);
    }

    public void setDuration(int seconds) {
        this.durationSeconds = seconds;
    }

    public int getDuration() {
        return durationSeconds;
    }

    /**
     * Convenience method to configure scene from an AnimationRequest.
     */
    public void configureFromRequest(AnimationRequest req) {
        setBackgroundColor(req.background);
        Character c = new Character();
        c.setColor(req.characterColor);
        c.setType(req.characterType);
        c.setAction(req.action);
        setCharacter(c);
        setDuration(req.duration);
    }
}

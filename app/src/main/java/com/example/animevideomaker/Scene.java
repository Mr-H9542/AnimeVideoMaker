package com.example.animevideomaker;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an animated scene with a background and one or more characters.
 */
public class Scene {

    private Bitmap background;
    private final List<Character> characters = new ArrayList<>();
    private int durationSeconds = 5;

    // Default constructor initializes with a black background
    public Scene() {
        setBackgroundColor("black");
    }

    // Optional constructor to accept a pre-made background bitmap
    public Scene(Bitmap background) {
        this.background = background;
    }

    /**
     * Sets a solid color background using a color name.
     * Accepts: white, red, blue, gray, black (default).
     */
    public void setBackgroundColor(String colorName) {
        int w = 720, h = 1280;
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        int color = switch (colorName.toLowerCase()) {
            case "white" -> Color.WHITE;
            case "red" -> Color.RED;
            case "blue" -> Color.BLUE;
            case "gray" -> Color.GRAY;
            default -> Color.BLACK;
        };
        canvas.drawColor(color);
        this.background = bmp;
    }

    public void setBackground(Bitmap background) {
        this.background = background;
    }

    public Bitmap getBackground() {
        return background;
    }

    public void setCharacter(Character character) {
        characters.clear();
        characters.add(character);
    }

    public void addCharacter(Character character) {
        characters.add(character);
    }

    public List<Character> getCharactersByDepth() {
        return characters;
    }

    public void setDuration(int seconds) {
        this.durationSeconds = Math.max(1, seconds);
    }

    public int getDuration() {
        return durationSeconds;
    }

    /**
     * Configures the scene based on a parsed AnimationRequest.
     */
    public void configureFromRequest(AnimationRequest req) {
        setBackgroundColor(req.background);

        Character c = new Character();
        c.setColor(req.characterColor);
        c.setType(req.characterType);
        c.setAction(req.action);
        setCharacter(c); // replaces all characters

        setDuration(req.duration);
    }
}

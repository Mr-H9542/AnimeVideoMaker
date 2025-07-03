package com.example.animevideomaker;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Scene implements Serializable {
    private transient Bitmap background; // transient to avoid serialization issues
    private final List<Character> characters = new ArrayList<>();
    private int durationSeconds = 5;

    public Scene() {
        setBackgroundColor("black");
    }

    public Scene(Bitmap background) {
        this.background = background;
    }

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

    public void configureFromRequest(AnimationRequest req) {
        setBackgroundColor(req.background.equals("default") ? "black" : req.background);
        Character c = new Character("char_" + System.currentTimeMillis(), req.characterType, req.characterColor, req.action, new PointF(100, 100), true);
        setCharacter(c);
        setDuration(req.duration);
    }
    }

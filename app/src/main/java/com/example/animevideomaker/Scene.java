package com.example.animevideomaker;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

public class Scene {
    private Bitmap background;
    private final List<Character> characters;
    private int durationSeconds = 5;  // default

    public Scene() {
        this.background = createColoredBitmap("black");
        this.characters = new ArrayList<>();
    }

    public Scene(Bitmap background) {
        this.background = background;
        this.characters = new ArrayList<>();
    }

    public Bitmap getBackground() {
        return background;
    }

    public void setBackground(Bitmap bg) {
        this.background = bg;
    }

    public void setBackgroundColor(String colorName) {
        this.background = createColoredBitmap(colorName);
    }

    public void addCharacter(Character c) {
        characters.add(c);
    }

    public void setCharacter(Character c) {
        characters.clear(); // replace all
        characters.add(c);
    }

    public List<Character> getCharactersByDepth() {
        return characters;
    }

    public void setDuration(int seconds) {
        this.durationSeconds = seconds;
    }

    public int getDuration() {
        return durationSeconds;
    }

    // 🧱 Helper: create bitmap from color name
    private Bitmap createColoredBitmap(String colorName) {
        int w = 720, h = 1280;
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        int color;
        switch (colorName.toLowerCase()) {
            case "white": color = Color.WHITE; break;
            case "red": color = Color.RED; break;
            case "blue": color = Color.BLUE; break;
            case "gray": color = Color.GRAY; break;
            default: color = Color.BLACK; break;
        }

        canvas.drawColor(color);
        return bmp;
    }

    // 🔗 Add this method for AITextParser integration
    public void configureFromRequest(AnimationRequest req) {
        setBackgroundColor(req.background);

        Character character = new Character();
        character.setColor(req.characterColor);
        character.setType(req.characterType);
        character.setAction(req.action);

        setCharacter(character);
        setDuration(req.duration);
    }
}

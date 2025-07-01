package com.example.animevideomaker;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import java.util.Collections;
import java.util.List;

public class Scene {
    private Bitmap background;
    private Character character;
    private int durationSeconds = 5;

    public Scene() {
        setBackgroundColor("black");
    }

    public void setBackgroundColor(String colorName) {
        int w = 720, h = 1280;
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

    public Bitmap getBackground() { return background; }

    public void setCharacter(Character character) {
        this.character = character;
    }

    public List<Character> getCharactersByDepth() {
        return Collections.singletonList(character);
    }

    public void setDuration(int sec) { this.durationSeconds = sec; }
    public int getDuration() { return durationSeconds; }

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

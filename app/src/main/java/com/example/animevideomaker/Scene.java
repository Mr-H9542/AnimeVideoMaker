package com.example.animevideomaker;

import android.graphics.Bitmap;
import java.util.ArrayList;
import java.util.List;

public class Scene {
    private Bitmap background;
    private final List<Character> characters;

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

    public void addCharacter(Character c) {
        characters.add(c);
    }

    public List<Character> getCharactersByDepth() {
        // For simplicity, return in same order. You can sort by Y if needed.
        return characters;
    }
}

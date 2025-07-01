package com.example.animevideomaker;

import android.graphics.PointF;

public class Character {
    private String id;
    private String type;
    private String color;
    private String action;
    private PointF position;
    private boolean mainCharacter;

    // Constructor with all fields
    public Character(String id, String type, String color, String action, PointF position, boolean mainCharacter) {
        this.id = id;
        this.type = type;
        this.color = color;
        this.action = action;
        this.position = position;
        this.mainCharacter = mainCharacter;
    }

    // Optional: default constructor (if you want)
    public Character() {
        this("default", "star", "blue", "idle", new PointF(0, 0), true);
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public PointF getPosition() { return position; }
    public void setPosition(PointF position) { this.position = position; }

    public boolean isMainCharacter() { return mainCharacter; }
    public void setMainCharacter(boolean mainCharacter) { this.mainCharacter = mainCharacter; }

    // New method to get asset path
    public String getAssetPath(String action, int size) {
        String baseType = (type != null) ? type : "default";
        String baseAction = (action != null) ? action : "idle";
        return "assets/" + baseType + "_" + baseAction + "_" + size + ".png";
    }
}

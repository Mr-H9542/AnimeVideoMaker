package com.example.animevideomaker;

import android.graphics.PointF;

public class Character {
    private String id;
    private String type = "star";
    private String color = "blue";
    private String action = "idle";
    private PointF position = new PointF(0, 0);
    private boolean mainCharacter = true;

    public Character() {
        this.id = "default";
    }

    public void setType(String type) { this.type = type; }
    public void setColor(String color) { this.color = color; }
    public void setAction(String action) {
        this.action = action;
        this.action = action;
    }

    public String getType() { return type; }
    public String getColor() { return color; }
    public String getAction() { return action; }

    // Existing methods retained...
}

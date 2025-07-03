package com.example.animevideomaker;

import android.graphics.PointF;

import java.io.Serializable;
import java.util.Objects;

public class Character implements Serializable {

    private String id;
    private String type;
    private String color;
    private String action;
    private PointF position;
    private boolean mainCharacter;

    public Character(String id, String type, String color, String action, PointF position, boolean mainCharacter) {
        this.id = id;
        this.type = type;
        this.color = color;
        this.action = action;
        this.position = new PointF(position.x, position.y); // Defensive copy
        this.mainCharacter = mainCharacter;
    }

    public Character() {
        this("default", "star", "blue", "bounce", new PointF(100, 100), true);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public PointF getPosition() {
        return new PointF(position.x, position.y);
    }

    public void setPosition(PointF position) {
        this.position = new PointF(position.x, position.y);
    }

    public boolean isMainCharacter() { return mainCharacter; }
    public void setMainCharacter(boolean mainCharacter) { this.mainCharacter = mainCharacter; }

    public String getAssetPath(int size) {
        return "assets/" + type + "_" + action + "_" + size + ".png";
    }

    @Override
    public String toString() {
        return color + " " + type + " doing " + action + " at (" + position.x + ", " + position.y + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Character character)) return false;
        return mainCharacter == character.mainCharacter &&
                Objects.equals(id, character.id) &&
                Objects.equals(type, character.type) &&
                Objects.equals(color, character.color) &&
                Objects.equals(action, character.action) &&
                Objects.equals(position, character.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, color, action, position, mainCharacter);
    }
}

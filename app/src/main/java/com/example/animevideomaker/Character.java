package com.example.animevideomaker;

import android.graphics.PointF;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Character implements Serializable {

    private static final String DEFAULT_ID = "default";
    private static final String DEFAULT_TYPE = "star";
    private static final String DEFAULT_COLOR = "blue";
    private static final String DEFAULT_ACTION = "bounce";
    private static final PointF DEFAULT_POSITION = new PointF(100, 100);
    private static final boolean DEFAULT_MAIN_CHARACTER = true;

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
        this(DEFAULT_ID, DEFAULT_TYPE, DEFAULT_COLOR, DEFAULT_ACTION, DEFAULT_POSITION, DEFAULT_MAIN_CHARACTER);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public PointF getPosition() {
        return new PointF(position.x, position.y); // Defensive copy
    }

    public void setPosition(PointF position) {
        this.position = new PointF(position.x, position.y); // Defensive copy
    }

    public boolean isMainCharacter() { return mainCharacter; }
    public void setMainCharacter(boolean mainCharacter) { this.mainCharacter = mainCharacter; }

    // Asset path accessors
    public String getAssetPath(int size) {
        return "assets/" + type + "_" + action + "_" + size + ".png";
    }

    // Default size version for backward compatibility
    public String getAssetPath() {
        return getAssetPath(100); // Default size
    }

    @Override
    public String toString() {
        return String.format("%s %s doing %s at (%.1f, %.1f)", color, type, action, position.x, position.y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Character)) return false;
        Character character = (Character) o;
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

    /**
     * Creates a Character from a plain text description using keyword matching.
     * Example input: "blue star jumping"
     */
    public static Character fromTextInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new Character();
        }

        String lower = input.toLowerCase();

        Map<String, String> typeKeywords = Map.of(
                "circle", "circle",
                "square", "square",
                "star", "star"
        );

        Map<String, String> colorKeywords = Map.of(
                "red", "red",
                "green", "green",
                "yellow", "yellow",
                "black", "black",
                "blue", "blue"
        );

        Map<String, String> actionKeywords = Map.of(
                "jump", "jump",
                "jumping", "jump",
                "bounce", "bounce",
                "run", "run",
                "running", "run"
        );

        String type = typeKeywords.keySet().stream().filter(lower::contains).findFirst().map(typeKeywords::get).orElse(DEFAULT_TYPE);
        String color = colorKeywords.keySet().stream().filter(lower::contains).findFirst().map(colorKeywords::get).orElse(DEFAULT_COLOR);
        String action = actionKeywords.keySet().stream().filter(lower::contains).findFirst().map(actionKeywords::get).orElse("idle");

        return new Character(
                "char_" + System.currentTimeMillis(),
                type,
                color,
                action,
                new PointF(DEFAULT_POSITION.x, DEFAULT_POSITION.y),
                DEFAULT_MAIN_CHARACTER
        );
    }
    }

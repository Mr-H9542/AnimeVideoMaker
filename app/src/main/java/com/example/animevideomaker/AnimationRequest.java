package com.example.animevideomaker;

public class AnimationRequest {
    public String characterType;
    public String characterColor;
    public String action;
    public String background;
    public int duration;

    public AnimationRequest() {
        this("star", "blue", "bounce", "black", 5);
    }

    public AnimationRequest(String characterType, String characterColor,
                            String action, String background, int duration) {
        this.characterType = characterType;
        this.characterColor = characterColor;
        this.action = action;
        this.background = background;
        this.duration = duration;
    }

    @Override
    public String toString() {
        return characterColor + " " + characterType + " " + action + " on " +
               background + " for " + duration + " seconds";
    }
}

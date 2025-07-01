package com.example.animevideomaker;

public class AnimationRequest {
    public String characterType = "star";
    public String characterColor = "blue";
    public String action = "bounce";
    public String background = "black";
    public int duration = 5;

    @Override
    public String toString() {
        return characterColor + " " + characterType + " " + action + " on " +
               background + " for " + duration + " seconds";
    }
}

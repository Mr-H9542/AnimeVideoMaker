package com.example.animevideomaker;

public class AITextParser {
    public static AnimationRequest parse(String prompt) {
        AnimationRequest req = new AnimationRequest();

        prompt = prompt.toLowerCase();

        if (prompt.contains("red")) req.characterColor = "red";
        if (prompt.contains("blue")) req.characterColor = "blue";
        if (prompt.contains("star")) req.characterType = "star";
        if (prompt.contains("ball")) req.characterType = "ball";
        if (prompt.contains("bouncing")) req.action = "bounce";
        if (prompt.contains("rotating")) req.action = "rotate";
        if (prompt.contains("walking")) req.action = "walk";
        if (prompt.contains("background")) {
            int idx = prompt.indexOf("background");
            String[] parts = prompt.substring(idx).split(" ");
            if (parts.length > 0) req.background = parts[0];
        }
        if (prompt.contains("second")) {
            String[] parts = prompt.split(" ");
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals("seconds") || parts[i].equals("second")) {
                    try {
                        req.duration = Integer.parseInt(parts[i - 1]);
                    } catch (Exception ignored) {}
                }
            }
        }

        return req;
    }
}

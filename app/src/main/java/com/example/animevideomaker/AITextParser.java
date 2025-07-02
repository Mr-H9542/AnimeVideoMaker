package com.example.animevideomaker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AITextParser {

    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)\\s*second", Pattern.CASE_INSENSITIVE);
    private static final Pattern BACKGROUND_PATTERN = Pattern.compile("background\\s*(is|:)?\\s*(\\w+)", Pattern.CASE_INSENSITIVE);

    public static AnimationRequest parse(String prompt) {
        AnimationRequest req = new AnimationRequest();
        String text = prompt.toLowerCase();

        req.characterColor = extractColor(text);
        req.characterType = extractType(text);
        req.action = extractAction(text);
        req.background = extractBackground(text);
        req.duration = extractDuration(text);

        return req;
    }

    private static String extractColor(String text) {
        if (text.contains("red")) return "red";
        if (text.contains("blue")) return "blue";
        if (text.contains("green")) return "green";
        if (text.contains("yellow")) return "yellow";
        return "blue"; // default
    }

    private static String extractType(String text) {
        if (text.contains("star")) return "star";
        if (text.contains("ball")) return "ball";
        if (text.contains("cat")) return "cat";
        return "star"; // default
    }

    private static String extractAction(String text) {
        if (text.contains("bounce")) return "bounce";
        if (text.contains("rotate")) return "rotate";
        if (text.contains("walk")) return "walk";
        if (text.contains("jump")) return "jump";
        return "idle"; // default
    }

    private static String extractBackground(String text) {
        Matcher matcher = BACKGROUND_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(2); // group(2) captures the background word
        }
        return "default"; // fallback background
    }

    private static int extractDuration(String text) {
        Matcher matcher = DURATION_PATTERN.matcher(text);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return 5; // default duration
    }
}

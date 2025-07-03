package com.example.animevideomaker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AITextParser {

    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)\\s*second", Pattern.CASE_INSENSITIVE);
    private static final Pattern BACKGROUND_PATTERN = Pattern.compile("background\\s*(is|:)?\\s*(\\w+)", Pattern.CASE_INSENSITIVE);

    // Allowable values (easier to update later)
    private static final String[] COLORS = {"red", "blue", "green", "yellow"};
    private static final String[] TYPES = {"star", "ball", "cat"};
    private static final String[] ACTIONS = {"bounce", "rotate", "walk", "jump"};

    public static AnimationRequest parse(String prompt) {
        AnimationRequest req = new AnimationRequest();
        String text = prompt.toLowerCase();

        req.characterColor = extractMatch(text, COLORS, "blue");
        req.characterType = extractMatch(text, TYPES, "star");
        req.action = extractMatch(text, ACTIONS, "idle");
        req.background = extractBackground(text);
        req.duration = extractDuration(text);

        return req;
    }

    /**
     * Extracts the first matching word from a list, or returns the default.
     */
    private static String extractMatch(String text, String[] keywords, String defaultValue) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return keyword;
            }
        }
        return defaultValue;
    }

    /**
     * Extracts background keyword using a regex pattern.
     */
    private static String extractBackground(String text) {
        Matcher matcher = BACKGROUND_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return "default";
    }

    /**
     * Extracts duration in seconds from the prompt.
     */
    private static int extractDuration(String text) {
        Matcher matcher = DURATION_PATTERN.matcher(text);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return 5;
    }
}

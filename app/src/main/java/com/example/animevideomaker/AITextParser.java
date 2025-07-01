package com.example.animevideomaker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AITextParser {
    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)\\s*second");

    public static AnimationRequest parse(String prompt) {
        AnimationRequest req = new AnimationRequest();
        String text = prompt.toLowerCase();

        req.characterColor = text.contains("red") ? "red" :
                              text.contains("blue") ? "blue" : "blue";

        req.characterType = text.contains("star") ? "star" :
                             text.contains("ball") ? "ball" : "star";

        req.action = text.contains("bounce") ? "bounce" :
                     text.contains("rotate") ? "rotate" :
                     text.contains("walk") ? "walk" : "idle";

        if (text.contains("background")) {
            int idx = text.indexOf("background");
            String[] parts = text.substring(idx + 10).trim().split("\\s+");
            req.background = parts.length > 0 ? parts[0] : req.background;
        }

        Matcher m = DURATION_PATTERN.matcher(text);
        if (m.find()) {
            try {
                req.duration = Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {}
        }

        return req;
    }
}

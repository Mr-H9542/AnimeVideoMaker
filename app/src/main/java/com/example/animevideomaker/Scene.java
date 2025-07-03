package com.example.animevideomaker;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Scene implements Serializable {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler uiHandler = new Handler(Looper.getMainLooper());

    private final int width;
    private final int height;
    private transient Bitmap background;
    private final List<Character> characters;
    private int durationSeconds;

    // === Constructors ===

    public Scene() {
        this(720, 1280);
    }

    public Scene(int width, int height) {
        this.width = width;
        this.height = height;
        this.characters = new ArrayList<>();
        this.durationSeconds = 5;
        setBackgroundColor("black");
    }

    public Scene(Bitmap background) {
        this.width = background.getWidth();
        this.height = background.getHeight();
        this.characters = new ArrayList<>();
        this.durationSeconds = 5;
        this.background = background;
    }

    // === Background ===

    public void setBackgroundColor(String colorName) {
        int color = switch (colorName.toLowerCase()) {
            case "white" -> Color.WHITE;
            case "red" -> Color.RED;
            case "blue" -> Color.BLUE;
            case "gray" -> Color.GRAY;
            case "yellow" -> Color.YELLOW;
            default -> Color.BLACK;
        };
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        new Canvas(bmp).drawColor(color);
        this.background = bmp;
    }

    public Bitmap getBackground() {
        return background;
    }

    // === Characters ===

    public void addCharacter(Character character) {
        if (character != null) {
            characters.add(character);
        }
    }

    public void replaceWithCharacter(Character character) {
        characters.clear();
        addCharacter(character);
    }

    public List<Character> getCharacters() {
        return Collections.unmodifiableList(characters);
    }

    public List<Character> getCharactersByDepth() {
        // Future implementation: sort by depth if needed
        return getCharacters();
    }

    // === Duration ===

    public void setDuration(int seconds) {
        this.durationSeconds = Math.max(1, seconds);
    }

    public int getDuration() {
        return durationSeconds;
    }

    // === Request Configuration ===

    public void configureFromRequest(AnimationRequest req) {
        if (req == null) return;

        String bgColor = req.background == null || req.background.equalsIgnoreCase("default")
                ? "black"
                : req.background;

        setBackgroundColor(bgColor);

        Character character = new Character(
                "char_" + System.currentTimeMillis(),
                req.characterType,
                req.characterColor,
                req.action,
                new PointF(100, 100),
                true
        );

        replaceWithCharacter(character);
        setDuration(req.duration);
    }

    // === Frame Generation (Mock/Async) ===

    public void generateFrames(Context context, Dialog loadingDialog, Runnable onComplete) {
        if (loadingDialog != null) {
            uiHandler.post(loadingDialog::show);
        }

        executor.execute(() -> {
            try {
                // Simulate long operation (e.g., generating video frames)
                Thread.sleep(durationSeconds * 1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            uiHandler.post(() -> {
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
                if (onComplete != null) {
                    onComplete.run();
                }
            });
        });
    }
        }

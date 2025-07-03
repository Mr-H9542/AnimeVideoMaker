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

    private static final int WIDTH = 720;
    private static final int HEIGHT = 1280;

    private transient Bitmap background;
    private final List<Character> characters = new ArrayList<>();
    private int durationSeconds = 5;

    // Threading
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler uiHandler = new Handler(Looper.getMainLooper());

    public Scene() {
        setBackgroundColor("black");
    }

    public Scene(Bitmap background) {
        this.background = background;
    }

    // Sets a solid background color
    public void setBackgroundColor(String colorName) {
        int color = switch (colorName.toLowerCase()) {
            case "white" -> Color.WHITE;
            case "red" -> Color.RED;
            case "blue" -> Color.BLUE;
            case "gray" -> Color.GRAY;
            default -> Color.BLACK;
        };

        Bitmap bmp = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        canvas.drawColor(color);
        this.background = bmp;
    }

    public void setBackground(Bitmap background) {
        this.background = background;
    }

    public Bitmap getBackground() {
        return background;
    }

    // Overwrites characters list
    public void setCharacter(Character character) {
        characters.clear();
        if (character != null) {
            characters.add(character);
        }
    }

    public void addCharacter(Character character) {
        if (character != null) {
            characters.add(character);
        }
    }

    public List<Character> getCharactersByDepth() {
        return Collections.unmodifiableList(characters);
    }

    public void setDuration(int seconds) {
        this.durationSeconds = Math.max(1, seconds); // Avoid 0 or negative
    }

    public int getDuration() {
        return durationSeconds;
    }

    // Configure scene from parsed AI prompt
    public void configureFromRequest(AnimationRequest req) {
        if (req == null) return;

        setBackgroundColor(req.background.equalsIgnoreCase("default") ? "black" : req.background);

        Character character = new Character(
                "char_" + System.currentTimeMillis(),
                req.characterType,
                req.characterColor,
                req.action,
                new PointF(100, 100),
                true
        );

        setCharacter(character);
        setDuration(req.duration);
    }

    /**
     * Simulates frame generation for the scene.
     * Replace this with actual animation logic if needed.
     */
    public void generateFrames(Context context, Dialog loadingDialog, Runnable onComplete) {
        if (loadingDialog != null) {
            uiHandler.post(loadingDialog::show);
        }

        executor.execute(() -> {
            try {
                // Simulate time to generate animation
                Thread.sleep(durationSeconds * 1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            uiHandler.post(() -> {
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
                if (onComplete != null) onComplete.run();
            });
        });
    }
                }

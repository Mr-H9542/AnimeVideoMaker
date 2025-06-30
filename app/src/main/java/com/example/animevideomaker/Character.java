package com.example.animevideomaker;

import android.graphics.PointF;

public class Character {
    private String id;
    private String baseModel;
    private String pose;
    private PointF position;
    private boolean mainCharacter;

    public Character(String id, String baseModel, PointF position, boolean isMain) {
        this.id = id;
        this.baseModel = baseModel;
        this.position = position;
        this.pose = "idle";
        this.mainCharacter = isMain;
    }

    public String getId() {
        return id;
    }

    public String getBaseModel() {
        return baseModel;
    }

    public String getCurrentPose() {
        return pose;
    }

    public void setPose(String pose) {
        this.pose = pose;
    }

    public PointF getPosition() {
        return position;
    }

    public boolean isMainCharacter() {
        return mainCharacter;
    }

    public String getAssetPath(String type, int res) {
        return "characters/" + id + "/" + type + "_" + res + ".png";
    }

    public int getBaseTextureResolution(String type) {
        return "diffuse".equals(type) ? 512 : 256;
    }

    public int getTriangleCount() {
        return 400; // placeholder
    }

    public int getBoneCount() {
        return 10; // placeholder
    }

    public void simplifyMesh(int maxTriangles) {
        // Placeholder logic
    }

    public void simplifyRig(int maxBones) {
        // Placeholder logic
    }

    public int getBaseColor() {
        return 0xFFCCCCCC; // fallback color
    }
  }

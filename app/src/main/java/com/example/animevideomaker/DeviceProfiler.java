package com.example.animevideomaker;

import android.os.Build;

public class DeviceProfiler {
    public static final int QUALITY_LOW = 0;
    public static final int QUALITY_MEDIUM = 1;
    public static final int QUALITY_HIGH = 2;

    public static int getShaderQuality() {
        if (Build.MODEL.equalsIgnoreCase("itel A70")) {
            return QUALITY_MEDIUM;
        }

        if (Build.HARDWARE.toLowerCase().contains("qcom") || 
            Build.HARDWARE.toLowerCase().contains("mt")) {
            return QUALITY_MEDIUM;
        }

        return QUALITY_LOW;
    }

    public static boolean supportsFeature(String feature) {
        // Basic example for expansion
        if ("NORMAL_MAPPING".equals(feature)) {
            return getShaderQuality() >= QUALITY_MEDIUM;
        }
        return false;
    }
}

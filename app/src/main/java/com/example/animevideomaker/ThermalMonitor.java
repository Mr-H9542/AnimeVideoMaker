package com.example.animevideomaker;

import android.content.Context;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class ThermalMonitor {
    private static final float WARNING_TEMP = 40.0f; // Celsius
    private static final float CRITICAL_TEMP = 45.0f; // Celsius
    private static final long INTERVAL = 10000; // 10s

    private final BatteryManager batteryManager;
    private final Handler handler;
    private final Context context;

    public ThermalMonitor(Context context) {
        this.context = context;
        this.batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void startMonitoring() {
        handler.postDelayed(new ThermalRunnable(), INTERVAL);
    }

    public void stopMonitoring() {
        handler.removeCallbacksAndMessages(null);
    }

    private class ThermalRunnable implements Runnable {
        @Override
        public void run() {
            float temp = getBatteryTemp();

            if (temp >= CRITICAL_TEMP) {
                Log.w("ThermalMonitor", "CRITICAL: Overheating! Pausing rendering.");
                // Add rendering pause logic
            } else if (temp >= WARNING_TEMP) {
                Log.w("ThermalMonitor", "WARNING: High temperature detected.");
                // Add quality downgrade logic
            }

            handler.postDelayed(this, INTERVAL);
        }
    }

    private float getBatteryTemp() {
        int temp = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_TEMPERATURE);
        return temp / 10f;
    }
}

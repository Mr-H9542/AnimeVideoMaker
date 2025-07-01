package com.example.animevideomaker;

import android.content.Context;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class ThermalMonitor {
    private static final String TAG = "ThermalMonitor";

    private static final float WARNING_TEMP_C = 40.0f;
    private static final float CRITICAL_TEMP_C = 45.0f;
    private static final long CHECK_INTERVAL_MS = 10_000L; // 10 seconds

    private final BatteryManager batteryManager;
    private final Handler handler;

    private final Runnable thermalCheckTask = new Runnable() {
        @Override
        public void run() {
            float temperature = readBatteryTemperature();

            if (temperature >= CRITICAL_TEMP_C) {
                Log.w(TAG, "CRITICAL: Overheating! Consider pausing rendering.");
                // TODO: Add rendering pause logic
            } else if (temperature >= WARNING_TEMP_C) {
                Log.w(TAG, "WARNING: High temperature detected.");
                // TODO: Add quality downgrade logic
            } else {
                Log.d(TAG, "Temperature OK: " + temperature + "°C");
            }

            handler.postDelayed(this, CHECK_INTERVAL_MS);
        }
    };

    public ThermalMonitor(Context context) {
        this.batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void startMonitoring() {
        if (batteryManager == null) {
            Log.e(TAG, "BatteryManager not available. Cannot monitor temperature.");
            return;
        }
        handler.post(thermalCheckTask);
    }

    public void stopMonitoring() {
        handler.removeCallbacks(thermalCheckTask);
    }

    private float readBatteryTemperature() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            int rawTemp = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_TEMPERATURE);
            return rawTemp / 10f;
        } else {
            Log.w(TAG, "Temperature monitoring not supported on this Android version.");
            return -1f;
        }
    }
            }

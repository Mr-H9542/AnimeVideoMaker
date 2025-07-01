package com.example.animevideomaker;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    private final Context context;

    private final Runnable thermalCheckTask = new Runnable() {
        @Override
        public void run() {
            float temperature = readBatteryTemperature();

            if (temperature >= CRITICAL_TEMP_C) {
                Log.w(TAG, "CRITICAL: Overheating! Consider pausing rendering.");
                AppNotifier.showCritical(context, "🔥 Overheating! Rendering paused.");
                // TODO: Add rendering pause logic
            } else if (temperature >= WARNING_TEMP_C) {
                Log.w(TAG, "WARNING: High temperature detected.");
                AppNotifier.showWarning(context, "⚠️ Device getting warm. Reducing quality.");
                // TODO: Add quality downgrade logic
            } else {
                Log.d(TAG, "Temperature OK: " + temperature + "°C");
            }

            handler.postDelayed(this, CHECK_INTERVAL_MS);
        }
    };

    public ThermalMonitor(Context context) {
        this.context = context;
        this.batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void startMonitoring() {
        if (batteryManager == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Log.e(TAG, "BatteryManager not supported or API level < 21. Cannot monitor temperature.");
            return;
        }
        handler.post(thermalCheckTask);
    }

    public void stopMonitoring() {
        handler.removeCallbacks(thermalCheckTask);
    }

    private float readBatteryTemperature() {
        if (batteryManager == null) {
            return -1f;
        }
        
        // Use literal value for API 28+ constant (BatteryManager.BATTERY_PROPERTY_TEMPERATURE = 4)
        final int BATTERY_PROPERTY_TEMPERATURE = 4;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            int rawTemp = batteryManager.getIntProperty(BATTERY_PROPERTY_TEMPERATURE);
            return rawTemp / 10f;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Fallback for older APIs (21-27)
            Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            if (batteryIntent != null) {
                int temperature = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
                return temperature / 10f;
            }
        }
        return -1f;
    }
                    }

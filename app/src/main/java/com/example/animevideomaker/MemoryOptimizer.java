package com.example.animevideomaker;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class MemoryOptimizer {
    private static final long INTERVAL = 30000; // 30 seconds
    private final Handler handler;
    private final Context context;

    public MemoryOptimizer(Context context) {
        this.context = context;
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void start() {
        handler.postDelayed(new MemoryRunnable(), INTERVAL);
    }

    public void stop() {
        handler.removeCallbacksAndMessages(null);
    }

    private class MemoryRunnable implements Runnable {
        @Override
        public void run() {
            long free = getFreeMem();
            long total = getTotalMem();

            if (free < total * 0.2) {
                Log.w("MemoryOptimizer", "Low memory! Optimizing...");
                System.gc();
            }

            handler.postDelayed(this, INTERVAL);
        }
    }

    private long getFreeMem() {
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        am.getMemoryInfo(info);
        return info.availMem;
    }

    private long getTotalMem() {
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        am.getMemoryInfo(info);
        return info.totalMem;
    }
}

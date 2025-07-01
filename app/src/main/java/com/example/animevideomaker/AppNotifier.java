package com.example.animevideomaker;

import android.content.Context;
import android.widget.Toast;

public class AppNotifier {
    public static void showWarning(Context context, String message) {
        Toast.makeText(context, "⚠️ " + message, Toast.LENGTH_SHORT).show();
    }

    public static void showCritical(Context context, String message) {
        Toast.makeText(context, "🔥 " + message, Toast.LENGTH_LONG).show();
    }
}

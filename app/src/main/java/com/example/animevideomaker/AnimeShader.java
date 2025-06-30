package com.example.animevideomaker;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Shader;
import android.graphics.BitmapShader;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;

public class AnimeShader extends Shader {
    private final Bitmap diffuseTexture;
    private final PointF lightDirection;
    private final int qualityLevel;

    public AnimeShader(Bitmap diffuseTexture, PointF lightDirection, int qualityLevel) {
        this.diffuseTexture = diffuseTexture;
        this.lightDirection = lightDirection;
        this.qualityLevel = qualityLevel;
    }

    public void onDraw(Canvas canvas, Paint paint, Path path) {
        Paint shadingPaint = new Paint();
        shadingPaint.setShader(new BitmapShader(diffuseTexture, TileMode.CLAMP, TileMode.CLAMP));
        shadingPaint.setColorFilter(new LightingColorFilter(Color.WHITE, Color.BLACK));
        canvas.drawPath(path, shadingPaint);
    }
}

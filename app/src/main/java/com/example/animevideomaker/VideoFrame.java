package com.example.animevideomaker;

import android.graphics.Bitmap;

public class VideoFrame {
    private final Bitmap bitmap;
    private final int index;

    public VideoFrame(Bitmap bitmap, int index) {
        this.bitmap = bitmap;
        this.index = index;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getIndex() {
        return index;
    }

    public void recycle() {
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }
}

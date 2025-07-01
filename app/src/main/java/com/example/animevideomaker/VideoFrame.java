package com.example.animevideomaker;

import android.graphics.Bitmap;

/**
 * Represents a single video frame with its bitmap and index.
 * Includes safe resource management for the bitmap.
 */
public class VideoFrame {
    private final Bitmap bitmap;
    private final int index;

    /**
     * Constructs a VideoFrame with a non-null bitmap and frame index.
     *
     * @param bitmap the bitmap image of this frame (must not be null)
     * @param index  the frame index or sequence number
     * @throws IllegalArgumentException if bitmap is null
     */
    public VideoFrame(Bitmap bitmap, int index) {
        if (bitmap == null) {
            throw new IllegalArgumentException("Bitmap cannot be null");
        }
        this.bitmap = bitmap;
        this.index = index;
    }

    /**
     * Gets the bitmap of this frame.
     *
     * @return the bitmap image
     */
    public Bitmap getBitmap() {
        return bitmap;
    }

    /**
     * Gets the index of this frame.
     *
     * @return the frame index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Recycles the bitmap to free memory if it has not already been recycled.
     */
    public void recycle() {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }
}

package com.example.animevideomaker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import java.io.IOException;
import java.io.InputStream;

public class TextureStreamer {
    private final Context context;
    private final LruCache<String, Bitmap> textureCache;

    public TextureStreamer(Context context) {
        this.context = context;
        this.textureCache = new LruCache<>(10 * 1024 * 1024); // 10MB cache
    }

    public Bitmap loadTexture(String assetName, int maxSize) {
        Bitmap bitmap = textureCache.get(assetName);
        if (bitmap != null) return bitmap;

        try (InputStream is = context.getAssets().open(assetName)) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = true;
            bitmap = BitmapFactory.decodeStream(is, null, options);
            textureCache.put(assetName, bitmap);
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return Bitmap.createBitmap(maxSize, maxSize, Bitmap.Config.RGB_565);
        }
    }

    public void clearCache() {
        textureCache.evictAll();
    }
}

package com.example.animevideomaker;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class VideoEncoder {
    public static void save(List<VideoFrame> frames) {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "AnimeFrames");
        if (!dir.exists() && !dir.mkdirs()) Log.e("VideoEncoder", "Could not create frames directory");

        for (int i = 0; i < frames.size(); i++) {
            Bitmap bmp = frames.get(i).getBitmap();
            File f = new File(dir, String.format("frame_%03d.png", i));
            try (FileOutputStream out = new FileOutputStream(f)) {
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (Exception e) {
                Log.e("VideoEncoder", "Error saving: " + e.getMessage());
            }
        }
        Log.d("VideoEncoder", "Saved " + frames.size() + " frames to " + dir);
    }
}

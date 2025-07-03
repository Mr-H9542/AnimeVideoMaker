package com.example.animevideomaker;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    /**
     * Copies a file from the assets folder to internal storage if not already copied.
     *
     * @param context        Context to access assets and file system.
     * @param assetFileName  Name of the file inside assets/ folder.
     * @return Absolute path to the copied file in internal storage, or null if failed.
     */
    public static String copyAssetToInternalStorage(Context context, String assetFileName) {
        File file = new File(context.getFilesDir(), assetFileName);
        if (!file.exists()) {
            try (InputStream in = context.getAssets().open(assetFileName);
                 OutputStream out = new FileOutputStream(file)) {

                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                out.flush();
            } catch (IOException e) {
                Log.e("FileUtils", "Failed to copy asset to internal storage", e);
                return null;
            }
        }
        return file.getAbsolutePath();
    }
}

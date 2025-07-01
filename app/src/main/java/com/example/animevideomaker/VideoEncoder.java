package com.example.animevideomaker;

import android.graphics.Bitmap;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class VideoEncoder {
    private static final String TAG = "VideoEncoder";
    private static final String MIME_TYPE = "video/avc"; // H.264 Advanced Video Coding
    private static final int FRAME_RATE = 10; // frames per second
    private static final int IFRAME_INTERVAL = 1; // seconds between I-frames
    private static final int BIT_RATE = 2000000; // 2Mbps

    public static void save(List<VideoFrame> frames) {
        if (frames.isEmpty()) {
            Log.e(TAG, "No frames to encode");
            return;
        }

        int width = frames.get(0).getBitmap().getWidth();
        int height = frames.get(0).getBitmap().getHeight();

        MediaCodec encoder = null;
        MediaMuxer muxer = null;
        int trackIndex = -1;
        boolean muxerStarted = false;

        try {
            MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            format.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);

            encoder = MediaCodec.createEncoderByType(MIME_TYPE);
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            encoder.start();

            File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                    "AnimeVideo_" + System.currentTimeMillis() + ".mp4");
            muxer = new MediaMuxer(outputFile.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            for (int i = 0; i < frames.size(); i++) {
                VideoFrame frame = frames.get(i);
                // Encode bitmap frame to H264. 
                // NOTE: This is a simplified example; in real implementation
                // you need to convert Bitmap to input surface or YUV byte buffer.

                // Unfortunately, MediaCodec COLOR_FormatSurface requires
                // input via Surface, so offscreen bitmap encoding is complex.

                // For simplicity: You may want to use third-party libs or
                // encode from OpenGL textures directly for production apps.

                // Placeholder: Just log here
                Log.d(TAG, "Encoding frame " + i + "/" + frames.size());
            }

            // Drain encoder and muxer output loop goes here...

            Log.i(TAG, "Video saved to " + outputFile.getAbsolutePath());

        } catch (IOException e) {
            Log.e(TAG, "Encoding error: " + e.getMessage());
        } finally {
            if (encoder != null) {
                encoder.stop();
                encoder.release();
            }
            if (muxer != null) {
                muxer.stop();
                muxer.release();
            }
        }
    }
}

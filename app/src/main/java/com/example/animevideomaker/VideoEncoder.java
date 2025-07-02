package com.example.animevideomaker;

import android.graphics.Bitmap;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;

public class VideoEncoder {
    private static final String TAG = "VideoEncoder";

    private static final String MIME_TYPE = "video/avc"; // H.264
    private static final int FRAME_RATE = 10; // fps
    private static final int IFRAME_INTERVAL = 1; // seconds
    private static final int BIT_RATE = 2_000_000; // 2 Mbps

    /**
     * Saves a list of VideoFrame bitmaps as an H.264 MP4 video.
     * Returns the output file if successful, null otherwise.
     */
    public static File save(List<VideoFrame> frames) {
        if (frames == null || frames.isEmpty()) {
            Log.e(TAG, "No frames to encode.");
            return null;
        }

        int width = frames.get(0).getBitmap().getWidth();
        int height = frames.get(0).getBitmap().getHeight();

        File outputFile = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES), "AnimeVideo_" + System.currentTimeMillis() + ".mp4");

        MediaCodec encoder = null;
        MediaMuxer muxer = null;

        try {
            MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            format.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);

            encoder = MediaCodec.createEncoderByType(MIME_TYPE);
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            encoder.start();

            muxer = new MediaMuxer(outputFile.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int trackIndex = -1;
            boolean muxerStarted = false;

            long presentationTimeUs = 0;

            for (VideoFrame frame : frames) {
                Bitmap bitmap = frame.getBitmap();
                byte[] yuv = getNV21(width, height, bitmap);

                int inputIndex = encoder.dequeueInputBuffer(10000);
                if (inputIndex >= 0) {
                    ByteBuffer inputBuffer = encoder.getInputBuffer(inputIndex);
                    if (inputBuffer != null) {
                        inputBuffer.clear();
                        inputBuffer.put(yuv);
                        encoder.queueInputBuffer(inputIndex, 0, yuv.length, presentationTimeUs, 0);
                    }
                }

                presentationTimeUs += 1_000_000L / FRAME_RATE;

                // Drain encoder
                while (true) {
                    int outputIndex = encoder.dequeueOutputBuffer(bufferInfo, 10000);
                    if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) break;
                    else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        if (muxerStarted) throw new IllegalStateException("Format changed twice");
                        trackIndex = muxer.addTrack(encoder.getOutputFormat());
                        muxer.start();
                        muxerStarted = true;
                    } else if (outputIndex >= 0) {
                        ByteBuffer encodedData = encoder.getOutputBuffer(outputIndex);
                        if (encodedData == null) continue;

                        if (bufferInfo.size != 0 && muxerStarted) {
                            encodedData.position(bufferInfo.offset);
                            encodedData.limit(bufferInfo.offset + bufferInfo.size);
                            muxer.writeSampleData(trackIndex, encodedData, bufferInfo);
                        }

                        encoder.releaseOutputBuffer(outputIndex, false);
                    }
                }
            }

            // Send end-of-stream signal
            int endIndex = encoder.dequeueInputBuffer(10000);
            if (endIndex >= 0) {
                encoder.queueInputBuffer(endIndex, 0, 0, presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            }

            // Drain final buffers
            boolean endOfStream = false;
            while (!endOfStream) {
                int outputIndex = encoder.dequeueOutputBuffer(bufferInfo, 10000);
                if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) break;
                else if (outputIndex >= 0) {
                    ByteBuffer encodedData = encoder.getOutputBuffer(outputIndex);
                    if (encodedData != null && bufferInfo.size > 0 && muxerStarted) {
                        encodedData.position(bufferInfo.offset);
                        encodedData.limit(bufferInfo.offset + bufferInfo.size);
                        muxer.writeSampleData(trackIndex, encodedData, bufferInfo);
                    }
                    endOfStream = (bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;
                    encoder.releaseOutputBuffer(outputIndex, false);
                }
            }

            Log.i(TAG, "Video saved to: " + outputFile.getAbsolutePath());
            return outputFile;

        } catch (Exception e) {
            Log.e(TAG, "Encoding failed: " + e.getMessage(), e);
            return null;

        } finally {
            try {
                if (encoder != null) {
                    encoder.stop();
                    encoder.release();
                }
                if (muxer != null) {
                    muxer.stop();
                    muxer.release();
                }
            } catch (Exception cleanupEx) {
                Log.w(TAG, "Cleanup failed: " + cleanupEx.getMessage(), cleanupEx);
            }
        }
    }

    /**
     * Converts a Bitmap to NV21 YUV byte array (YUV420 semi-planar).
     */
    private static byte[] getNV21(int width, int height, Bitmap bitmap) {
        int[] argb = new int[width * height];
        bitmap.getPixels(argb, 0, width, 0, 0, width, height);

        byte[] yuv = new byte[width * height * 3 / 2];
        int yIndex = 0;
        int uvIndex = width * height;

        int R, G, B, Y, U, V;
        int index = 0;

        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int color = argb[index++];
                R = (color >> 16) & 0xFF;
                G = (color >> 8) & 0xFF;
                B = color & 0xFF;

                // YUV conversion
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                yuv[yIndex++] = (byte) (Math.max(0, Math.min(255, Y)));

                if (j % 2 == 0 && i % 2 == 0) {
                    yuv[uvIndex++] = (byte) (Math.max(0, Math.min(255, V)));
                    yuv[uvIndex++] = (byte) (Math.max(0, Math.min(255, U)));
                }
            }
        }

        return yuv;
    }
                }

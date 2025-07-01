package com.example.animevideomaker;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class VideoEncoder {
    private static final String TAG = "VideoEncoder";

    private static final String MIME_TYPE = "video/avc"; // H.264
    private static final int FRAME_RATE = 10; // fps
    private static final int IFRAME_INTERVAL = 1; // seconds
    private static final int BIT_RATE = 2000_000; // 2Mbps

    public static void save(List<VideoFrame> frames) {
        if (frames == null || frames.isEmpty()) {
            Log.e(TAG, "No frames to encode.");
            return;
        }

        int width = frames.get(0).getBitmap().getWidth();
        int height = frames.get(0).getBitmap().getHeight();

        try {
            File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                    "AnimeVideo_" + System.currentTimeMillis() + ".mp4");

            MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            format.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);

            MediaCodec encoder = MediaCodec.createEncoderByType(MIME_TYPE);
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            encoder.start();

            MediaMuxer muxer = new MediaMuxer(outputFile.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            int trackIndex = -1;
            boolean muxerStarted = false;

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            long presentationTimeUs = 0;

            for (VideoFrame frame : frames) {
                Bitmap bitmap = frame.getBitmap();
                byte[] yuv = getNV21(width, height, bitmap);

                int inputBufferIndex = encoder.dequeueInputBuffer(10000);
                if (inputBufferIndex >= 0) {
                    ByteBuffer inputBuffer = encoder.getInputBuffer(inputBufferIndex);
                    inputBuffer.clear();
                    inputBuffer.put(yuv);
                    encoder.queueInputBuffer(inputBufferIndex, 0, yuv.length, presentationTimeUs, 0);
                }

                presentationTimeUs += 1_000_000L / FRAME_RATE;

                // Drain encoder output
                while (true) {
                    int outputIndex = encoder.dequeueOutputBuffer(bufferInfo, 10000);
                    if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        break;
                    } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        if (muxerStarted) {
                            throw new IllegalStateException("Format changed twice");
                        }
                        MediaFormat newFormat = encoder.getOutputFormat();
                        trackIndex = muxer.addTrack(newFormat);
                        muxer.start();
                        muxerStarted = true;
                    } else if (outputIndex >= 0) {
                        ByteBuffer encodedData = encoder.getOutputBuffer(outputIndex);
                        if (bufferInfo.size != 0 && muxerStarted) {
                            encodedData.position(bufferInfo.offset);
                            encodedData.limit(bufferInfo.offset + bufferInfo.size);
                            muxer.writeSampleData(trackIndex, encodedData, bufferInfo);
                        }
                        encoder.releaseOutputBuffer(outputIndex, false);
                    }
                }
            }

            // Send end-of-stream
            int inputBufferIndex = encoder.dequeueInputBuffer(10000);
            if (inputBufferIndex >= 0) {
                encoder.queueInputBuffer(inputBufferIndex, 0, 0, presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            }

            // Drain remaining
            boolean end = false;
            while (!end) {
                int outputIndex = encoder.dequeueOutputBuffer(bufferInfo, 10000);
                if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    break;
                } else if (outputIndex >= 0) {
                    ByteBuffer encodedData = encoder.getOutputBuffer(outputIndex);
                    if (bufferInfo.size != 0 && muxerStarted) {
                        encodedData.position(bufferInfo.offset);
                        encodedData.limit(bufferInfo.offset + bufferInfo.size);
                        muxer.writeSampleData(trackIndex, encodedData, bufferInfo);
                    }
                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        end = true;
                    }
                    encoder.releaseOutputBuffer(outputIndex, false);
                }
            }

            encoder.stop();
            encoder.release();
            muxer.stop();
            muxer.release();

            Log.i(TAG, "Video saved to: " + outputFile.getAbsolutePath());

        } catch (Exception e) {
            Log.e(TAG, "Encoding failed: " + e.getMessage(), e);
        }
    }

    /**
     * Converts a Bitmap to NV21 YUV format byte[].
     * This is needed for MediaCodec with COLOR_FormatYUV420Flexible.
     */
    private static byte[] getNV21(int inputWidth, int inputHeight, Bitmap scaled) {
        int[] argb = new int[inputWidth * inputHeight];
        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);

        byte[] yuv = new byte[inputWidth * inputHeight * 3 / 2];
        int yIndex = 0;
        int uvIndex = inputWidth * inputHeight;

        int a, R, G, B, Y, U, V;
        int index = 0;

        for (int j = 0; j < inputHeight; j++) {
            for (int i = 0; i < inputWidth; i++) {
                a = (argb[index] & 0xff000000) >> 24; // unused
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff);

                // YUV conversion
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                yuv[yIndex++] = (byte) (Math.max(0, Math.min(255, Y)));

                if (j % 2 == 0 && i % 2 == 0) {
                    yuv[uvIndex++] = (byte) (Math.max(0, Math.min(255, V)));
                    yuv[uvIndex++] = (byte) (Math.max(0, Math.min(255, U)));
                }

                index++;
            }
        }

        return yuv;
    }
}

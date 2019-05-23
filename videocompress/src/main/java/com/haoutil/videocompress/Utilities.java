package com.haoutil.videocompress;

import android.media.MediaCodecInfo;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.MediaBox;
import com.coremedia.iso.boxes.MediaHeaderBox;
import com.coremedia.iso.boxes.SampleSizeBox;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.TrackHeaderBox;
import com.googlecode.mp4parser.util.Matrix;
import com.googlecode.mp4parser.util.Path;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;

public class Utilities {

    public static VideoEditedInfo createCompressionSettings(String videoPath) {
        TrackHeaderBox trackHeaderBox = null;
        int originalBitrate = 0;
        int bitrate = 0;
        float videoDuration = 0.0f;
        long videoFramesSize = 0;
        long audioFramesSize = 0;
        int videoFramerate = 25;
        try {
            IsoFile isoFile = new IsoFile(videoPath);
            List<Box> boxes = Path.getPaths(isoFile, "/moov/trak/");

            Box boxTest = Path.getPath(isoFile, "/moov/trak/mdia/minf/stbl/stsd/mp4a/");
            if (boxTest == null) {
            }

            boxTest = Path.getPath(isoFile, "/moov/trak/mdia/minf/stbl/stsd/avc1/");
            if (boxTest == null) {
                return null;
            }

            for (int b = 0; b < boxes.size(); b++) {
                Box box = boxes.get(b);
                TrackBox trackBox = (TrackBox) box;
                long sampleSizes = 0;
                long trackBitrate = 0;
                MediaBox mediaBox = null;
                MediaHeaderBox mediaHeaderBox = null;
                try {
                    mediaBox = trackBox.getMediaBox();
                    mediaHeaderBox = mediaBox.getMediaHeaderBox();
                    SampleSizeBox sampleSizeBox = mediaBox.getMediaInformationBox().getSampleTableBox().getSampleSizeBox();
                    long[] sizes = sampleSizeBox.getSampleSizes();
                    for (int a = 0; a < sizes.length; a++) {
                        sampleSizes += sizes[a];
                    }
                    if (videoDuration == 0) {
                        videoDuration = (float) mediaHeaderBox.getDuration() / (float) mediaHeaderBox.getTimescale();
                        if (videoDuration == 0) {
                            MediaPlayer player = null;
                            try {
                                player = new MediaPlayer();
                                player.setDataSource(videoPath);
                                player.prepare();
                                videoDuration = player.getDuration() / 1000.0f;
                                if (videoDuration < 0) {
                                    videoDuration = 0;
                                }
                            } catch (Throwable ignore) {

                            } finally {
                                try {
                                    if (player != null) {
                                        player.release();
                                    }
                                } catch (Throwable ignore) {

                                }
                            }
                        }
                    }
                    if (videoDuration != 0) {
                        trackBitrate = (int) (sampleSizes * 8 / videoDuration);
                    } else {
                        trackBitrate = 400000;
                    }
                } catch (Exception e) {
                    Log.e("tmessages", e.getMessage());
                }
                TrackHeaderBox headerBox = trackBox.getTrackHeaderBox();
                if (headerBox.getWidth() != 0 && headerBox.getHeight() != 0) {
                    if (trackHeaderBox == null || trackHeaderBox.getWidth() < headerBox.getWidth() || trackHeaderBox.getHeight() < headerBox.getHeight()) {
                        trackHeaderBox = headerBox;
                        originalBitrate = bitrate = (int) (trackBitrate / 100000 * 100000);
                        if (bitrate > 900000) {
                            bitrate = 900000;
                        }
                        videoFramesSize += sampleSizes;

                        if (mediaBox != null && mediaHeaderBox != null) {
                            TimeToSampleBox timeToSampleBox = mediaBox.getMediaInformationBox().getSampleTableBox().getTimeToSampleBox();
                            if (timeToSampleBox != null) {
                                List<TimeToSampleBox.Entry> entries = timeToSampleBox.getEntries();
                                long delta = 0;
                                int size = Math.min(entries.size(), 11);
                                for (int a = 1; a < size; a++) {
                                    delta += entries.get(a).getDelta();
                                }
                                if (delta != 0) {
                                    videoFramerate = (int) ((double) mediaHeaderBox.getTimescale() / (delta / (size - 1)));
                                }
                            }
                        }
                    }
                } else {
                    audioFramesSize += sampleSizes;
                }
            }
        } catch (Exception e) {
            Log.e("tmessages", e.getMessage());
            return null;
        }
        if (trackHeaderBox == null) {
            return null;
        }

        if (Build.VERSION.SDK_INT < 18) {
            try {
                MediaCodecInfo codecInfo = MediaController.selectCodec(MediaController.MIME_TYPE);
                if (codecInfo == null) {
                    return null;
                } else {
                    String name = codecInfo.getName();
                    if (name.equals("OMX.google.h264.encoder") ||
                            name.equals("OMX.ST.VFM.H264Enc") ||
                            name.equals("OMX.Exynos.avc.enc") ||
                            name.equals("OMX.MARVELL.VIDEO.HW.CODA7542ENCODER") ||
                            name.equals("OMX.MARVELL.VIDEO.H264ENCODER") ||
                            name.equals("OMX.k3.video.encoder.avc") ||
                            name.equals("OMX.TI.DUCATI1.VIDEO.H264E")) {
                        return null;
                    } else {
                        if (MediaController.selectColorFormat(codecInfo, MediaController.MIME_TYPE) == 0) {
                            return null;
                        }
                    }
                }
            } catch (Exception e) {
                return null;
            }
        }
        videoDuration *= 1000;

        VideoEditedInfo videoEditedInfo = new VideoEditedInfo();
        videoEditedInfo.startTime = -1;
        videoEditedInfo.endTime = -1;
        videoEditedInfo.bitrate = bitrate;
        videoEditedInfo.originalPath = videoPath;
        videoEditedInfo.framerate = videoFramerate;
        videoEditedInfo.estimatedDuration = (long) Math.ceil(videoDuration);
        videoEditedInfo.resultWidth = videoEditedInfo.originalWidth = (int) trackHeaderBox.getWidth();
        videoEditedInfo.resultHeight = videoEditedInfo.originalHeight = (int) trackHeaderBox.getHeight();

        Matrix matrix = trackHeaderBox.getMatrix();
        if (matrix.equals(Matrix.ROTATE_90)) {
            videoEditedInfo.rotationValue = 90;
        } else if (matrix.equals(Matrix.ROTATE_180)) {
            videoEditedInfo.rotationValue = 180;
        } else if (matrix.equals(Matrix.ROTATE_270)) {
            videoEditedInfo.rotationValue = 270;
        } else {
            videoEditedInfo.rotationValue = 0;
        }

        int selectedCompression = 1;
        int compressionsCount;
        if (videoEditedInfo.originalWidth > 1280 || videoEditedInfo.originalHeight > 1280) {
            compressionsCount = 5;
        } else if (videoEditedInfo.originalWidth > 848 || videoEditedInfo.originalHeight > 848) {
            compressionsCount = 4;
        } else if (videoEditedInfo.originalWidth > 640 || videoEditedInfo.originalHeight > 640) {
            compressionsCount = 3;
        } else if (videoEditedInfo.originalWidth > 480 || videoEditedInfo.originalHeight > 480) {
            compressionsCount = 2;
        } else {
            compressionsCount = 1;
        }

        if (selectedCompression >= compressionsCount) {
            selectedCompression = compressionsCount - 1;
        }
        if (selectedCompression != compressionsCount - 1) {
            float maxSize;
            int targetBitrate;
            switch (selectedCompression) {
                case 0:
                    maxSize = 432.0f;
                    targetBitrate = 400000;
                    break;
                case 1:
                    maxSize = 640.0f;
                    targetBitrate = 900000;
                    break;
                case 2:
                    maxSize = 848.0f;
                    targetBitrate = 1100000;
                    break;
                case 3:
                default:
                    targetBitrate = 2500000;
                    maxSize = 1280.0f;
                    break;
            }
            float scale = videoEditedInfo.originalWidth > videoEditedInfo.originalHeight ? maxSize / videoEditedInfo.originalWidth : maxSize / videoEditedInfo.originalHeight;
            videoEditedInfo.resultWidth = Math.round(videoEditedInfo.originalWidth * scale / 2) * 2;
            videoEditedInfo.resultHeight = Math.round(videoEditedInfo.originalHeight * scale / 2) * 2;
            if (bitrate != 0) {
                bitrate = Math.min(targetBitrate, (int) (originalBitrate / scale));
                videoFramesSize = (long) (bitrate / 8 * videoDuration / 1000);
            }
        }

        if (selectedCompression == compressionsCount - 1) {
            videoEditedInfo.resultWidth = videoEditedInfo.originalWidth;
            videoEditedInfo.resultHeight = videoEditedInfo.originalHeight;
            videoEditedInfo.bitrate = originalBitrate;
            videoEditedInfo.estimatedSize = (int) (new File(videoPath).length());
        } else {
            videoEditedInfo.bitrate = bitrate;
            videoEditedInfo.estimatedSize = (int) (audioFramesSize + videoFramesSize);
            videoEditedInfo.estimatedSize += videoEditedInfo.estimatedSize / (32 * 1024) * 16;
        }
        if (videoEditedInfo.estimatedSize == 0) {
            videoEditedInfo.estimatedSize = 1;
        }

        return videoEditedInfo;
    }

    public native static int convertVideoFrame(ByteBuffer src, ByteBuffer dest, int destFormat, int width, int height, int padding, int swap);

    static {
        System.loadLibrary("videocompress");
    }
}

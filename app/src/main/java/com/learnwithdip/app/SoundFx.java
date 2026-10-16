package com.learnwithdip.app;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

/**
 * Âm thanh đúng/sai (v4.2): tạo sóng trực tiếp bằng AudioTrack, KHÔNG cần
 * file asset — chạy 100% offline. Đúng = 2 nốt sine đi lên ("ting"),
 * sai = sóng vuông trầm ("buzz"). Tôn trọng nút tắt âm thanh ở Hồ sơ.
 */
public class SoundFx {
    private static final int SR = 22050;

    public static void correct(Context c) {
        if (c == null || !new Store(c).isSoundOn()) return;
        playAsync(concat(sineTone(659.25, 0.09, 0.5), sineTone(880.0, 0.14, 0.5)));
    }

    public static void wrong(Context c) {
        if (c == null || !new Store(c).isSoundOn()) return;
        playAsync(buzzTone(140.0, 0.22, 0.35));
    }

    /** Nối 2 đoạn PCM. */
    private static short[] concat(short[] a, short[] b) {
        short[] out = new short[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }

    /** Sóng sine tần số f (Hz), dài dur giây, biên độ amp (0..1), fade 10ms. */
    private static short[] sineTone(double f, double dur, double amp) {
        int n = (int) (SR * dur);
        short[] pcm = new short[n];
        int fade = (int) (SR * 0.01);
        for (int i = 0; i < n; i++) {
            double env = 1.0;
            if (i < fade) env = (double) i / fade;
            if (i >= n - fade) env = (double) (n - 1 - i) / fade;
            pcm[i] = (short) (Math.sin(2 * Math.PI * f * i / SR) * amp * env * Short.MAX_VALUE);
        }
        return pcm;
    }

    /** Sóng vuông trầm = tiếng "buzz", decay dần. */
    private static short[] buzzTone(double f, double dur, double amp) {
        int n = (int) (SR * dur);
        short[] pcm = new short[n];
        for (int i = 0; i < n; i++) {
            double env = 1.0 - (double) i / n; // decay
            double v = (Math.sin(2 * Math.PI * f * i / SR) >= 0 ? 1 : -1);
            pcm[i] = (short) (v * amp * env * Short.MAX_VALUE);
        }
        return pcm;
    }

    private static void playAsync(final short[] pcm) {
        new Thread(() -> {
            AudioTrack at = null;
            try {
                at = new AudioTrack.Builder()
                        .setAudioAttributes(new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build())
                        .setAudioFormat(new AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(SR)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build())
                        .setBufferSizeInBytes(pcm.length * 2)
                        .setTransferMode(AudioTrack.MODE_STATIC)
                        .build();
                at.write(pcm, 0, pcm.length);
                at.play();
                Thread.sleep((long) (pcm.length * 1000.0 / SR) + 60);
            } catch (Exception ignored) {
            } finally {
                if (at != null) {
                    try { at.stop(); } catch (Exception ignored) { }
                    at.release();
                }
            }
        }).start();
    }
}

package com.learnwithdip.app;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

/** Phát âm từ vựng: ưu tiên file MP3 bundle trong assets/audio, fallback sang TextToSpeech của máy. */
public class AudioPlayer {
    private static MediaPlayer mp;
    private static TextToSpeech tts;

    public static void play(Context c, String audioSlug, String fallbackText) {
        stop();
        try {
            AssetFileDescriptor afd = c.getAssets().openFd("audio/" + audioSlug + ".mp3");
            MediaPlayer p = new MediaPlayer();
            p.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            p.prepare();
            p.start();
            mp = p;
            p.setOnCompletionListener(m -> {
                m.release();
                if (mp == m) mp = null;
            });
        } catch (Exception e) {
            speakFallback(c.getApplicationContext(), fallbackText);
        }
    }

    private static void speakFallback(Context c, final String text) {
        try {
            if (tts == null) {
                tts = new TextToSpeech(c, status -> {
                    if (status == TextToSpeech.SUCCESS) {
                        tts.setLanguage(Locale.US);
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "dip1");
                    }
                });
            } else {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "dip1");
            }
        } catch (Exception ignored) { }
    }

    public static void stop() {
        if (mp != null) {
            try { mp.stop(); mp.release(); } catch (Exception ignored) { }
            mp = null;
        }
        if (tts != null) {
            try { tts.stop(); } catch (Exception ignored) { }
        }
    }
}

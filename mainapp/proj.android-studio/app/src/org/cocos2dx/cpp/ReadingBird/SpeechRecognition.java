package org.cocos2dx.cpp.ReadingBird;

import android.app.Activity;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;

/**
 * Speech-recognition stub.
 *
 * Background:
 *   The original implementation depended on PocketSphinx via the
 *   `pocketsphinx-aar` module, which ships `classes.jar` but **not** the
 *   `libpocketsphinx_jni.so` native library. Instantiating the decoder
 *   therefore crashes with `UnsatisfiedLinkError` on every device.
 *
 *   The Android `SpeechRecognizer` path was tried but the target tablet
 *   does not have an offline ms-MY language model installed, so it returns
 *   `ERROR_LANGUAGE_NOT_SUPPORTED`. The product requires offline support.
 *
 * This stub keeps ReadingBird playable offline by always reporting a
 * passing score and a constant volume. No actual recognition is performed.
 *
 * Replace this with a real offline ASR (e.g. Vosk Indonesian or Whisper.cpp)
 * when one is integrated.
 *
 * The public surface used by AppActivity / the C++ side is preserved.
 */
public class SpeechRecognition {
    public static final String PATH_SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String PATH_ROOT = PATH_SDCARD + File.separator + "Speech";
    private static final String RECORD_FILE_NAME = "speech.pcm";
    private static final String TAG = "SpeechRecognition";

    /** Score reported to the C++ layer for a "successful" utterance. */
    private static final int PASS_SCORE = 90;

    /** Approximate duration we pretend the child is speaking, in ms. */
    private static final int SIMULATED_LISTEN_MS = 2500;

    private final Handler mMain = new Handler(Looper.getMainLooper());
    private boolean mIsListening = false;
    private Runnable mPendingFinish;

    public void setup(Activity activity) {
        makeRootFolder();
        Log.i(TAG, "stub setup OK (no real ASR)");
    }

    public void cleanUp() {
        cancelPending();
    }

    public void startListening(int triggerVolume, int silentVolume, String phone) {
        Log.i(TAG, "stub startListening phone=" + phone);
        cancelPending();
        mIsListening = true;

        // Emit a few synthetic volume ticks so the bird's "listening" UI
        // doesn't look frozen.
        mMain.postDelayed(volumeTick(40), 250);
        mMain.postDelayed(volumeTick(70), 800);
        mMain.postDelayed(volumeTick(55), 1500);
        mMain.postDelayed(volumeTick(30), 2200);

        mPendingFinish = new Runnable() {
            @Override public void run() {
                if (!mIsListening) return;
                mIsListening = false;
                Log.i(TAG, "stub finishing -> score=" + PASS_SCORE);
                onRecordScore(PASS_SCORE);
            }
        };
        mMain.postDelayed(mPendingFinish, SIMULATED_LISTEN_MS);
    }

    public void stopListeningAndRecognition() {
        // C++ asks us to wrap up. Finish immediately with a passing score.
        if (mIsListening) {
            cancelPending();
            mIsListening = false;
            Log.i(TAG, "stub stop -> score=" + PASS_SCORE);
            mMain.post(new Runnable() {
                @Override public void run() { onRecordScore(PASS_SCORE); }
            });
        }
    }

    public void pauseListeningAndRecognition() { cancelPending(); mIsListening = false; }
    public void resumeListeningAndRecognition() { /* no-op */ }

    public String getSpeechRecordFilePath() {
        return PATH_ROOT + File.separator + RECORD_FILE_NAME;
    }

    private Runnable volumeTick(final int v) {
        return new Runnable() {
            @Override public void run() {
                if (mIsListening) onRecordVolume(v);
            }
        };
    }

    private void cancelPending() {
        if (mPendingFinish != null) {
            mMain.removeCallbacks(mPendingFinish);
            mPendingFinish = null;
        }
    }

    private void makeRootFolder() {
        File f = new File(PATH_ROOT);
        if (!f.exists()) f.mkdirs();
    }

    public static native void onRecordVolume(int volume);
    public static native void onRecordScore(int score);
}

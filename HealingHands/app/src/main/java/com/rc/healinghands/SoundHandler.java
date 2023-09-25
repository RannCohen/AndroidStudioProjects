package com.rc.healinghands;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

public class SoundHandler {

    private static final String TAG = SoundHandler.class.getSimpleName();

    static void playWelcomeSound(Context context) {
        Log.i(TAG, "Shalom");
        MediaPlayer shalom = MediaPlayer.create(context, R.raw.shalom);
        shalom.start();
    }

    static void playIntervalSound(Context context) {
        Log.i(TAG, "Big Ding");
        MediaPlayer big_ding = MediaPlayer.create(context, R.raw.interval_sound);
        big_ding.start();
    }

    static void playEndSound(Context context) {
        Log.i(TAG, "Small Ding");
        MediaPlayer small_ding = MediaPlayer.create(context, R.raw.end_sound);
        small_ding.start();
    }
}

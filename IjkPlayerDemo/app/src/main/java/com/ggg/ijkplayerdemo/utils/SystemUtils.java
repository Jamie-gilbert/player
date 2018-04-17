package com.ggg.ijkplayerdemo.utils;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import android.view.WindowManager;

import com.ggg.ijkplayerdemo.application.BaseApplication;

/**
 * Created by ggg on 2018/4/16.
 */
public class SystemUtils {
    private static String TAG = "SystemUtils";

    public static float getCurrentVolume() {
        Context context = BaseApplication.getInstance();
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        float current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        return current;
    }

    public static float getMaxVolume() {
        Context context = BaseApplication.getInstance();
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        float maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        return maxVolume;
    }

    public static void setCurrentVolume(int dVolume) {

        Context context = BaseApplication.getInstance();
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, dVolume,
                AudioManager.FLAG_SHOW_UI);


    }


    public static float getScreenBrightness(Activity activity) {
        return activity.getWindow().getAttributes().screenBrightness;
    }

    public static void setScreenBrightness(Activity activity, float brightness) {
        WindowManager.LayoutParams layoutParams = activity.getWindow().getAttributes();
        layoutParams.screenBrightness = brightness;
        Log.d(TAG, "setScreenBrightness: " + brightness);
        AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0,
                AudioManager.FLAG_SHOW_UI);
        activity.getWindow().setAttributes(layoutParams);

    }

}

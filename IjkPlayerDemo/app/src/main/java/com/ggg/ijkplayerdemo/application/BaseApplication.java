package com.ggg.ijkplayerdemo.application;

import android.app.Application;

/**
 * Created by ggg on 2018/4/16.
 */
public class BaseApplication extends Application {
    private static BaseApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

    }

    public static BaseApplication getInstance() {
        return instance;
    }
}


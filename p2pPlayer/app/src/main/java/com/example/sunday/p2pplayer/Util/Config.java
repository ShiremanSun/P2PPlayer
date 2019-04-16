package com.example.sunday.p2pplayer.Util;

import android.os.Environment;

/**
 * Created by Sunday on 2019/4/8
 */
public class Config {
    public static final String SDCARD_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String DEFAULT_CACHE_DIR = SDCARD_DIR + "/PLDroidPlayer";
}

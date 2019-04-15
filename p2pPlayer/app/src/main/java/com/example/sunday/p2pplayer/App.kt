package com.example.sunday.p2pplayer

import android.app.Application
import com.alibaba.android.arouter.launcher.ARouter

/**
 * Created by Sunday on 2019/4/6
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        ARouter.init(this)
    }
}
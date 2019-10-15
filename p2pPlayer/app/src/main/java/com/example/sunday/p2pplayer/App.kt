package com.example.sunday.p2pplayer

import android.app.Application
import com.sunday.networklistener.manager.NetWorkManager


/**
 * Created by Sunday on 2019/4/6
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        NetWorkManager.initReceiver(this)
    }
}
package com.example.sunday.p2pplayer;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.example.sunday.p2pplayer.bittorrent.BTContext;
import com.example.sunday.p2pplayer.bittorrent.BTEngine;

import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.Random;

/**
 * Created by sunday on 19-4-25.
 */

public class MyApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    private static class BTEngineInitializer implements Runnable {
        private final WeakReference<Context> mainAppRef;

        BTEngineInitializer(WeakReference<Context> mainAppRef) {
            this.mainAppRef = mainAppRef;
        }

        public void run() {


            BTContext ctx = new BTContext();
//            ctx.homeDir = paths.libtorrent();
//            ctx.torrentsDir = paths.torrents();
//            ctx.dataDir = paths.data();
            ctx.optimizeMemory = true;

            // port range [37000, 57000]
            int port0 = 37000 + new Random().nextInt(20000);
            int port1 = port0 + 10; // 10 retries
            String iface = "0.0.0.0:%1$d,[::]:%1$d";
            ctx.interfaces = String.format(Locale.US, iface, port0);
            ctx.retries = port1 - port0;

//            Simulate slow BTContext initialization
//            try {
//                Thread.sleep(60000);
//            } catch (InterruptedException e) {
//            }
//            String[] vStrArray = Constants.FROSTWIRE_VERSION_STRING.split("\\.");
//            ctx.version[0] = Integer.valueOf(vStrArray[0]);
//            ctx.version[1] = Integer.valueOf(vStrArray[1]);
//            ctx.version[2] = Integer.valueOf(vStrArray[2]);
//            ctx.version[3] = BuildConfig.VERSION_CODE;

            BTEngine.ctx = ctx;
//            BTEngine.onCtxSetupComplete();
//            BTEngine.getInstance().start();


        }


    }
}

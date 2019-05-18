package com.example.sunday.p2pplayer.bittorrent

import android.content.SharedPreferences
import android.net.Uri
import android.os.Environment
import com.example.sunday.p2pplayer.transfer.TransferManager
import com.masterwok.simpletorrentandroid.TorrentSessionOptions
import java.util.concurrent.*

/**
 * Created by sunday on 19-4-23.
 */
object DownLoadManager  {


    private val CPU_COUNT = Runtime.getRuntime().availableProcessors() +1
    val threadPool = ThreadPoolExecutor(CPU_COUNT,
            CPU_COUNT*2 + 1,
            1L,
            TimeUnit.SECONDS,
            LinkedBlockingQueue()
            )



    fun downloadTorrent(uri : Uri, displayName:String) {
        val runnable= Runnable {
            TransferManager.downloadTorrent(uri, displayName)
        }
        DownLoadManager.threadPool.execute(runnable)
    }




    //下载任务

}
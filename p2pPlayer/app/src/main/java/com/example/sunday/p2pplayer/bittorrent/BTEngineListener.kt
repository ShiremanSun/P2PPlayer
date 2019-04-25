package com.example.sunday.p2pplayer.bittorrent

/**
 * Created by sunday on 19-4-25.
 */
interface BTEngineListener {


    abstract fun downloadAdded(engine: BTEngine, dl: BTDownload)

    abstract fun downloadUpdate(engine: BTEngine, dl: BTDownload)
}
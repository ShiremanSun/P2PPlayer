package com.example.sunday.p2pplayer.bittorrent

import java.io.File

/**
 * Created by sunday on 19-4-25.
 */
interface BTDownloadListener {
    abstract fun finished(dl: BTDownload)

    abstract fun removed(dl: BTDownload, incompleteFiles: Set<File>)
}
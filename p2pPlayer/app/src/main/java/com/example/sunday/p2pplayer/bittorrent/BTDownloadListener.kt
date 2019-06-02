package com.example.sunday.p2pplayer.bittorrent

import java.io.File

/**
 * Created by sunday on 19-4-25.
 */
interface BTDownloadListener {
    fun finished(dl: BTDownload)

    fun removed(dl: BTDownload, incompleteFiles: Set<File>)
}
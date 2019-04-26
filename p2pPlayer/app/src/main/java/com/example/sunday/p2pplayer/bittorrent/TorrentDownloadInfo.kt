package com.example.sunday.p2pplayer.bittorrent

/**
 *Created by sunday on 19-4-24.
 */
interface TorrentDownloadInfo {
    fun getTorrentUrl(): String

    fun getDisplayName(): String

}
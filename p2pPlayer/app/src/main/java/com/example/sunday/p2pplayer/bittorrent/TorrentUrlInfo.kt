package com.example.sunday.p2pplayer.bittorrent

/**
*Created by sunday on 19-4-24.
*/
class TorrentUrlInfo(private val url: String, private val displayName: String) : TorrentDownloadInfo {

    override fun getTorrentUrl(): String {
        return url
    }


    override fun getDisplayName(): String {
        return displayName
    }







}
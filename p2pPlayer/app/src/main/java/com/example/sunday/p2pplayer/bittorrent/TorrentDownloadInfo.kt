package com.example.sunday.p2pplayer.bittorrent

/**
 * Created by sunday on 19-4-24.
 */
interface TorrentDownloadInfo {
    abstract fun makeMagnetUri(): String

    abstract fun getTorrentUrl(): String

    abstract fun getDetailsUrl(): String

    abstract fun getDisplayName(): String

    abstract fun getSize(): Long

    abstract fun getHash(): String

    abstract fun getRelativePath(): String

    abstract fun getReferrerUrl(): String
}
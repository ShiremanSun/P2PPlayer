package com.example.sunday.p2pplayer.bittorrent

/**
 * Created by sunday on 19-4-24.
 */
class TorrentUrlInfo : TorrentDownloadInfo {
    private val url: String
    private val displayName: String?
    constructor(url : String, displayName: String){
        this.url = url
        this.displayName = displayName

    }
    override fun makeMagnetUri(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTorrentUrl(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getDetailsUrl(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getDisplayName(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSize(): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getHash(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getRelativePath(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getReferrerUrl(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
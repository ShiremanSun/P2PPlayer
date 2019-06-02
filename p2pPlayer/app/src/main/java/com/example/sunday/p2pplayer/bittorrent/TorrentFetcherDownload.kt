package com.example.sunday.p2pplayer.bittorrent

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.util.Log
import com.example.sunday.p2pplayer.Util.HttpUtil
import com.example.sunday.p2pplayer.transfer.TransferItem
import com.example.sunday.p2pplayer.transfer.TransferManager
import com.example.sunday.p2pplayer.transfer.TransferState
import com.frostwire.jlibtorrent.TorrentInfo
import java.io.File
import java.net.URI
import java.util.*

/**
 *Created by sunday on 19-4-24.
 */
class TorrentFetcherDownload(private val torrentDownloadInfo: TorrentDownloadInfo) : BitTorrentDownload {


    private var state : TransferState
    override fun getName(): String {
        return torrentDownloadInfo.getDisplayName()
    }

    override fun getDisplayName(): String {
        return torrentDownloadInfo.getDisplayName()

    }

    override fun getInfoHash(): String {
        return ""
    }

    override fun getSavePath(): File? {
        return null
    }

    override fun previewFile(): File? {
        return null
    }

    override fun getSize(): Long {
        return -1
    }

    override fun getCreated(): Date {
        return Date()
    }

    override fun getState(): TransferState {
        return state
    }

    override fun getBytesReceived(): Long {
        return 0L
    }

    override fun getBytesSent(): Long {
        return 0L
    }

    override fun getDownloadSpeed(): Long {
        return 0L
    }

    override fun magnetUri(): String {
        return ""
    }

    override fun getUploadSpeed(): Long {
        return 0
    }

    override fun getConnectedPeers(): Int {
        return 0
    }

    override fun isDownloading(): Boolean {
        return false
    }

    override fun getTotalPeers(): Int {
        return 0
    }

    override fun getConnectedSeeds(): Int {
        return 0
    }

    override fun getTotalSeeds(): Int {
        return 0
    }

    override fun getETA(): Long {
        return -1
    }

    override fun getProgress(): Int {
        return 0
    }

    override fun isComplete(): Boolean {
        return false
    }

    override fun getItems(): MutableList<TransferItem>? {
        return null
    }



    override fun getContentSavePath(): File? {
        return null
    }

    override fun isPaused(): Boolean {
        return true
    }

    override fun isSeeding(): Boolean {
        return false
    }

    override fun isFinished(): Boolean {
        return false
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun remove(deleteTorrent: Boolean, deleteData: Boolean) {
        state = TransferState.CANCELED
        TransferManager.remove(this)
    }

    override fun getPredominantFileExtension(): String {
        return ""
    }



    @SuppressLint("LongLogTag")
    private fun startDownload() {
        if (state === TransferState.CANCELED) {
            return
        }
        try {
            val data: ByteArray?
            val url = torrentDownloadInfo.getTorrentUrl()
            //开始下载种子
            data = HttpUtil.sendRequest(url).body()?.bytes()

            if (state === TransferState.CANCELED) {
                return
            }
            if (data != null) {
                try {
                    //先把当前的下载种子任务移除
                    remove(deleteTorrent = false,deleteData = false)
                    val ti = TorrentInfo.bdecode(data)
                    //开始下载文件
                    BTEngine.downloadFile(ti, null)
                } catch (e : Throwable) {
                    Log.e("Error downloading torrent", e.message)
                }
            } else {
                state = TransferState.ERROR
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            state = TransferState.ERROR
        }

    }
    init {
        this.state = TransferState.DOWNLOADING_TORRENT
        TransferManager.bitTorrentDownloads.add(this)
        TransferManager.bitTorrentDownloadMap[getInfoHash()] = this
        startDownload()
    }
}
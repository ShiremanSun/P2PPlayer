package com.example.sunday.p2pplayer.bittorrent

import android.annotation.SuppressLint
import android.util.Log
import com.example.sunday.p2pplayer.Util.HttpUtil
import com.example.sunday.p2pplayer.transfer.TransferItem
import com.example.sunday.p2pplayer.transfer.TransferManager
import com.example.sunday.p2pplayer.transfer.TransferState
import com.frostwire.jlibtorrent.TorrentInfo
import java.io.File
import java.util.*

/**
 *Created by sunday on 19-4-24.
 */
class TorrentFetcherDownload(private val torrentDownloadInfo: TorrentDownloadInfo) : BittorrentDownload {


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

    override fun getSavePath(): File {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun previewFile(): File {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSize(): Long {
        return -1
    }

    override fun getCreated(): Date? {
        return null
    }

    override fun getState(): TransferState {
        return state
    }

    override fun getBytesReceived(): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getBytesSent(): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getDownloadSpeed(): Long {
        return 0
    }

    override fun magnetUri(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getUploadSpeed(): Long {
        return 0
    }

    override fun getConnectedPeers(): Int {
        return 0
    }

    override fun isDownloading(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItems(): MutableList<TransferItem> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remove(deleteData: Boolean) {
        state = TransferState.CANCELED
        TransferManager.remove(this)

    }

    override fun getContentSavePath(): File {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isPaused(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isSeeding(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isFinished(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun pause() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun resume() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remove(deleteTorrent: Boolean, deleteData: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPredominantFileExtension(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
                    remove(false)
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
            state = TransferState.ERROR
        }

    }
    init {
        this.state = TransferState.DOWNLOADING_TORRENT
        TransferManager.bitTorrentDownloads.add(this)
        TransferManager.bitTorrentDownloadMap.put(infoHash, this)
        startDownload()
    }
}
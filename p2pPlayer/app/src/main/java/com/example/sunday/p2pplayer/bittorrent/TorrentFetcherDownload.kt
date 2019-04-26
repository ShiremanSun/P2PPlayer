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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCreated(): Date {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getState(): TransferState {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getBytesReceived(): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getBytesSent(): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getDownloadSpeed(): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun magnetUri(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getUploadSpeed(): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getConnectedPeers(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isDownloading(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTotalPeers(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getConnectedSeeds(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTotalSeeds(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getETA(): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getProgress(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    fun getTorrentUri() : String{
        return torrentDownloadInfo.getTorrentUrl()
    }

    private inner class FetcherRunnable : Runnable {

        override fun run() {
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
                        downloadTorrent(data)
                    } finally {
                        remove(false)
                    }
                } else {
                    state = TransferState.ERROR
                }
            } catch (e: Throwable) {
                state = TransferState.ERROR

            }

        }
    }

    //开始真正的文件下载工作
    @SuppressLint("LongLogTag")
    private fun downloadTorrent(data : ByteArray) {
        try {
            val ti = TorrentInfo.bdecode(data)


           //val selection = calculateSelection(ti, torrentDownloadInfo.getRelativePath())


            BTEngine.downloadFile(ti, null)
        } catch (e: Throwable) {
            Log.e("Error downloading torrent", e.message)
        }

    }

    private fun calculateSelection(ti: TorrentInfo, path: String): BooleanArray {
        val selection = BooleanArray(ti.numFiles())

        val fs = ti.files()
        for (i in selection.indices) {
            val filePath = fs.filePath(i)
            if (path.endsWith(filePath) || filePath.endsWith(path)) {
                selection[i] = true
            }
        }

        return selection
    }

    init {
        this.state = TransferState.DOWNLOADING_TORRENT
        val runnable = FetcherRunnable()
        Thread(runnable, "FetcherThread" + torrentDownloadInfo.getTorrentUrl()).start()
    }
}
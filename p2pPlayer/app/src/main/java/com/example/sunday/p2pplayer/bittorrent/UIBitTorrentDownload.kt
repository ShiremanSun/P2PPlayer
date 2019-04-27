package com.example.sunday.p2pplayer.bittorrent

import android.util.Log
import com.example.sunday.p2pplayer.transfer.TransferItem
import com.example.sunday.p2pplayer.transfer.TransferManager
import com.example.sunday.p2pplayer.transfer.TransferState
import java.io.File
import java.io.IOException
import java.util.*

/**
* Created by sunday on 19-4-25.
*/
class UIBitTorrentDownload(private val dl: BTDownload) : BittorrentDownload {
    private var displayName : String
    private var size : Long
    private var list : List<TransferItem>

    init {
        dl.setListener(StatusListener())
        this.displayName = dl.displayName
        size = calculateSize(dl)
        this.list = calculateItems(dl)
        if (!dl.wasPause()) {
            dl.resume()
        }
    }
    fun updateUI(dl: BTDownload) {
        displayName = dl.displayName
        size = calculateSize(dl)
        list = calculateItems(dl)

    }
    companion object {
        private const val TAG = "UIBitTorrent"
    }
    override fun getName(): String {
        return dl.name
    }

    override fun getDisplayName(): String {
        return dl.displayName
    }

    override fun getInfoHash(): String {
        return dl.infoHash
    }

    override fun getSavePath(): File {
        return dl.savePath
    }

    override fun previewFile(): File? {
        return dl.previewFile()
    }

    override fun getSize(): Long {
        return dl.size
    }

    override fun getCreated(): Date {
        return dl.created
    }

    override fun getState(): TransferState {
        return dl.state
    }

    override fun getBytesReceived(): Long {
        return dl.bytesReceived
    }

    override fun getBytesSent(): Long {
        return dl.bytesSent
    }

    override fun getDownloadSpeed(): Long {
        return dl.downloadSpeed
    }

    override fun magnetUri(): String {
        return dl.magnetUri()
    }

    override fun getUploadSpeed(): Long {
        return dl.uploadSpeed
    }

    override fun getConnectedPeers(): Int {
        return dl.connectedPeers
    }

    override fun isDownloading(): Boolean {
        return dl.isDownloading
    }

    override fun getTotalPeers(): Int {
        return dl.totalPeers
    }

    override fun getConnectedSeeds(): Int {
        return dl.connectedSeeds
    }

    override fun getTotalSeeds(): Int {
        return dl.totalSeeds
    }

    override fun getETA(): Long {
        return dl.eta
    }

    override fun getProgress(): Int {
        return dl.progress
    }

    override fun isComplete(): Boolean {
        return dl.isComplete
    }

    override fun getItems(): MutableList<TransferItem> {
        return dl.items
    }

    override fun remove(deleteData: Boolean) {
        dl.remove(deleteData)
        TransferManager.remove(this)
    }

    override fun getContentSavePath(): File? {
        return dl.contentSavePath
    }

    override fun isPaused(): Boolean {
        return dl.isPaused
    }

    override fun isSeeding(): Boolean {
        return dl.isSeeding
    }

    override fun isFinished(): Boolean {
        return dl.isFinished
    }

    override fun pause() {
        dl.pause()
    }

    override fun resume() {
        dl.resume()
    }

    override fun remove(deleteTorrent: Boolean, deleteData: Boolean) {
        remove(deleteData)
    }

    override fun getPredominantFileExtension(): String? {
        return dl.predominantFileExtension
    }

    private fun calculateSize(dl: BTDownload) : Long{
        var size = dl.size
        val partial = dl.isPartial()
        if (partial) {
            val lists = dl.items
            val totalSize: Long = lists
                    .filterNot { it.isSkipped() }
                    .map { it.getSize() }
                    .sum()
            if (totalSize > 0) {
                size = totalSize
            }
        }
        return size
    }

    private fun calculateItems(dl: BTDownload) : List<TransferItem>{
        return dl.items.filterNot { it.isSkipped() }
    }

    private inner class StatusListener : BTDownloadListener{
        override fun finished(dl: BTDownload) {
            //下载完成，应该添加到下载完成列表

        }

        override fun removed(dl: BTDownload, incompleteFiles: Set<File>) {
            cleanFile(incompleteFiles)
        }

        private fun cleanFile(incompleteFiles: Set<File>) {
            incompleteFiles.forEach {
                if (it.exists()  && !it.delete()) {
                    Log.i(TAG, "无法删除")
                }
            }
            deleteInCompleteFile(dl.savePath)
        }

        private fun deleteInCompleteFile(dictionary : File) : Boolean {
            //删除临时文件夹
            val canonicalParent = dictionary.canonicalPath

            if (! dictionary.isDirectory) {
                return false
            }
            var canDelete = true
            val files = dictionary.listFiles()
            if (files !=null && files.isNotEmpty()) {
                files.forEach {
                    try {
                        if (!it.canonicalPath.startsWith(canonicalParent)) {
                            return@forEach
                        }
                    }catch (e:IOException) {
                        canDelete = false
                    }
                  if (!deleteInCompleteFile(it)) {
                      canDelete = false
                  }
                }
            }
            return canDelete && dictionary.delete()
        }
    }


}
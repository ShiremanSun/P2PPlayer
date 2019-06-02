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
class UIBitTorrentDownload(val dl: BTDownload) : BitTorrentDownload {
    private var displayName : String
    private var size : Long
    private var list : List<TransferItem>

    init {
        dl.setListener(StatusListener())
        this.displayName = dl.getDisplayName()
        size = calculateSize(dl)
        this.list = calculateItems(dl)
        if (!dl.wasPause()) {
            dl.resume()
        }
    }
    fun updateUI(dl: BTDownload) {
        displayName = dl.getDisplayName()
        size = calculateSize(dl)
        list = calculateItems(dl)

    }
    companion object {
        private const val TAG = "UIBitTorrent"
    }
    override fun getName(): String {
        return dl.getName()
    }

    override fun getDisplayName(): String {
        return dl.getDisplayName()
    }

    override fun getInfoHash(): String {
        return dl.getInfoHash()
    }

    override fun getSavePath(): File {
        return dl.getSavePath()
    }

    override fun previewFile(): File {
        return dl.previewFile()!!
    }

    override fun getSize(): Long {
        return dl.getSize()
    }

    override fun getCreated(): Date {
        return dl.getCreated()
    }

    override fun getState(): TransferState {
        return dl.getState()
    }

    override fun getBytesReceived(): Long {
        return dl.getBytesReceived()
    }

    override fun getBytesSent(): Long {
        return dl.getBytesSent()
    }

    override fun getDownloadSpeed(): Long {
        return dl.getDownloadSpeed()
    }

    override fun magnetUri(): String {
        return dl.magnetUri()
    }

    override fun getUploadSpeed(): Long {
        return dl.getUploadSpeed()
    }

    override fun getConnectedPeers(): Int {
        return dl.getConnectedPeers()
    }

    override fun isDownloading(): Boolean {
        return dl.isDownloading()
    }

    override fun getTotalPeers(): Int {
        return dl.getTotalPeers()
    }

    override fun getConnectedSeeds(): Int {
        return dl.getConnectedSeeds()
    }

    override fun getTotalSeeds(): Int {
        return dl.getTotalSeeds()
    }

    override fun getETA(): Long {
        return dl.getETA()
    }

    override fun getProgress(): Int {
        return dl.getProgress()
    }

    override fun isComplete(): Boolean {
        return dl.isComplete()
    }

    override fun getItems(): MutableList<TransferItem> {
        return dl.getItems()
    }



    override fun getContentSavePath(): File? {
        return dl.getContentSavePath()
    }

    override fun isPaused(): Boolean {
        return dl.isPaused()
    }

    override fun isSeeding(): Boolean {
        return dl.isSeeding()
    }

    override fun isFinished(): Boolean {
        return dl.isFinished()
    }

    override fun pause() {
        dl.pause()
    }

    override fun resume() {
        dl.resume()
    }

    override fun remove(deleteTorrent: Boolean, deleteData: Boolean) {
        dl.remove(deleteData = deleteData)
        TransferManager.remove(this)
    }

    override fun getPredominantFileExtension(): String? {
        return dl.getPredominantFileExtension()
    }



    private fun calculateSize(dl: BTDownload) : Long{
        var size = dl.getSize()
        val partial = dl.isPartial()
        if (partial) {
            val lists = dl.getItems()
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
        return dl.getItems().filterNot { it.isSkipped() }
    }

    private inner class StatusListener : BTDownloadListener{
        override fun finished(dl: BTDownload) {
            //下载完成，应该添加到下载完成列表
            //原来是用来判断是否要不要停止播种
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
            deleteInCompleteFile(dl.getSavePath())
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
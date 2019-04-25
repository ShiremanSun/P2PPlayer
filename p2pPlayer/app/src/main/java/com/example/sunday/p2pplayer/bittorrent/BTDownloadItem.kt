package com.example.sunday.p2pplayer.bittorrent

import com.example.sunday.p2pplayer.transfer.TransferItem
import com.frostwire.jlibtorrent.PiecesTracker
import com.frostwire.jlibtorrent.Priority
import com.frostwire.jlibtorrent.TorrentHandle
import java.io.File

/**
* Created by sunday on 19-4-25.
*/
class BTDownloadItem : TransferItem{

    private val th: TorrentHandle
    private val index: Int

    private val file: File
    private val name: String
    private val size: Long

    private val piecesTracker: PiecesTracker?

    constructor(th: TorrentHandle, index: Int, filePath: String, fileSize: Long, piecesTracker: PiecesTracker){
        this.th = th
        this.index = index

        this.file = File(th.savePath(), filePath)
        this.name = file.name
        this.size = fileSize

        this.piecesTracker = piecesTracker
    }
    override fun getName(): String {
        return name    }

    override fun getDisplayName(): String {
        return name    }

    override fun getFile(): File {
        return file    }

    override fun getSize(): Long {
        return size    }

    override fun isSkipped(): Boolean {
        return th.filePriority(index) == Priority.IGNORE    }

    override fun getDownloaded(): Long {
        if (!th.isValid) {
            return 0
        }

        val progress = th.fileProgress(TorrentHandle.FileProgressFlags.PIECE_GRANULARITY)
        return progress[index]    }

    override fun getProgress(): Int {
        if (!th.isValid || size == 0L) { // edge cases
            return 0
        }

        val progress: Int
        val downloaded = getDownloaded()

        progress = if (downloaded == size) {
            100
        } else {
            ((getDownloaded() * 100).toFloat() / size.toFloat()).toInt()
        }

        return progress    }

    override fun isComplete(): Boolean {
        return getDownloaded() == size    }

    fun getSequentialDownloaded(): Long {
        return piecesTracker?.getSequentialDownloadedBytes(index) ?: 0
    }
}
package com.example.sunday.p2pplayer.bittorrent

import com.example.sunday.p2pplayer.transfer.TransferItem
import com.example.sunday.p2pplayer.transfer.TransferState
import java.io.File
import java.util.*

/**
 * Created by Sunday on 2019/5/31
 */
interface BitTorrentDownload {
     fun getInfoHash(): String

    /**
     * Generates a magnet URI using the current information in
     * the torrent. If the underlying torrent handle is invalid,
     * null is returned.
     *
     * @return
     */
     fun magnetUri(): String

     fun getConnectedPeers(): Int

     fun getTotalPeers(): Int

     fun getConnectedSeeds(): Int

     fun getTotalSeeds(): Int

    /**
     * For multi files torrents, returns the folder containing the files (savePath/torrentName)
     * For single file torrents, returns the path to the single file of the torrent (savePath/singleFile)
     *
     * @return
     */
     fun getContentSavePath(): File?

     fun isPaused(): Boolean

     fun isSeeding(): Boolean

     fun isFinished(): Boolean

     fun pause()

     fun resume()


    /**
     * Adds up the number of bytes per file extension and returns
     * the winning file extension for the torrent.
     *
     * If the files are not known, then it returns "torrent"
     * @return
     */
     fun getPredominantFileExtension(): String?

      fun getName(): String

      fun getDisplayName(): String

      fun getSavePath(): File?

      fun previewFile(): File?

      fun getSize(): Long

      fun getCreated(): Date

      fun getState(): TransferState

      fun getBytesReceived(): Long

      fun getBytesSent(): Long

      fun getDownloadSpeed(): Long

      fun getUploadSpeed(): Long

      fun isDownloading(): Boolean

    // TODO: add this method in the future
    //boolean isUploading();

      fun getETA(): Long

    /**
     * [0..100]
     *
     * @return
     */
      fun getProgress(): Int

      fun isComplete(): Boolean

      fun getItems(): List<TransferItem>?

      fun remove(deleteTorrent:Boolean=true, deleteData:Boolean)
}
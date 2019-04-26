package com.example.sunday.p2pplayer.transfer

import android.net.Uri
import com.example.sunday.p2pplayer.bittorrent.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 *Created by sunday on 19-4-24.
 */
object TransferManager{

    private val bitTorrentDownloads = CopyOnWriteArrayList<BittorrentDownload>()
    private val bitTorrentDownloadMap = ConcurrentHashMap<String, BittorrentDownload>(0)

    private val alreadyDownloadingMonitor = Any()

    init {
        loadTorrentTask()
    }

    fun downloadTorrent(url : String, displayName : String) : BittorrentDownload? {
        val fixedUrl = url.trim()
        if (isAlreadyDownloadingTorrentByUri(fixedUrl)) {
            return null
        }
        val u = Uri.parse(url)
        val download = TorrentFetcherDownload(TorrentUrlInfo(u.toString(), displayName))
        bitTorrentDownloads.add(download)
        bitTorrentDownloadMap.put(download.infoHash, download)
        return download

    }

    private fun isAlreadyDownloadingTorrentByUri(uri: String): Boolean {
        synchronized(alreadyDownloadingMonitor) {
            bitTorrentDownloads
                    .filterIsInstance<TorrentFetcherDownload>()
                    .map { it.getTorrentUri() }
                    .filter { it == uri }
                    .forEach { return true }
        }
        return false
    }

    fun remove(transfer : Transfer) : Boolean{
        bitTorrentDownloadMap.remove((transfer as BittorrentDownload).infoHash)
        return bitTorrentDownloads.remove(transfer)
    }

    private fun loadTorrentTask() {
        bitTorrentDownloadMap.clear()
        bitTorrentDownloads.clear()
        BTEngine.setBTEListener(object : BTEngineListener {

            override fun downloadAdded(engine: BTEngine, dl: BTDownload) {
                val savePath = dl.savePath
                val name = dl.name
                if (name != null && name.contains("fetch_magnet")) {
                    return
                }
                if (savePath != null && savePath.toString().contains("fetch_magnet")) {
                    return
                }
                val uiBitTorrentDownload = UIBitTorrentDownload(dl)
                bitTorrentDownloads.add(uiBitTorrentDownload)
                bitTorrentDownloadMap.put(dl.infoHash, uiBitTorrentDownload)
            }

            override fun downloadUpdate(engine: BTEngine, dl: BTDownload) {
                val download = bitTorrentDownloadMap[dl.infoHash]
                if (download is UIBitTorrentDownload) {
                    download.updateUI(dl)
                }
            }

        })
    }
}
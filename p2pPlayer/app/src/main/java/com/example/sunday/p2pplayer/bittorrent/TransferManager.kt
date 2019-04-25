package com.example.sunday.p2pplayer.bittorrent

import android.net.Uri
import com.example.sunday.p2pplayer.transfer.BittorrentDownload
import com.example.sunday.p2pplayer.transfer.Transfer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Created by sunday on 19-4-24.
 */
object TransferManager {
    private val bitTorrentDownloads = CopyOnWriteArrayList<BittorrentDownload>()
    private val bitTorrentDownloadMap = ConcurrentHashMap<String, BittorrentDownload>(0)


    private val alreadyDownloadingMonitor = Any()

    public fun downloadTorrent(url : String, displayName : String) : BittorrentDownload? {
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
                    .filter { it != null && it == uri }
                    .forEach { return true }
        }
        return false
    }
    public fun remove(transfer : Transfer) : Boolean{
        bitTorrentDownloadMap.remove((transfer as BittorrentDownload).infoHash)
        return bitTorrentDownloads.remove(transfer)
    }

    public fun loadTorrentTask() {
        bitTorrentDownloadMap.clear()
        bitTorrentDownloads.clear()
        BTEngine.setBTEListener(object : BTEngineListener{


            override fun downloadAdded(engine: BTEngine, dl: BTDownload) {

            }

            override fun downloadUpdate(engine: BTEngine, dl: BTDownload) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })
    }
}
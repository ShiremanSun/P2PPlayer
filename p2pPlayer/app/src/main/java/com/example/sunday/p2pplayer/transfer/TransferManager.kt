package com.example.sunday.p2pplayer.transfer

import android.net.Uri
import com.example.sunday.p2pplayer.bittorrent.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 *Created by sunday on 19-4-24.
 */
object TransferManager{

    val bitTorrentDownloads = CopyOnWriteArrayList<BittorrentDownload>()
     val bitTorrentDownloadMap = ConcurrentHashMap<String, BittorrentDownload>(0)


    init {
        loadTorrentTask()
    }

    fun downloadTorrent(url : String, displayName : String) : BittorrentDownload? {
        val u = Uri.parse(url)
         return TorrentFetcherDownload(TorrentUrlInfo(u.toString(), displayName))

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
        //初始化的时候，检查一遍种子文件，然后开启下载，引擎里会判断每个种子的下载状态
        BTEngine.restoreDownloads()
    }
}
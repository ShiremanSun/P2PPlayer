package com.example.sunday.p2pplayer.downloading

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.example.sunday.p2pplayer.bittorrent.BitTorrentDownload

/**
 *Created by sunday on 19-4-25.
 */
class DownloadingViewModel : ViewModel() {
    val downloadList = MutableLiveData<List<BitTorrentDownload>>()

}
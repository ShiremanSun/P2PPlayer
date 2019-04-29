package com.example.sunday.p2pplayer.downloaded

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.example.sunday.p2pplayer.bittorrent.BittorrentDownload

/**
 *Created by sunday on 19-4-29.
 */
class DownloadedVM : ViewModel(){
     val liveData = MutableLiveData<List<BittorrentDownload>>()
}
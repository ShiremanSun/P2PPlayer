package com.example.sunday.p2pplayer.downloaded

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.sunday.p2pplayer.R
import com.example.sunday.p2pplayer.bittorrent.BTDownload
import com.example.sunday.p2pplayer.bittorrent.BTDownloadListener
import com.example.sunday.p2pplayer.bittorrent.BittorrentDownload
import com.example.sunday.p2pplayer.downloading.FragmentDownloading
import com.gyf.immersionbar.ImmersionBar
import java.io.File

/**
 * Created by Sunday on 2019/4/15
 */
class FragmentDownloaded : Fragment(), FragmentDownloading.CompleteListener{


    override fun complete(bittorrentDownload: BittorrentDownload) {
        completedDownloads.add(bittorrentDownload)
        viewModel.liveData.value = completedDownloads    }

    private val completedDownloads = ArrayList<BittorrentDownload>()
    //从已下载的缓存中读取内容
    private lateinit var recyclerView : RecyclerView
    private lateinit var viewModel : DownloadedVM
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        viewModel = ViewModelProviders.of(this).get(DownloadedVM::class.java)

        val view = inflater.inflate(R.layout.fragment_downloaded, container, false)
        recyclerView = view.findViewById(R.id.recyclerView3)
        val toolBar = view.findViewById<Toolbar>(R.id.toolBar)
        ImmersionBar.setTitleBar(activity, toolBar)
        return view
    }



}
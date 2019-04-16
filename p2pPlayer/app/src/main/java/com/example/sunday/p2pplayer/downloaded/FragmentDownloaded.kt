package com.example.sunday.p2pplayer.downloaded

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.sunday.p2pplayer.R
import com.example.sunday.p2pplayer.model.Movie

/**
 * Created by Sunday on 2019/4/15
 */
class FragmentDownloaded : Fragment() {

    //从已下载的缓存中读取内容
    private lateinit var recyclerView : RecyclerView
    public val list = ArrayList<Movie>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       val view = inflater.inflate(R.layout.fragment_downloaded, container, false)
        recyclerView = view.findViewById(R.id.recyclerView3)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

}
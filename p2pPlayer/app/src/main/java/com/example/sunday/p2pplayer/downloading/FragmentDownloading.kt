package com.example.sunday.p2pplayer.downloading

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.sunday.p2pplayer.R
import com.example.sunday.p2pplayer.model.Movie

/**
 * Created by Sunday on 2019/4/15
 */
class FragmentDownloading : Fragment() {


    private lateinit var recyclerView : RecyclerView
    public val list = ArrayList<Movie>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_downloading, container, false)
        recyclerView = view.findViewById(R.id.recyclerView2)

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        //从正在下载的list中获取
        Log.d("FragmentDownloading","onResume")
        super.onResume()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        Log.d("FragmentDownloading","hidden")
        super.onHiddenChanged(hidden)
    }


}
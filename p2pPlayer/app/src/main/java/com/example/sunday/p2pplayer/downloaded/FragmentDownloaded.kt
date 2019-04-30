package com.example.sunday.p2pplayer.downloaded

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.sunday.p2pplayer.R
import com.example.sunday.p2pplayer.Util.MOVIE_URL
import com.example.sunday.p2pplayer.bittorrent.BittorrentDownload
import com.example.sunday.p2pplayer.downloading.FragmentDownloading
import com.example.sunday.p2pplayer.movieplay.VideoActivity
import com.gyf.immersionbar.ImmersionBar
import java.util.*

/**
 * Created by Sunday on 2019/4/15
 */
class FragmentDownloaded : Fragment(), FragmentDownloading.CompleteListener{


    private val completedDownloads = ArrayList<BittorrentDownload>()

    private lateinit var recyclerView : RecyclerView


    private val mAdapter by lazy {
        MyAdapter()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_downloaded, container, false)
        recyclerView = view.findViewById(R.id.recyclerView3)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = mAdapter
        val toolBar = view.findViewById<Toolbar>(R.id.toolBar)
        ImmersionBar.setTitleBar(activity, toolBar)
        return view
    }
    override fun complete(bittorrentDownload: BittorrentDownload) {
        if (!completedDownloads.contains(bittorrentDownload)) {
            completedDownloads.add(bittorrentDownload)
            mAdapter.notifyDataSetChanged()
        }
    }

    inner class MyAdapter : RecyclerView.Adapter<MyAdapter.ViewHolder>(){

        override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
            p0.title.text = completedDownloads[p1].name
            Glide.with(this@FragmentDownloaded).
                    load(completedDownloads[p1].contentSavePath.absolutePath).
                    placeholder(R.drawable.noimage).
                    transform(CenterCrop(), RoundedCorners(10)).
                    diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                    into(p0.cur)
            //读取sharedPreference,判断已经观看的时间
            p0.itemView.setOnClickListener {
                val intent = Intent(activity, VideoActivity::class.java)
                intent.putExtra(MOVIE_URL, completedDownloads[p1].contentSavePath.absolutePath)
                activity?.startActivity(intent)
            }
            p0.itemView.setOnLongClickListener {
                val dialog = AlertDialog.Builder(context)
                return@setOnLongClickListener true
            }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            val view = LayoutInflater.from(activity).inflate(R.layout.item_downloaded, p0, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return completedDownloads.size
        }

        inner class ViewHolder(v : View) : RecyclerView.ViewHolder(v){
            val cur = v.findViewById<ImageView>(R.id.movie_cav)
            val title = v.findViewById<TextView>(R.id.movie_title)
        }
    }

}
package com.example.sunday.p2pplayer.downloaded

import android.app.AlertDialog
import android.content.Context
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
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.sunday.p2pplayer.R
import com.example.sunday.p2pplayer.Util.MOVIE_NAME
import com.example.sunday.p2pplayer.Util.MOVIE_URL
import com.example.sunday.p2pplayer.Util.TIME_PREFERENCE
import com.example.sunday.p2pplayer.Util.getBytesInHuman
import com.example.sunday.p2pplayer.bittorrent.BitTorrentDownload
import com.example.sunday.p2pplayer.downloading.FragmentDownloading
import com.example.sunday.p2pplayer.movieplay.VideoActivity
import com.gyf.immersionbar.ImmersionBar
import java.util.*

/**
 * Created by Sunday on 2019/4/15
 */
class FragmentDownloaded : Fragment(), FragmentDownloading.CompleteListener{


    private val completedDownloads = ArrayList<BitTorrentDownload>()

    private lateinit var recyclerView : RecyclerView


    private val mAdapter by lazy {
        MyAdapter()
    }


    private  var mLoadingView : LinearLayout? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_downloaded, container, false)
        recyclerView = view.findViewById(R.id.recyclerView3)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = mAdapter
        val toolBar = view.findViewById<Toolbar>(R.id.toolBar)
        ImmersionBar.setTitleBar(activity, toolBar)
        mLoadingView = view.findViewById(R.id.LoadingView)
        return view
    }
    override fun complete(bitTorrentDownload: BitTorrentDownload) {
        if (!completedDownloads.contains(bitTorrentDownload)) {
            completedDownloads.add(bitTorrentDownload)
            if (mLoadingView?.visibility == View.VISIBLE) {
                mLoadingView?.visibility = View.GONE
            }
        }
        mAdapter.notifyDataSetChanged()
    }

    inner class MyAdapter : RecyclerView.Adapter<MyAdapter.ViewHolder>(){

        override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
            p0.title.text = completedDownloads[p1].getDisplayName()
            p0.uploadSpeed.text = String.format("%s/s", getBytesInHuman(completedDownloads[p1].getUploadSpeed()))
            Glide.with(this@FragmentDownloaded).
                    load(completedDownloads[p1].getContentSavePath()?.absolutePath).
                    placeholder(R.drawable.noimage).
                    transform(CenterCrop(), RoundedCorners(10)).
                    into(p0.cur)
            //读取sharedPreference,判断已经观看的时间
            val time = context?.getSharedPreferences(TIME_PREFERENCE, Context.MODE_PRIVATE)?.
                    getString(completedDownloads[p1].getContentSavePath()?.absolutePath, "0")
            p0.detail.text = String.format("上次观看到： %s", time)
            p0.movieSize.text = String.format("文件大小：%s", getBytesInHuman(completedDownloads[p1].getSize()))
            p0.itemView.setOnClickListener {
                val intent = Intent(activity, VideoActivity::class.java)
                intent.putExtra(MOVIE_NAME, completedDownloads[p1].getDisplayName())
                intent.putExtra(MOVIE_URL, completedDownloads[p1].getContentSavePath()?.absolutePath)
                activity?.startActivity(intent)
            }
            p0.itemView.setOnLongClickListener {
                val dialog = AlertDialog.Builder(context)
                dialog.setMessage("是否要删除下载文件")
                dialog.setPositiveButton("确定") { _, _ ->
                    val editor = context?.getSharedPreferences(TIME_PREFERENCE, Context.MODE_PRIVATE)?.edit()
                    editor?.remove(completedDownloads[p1].getContentSavePath()?.absolutePath)
                    editor?.apply()
                    completedDownloads[p1].remove(deleteData = true)
                    completedDownloads.removeAt(p1)
                    notifyDataSetChanged()
                }
                dialog.setNegativeButton("取消") { _dialog, _ ->
                    _dialog.cancel()
                }
                dialog.setCancelable(true)
                dialog.show()
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
            val cur = v.findViewById<ImageView>(R.id.movie_cav)!!
            val title = v.findViewById<TextView>(R.id.movie_title)!!
            val detail = v.findViewById<TextView>(R.id.movie_detail)!!
            val uploadSpeed = v.findViewById<TextView>(R.id.upload_speed)!!
            val movieSize = v.findViewById<TextView>(R.id.movie_size)!!
        }
    }

}
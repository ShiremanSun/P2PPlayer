package com.example.sunday.p2pplayer.downloading

import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.example.sunday.p2pplayer.R
import com.example.sunday.p2pplayer.Util.INFOHASH
import com.example.sunday.p2pplayer.Util.getBytesInHuman
import com.example.sunday.p2pplayer.bittorrent.BittorrentDownload
import com.example.sunday.p2pplayer.transfer.Transfer
import com.example.sunday.p2pplayer.transfer.TransferManager
import com.example.sunday.p2pplayer.transfer.TransferState
import com.example.sunday.p2pplayer.transferdetail.DetailActivity
import com.gyf.immersionbar.ImmersionBar
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by Sunday on 2019/4/15
 */
class FragmentDownloading : Fragment(){

    companion object {
        private const val TAG = "DownLoading"
    }
    private lateinit var recyclerView : RecyclerView

    lateinit var viewModel : DownloadingViewModel

    private val list = ArrayList<BittorrentDownload>()

    var bitDownloadListener : CompleteListener? = null
    private val mHandler = Handler(Looper.getMainLooper())
    private val comparator = TransferComparator()

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel = ViewModelProviders.of(this).get(DownloadingViewModel::class.java)
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_downloading, container, false)
        recyclerView = view.findViewById(R.id.recyclerView2)
        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        val adapter = MyAdapter()
        recyclerView.adapter = adapter
        val toolBar = view.findViewById<Toolbar>(R.id.toolBar)
        ImmersionBar.setTitleBar(activity, toolBar)
        viewModel.downloadList.observe(this, Observer {
            list.clear()
            if (it != null) {
                //把下载完成的过滤掉，然后排序
                val newList = filter(it)
                Collections.sort(newList, comparator)
                list.addAll(newList)
            }
            adapter.notifyDataSetChanged()
        })

        mHandler.post(MyTimerTask(this))
        return view
    }

    private fun filter(list : List<BittorrentDownload>) : List<BittorrentDownload> {
        //返回正在下载的
        return list.filter { isDownloading(it) }
    }

    inner class MyAdapter : RecyclerView.Adapter<MyAdapter.ViewHolder>() {
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder{
            val view = LayoutInflater.from(activity).inflate(R.layout.item_downloading, p0,false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
            p0.updateUI(p1)
            p0.itemView.setOnClickListener({
                val download = list[p1]
                when(download.state) {
                    TransferState.DOWNLOADING -> download.pause()
                    TransferState.PAUSED -> download.resume()
                    else -> download.pause()
                }
            })
            p0.itemView.setOnLongClickListener {
                val dialog = AlertDialog.Builder(context)
                dialog.setMessage("是否要删除任务以及下载文件")
                dialog.setPositiveButton("确定", {_, _ ->
                    list[p1].remove(true)
                    list.removeAt(p1)
                    notifyItemChanged(p1)
                })
                dialog.setNegativeButton("取消", {_dialog, _ ->
                    _dialog.cancel()
                })
                dialog.show()
                return@setOnLongClickListener true
            }

            p0.detail.setOnClickListener {
                val intent = Intent(activity, DetailActivity::class.java)
                intent.putExtra(INFOHASH, list[p1].infoHash)
                activity?.startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return list.size
        }

        inner class ViewHolder(v : View) : RecyclerView.ViewHolder(v) {
            val title = v.findViewById<TextView>(R.id.view_transfer_list_item_title)!!
            val progessBar = v.findViewById<ProgressBar>(R.id.view_transfer_list_item_progress)!!
            val status = v.findViewById<TextView>(R.id.view_transfer_list_item_status)!!
            private val peers = v.findViewById<TextView>(R.id.view_transfer_list_item_peers)!!
            val size = v.findViewById<TextView>(R.id.view_transfer_list_item_size)!!
            val downSpeed = v.findViewById<TextView>(R.id.view_transfer_list_item_speed)!!
            val uploadSpeed = v.findViewById<TextView>(R.id.view_transfer_list_item_upspeed)!!
            private val seeds = v.findViewById<TextView>(R.id.view_transfer_list_item_seeds)!!
            val detail = v.findViewById<ImageButton>(R.id.torrent_details_button)!!
            fun updateUI(position : Int) {
                val download = list[position]
                title.text = download.name
                peers.text = String.format("节点 %s", formatPeers(download))
                seeds.text = String.format("播种 %s", formatSeeds(download))
                setProgress(progessBar,download.progress)
                val downloadStatus = when(download.state) {
                    TransferState.FINISHING -> "即将完成"
                    TransferState.CHECKING -> "正在检查"
                    TransferState.DOWNLOADING -> "正在下载"
                    TransferState.DOWNLOADING_TORRENT -> "正在下载种子"
                    TransferState.FINISHED -> "已完成"
                    TransferState.PAUSED -> "暂停"
                    TransferState.ERROR, TransferState.ERROR_CONNECTION_TIMED_OUT,
                    TransferState.ERROR_DISK_FULL, TransferState.ERROR_HASH_MD5,
                    TransferState.ERROR_MOVING_INCOMPLETE, TransferState.ERROR_NOT_ENOUGH_PEERS,
                    TransferState.ERROR_NO_INTERNET, TransferState.ERROR_SAVE_DIR,
                    TransferState.ERROR_SIGNATURE, TransferState.ERROR_TEMP_DIR -> "错误"
                    else -> ""
                }
                status.text = downloadStatus
                downSpeed.text = String.format("%s/s", getBytesInHuman(download.downloadSpeed))
                uploadSpeed.text = String.format("%s/s", getBytesInHuman(download.uploadSpeed))
                size.text = getBytesInHuman(download.size)
            }
        }

        private fun formatPeers(dl: BittorrentDownload): String {
            val connectedPeers = dl.connectedPeers
            val peers = dl.totalPeers
            var tmp = if (connectedPeers > peers) "%1" else "%1 " + "/" + " %2"
            tmp = tmp.replace("%1", connectedPeers.toString())
            tmp = tmp.replace("%2", peers.toString())
            return tmp
        }
        private fun formatSeeds(dl: BittorrentDownload): String {
            val connectedSeeds = dl.connectedSeeds
            val seeds = dl.totalSeeds
            var tmp = if (connectedSeeds > seeds) "%1" else "%1 " + "/" + " %2"
            tmp = tmp.replace("%1", connectedSeeds.toString())
            tmp = tmp.replace("%2", seeds.toString())
            return tmp
        }

        private fun setProgress(progressBar: ProgressBar, progress : Int) {
            if (progressBar.progress != progress) {
                progressBar.progress = progress
            }
        }
    }

    private fun isDownloading(transfer: BittorrentDownload): Boolean {
        val state = transfer.state
        return state === TransferState.CHECKING ||
                state === TransferState.DOWNLOADING ||
                state === TransferState.DEMUXING ||
                state === TransferState.ALLOCATING ||
                state === TransferState.DOWNLOADING_METADATA ||
                state === TransferState.DOWNLOADING_TORRENT ||
                state === TransferState.FINISHING ||
                state === TransferState.PAUSING ||
                state === TransferState.PAUSED
    }

    private fun isSeeding(transfer: Transfer): Boolean {
        return transfer.state === TransferState.SEEDING
    }



     class MyTimerTask(fragment: FragmentDownloading) : Runnable {
         private val weakReference = WeakReference<FragmentDownloading>(fragment)
        override fun run() {
            if (weakReference.get() == null || !weakReference.get()!!.isVisible) {
                return
            }
            weakReference.get()?.viewModel?.downloadList?.value = TransferManager.bitTorrentDownloads
            TransferManager.bitTorrentDownloads.filter { isCompleted(it) }
                    .forEach { weakReference.get()?.bitDownloadListener?.complete(it) }
            weakReference.get()?.mHandler?.postDelayed(this, 2000)
        }
         private inline fun isCompleted(transfer: Transfer): Boolean {
             val state = transfer.state
             return state === TransferState.FINISHED || state === TransferState.COMPLETE || state === TransferState.SEEDING
         }

    }
    class TransferComparator : Comparator<Transfer> {
        override fun compare(o1: Transfer?, o2: Transfer?): Int {
            try {
                return o1!!.created.compareTo(o2!!.created)
            }catch (e : Exception) {

            }
            return 0
        }

    }



    interface CompleteListener{
        fun complete(bittorrentDownload: BittorrentDownload)
    }

}
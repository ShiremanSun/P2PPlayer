package com.example.sunday.p2pplayer.downloading

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.example.sunday.p2pplayer.R
import com.example.sunday.p2pplayer.Util.getBytesInHuman
import com.example.sunday.p2pplayer.bittorrent.BittorrentDownload
import com.example.sunday.p2pplayer.transfer.Transfer
import com.example.sunday.p2pplayer.transfer.TransferManager
import com.example.sunday.p2pplayer.transfer.TransferState
import com.gyf.immersionbar.ImmersionBar
import java.lang.ref.WeakReference
import java.util.*
import kotlin.Comparator

/**
 * Created by Sunday on 2019/4/15
 */
class FragmentDownloading : Fragment(){

    companion object {
        private const val TAG = "DownLoading"
    }

    private lateinit var recyclerView : RecyclerView
    private val mTimerTask = MyTimerTask(this)
    private val mTimer = Timer()
    lateinit var viewModel : DownloadingViewModel

    private val list = ArrayList<BittorrentDownload>()


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
                //先排序
                Collections.sort(it, comparator)
                list.addAll(it)
            }
            adapter.notifyDataSetChanged()
        })
        mTimer.schedule(mTimerTask, 0,2000)
        return view
    }


    inner class MyAdapter : RecyclerView.Adapter<MyAdapter.ViewHolder>() {
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder{
            val view = LayoutInflater.from(activity).inflate(R.layout.item_downloading, p0,false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
            p0.updateUI(p1)
        }

        override fun getItemCount(): Int {
            return list.size
        }

        inner class ViewHolder(v : View) : RecyclerView.ViewHolder(v) {
            val curve = v.findViewById<ImageView>(R.id.view_transfer_list_item_download_type_indicator)!!
            val title = v.findViewById<TextView>(R.id.view_transfer_list_item_title)!!
            val progessBar = v.findViewById<ProgressBar>(R.id.view_transfer_list_item_progress)!!
            val status = v.findViewById<TextView>(R.id.view_transfer_list_item_status)!!
            private val peers = v.findViewById<TextView>(R.id.view_transfer_list_item_peers)!!
            val size = v.findViewById<TextView>(R.id.view_transfer_list_item_size)!!
            val downSpeed = v.findViewById<TextView>(R.id.view_transfer_list_item_speed)!!
            val upSpeed = v.findViewById<TextView>(R.id.view_transfer_list_item_speed_upload)!!
            private val seeds = v.findViewById<TextView>(R.id.view_transfer_list_item_seeds)!!
            fun updateUI(position : Int) {
                val download = list[position]
                val rootView = itemView as LinearLayout
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
                upSpeed.text = String.format("%s/s", getBytesInHuman(download.uploadSpeed))
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

    private fun isDownloading(transfer: Transfer): Boolean {
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

    private fun isCompleted(transfer: Transfer): Boolean {
        val state = transfer.state
        return state === TransferState.FINISHED || state === TransferState.COMPLETE
    }

     class MyTimerTask(fragment: FragmentDownloading) : TimerTask() {
         private val weakReference = WeakReference<FragmentDownloading>(fragment)
        override fun run() {
            if (weakReference.get() == null || !weakReference.get()!!.isVisible) {
                return
            }
            weakReference.get()?.activity?.runOnUiThread({
                weakReference.get()!!.viewModel.downloadList.value = TransferManager.bitTorrentDownloads
            })
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

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        Log.d("Downloading",isVisibleToUser.toString())

        super.setUserVisibleHint(isVisibleToUser)
    }

    override fun onDestroy() {
        mTimer.cancel()
        super.onDestroy()
    }

}
package com.example.sunday.p2pplayer.transferdetail

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.example.sunday.p2pplayer.R
import com.example.sunday.p2pplayer.Util.INFOHASH
import com.example.sunday.p2pplayer.Util.getBytesInHuman
import com.example.sunday.p2pplayer.bittorrent.BittorrentDownload
import com.example.sunday.p2pplayer.bittorrent.UIBitTorrentDownload
import com.example.sunday.p2pplayer.transfer.TransferManager
import com.example.sunday.p2pplayer.transfer.TransferState
import com.frostwire.jlibtorrent.PeerInfo
import com.frostwire.jlibtorrent.TcpEndpoint
import com.gyf.immersionbar.ImmersionBar
import java.lang.ref.WeakReference

class DetailActivity : AppCompatActivity() {


    companion object {
        private val TAG = "DetailActivity"
    }
    private lateinit var peersInfo : MutableList<PeerInfo>

    val mHandler = Handler(Looper.getMainLooper())
    private lateinit var mBTDownload : BittorrentDownload

    private lateinit var mStatus : TextView
    private lateinit var mState : String
    private lateinit var adapter : DetailsListAdapter
    private val mRecyclerView by lazy { findViewById<RecyclerView>(R.id.details_list) }
    private val mPeersText by lazy { findViewById<TextView>(R.id.num) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val toolBar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolBar)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        toolBar.setNavigationOnClickListener {
            finish()
        }
        ImmersionBar.with(this).navigationBarColor(R.color.colorPrimary).init()
        ImmersionBar.with(this).titleBar(toolBar).init()

        val headLayout = findViewById<LinearLayout>(R.id.head_layout)
        val appBar = findViewById<AppBarLayout>(R.id.appBar)
        val collapsing = findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar)
        mStatus = findViewById(R.id.status)

        mRecyclerView.layoutManager = LinearLayoutManager(this)

        val infoHash = intent.getStringExtra(INFOHASH)

        //拿到点击的下载任务
        mBTDownload = TransferManager.getBitDownload(infoHash)

        mState = when (mBTDownload.state) {
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
        mStatus.text = mState
        peersInfo = if (mBTDownload is UIBitTorrentDownload) {
            (mBTDownload as UIBitTorrentDownload).dl.th.peerInfo()
        } else {
            ArrayList()
        }

        mPeersText.text = String.format("共%d个节点", peersInfo.size)
        adapter = DetailsListAdapter(peersInfo)

        mRecyclerView.adapter = adapter

        appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { p0, p1 ->
            if (p1 == 0 ) {
                collapsing.title = ""
                headLayout.visibility = View.VISIBLE
            }else {
                collapsing.title = mState
                headLayout.visibility = View.INVISIBLE
            }
        })

        mHandler.post(MyTask(this))

    }

    class DetailsListAdapter(list: MutableList<PeerInfo>) : RecyclerView.Adapter<DetailsListAdapter.ViewHolder>() {

       private  val peersList : MutableList<PeerInfo> = list

       fun updatePeers(list: MutableList<PeerInfo>) {
           peersList.clear()
           peersList.addAll(list)
       }
       override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
           val peer = peersList[p1].swig()
           val peerInfo = peersList[p1]
           p0.address.text = TcpEndpoint(peer.ip).toString()
          if (String(peerInfo.client()).isEmpty()) {
              p0.client.text = "未知"
          } else {
              p0.client.text = String(peerInfo.client())
          }
           p0.downSpeed.text = getBytesInHuman(peer.down_speed.toLong())
           p0.upSpeed.text = getBytesInHuman(peer.up_speed.toLong())
           p0.downlodedSize.text = String.format("已下载%s", getBytesInHuman(peerInfo.totalDownload()))
           p0.uploadedSize.text = String.format("已上传%s", getBytesInHuman(peerInfo.totalUpload()))
        }

        override fun getItemCount(): Int {
            return peersList.size
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            val view = LayoutInflater.from(p0.context).inflate(R.layout.item_transfer_details, p0, false)
            return ViewHolder(view)
        }

         class ViewHolder(v : View) : RecyclerView.ViewHolder(v) {
             val address = v.findViewById<TextView>(R.id.peer_item_address)!!
             val client = v.findViewById<TextView>(R.id.peer_item_client)!!
             val downSpeed = v.findViewById<TextView>(R.id.peer_item_down_speed)!!
             val upSpeed = v.findViewById<TextView>(R.id.peer_item_up_speed)!!
             val downlodedSize = v.findViewById<TextView>(R.id.peer_downloaded)!!
             val uploadedSize = v.findViewById<TextView>(R.id.peer_uploaded)!!
        }
    }

     class MyTask(activity: DetailActivity) : Runnable{
        private val weakReference = WeakReference<DetailActivity>(activity)
        override fun run() {
            val activity = weakReference.get()
            val download = activity?.mBTDownload
            val peersList = if (download is UIBitTorrentDownload) {
                download.dl.th.peerInfo()
            } else {
                ArrayList<PeerInfo>(0)
            }
            activity?.adapter?.updatePeers(peersList)
            activity?.mPeersText?.text = String.format("共%d个节点", peersList.size)
            activity?.mState = when (download?.state) {
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
            activity?.mStatus?.text = activity?.mState
            activity?.adapter?.notifyDataSetChanged()
            activity?.mHandler?.postDelayed(this, 2000)
        }
    }
}

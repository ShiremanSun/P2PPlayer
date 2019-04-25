package com.example.sunday.p2pplayer.bittorrent

import android.annotation.SuppressLint
import android.util.Log
import com.frostwire.jlibtorrent.*
import com.frostwire.jlibtorrent.alerts.*
import java.io.File
import java.util.*

import com.frostwire.jlibtorrent.alerts.AlertType.ADD_TORRENT
import com.frostwire.jlibtorrent.alerts.AlertType.DHT_BOOTSTRAP
import com.frostwire.jlibtorrent.alerts.AlertType.EXTERNAL_IP
import com.frostwire.jlibtorrent.alerts.AlertType.FASTRESUME_REJECTED
import com.frostwire.jlibtorrent.alerts.AlertType.LISTEN_FAILED
import com.frostwire.jlibtorrent.alerts.AlertType.LISTEN_SUCCEEDED
import com.frostwire.jlibtorrent.alerts.AlertType.PEER_LOG
import com.frostwire.jlibtorrent.alerts.AlertType.TORRENT_LOG
import java.util.logging.Logger

/**
 * Created by sunday on 19-4-24.
 */
object BTEngine : SessionManager() {

    private const val TAG = "BTEngine"
    public lateinit var ctx : BTContext
    private val innerListener: InnerListener = InnerListener()
    init {
        addListener(innerListener)
    }
    private var listener: BTEngineListener? = null
    private val INNER_LISTENER_TYPES = intArrayOf(ADD_TORRENT.swig(), LISTEN_SUCCEEDED.swig(), LISTEN_FAILED.swig(), EXTERNAL_IP.swig(), FASTRESUME_REJECTED.swig(), DHT_BOOTSTRAP.swig(), TORRENT_LOG.swig(), PEER_LOG.swig(), AlertType.LOG.swig())


    public fun setBTEListener(btEngineListener: BTEngineListener) {

        this.listener = btEngineListener
    }

    internal fun resumeDataFile(infoHash: String): File {
        return File(ctx.homeDir, infoHash + ".resume")
    }

    internal fun resumeTorrentFile(infoHash: String): File {
        return File(ctx.homeDir, infoHash + ".torrent")
    }

    fun download(ti: TorrentInfo, saveDir: File?, selection: BooleanArray?, peers: List<TcpEndpoint>?, dontSaveTorrentFile: Boolean) {
        var saveDir = saveDir
        var selection = selection
        if (swig() == null) {
            return
        }

        saveDir = setupSaveDir(saveDir)
        if (saveDir == null) {
            return
        }

        if (selection == null) {
            selection = BooleanArray(ti.numFiles())
            Arrays.fill(selection, true)
        }

        var priorities: Array<Priority>? = null

        val th = find(ti.infoHash())
        val torrentHandleExists = th != null
        if (torrentHandleExists) {
            try {
                priorities = th!!.filePriorities()
            } catch (t: Throwable) {
                t.printStackTrace()
            }

        } else {
            priorities = Priority.array(Priority.IGNORE, ti.numFiles())
        }
        if (priorities != null) {
            var changed = false
            for (i in selection.indices) {
                if (selection[i] && i < priorities.size && priorities[i] == Priority.IGNORE) {
                    priorities[i] = Priority.NORMAL
                    changed = true
                }
            }

            if (!changed) { // nothing to do
                return
            }
        }
        download(ti, saveDir, priorities, null, peers)

    }

    private fun download(ti: TorrentInfo, saveDir: File, priorities: Array<Priority>?, resumeFile: File?, peers: List<TcpEndpoint>?) {

        var th: TorrentHandle? = find(ti.infoHash())

        if (th != null) {
            // found a download with the same hash, just adjust the priorities if needed
            if (priorities != null) {
                if (ti.numFiles() != priorities.size) {
                    throw IllegalArgumentException("The priorities length should be equals to the number of files")
                }

                th.prioritizeFiles(priorities)
                fireDownloadUpdate(th)
                th.resume()
            } else {
                // did they just add the entire torrent (therefore not selecting any priorities)
                val wholeTorrentPriorities = Priority.array(Priority.NORMAL, ti.numFiles())
                th.prioritizeFiles(wholeTorrentPriorities)
                fireDownloadUpdate(th)
                th.resume()
            }
        } else { // new download
            download(ti, saveDir, resumeFile, priorities, peers)
            th = find(ti.infoHash())
            if (th != null) {
                fireDownloadUpdate(th)
            }
        }
    }

    @SuppressLint("LongLogTag")
    private fun fireDownloadUpdate(th: TorrentHandle) {
        try {
            val dl = BTDownload(this, th)
            if (listener != null) {
                listener!!.downloadUpdate(this, dl)
            }
        } catch (e: Throwable) {
            Log.e("Unable to notify update the a download", e.message)
        }

    }
    //  设置存储位置
    private fun setupSaveDir(saveDir: File?): File? {
        var result: File? = null

        if (saveDir == null) {
            if (ctx.dataDir != null) {
                result = ctx.dataDir
            } else {
                Log.w("","Unable to setup save dir path, review your logic, both saveDir and ctx.dataDir are null.")
            }
        } else {
            result = saveDir
        }

//        val fs = Platforms.get().fileSystem()
//
//        if (result != null && !fs.isDirectory(result) && !fs.mkdirs(result)) {
//            result = null
//            LOG.warn("Failed to create save dir to download")
//        }
//
//        if (result != null && !fs.canWrite(result)) {
//            result = null
//            LOG.warn("Failed to setup save dir with write access")
//        }

        return result
    }

    private  class InnerListener : AlertListener {
        override fun types(): IntArray {
            return INNER_LISTENER_TYPES
        }
        override fun alert(alert: Alert<*>) {

            val type = alert.type()

            when (type) {
                ADD_TORRENT -> {
                    val torrentAlert = alert as TorrentAlert<*>
                    fireDownloadAdded(torrentAlert)
                    //应该是继续下载吧
                    //runNextRestoreDownloadTask()
                }
                LISTEN_SUCCEEDED -> onListenSucceeded(alert as ListenSucceededAlert)
                LISTEN_FAILED -> onListenFailed(alert as ListenFailedAlert)
                EXTERNAL_IP -> onExternalIpAlert(alert as ExternalIpAlert)
                FASTRESUME_REJECTED -> onFastresumeRejected(alert as FastresumeRejectedAlert)
                DHT_BOOTSTRAP -> onDhtBootstrap()
                TORRENT_LOG, PEER_LOG, AlertType.LOG -> printAlert(alert)
            }
        }
    }

    private fun onExternalIpAlert(alert: ExternalIpAlert) {
        try {
            // libtorrent perform all kind of tests
            // to avoid non usable addresses
            val address = alert.externalAddress().toString()
            Log.i("External IP: " + address)
        } catch (e: Throwable) {
            LOG.error("Error saving reported external ip", e)
        }

    }
    @SuppressLint("LongLogTag")
    private fun onListenSucceeded(alert: ListenSucceededAlert) {
        try {
            val endp = alert.address().toString() + ":" + alert.port()
            val s = "endpoint: " + endp + " type:" + alert.socketType()
            Log.i("Listen succeeded on " , s)
        } catch (e: Throwable) {
            Log.e("Error adding listen endpoint to internal list", e.message)
        }
    }

    private fun onListenFailed(alert: ListenFailedAlert) {
        val endp = alert.address().toString() + ":" + alert.port()
        val s = "endpoint: " + endp + " type:" + alert.socketType()
        val message = alert.error().message()
        Log.i("Listen failed on ", message)
    }
    @SuppressLint("LongLogTag")
    private fun fireDownloadAdded(alert: TorrentAlert<*>) {
        try {
            val th = find(alert.handle().infoHash())
            if (th != null) {
                val dl = BTDownload(this, th)
                if (listener != null) {
                    listener!!.downloadAdded(this, dl)
                }
            } else {
                Log.i("torrent was not successfully added","")
            }
        } catch (e: Throwable) {
            Log.e("Unable to create and/or notify the new download", e.message)
        }

    }
}
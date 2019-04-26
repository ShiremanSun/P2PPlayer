package com.example.sunday.p2pplayer.bittorrent

import android.annotation.SuppressLint
import android.os.Environment
import android.util.Log
import com.frostwire.jlibtorrent.*
import com.frostwire.jlibtorrent.alerts.*
import com.frostwire.jlibtorrent.alerts.AlertType.*
import com.masterwok.simpletorrentandroid.TorrentSession
import com.masterwok.simpletorrentandroid.TorrentSessionOptions
import java.io.File
import java.util.*

/**
 * Created by sunday on 19-4-24.
 */
object BTEngine : SessionManager() {

    private const val TAG = "BTEngine"

    private val innerListener: InnerListener = InnerListener()

    private val path = Environment.getExternalStorageDirectory().toString() + "/movies"
    val file = File(path)

    private val torrentSessionOptions = TorrentSessionOptions(
            downloadLocation = file
            , onlyDownloadLargestFile = true
            , enableLogging = false
            , shouldStream = true
    )
    private val sessionParams = SessionParams(torrentSessionOptions.settingsPack)
    private var listener: BTEngineListener? = null
    private val INNER_LISTENER_TYPES = intArrayOf(ADD_TORRENT.swig(), LISTEN_SUCCEEDED.swig(), LISTEN_FAILED.swig(), EXTERNAL_IP.swig(), FASTRESUME_REJECTED.swig(), DHT_BOOTSTRAP.swig(), TORRENT_LOG.swig(), PEER_LOG.swig(), AlertType.LOG.swig())


    //下载引擎开始工作
    override fun start() {
        if (!isRunning){
            super.start(sessionParams)
        }
    }

    fun setBTEListener(btEngineListener: BTEngineListener) {

        this.listener = btEngineListener
    }

     fun resumeDataFile(infoHash: String): File {
        return File(torrentSessionOptions.downloadLocation, infoHash + ".resume")
    }

     fun resumeTorrentFile(infoHash: String): File {
        return File(torrentSessionOptions.downloadLocation, infoHash + ".torrent")
    }


    fun downloadFile(ti: TorrentInfo, saveDir: File?) {

        var saveDir = saveDir
        /*if (swig() == null) {
            return
        }*/
        saveDir = setupSaveDir(saveDir)
        if (saveDir == null) {
            return
        }
        var th: TorrentHandle? = find(ti.infoHash())

        if (th != null) {
            // found a download with the same hash, just adjust the priorities if needed

                // did they just add the entire torrent (therefore not selecting any priorities)
            val wholeTorrentPriorities = Priority.array(Priority.NORMAL, ti.numFiles())
            th.prioritizeFiles(wholeTorrentPriorities)
            fireDownloadUpdate(th)
            th.resume()

        } else {
            // new download
            download(ti, saveDir, null, null, null)
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
        val result: File?

        result = saveDir ?: torrentSessionOptions.downloadLocation

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

    private fun printAlert(alert: Alert<*>) {
       System.out.print(TAG+alert)
    }
    private fun onDhtBootstrap() {
        //long nodes = stats().dhtNodes();
        //LOG.info("DHT bootstrap, total nodes=" + nodes);
    }

    private fun onFastresumeRejected(alert: FastresumeRejectedAlert) {
        try {
            Log.w(TAG,"Failed to load fastresume data, path: " + alert.filePath() +
                    ", operation: " + alert.operation() + ", error: " + alert.error().message())
        } catch (e: Throwable) {
            Log.e(TAG,"Error logging fastresume rejected alert", e)
        }

    }
    private fun onExternalIpAlert(alert: ExternalIpAlert) {
        try {
            // libtorrent perform all kind of tests
            // to avoid non usable addresses
            val address = alert.externalAddress().toString()
            Log.i(TAG,"External IP: " + address)
        } catch (e: Throwable) {
            Log.e(TAG, "Error saving reported external ip", e)
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
    init {
        addListener(innerListener)
        if (!file.exists()) {
            file.mkdirs()
        }
        start()
    }
}
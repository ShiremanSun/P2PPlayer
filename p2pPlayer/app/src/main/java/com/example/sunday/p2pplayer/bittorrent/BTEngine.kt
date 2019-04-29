@file:Suppress("NAME_SHADOWING")

package com.example.sunday.p2pplayer.bittorrent

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.os.Environment
import android.util.Log
import com.frostwire.jlibtorrent.*
import com.frostwire.jlibtorrent.alerts.*
import com.frostwire.jlibtorrent.alerts.AlertType.*
import com.frostwire.jlibtorrent.swig.entry
import com.masterwok.simpletorrentandroid.TorrentSessionOptions
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.util.*

/**
 *Created by sunday on 19-4-24.
 */
object BTEngine : SessionManager() {

    private const val TAG = "BTEngine"

    private val TORRENT_ORIG_PATH_KEY = "torrent_orig_path"
    private val innerListener: InnerListener = InnerListener()

    private val path = Environment.getExternalStorageDirectory().toString() + "/movies"
    private val file = File(path)
    private val torrentPath = path + "/torrents"
    private val torrentFiles = File(torrentPath)

    private val restoreDownloadsQueue  = LinkedList<RestoreDownloadTask>()
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
        return File(torrentFiles, infoHash + ".torrent")
    }


    fun downloadFile(ti: TorrentInfo, saveDir: File?) {

        var saveDir = saveDir
        if (swig() == null) {
            return
        }
        saveDir = setupSaveDir(saveDir)
        if (saveDir == null) {
            return
        }
        var th: TorrentHandle? = find(ti.infoHash())

        if (th != null) {
            fireDownloadUpdate(th)
            th.resume()

        } else {
            // new download
            saveResumeTorrent(ti)
            download(ti, saveDir, null, null, null)
            th = find(ti.infoHash())
            if (th != null) {
                fireDownloadUpdate(th)
            }
        }
    }

    @SuppressLint("LongLogTag")
    private fun saveResumeTorrent(ti: TorrentInfo) {

        try {
            val name = getEscapedFilename(ti)

            val e = ti.toEntry().swig()
            e.dict().set(TORRENT_ORIG_PATH_KEY, entry(torrentFile(name).absolutePath))
            val arr = Vectors.byte_vector2bytes(e.bencode())
            FileUtils.writeByteArrayToFile(resumeTorrentFile(ti.infoHash().toString()), arr)
        } catch (e: Throwable) {
            Log.w("Error saving resume torrent", e)
        }

    }
    private fun torrentFile(name: String): File {
        return File(torrentFiles, name + ".torrent")
    }
    private fun getEscapedFilename(ti: TorrentInfo): String {
        var name: String? = ti.name()
        if (name == null || name.isEmpty()) {
            name = ti.infoHash().toString()
        }
        return escapeFilename(name)
    }

    private fun escapeFilename(s: String): String {
        return s.replace("[\\\\/:*?\"<>|\\[\\]]+".toRegex(), "_")
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

    fun restoreDownloads() {
        if (swig() == null) {
            return
        }
        if (!file.exists() || !torrentFiles.exists()) {
            return
        }
        torrentFiles.listFiles { _, name ->
            name != null && FilenameUtils.getExtension(name).toLowerCase() == "torrent" }
                ?.forEach {
                    val infoHash = FilenameUtils.getBaseName(it.name)
                    if (infoHash != null) {
                        val resumeFile = resumeDataFile(infoHash)
                        val savePath = readSavePath(infoHash)
                        if (setupSaveDir(savePath) == null) {
                            return
                        }
                        restoreDownloadsQueue.add(RestoreDownloadTask(it, null, null, resumeFile))
                    }
        }
        runNextRestoreDownloadTask()
    }

    private fun runNextRestoreDownloadTask() {
        var task: RestoreDownloadTask? = null
        try {
            if (!restoreDownloadsQueue.isEmpty()) {
                task = restoreDownloadsQueue.poll()
            }
        } catch (t: Throwable) {

            // on Android, LinkedList's .poll() implementation throws a NoSuchElementException
        }
        if (task != null) {
            DownLoadManager.threadPool.execute(task)
        }
    }
    //从resume文件中读取,获得存贮位置
    private fun readSavePath(infoHash: String): File? {
        var savePath: File? = null

        try {
            val arr = FileUtils.readFileToByteArray(resumeDataFile(infoHash))
            val e = entry.bdecode(Vectors.bytes2byte_vector(arr))
            savePath = File(e.dict().get("save_path").string())
        } catch (e: Throwable) {
            // can't recover original torrent path
        }

        return savePath
    }
    //  设置存储位置
    private fun setupSaveDir(saveDir: File?): File? {
        val result: File?

        result = saveDir ?: torrentSessionOptions.downloadLocation

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

    private  class RestoreDownloadTask(private val torrent: File, private val saveDir: File?, private val priorities: Array<Priority>?, private val resume: File) : Runnable {

        @SuppressLint("LongLogTag")
        override fun run() {
            try {
                download(TorrentInfo(torrent), saveDir, resume, priorities, null)
            } catch (e: Throwable) {
                Log.e("Unable to restore download from previous session. (" + torrent.absolutePath + ")", e.message)
            }

        }
    }
}
package com.example.sunday.p2pplayer.bittorrent

import android.annotation.SuppressLint
import android.util.Log
import com.example.sunday.p2pplayer.transfer.TransferItem
import com.example.sunday.p2pplayer.transfer.TransferState
import com.frostwire.jlibtorrent.*
import com.frostwire.jlibtorrent.alerts.*
import com.frostwire.jlibtorrent.swig.add_torrent_params
import com.frostwire.jlibtorrent.swig.entry
import com.frostwire.jlibtorrent.swig.string_entry_map
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Created by sunday on 19-4-25.
 */
class BTDownload(private val engine: BTEngine, private val th: TorrentHandle) : BittorrentDownload {
    private val SAVE_RESUME_RESOLUTION_MILLIS: Long = 10000
    private val ALERT_TYPES = intArrayOf(AlertType.TORRENT_FINISHED.swig(),
            AlertType.TORRENT_REMOVED.swig(),
            AlertType.TORRENT_CHECKED.swig(),
            AlertType.SAVE_RESUME_DATA.swig(),
            AlertType.PIECE_FINISHED.swig(),
            AlertType.STORAGE_MOVED.swig())
    private val EXTRA_DATA_KEY = "extra_data"
    private val WAS_PAUSED_EXTRA_KEY = "was_paused"

    private val savePath: File
    private val created: Date
    private val piecesTracker: PiecesTracker?
    private val parts: File?

    private val extra: HashMap<String, String>

    private lateinit var listener: BTDownloadListener

    private var incompleteFilesToRemove: Set<File>? = null

    private var lastSaveResumeTime: Long = 0


    private val innerListener: InnerListener

    private var predominantFileExtension: String? = null

    fun setListener(listener: BTDownloadListener) {
        this.listener = listener
    }
    override fun getName(): String {
        return th.name()
    }

    override fun getDisplayName(): String {
        val priorities = th.filePriorities()

        var count = 0
        var index = 0
        for (i in priorities.indices) {
            if (Priority.IGNORE != priorities[i]) {
                count++
                index = i
            }
        }

        return if (count != 1) th.name() else FilenameUtils.getName(th.torrentFile().files().filePath(index))    }

    override fun getInfoHash(): String {
        return th.infoHash().toString()
    }

    override fun getSavePath(): File {
        return savePath
    }

    override fun previewFile(): File? {
        return null
    }

    override fun getSize(): Long {
        val ti = th.torrentFile()
        return ti?.totalSize() ?: 0
    }

    override fun getCreated(): Date {
        return created
    }

    override fun getState(): TransferState {
        if (!engine.isRunning) {
            return TransferState.STOPPED
        }

        if (engine.isPaused) {
            return TransferState.PAUSED
        }

        if (!th.isValid) {
            return TransferState.ERROR
        }

        val status = th.status()
        val isPaused = isPaused(status)

        if (isPaused && status.isFinished) {
            return TransferState.FINISHED
        }

        if (isPaused && !status.isFinished) {
            return TransferState.PAUSED
        }

        if (!isPaused && status.isFinished) { // see the docs of isFinished
            return TransferState.SEEDING
        }

        val state = status.state()

        when (state) {
            TorrentStatus.State.CHECKING_FILES -> return TransferState.CHECKING
            TorrentStatus.State.DOWNLOADING_METADATA -> return TransferState.DOWNLOADING_METADATA
            TorrentStatus.State.DOWNLOADING -> return TransferState.DOWNLOADING
            TorrentStatus.State.FINISHED -> return TransferState.FINISHED
            TorrentStatus.State.SEEDING -> return TransferState.SEEDING
            TorrentStatus.State.ALLOCATING -> return TransferState.ALLOCATING
            TorrentStatus.State.CHECKING_RESUME_DATA -> return TransferState.CHECKING
            TorrentStatus.State.UNKNOWN -> return TransferState.UNKNOWN
            else -> return TransferState.UNKNOWN
        }    }

    override fun getBytesReceived(): Long {
        return if (th.isValid) th.status().totalDone() else 0
    }

    override fun getBytesSent(): Long {
        return if (th.isValid) th.status().totalUpload() else 0
    }

    override fun getDownloadSpeed(): Long {
        return if (!th.isValid || isFinished || isPaused || isSeeding) 0 else th.status().downloadPayloadRate().toLong()
    }

    override fun magnetUri(): String {
        return th.makeMagnetUri()
    }

    override fun getUploadSpeed(): Long {
        return if (!th.isValid || isFinished && !isSeeding || isPaused) 0 else th.status().uploadPayloadRate().toLong()
    }

    override fun getConnectedPeers(): Int {
        return if (th.isValid) th.status().numPeers() else 0
    }

    override fun isDownloading(): Boolean {
        return downloadSpeed > 0
    }

    override fun getTotalPeers(): Int {
        return if (th.isValid) th.status().listPeers() else 0
    }

    override fun getConnectedSeeds(): Int {
        return if (th.isValid) th.status().numSeeds() else 0
    }

    override fun getTotalSeeds(): Int {
        return if (th.isValid) th.status().listSeeds() else 0
    }

    //剩余时间
    override fun getETA(): Long {
        if (!th.isValid) {
            return 0
        }
        val ti = th.torrentFile() ?: return 0
        val status = th.status()
        val left = ti.totalSize() - status.totalDone()
        val rate = status.downloadPayloadRate().toLong()
        if (left <= 0) {
            return 0
        }
        return if (rate <= 0) {
            -1
        } else left / rate
    }

    //下载进度
    override fun getProgress(): Int {
        if (!th.isValid) {
            return 0
        }

        val ts = th.status() ?: // this can't never happens
                return 0

        val fp = ts.progress()
        val state = ts.state()

        if (java.lang.Float.compare(fp, 1f) == 0 && state != TorrentStatus.State.CHECKING_FILES) {
            return 100
        }

        val p = (fp * 100).toInt()
        return if (p > 0 && state != TorrentStatus.State.CHECKING_FILES) {
            Math.min(p, 100)
        } else 0

    }

    override fun isComplete(): Boolean {
        return progress == 100
    }

    override fun getItems(): ArrayList<TransferItem> {
        val items = ArrayList<TransferItem>()
        if (th.isValid) {
            val ti = th.torrentFile()
            if (ti != null && ti.isValid) {
                val fs = ti.files()
                val numFiles = ti.numFiles()
                if (piecesTracker != null) {
                    (0 until numFiles).mapTo(items) { BTDownloadItem(th, it, fs.filePath(it), fs.fileSize(it), piecesTracker) }

                    val numPieces = ti.numPieces()
                    // perform piece complete check

                    (0 until numPieces)
                            .filter { th.havePiece(it) }
                            .forEach { piecesTracker.setComplete(it, true) }
                }
            }
        }
        return items
    }

    override fun remove(deleteData: Boolean) {
        remove(true, deleteData)    }

    @SuppressLint("LongLogTag")
    override fun getContentSavePath(): File? {
        try {
            if (!th.isValid) {
                return null
            }

            val ti = th.torrentFile()
            if (ti?.swig() != null) {
                return File(savePath.absolutePath, if (ti.numFiles() > 1) th.name() else ti.files().filePath(0))
            }
        } catch (e: Throwable) {
            Log.w("Could not retrieve download content save path", e)
        }
        return null
    }

    override fun isPaused(): Boolean {
        return th.isValid && (isPaused(th.status()) || engine.isPaused || !engine.isRunning)
    }

    override fun isSeeding(): Boolean {
        return th.isValid && th.status().isSeeding
    }

    override fun isFinished(): Boolean {
       return th.isValid && th.status(false).isFinished
    }

    override fun pause() {
        if (!th.isValid) {
            return
        }

        extra.put(WAS_PAUSED_EXTRA_KEY, java.lang.Boolean.TRUE.toString())

        th.unsetFlags(TorrentFlags.AUTO_MANAGED)
        th.pause()

        doResumeData(true)
    }

    override fun resume() {
        if (!th.isValid) {
            return
        }

        extra.put(WAS_PAUSED_EXTRA_KEY, java.lang.Boolean.FALSE.toString())

        th.setFlags(TorrentFlags.AUTO_MANAGED)
        th.resume()

        doResumeData(true)
    }

    fun wasPause() : Boolean{
        var flag  = false
        if (extra.containsKey(WAS_PAUSED_EXTRA_KEY)) {
            if (extra[WAS_PAUSED_EXTRA_KEY] != null) {
                flag = extra[WAS_PAUSED_EXTRA_KEY]?.toBoolean() ?: false
            }
        }
        return flag
    }

    override fun remove(deleteTorrent: Boolean, deleteData: Boolean) {
        val infoHash = this.infoHash

        incompleteFilesToRemove = getIncompleteFiles()

        if (th.isValid) {
            if (deleteData) {
                engine.remove(th, SessionHandle.DELETE_FILES)
            } else {
                engine.remove(th)
            }
        }

        if (deleteTorrent) {
            //删除下载文件
//            val torrent = engine.readTorrentPath(infoHash)
//            if (torrent != null) {
//                //删除下载的文件
//            }
        }

        engine.resumeDataFile(infoHash).delete()
        engine.resumeTorrentFile(infoHash).delete()
    }

    override fun getPredominantFileExtension(): String? {
        if (predominantFileExtension == null) {
            val torrentInfo = th.torrentFile()
            if (torrentInfo != null) {
                val files = torrentInfo.files()
                val extensionByteSums = java.util.HashMap<String, Long>()
                val numFiles = files.numFiles()
                if (files.paths() != null) {
                    for (i in 0 until numFiles) {
                        val path = files.filePath(i)
                        val extension = FilenameUtils.getExtension(path)
                        if ("" == extension) {
                            // skip folders
                            continue
                        }
                        if (extensionByteSums.containsKey(extension)) {
                            val bytes = extensionByteSums[extension]
                            extensionByteSums.put(extension, bytes!! + files.fileSize(i))
                        } else {
                            extensionByteSums.put(extension, files.fileSize(i))
                        }
                    }
                    var extensionCandidate: String? = null
                    val exts = extensionByteSums.keys
                    for (ext in exts) {
                        if (extensionCandidate == null) {
                            extensionCandidate = ext
                        } else {
                            if (extensionByteSums[ext]!! > extensionByteSums[extensionCandidate]!!) {
                                extensionCandidate = ext
                            }
                        }
                    }
                    predominantFileExtension = extensionCandidate
                }
            }
        }
        return predominantFileExtension
    }

    private fun isPaused(s: TorrentStatus): Boolean {
        return s.flags().and_(TorrentFlags.PAUSED).nonZero()
    }


    private inner class InnerListener : AlertListener {

        override fun types(): IntArray {
            return ALERT_TYPES
        }

        override fun alert(alert: Alert<*>) {
            if (alert !is TorrentAlert<*>) {
                return
            }
            if (!alert.handle().swig().op_eq(th.swig())) {
                return
            }
            val type = alert.type()
            when (type) {
                AlertType.TORRENT_FINISHED -> torrentFinished()
                AlertType.TORRENT_REMOVED -> torrentRemoved()
                AlertType.TORRENT_CHECKED -> torrentChecked()
                AlertType.SAVE_RESUME_DATA -> serializeResumeData(alert as SaveResumeDataAlert)
                AlertType.PIECE_FINISHED -> {
                    pieceFinished(alert as PieceFinishedAlert)
                    doResumeData(false)
                }
                AlertType.STORAGE_MOVED -> doResumeData(true)
            }
        }
    }

    @SuppressLint("LongLogTag")
    private fun pieceFinished(alert: PieceFinishedAlert) {
        try {
            piecesTracker?.setComplete(alert.pieceIndex(), true)
        } catch (e: Throwable) {
            Log.w("Error handling piece finished logic", e)
        }

    }

    @SuppressLint("LongLogTag")
    private fun doResumeData(force: Boolean) {
        val now = System.currentTimeMillis()
        if (force || now - lastSaveResumeTime >= SAVE_RESUME_RESOLUTION_MILLIS) {
            lastSaveResumeTime = now
        } else {
            // skip, too fast, see SAVE_RESUME_RESOLUTION_MILLIS
            return
        }
        try {
            if (th != null && th.isValid) {
                th.saveResumeData(TorrentHandle.SAVE_INFO_DICT)
            }
        } catch (e: Throwable) {
            Log.w("Error triggering resume data", e)
        }

    }
    @SuppressLint("LongLogTag")
    private fun serializeResumeData(alert: SaveResumeDataAlert) {
        try {
            if (th.isValid) {
                val infoHash = th.infoHash().toString()
                val file = engine.resumeDataFile(infoHash)
                val e = add_torrent_params.write_resume_data(alert.swig().params)
                e.dict().set(EXTRA_DATA_KEY, Entry.fromMap(extra).swig())
                FileUtils.writeByteArrayToFile(file, Vectors.byte_vector2bytes(e.bencode()))
            }
        } catch (e: Throwable) {
            Log.w("Error saving resume data", e.message)
        }

    }

    @SuppressLint("LongLogTag")
    private fun torrentChecked() {
        try {
            if (th.isValid) {
                // trigger items calculation
                items
            }
        } catch (e: Throwable) {
            Log.w("Error handling torrent checked logic", e.message)
        }

    }


    private fun torrentRemoved() {
        engine.removeListener(innerListener)

        parts?.delete()
        try {
            listener.removed(this, incompleteFilesToRemove!!)
        } catch (e: Throwable) {
            Log.e("Error calling listener", e.message)
        }


    }

    public fun isPartial() : Boolean{
        if (th.isValid) {
            val priorities = th.filePriorities()
            priorities.forEach {
                if (Priority.IGNORE == it) {
                    return true
                }
            }
        }
        return false
    }

    @SuppressLint("LongLogTag")
    private fun getIncompleteFiles(): Set<File> {
        val s = HashSet<File>()
        try {
            if (!th.isValid) {
                return s
            }
            val progress = th.fileProgress(TorrentHandle.FileProgressFlags.PIECE_GRANULARITY)
            val ti = th.torrentFile() ?: // still downloading the info (from magnet)
                    return s
            val fs = ti.files()
            val prefix = savePath.absolutePath
            val createdTime = created.time
            for (i in progress.indices) {
                val fePath = fs.filePath(i)
                val feSize = fs.fileSize(i)
                if (progress[i] < feSize) {
                    // lets see if indeed the file is incomplete
                    val f = File(prefix, fePath)
                    if (!f.exists()) {
                        continue // nothing to do here
                    }
                    if (f.lastModified() >= createdTime) {
                        // we have a file modified (supposedly) by this transfer
                        s.add(f)
                    }
                }
            }
        } catch (e: Throwable) {
            Log.e("Error calculating the incomplete files set", e.message)
        }

        return s
    }

    private fun torrentFinished() {
            try {
                listener.finished(this)
            } catch (e: Throwable) {
                Log.e("Error calling listener", e.message)
            }
    }
    private fun createExtra(): HashMap<String, String> {
        val map = HashMap<String, String>()
        try {
            val infoHash = infoHash
            val file = engine.resumeDataFile(infoHash)
            if (file.exists()) {
                val arr = FileUtils.readFileToByteArray(file)
                val e = entry.bdecode(Vectors.bytes2byte_vector(arr))
                val d = e.dict()
                if (d.has_key(EXTRA_DATA_KEY)) {
                    readExtra(d.get(EXTRA_DATA_KEY).dict(), map)
                }
            }

        } catch (e: Throwable) {
            Log.e("Error reading extra", e.stackTrace.toString())
        }

        return map
    }

    private fun readExtra(dict: string_entry_map, map: MutableMap<String, String>) {
        val keys = dict.keys()
        val size = keys.size().toInt()
        for (i in 0 until size) {
            val k = keys.get(i)
            val e = dict.get(k)
            if (e.type() == entry.data_type.string_t) {
                map.put(k, e.string())
            }
        }
    }

    init {
        savePath = File(th.savePath())
        this.created = Date(th.status().addedTime())
        val ti = th.torrentFile()
        this.piecesTracker = if (ti != null) PiecesTracker(ti) else null
        this.parts = if (ti != null) File(savePath, "." + ti.infoHash() + ".parts") else null
        this.extra = createExtra()
        this.innerListener = InnerListener()
        engine.addListener(innerListener)
    }

}
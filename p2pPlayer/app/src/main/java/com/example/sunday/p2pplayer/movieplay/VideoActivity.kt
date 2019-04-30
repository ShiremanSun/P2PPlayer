package com.example.sunday.p2pplayer.movieplay

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.example.sunday.p2pplayer.R
import com.example.sunday.p2pplayer.Util.Config
import com.example.sunday.p2pplayer.Util.MOVIE_URL
import com.gyf.immersionbar.ImmersionBar
import com.pili.pldroid.player.*
import com.pili.pldroid.player.widget.PLVideoView
import java.util.*
import kotlin.experimental.and


class VideoActivity : AppCompatActivity(), PLOnCompletionListener,
        MediaController.OnShownListener, MediaController.OnHiddenListener{
    override fun onShown() {
        mBackButton.visibility = View.VISIBLE
    }

    override fun onHidden() {
        mBackButton.visibility = View.INVISIBLE
    }

    private val errorView by lazy {
        findViewById<TextView>(R.id.error_view)
    }

    private lateinit var mVideoView: PLVideoView
    private lateinit var mMediaController : MediaController
    private val mCover by lazy {
        findViewById<ImageView>(R.id.Cover)
    }

    private val mBackButton by lazy { findViewById<ImageButton>(R.id.back) }
    private val mLoadingView by lazy {
        findViewById<View>(R.id.LoadingView)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        ImmersionBar.with(this).navigationBarColor(R.color.colorWhite).init()
        mMediaController = MediaController(this)

        mMediaController.setOnShownListener(this)
        mMediaController.setOnHiddenListener(this)

        //隐藏状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val decorView = window.decorView
            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        mVideoView = findViewById(R.id.videoView)

        val options = AVOptions()
        // the unit of timeout is ms
        options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000)
       // 1 -> hw codec enable, 0 -> disable [recommended]
        options.setInteger(AVOptions.KEY_MEDIACODEC, AVOptions.MEDIA_CODEC_HW_DECODE)

        options.setString(AVOptions.KEY_CACHE_DIR, Config.DEFAULT_CACHE_DIR)


        mVideoView.setBufferingIndicator(mLoadingView)
        mVideoView.setCoverView(mCover)
        mVideoView.setAVOptions(options)
        mVideoView.isLooping = false
        mVideoView.setMediaController(mMediaController)

        // Set some listeners
        mVideoView.setOnInfoListener(mOnInfoListener)
        mVideoView.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener)
        mVideoView.setOnBufferingUpdateListener(mOnBufferingUpdateListener)
        mVideoView.setOnCompletionListener(mOnCompletionListener)
        mVideoView.setOnErrorListener(mOnErrorListener)
        mVideoView.setOnVideoFrameListener(mOnVideoFrameListener)
        mVideoView.setOnAudioFrameListener(mOnAudioFrameListener)
        val intent = intent
        val url = intent.getStringExtra(MOVIE_URL)
        mVideoView.setVideoPath(url)

        mVideoView.setOnPreparedListener({
            mVideoView.start()
        })
        mBackButton.setOnClickListener {
            finish()
        }

    }



    override fun onDestroy() {
        mVideoView.stopPlayback()
        super.onDestroy()
    }
    override fun onCompletion() {
        mMediaController.refreshProgress()
    }

    private val mOnInfoListener = PLOnInfoListener { what, _ ->

        when (what) {
            PLOnInfoListener.MEDIA_INFO_BUFFERING_START -> {
            }
            PLOnInfoListener.MEDIA_INFO_BUFFERING_END -> {
            }

            PLOnInfoListener.MEDIA_INFO_AUDIO_RENDERING_START -> {
            }


        }
    }

    private val mOnErrorListener = PLOnErrorListener { errorCode ->
        Log.e("VideoActivity", "Error happened, errorCode = $errorCode")
        when (errorCode) {
            PLOnErrorListener.ERROR_CODE_IO_ERROR -> {
                /**
                 * SDK will do reconnecting automatically
                 */
                /**
                 * SDK will do reconnecting automatically
                 */
                Log.e("VideoActivity", "IO Error!")
                return@PLOnErrorListener false
            }

            PLOnErrorListener.ERROR_CODE_SEEK_FAILED -> {

                return@PLOnErrorListener true
            }
            PLOnErrorListener.ERROR_CODE_OPEN_FAILED -> {
                mCover.visibility = View.GONE
                errorView.visibility = View.VISIBLE
                return@PLOnErrorListener true
            }

        }
        true
    }

    private val mOnCompletionListener = PLOnCompletionListener {
        Log.i("", "Play Completed !")

        //finish();
    }

    private val mOnBufferingUpdateListener = PLOnBufferingUpdateListener { precent -> Log.i("", "onBufferingUpdate: $precent") }

    private val mOnVideoSizeChangedListener = PLOnVideoSizeChangedListener { width, height -> Log.i("", "onVideoSizeChanged: width = $width, height = $height") }

    private val mOnVideoFrameListener = PLOnVideoFrameListener { data, size, width, height, format, ts ->
        Log.i("", "onVideoFrameAvailable: $size, $width x $height, $format, $ts")
        if (format == PLOnVideoFrameListener.VIDEO_FORMAT_SEI && bytesToHex(Arrays.copyOfRange(data, 19, 23)) == "74733634") {

            Log.i("", " timestamp: " + java.lang.Long.valueOf(bytesToHex(Arrays.copyOfRange(data, 23, 31)), 16))
        }
    }

    private val mOnAudioFrameListener = PLOnAudioFrameListener { data, size, samplerate, channels, datawidth, ts -> Log.i("123", "onAudioFrameAvailable: $size, $samplerate, $channels, $datawidth, $ts") }
    private fun bytesToHex(bytes: ByteArray): String {
        val hexArray = "0123456789ABCDEF".toCharArray()
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v:Int = (bytes[j] and 0xFF.toByte()).toInt()
            hexChars[j * 2] = hexArray[v.ushr(4)]
            hexChars[j * 2 + 1] = hexArray[(v and 0x0F)]
        }
        return String(hexChars)
    }

    override fun onStop() {
        mVideoView.pause()
        super.onStop()
    }

}

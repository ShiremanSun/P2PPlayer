package com.example.sunday.p2pplayer.movieplay

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.sunday.p2pplayer.R
import com.example.sunday.p2pplayer.Util.MOVIE_URL
import io.vov.vitamio.Vitamio
import io.vov.vitamio.widget.MediaController
import io.vov.vitamio.widget.VideoView

class VVideoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vvideo)
        Vitamio.isInitialized(applicationContext)

        val videoView = findViewById<VideoView>(R.id.videoView)

        videoView.setVideoPath(intent.getStringExtra(MOVIE_URL))
        videoView.setMediaController(MediaController(this))
        videoView.requestFocus()
        videoView.setOnPreparedListener {
            videoView.start()
        }

    }
}

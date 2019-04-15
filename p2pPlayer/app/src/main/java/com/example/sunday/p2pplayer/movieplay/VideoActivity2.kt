package com.example.sunday.p2pplayer.movieplay

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView
import com.example.sunday.p2pplayer.R
import com.example.sunday.p2pplayer.MainActivity

class VideoActivity2 : AppCompatActivity() {

    private lateinit var videoView: VideoView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video2)

        videoView = findViewById(R.id.videoView2)

        videoView.setMediaController(MediaController(this))
        val intent = intent
        val url = intent.getStringExtra(MainActivity.MOVIEURL)
        videoView.setVideoPath(url)

        videoView.start()
    }
}

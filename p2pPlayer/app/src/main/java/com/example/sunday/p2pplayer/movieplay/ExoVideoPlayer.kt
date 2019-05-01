package com.example.sunday.p2pplayer.movieplay

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.sunday.p2pplayer.R
import com.example.sunday.p2pplayer.Util.MOVIE_URL
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class ExoVideoPlayer : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exo_video_player)

        val player = ExoPlayerFactory.newSimpleInstance(this,
                DefaultRenderersFactory(this),
                DefaultTrackSelector(),
                DefaultLoadControl())
        val playView = findViewById<PlayerView>(R.id.videoView)
        playView.player = player
        val bind = DefaultBandwidthMeter()

        val path = intent.getStringExtra(MOVIE_URL)

        val data = Uri.parse(path)
        val dataSourceFactory = DefaultDataSourceFactory(this,
                Util.getUserAgent(this, packageName),
                bind)
        val extralFactory = DefaultExtractorsFactory()
        val factory = ExtractorMediaSource.Factory(dataSourceFactory)
        factory.setExtractorsFactory(extralFactory)

        val videoSource = factory.createMediaSource(data)

        player.prepare(videoSource)
        player.addListener(object :Player.EventListener {
            override fun onPlayerError(error: ExoPlaybackException?) {
                error?.printStackTrace()
            }
        })


    }
}

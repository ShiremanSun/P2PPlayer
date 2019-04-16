package com.example.sunday.p2pplayer.Util

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Message
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Created by Sunday on 2019/4/1
 */
object HttpUtil {

    private val okHttpClient = OkHttpClient()
    fun sendRequest (url : String, callback: Callback)  {
        val request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).enqueue(callback)
    }

    //得到网络媒体的封面
    fun getNetVideoBitmap(url : String) : Bitmap?{

        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(url, HashMap())
       val bitmap = mediaMetadataRetriever.frameAtTime

        mediaMetadataRetriever.release()

        return bitmap
    }
}
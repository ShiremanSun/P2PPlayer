package com.example.sunday.p2pplayer.Util

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/**
 * Created by Sunday on 2019/4/1
 */
object HttpUtil {


    private val okHttpClient = OkHttpClient()
    fun sendRequest (url : String) : Response  {
        val request = Request.Builder().url(url).build()
        return okHttpClient.newCall(request).execute()
    }

    //得到网络媒体的封面

}
package com.example.sunday.p2pplayer.Util

import android.arch.lifecycle.LiveData
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Message
import com.example.sunday.p2pplayer.model.MovieBean
import io.reactivex.Observable
import okhttp3.*

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
    fun getNetVideoBitmap(url : String) : Bitmap?{

        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(url, HashMap())
       val bitmap = mediaMetadataRetriever.frameAtTime

        mediaMetadataRetriever.release()

        return bitmap
    }
}
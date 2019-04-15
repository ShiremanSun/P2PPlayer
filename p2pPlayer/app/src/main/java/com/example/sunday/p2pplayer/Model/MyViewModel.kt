package com.example.sunday.p2pplayer.Model


import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.example.sunday.p2pplayer.Util.HttpUtil
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

/**
 * Created by Sunday on 2019/4/1
 */
class MyViewModel : ViewModel() {
   val liveData = MutableLiveData<List<Movie>>()

   fun search(string: String) {
       val movie = Movie("123","http://2449.vod.myqcloud.com/2449_22ca37a6ea9011e5acaaf51d105342e3.f20.mp4", "ce=undefined")
       val list = ArrayList<Movie>()
       list.add(movie)
       liveData.value = list
       /*class MyCallback : Callback{
           override fun onFailure(call: Call, e: IOException) {
           }
           override fun onResponse(call: Call, response: Response) {
               //解析response并且调用
               val movie = Movie("123","123", "123")
               val list = ArrayList<Movie>()
               list.add(movie)
               liveData.value = list
           }

       }
       val callback = MyCallback()
      HttpUtil.sendRequest(string, callback)*/
   }

}
package com.example.sunday.p2pplayer.model


import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.example.sunday.p2pplayer.Util.HttpUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

/**
 * Created by Sunday on 2019/4/1
 */
class MyViewModel : ViewModel() {
   val liveData = MutableLiveData<List<MovieBean>>()

   fun search(string: String) {
       val url = "http://192.168.43.68:8080/BigFileUpload/search?movie_name="+string

        Observable.create<List<MovieBean>> { emitter ->
           val response = HttpUtil.sendRequest(url)
           //解析response并且调用
            try {
                val gson = Gson()
                val type = object : TypeToken<List<MovieBean>>() {

                }.type
                val list = gson.fromJson<List<MovieBean>>(
                        response.body()!!.string(),
                        type)

                emitter.onNext(list)
            }catch (e:Exception) {
                emitter.onError(e)
            }

       }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<List<MovieBean>>{
                    override fun onError(e: Throwable) {
                    }

                    override fun onComplete() {
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onNext(t: List<MovieBean>) {
                        liveData.value = t
                    }

                })
   }

}
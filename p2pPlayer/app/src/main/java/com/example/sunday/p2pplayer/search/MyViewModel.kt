package com.example.sunday.p2pplayer.search


import android.app.AlertDialog
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import com.example.sunday.p2pplayer.R
import com.example.sunday.p2pplayer.Util.HttpUtil
import com.example.sunday.p2pplayer.Util.SERVER_IP
import com.example.sunday.p2pplayer.model.MovieBean
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by Sunday on 2019/4/1
 */
class MyViewModel : ViewModel() {
   val liveData = MutableLiveData<List<MovieBean>>()

   fun search(string: String, context: Context?) {
       if (TextUtils.isEmpty(string)) {
           liveData.value = ArrayList(0)
           return
       }
       val preference = context?.getSharedPreferences(SERVER_IP, Context.MODE_PRIVATE)
       if("2222" == string) {
           val builder = AlertDialog.Builder(context)
           val view = LayoutInflater.from(context).inflate(R.layout.dialogview, null)
           val cancel = view.findViewById<Button>(R.id.cancel)
           val ok = view.findViewById<Button>(R.id.ok)
           val ip = view.findViewById<EditText>(R.id.ip)
           builder.setView(view)
           builder.setTitle("更改ip")
           val dialog = builder.create()
           dialog.show()
           cancel.setOnClickListener {
               dialog.dismiss()
           }
           ok.setOnClickListener {
               val ipAddress = ip.text.toString()
               val editor = preference?.edit()
               editor?.putString(SERVER_IP, ipAddress)
               editor?.apply()
               dialog.dismiss()
           }
           liveData.value = ArrayList(0)
           return
       }



       val url = "http://"+preference?.getString(SERVER_IP,"188.131.249.47:8080")+"/BigFileUpload/search?movie_name="+string

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
                        e.printStackTrace()
                        liveData.value = ArrayList(0)
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
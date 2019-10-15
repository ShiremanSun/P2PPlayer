package com.sunday.networklistener.utils

import android.content.Context
import android.net.ConnectivityManager
import com.sunday.networklistener.NetType
import com.sunday.networklistener.manager.NetWorkManager

/**
 * Created by Sunday on 2019/6/4
 */
object NetworkUtils {

    fun getNetType() : NetType {
       val connectivityManager = NetWorkManager.application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager == null)  return NetType.NONE
        val netInfo = connectivityManager.activeNetworkInfo ?: return NetType.NONE

        val nType = netInfo.type

        return when (nType) {
            ConnectivityManager.TYPE_MOBILE -> NetType.GPRS
            ConnectivityManager.TYPE_WIFI -> NetType.WIFI
            else -> NetType.NONE
        }




    }
}
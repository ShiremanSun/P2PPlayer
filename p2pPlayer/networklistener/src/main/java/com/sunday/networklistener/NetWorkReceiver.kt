package com.sunday.networklistener

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.sunday.networklistener.manager.NetWorkManager
import com.sunday.networklistener.utils.NetworkUtils

class NetWorkReceiver : BroadcastReceiver() {

    private var netType = NetType.NONE
    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == null) {
            return
        }
        if (intent.action.equals(ConnectivityManager.CONNECTIVITY_ACTION, true)) {
            netType = NetworkUtils.getNetType()
        }
        post(netType)
    }

    private fun post(netType: NetType) {
        val set = NetWorkManager.observer.keys
        set.filter { any: Any ->  NetWorkManager.observer[any] != null }
                .map { any:Any -> NetWorkManager.observer[any]?.map { it.invoke(any, netType) } }

    }
}

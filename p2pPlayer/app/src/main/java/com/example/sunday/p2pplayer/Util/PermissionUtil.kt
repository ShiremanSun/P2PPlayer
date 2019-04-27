package com.example.sunday.p2pplayer.Util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import java.security.AccessControlContext
import java.util.jar.Manifest

/**
 * Created by Sunday on 2019/4/6
 */
object PermissionUtil {

    private lateinit var permissionListener : IPermissionListener
    fun requestPermission(context: Activity,permissions: Array<String>, permissionListener : IPermissionListener) {
        this.permissionListener = permissionListener
        val list = ArrayList<String>()
        permissions.filter { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }.map { list.add(it) }

        if (list.isEmpty()) {
            permissionListener.permissionGranted()
        } else {
            ActivityCompat.requestPermissions(context, list.toArray(arrayOfNulls(list.size)),0)
        }
    }
    fun onResultPermission(requestCode : Int, permissions : Array<out String>, grantResults: IntArray) {
        if (requestCode == 0) {
            if (grantResults.isNotEmpty()) {
                grantResults.forEach {
                    if (it != PackageManager.PERMISSION_GRANTED ){
                        permissionListener.permissionDeny()
                        return
                    }
                }
                permissionListener.permissionGranted()
            }else {
                permissionListener.permissionDeny()
            }
        }
    }
    interface IPermissionListener {
        fun permissionGranted()
        fun permissionDeny()
    }
}
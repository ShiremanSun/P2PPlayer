package com.example.sunday.p2pplayer.Util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import java.security.AccessControlContext
import java.util.jar.Manifest

/**
 * Created by Sunday on 2019/4/6
 */
object PermissionUtil {

    private lateinit var permissionListener : IPermissionListener
    private lateinit var permissionGranted : () -> Unit //函数类型
    private lateinit var permissionDeny : () -> Unit //函数类型
    fun requestPermission(context: Activity,permissions: Array<String>, permissionListener : IPermissionListener, fragment: Fragment) {
        this.permissionListener = permissionListener
        val list = ArrayList<String>()
        permissions.filter { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }.map { list.add(it) }

        if (list.isEmpty()) {
            permissionListener.permissionGranted()
        } else {
            fragment.requestPermissions(list.toArray(arrayOfNulls(list.size)),0)
        }
    }
    fun onResultPermission(requestCode : Int, permissions : Array<String>, grantResults: IntArray) {
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

    fun requestPermission1(context: Activity,permissions: Array<String>,granted:()->Unit,deny:()->Unit,fragment: Fragment) {
        permissionGranted = granted
        permissionDeny = deny
        val list = ArrayList<String>()
        permissions.filter { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }
                .map { list.add(it) }

        if (list.isEmpty()) {
            granted()
        }else {
            fragment.requestPermissions(list.toArray(arrayOfNulls(list.size)),0)
        }
    }
}
package com.example.sunday.p2pplayer.Util

import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import java.security.AccessControlContext
import java.util.jar.Manifest

/**
 * Created by Sunday on 2019/4/6
 */
object PermissionUtil {
    fun requestPermission(context: Activity,permissions: Array<String>) {
        permissions.forEach {
            if (ContextCompat.checkSelfPermission(context,it) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context, arrayOf(it),1)
            }
        }
    }
}
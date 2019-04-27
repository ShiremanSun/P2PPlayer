package com.example.sunday.p2pplayer.Util

import java.util.*

/**
 *Created by sunday on 19-4-27.
 */
private val BYTE_UNITS = arrayOf("b", "KB", "Mb", "Gb", "Tb")

fun getBytesInHuman(size: Long): String {
    var i = 0
    var sizeFloat = size.toFloat()
    while (sizeFloat > 1024) {
        sizeFloat /= 1024f
        i++
    }
    return String.format(Locale.CHINA, "%.2f %s", sizeFloat, BYTE_UNITS[i])
}
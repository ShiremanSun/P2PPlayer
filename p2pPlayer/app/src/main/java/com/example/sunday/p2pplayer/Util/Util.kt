package com.example.sunday.p2pplayer.Util

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Path
import android.graphics.PathMeasure
import android.net.Uri
import android.support.annotation.NonNull
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import com.alibaba.android.arouter.utils.TextUtils
import com.example.sunday.p2pplayer.MainActivity
import com.example.sunday.p2pplayer.R
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 *Created by sunday on 19-4-27.
 */
private val BYTE_UNITS = arrayOf("B", "KB", "MB", "GB", "TB")

const val  MOVIE_URL = "movie_url"
const val MOVIE_NAME = "movie_name"
const val INFOHASH = "infoHash"

const val TIME_PREFERENCE = "time_preference"

const val SERVER_IP = "server_ip"

const val REQUEST_CODE_FILEPICKER = 1

fun getBytesInHuman(size: Long): String {
    var i = 0
    var sizeFloat = size.toFloat()
    while (sizeFloat > 1024) {
        sizeFloat /= 1024f
        i++
    }
    return String.format(Locale.CHINA, "%.2f %s", sizeFloat, BYTE_UNITS[i])
}

//点击下载动画
fun addDownloadAnimation(startView : View, endView : View,context : MainActivity) {
    val startLocation = IntArray(2)
    startView.getLocationInWindow(startLocation)

    val containerView = createAnimLayout(context)
    val floatingImg = ImageView(context)
    floatingImg.setImageResource(R.drawable.downloading_little)

    //动画开始的起始坐标
    val startX = startLocation[0] + startView.width / 2.0f - floatingImg.width / 2.0f
    val startY = startLocation[1] + startView.height / 2.0f - floatingImg.height / 2.0f

    val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
    lp.leftMargin = startX.toInt()
    lp.topMargin = startY.toInt()
    containerView.addView(floatingImg, lp)

    val endLocation = IntArray(2)
    endView.getLocationInWindow(endLocation)
    //动画结束的坐标
    val endX = endLocation[0] + endView.width / 2.5f
    val endY = endLocation[1] + endView.height / 2.0f - floatingImg.height / 2.0f
    //计算插值坐标
    val path = Path()
    path.moveTo(startX, startY)
    path.quadTo((startX + endX) / 2, startY, endX, endY)

    val pathMeasure = PathMeasure(path, false)

    //属性动画实现
    val valueAnimator = ValueAnimator.ofFloat(0F, pathMeasure.length)
    valueAnimator.duration = 500L
    valueAnimator.interpolator = LinearInterpolator()
    valueAnimator.addUpdateListener {
        // 当插值计算进行时，获取中间的每个值，
        // 这里这个值是中间过程中的曲线长度（下面根据这个值来得出中间点的坐标值）
        val value = it.animatedValue as Float

        val currentPosition = FloatArray(2)
        pathMeasure.getPosTan(value, currentPosition, null)
        val left = floatingImg.left
        val top = floatingImg.top
        floatingImg.translationX = currentPosition[0] - left
        floatingImg.translationY = currentPosition[1] - top

        //Log.d("position", currentPosition[0].toString() + "--" + currentPosition[1])
        //view.translationX = currentPosition[0]
        //view.translationY = currentPosition[1]
    }

    valueAnimator.start()

    valueAnimator.addListener(object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) {
        }

        override fun onAnimationEnd(animation: Animator?) {
            containerView.removeView(floatingImg)

        }

        override fun onAnimationCancel(animation: Animator?) {
        }

        override fun onAnimationStart(animation: Animator?) {
        }

    })


}

//创建动画层
private fun createAnimLayout(activity: MainActivity): ViewGroup {
    return activity.window.decorView as ViewGroup
}

//毫秒转为分钟字符串

fun millToTime(@NonNull mill : Long) : String{
    val second = (mill / 1000).toInt()
    val minute = (second / 60).toString()
    val newSecond = (second % 60).toString()
    return minute + "分" + newSecond + "秒"
}


//从content路径里获取真正的文件路径
fun getFilePath(context: Context, uri: Uri) : Uri?{
    val rootDataDir = context.filesDir
    val fileName = getFileName(uri)
    if (!TextUtils.isEmpty(fileName)) {
        val target = File(rootDataDir.absolutePath + File.separator + fileName)
        val inStream = context.contentResolver.openInputStream(uri)
        val outStream = FileOutputStream(target)
        val buffer = ByteArray(16384)

        if (inStream != null) {
            var bytesRead = inStream.read(buffer)
            while (bytesRead != -1) {
                outStream.write(buffer, 0, bytesRead)
                bytesRead = inStream.read(buffer)
            }
        }
        return Uri.parse("file://${target.absolutePath}")
    }
    return null
}

fun getFileName(uri: Uri) : String? {
    val path = uri.path
    var fileName : String? = null
    val cut = path?.lastIndexOf('/')
    if (cut != -1) {
        fileName = path?.substring(cut?.plus(1)!!)
    }
    return fileName
}
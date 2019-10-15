package com.sunday.networklistener.manager

import android.app.Application
import android.content.IntentFilter
import android.net.ConnectivityManager
import com.sunday.networklistener.NetType
import com.sunday.networklistener.NetWorkReceiver
import com.sunday.networklistener.annotate.NetWork
import java.lang.reflect.Method


/**
 * Created by Sunday on 2019/6/3
 */
object NetWorkManager {
    //保存每个类中的方法
    val observer = HashMap<Any, List<Method>>()
    lateinit var application: Application private set
    fun initReceiver(application: Application) {
        //注册网络广播
        this.application = application
        val filter = IntentFilter()
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        val receiver = NetWorkReceiver()
        application.registerReceiver(receiver, filter)
    }

    //注册方法
    fun register(register: Any) {
        val methodList = observer[register]
        if (methodList==null) {
            val methods = fidAnnotationMethod(register)
            observer[register] = methods
        }

    }

    //注销方法
    fun unregister(register: Any) {
        if (!observer.isEmpty()) {
            observer.remove(register)
        }
    }
    //从注解中找到
    private fun fidAnnotationMethod(register: Any): List<Method> {
        val list = ArrayList<Method>()
        val clazz = register.javaClass
        val methods = clazz.declaredMethods
        methods.filter {
            //参数个数为1
            (it.parameterTypes.size == 1) and
                    //方法注解不为null
                    (it.getAnnotation(NetWork::class.java) != null) and
                    //参数类型为NetType
                    (it.parameterTypes[0].isAssignableFrom(NetType::class.java)) }
                .map { list.add(it) }

        return list
    }

}
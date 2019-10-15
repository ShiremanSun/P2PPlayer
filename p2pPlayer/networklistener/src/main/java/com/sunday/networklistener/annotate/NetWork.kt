package com.sunday.networklistener.annotate

import com.sunday.networklistener.NetType

/**
 * Created by Sunday on 2019/6/3
 */
@Target(AnnotationTarget.FUNCTION)//作用于方法
@Retention(AnnotationRetention.RUNTIME)//运行时注解
annotation class NetWork
package com.example.sunday.p2pplayer

/**
 * Created by Sunday on 2019/4/1
 */
interface IPresenter {
    //注册View
    fun register(v : IView )
    fun unRegister()
}
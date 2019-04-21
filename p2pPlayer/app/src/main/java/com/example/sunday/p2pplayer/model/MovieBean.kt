package com.example.sunday.p2pplayer.model

import java.io.Serializable

/**
 * Created by Sunday on 2019/4/1
*/
data class MovieBean(
        val name:String,
        val torrentPathString : String,
        val imagePathString : String,
        val details : String,
        val datasourcePath : String) : Serializable{
    //序列化ID
    private  val serialVersionUID = -5809782578272943999L

}
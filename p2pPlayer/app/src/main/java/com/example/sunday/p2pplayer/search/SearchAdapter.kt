package com.example.sunday.p2pplayer.search


import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.sunday.p2pplayer.model.MovieBean
import com.example.sunday.p2pplayer.R
import com.example.sunday.p2pplayer.movieplay.VideoActivity

/**
 * Created by Sunday on 2019/4/5
 */
class SearchAdapter() : RecyclerView.Adapter<SearchAdapter.ViewHolder>(){


    private lateinit var list : List<MovieBean>

    private lateinit var mContext: Context

    constructor( list: List<MovieBean>) : this() {
        this.list = list
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        mContext = p0.context
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_movie, p0, false)

        
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {

        return list.size
    }

    override fun onBindViewHolder(p0:ViewHolder, p1: Int) {

        Glide.with(mContext).load(list[p1].imagePathString).into(p0.imageView)

        p0.movieName.text = list[p1].name
        p0.itemView.setOnClickListener{
            val intent = Intent(mContext, VideoActivity::class.java)
            intent.putExtra(FragmentSearch.MOVIE_URL, list[p1].datasourcePath)
            mContext.startActivity(intent)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var imageView = view.findViewById<ImageView>(R.id.movie_cav)
        var movieName = view.findViewById<TextView>(R.id.movie_title)

    }



}
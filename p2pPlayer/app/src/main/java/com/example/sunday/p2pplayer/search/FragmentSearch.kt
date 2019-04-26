package com.example.sunday.p2pplayer.search

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.SpannableString
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.bumptech.glide.Glide
import com.example.sunday.p2pplayer.MainActivity
import com.example.sunday.p2pplayer.model.MovieBean
import com.example.sunday.p2pplayer.R
import com.gyf.immersionbar.ImmersionBar
import java.util.ArrayList
import java.util.regex.Pattern
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import com.example.sunday.p2pplayer.bittorrent.DownLoadManager


/**
 * Created by Sunday on 2019/4/15
 */
class FragmentSearch : Fragment() {
    companion object {
        const val MOVIE_URL = "movieUrl"
    }


    private lateinit var mViewModel : MyViewModel

    private val mList = ArrayList<MovieBean>()
    private var mAdapter = SearchAdapter(mList)
    private lateinit var editText : EditText
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mViewModel = ViewModelProviders.of(this).get(MyViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val toolbar = view.findViewById<Toolbar>(R.id.toolBar)
        (activity as MainActivity).setSupportActionBar(toolbar)

        ImmersionBar.setTitleBar(activity,toolbar)
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = mAdapter

        mViewModel.liveData.observe(this, Observer {
           mList.clear()
           if (it != null) {
               mList.addAll(it)
           }
           mAdapter.notifyDataSetChanged()
        })
       editText = view.findViewById(R.id.search_editText)


        editText.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                (editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                        .hideSoftInputFromWindow(activity!!.currentFocus!!.windowToken
                                , InputMethodManager.HIDE_NOT_ALWAYS)
                mViewModel.search(editText.text.toString().trim())
                return@OnEditorActionListener true
            }
            return@OnEditorActionListener false
        } )
        return view
    }

    override fun onResume() {
        Log.d("Fragment" , "onResume")
        super.onResume()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        Log.d("Fragment", hidden.toString())
        super.onHiddenChanged(hidden)
    }

   inner class SearchAdapter() : RecyclerView.Adapter<SearchAdapter.ViewHolder>(){


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

            Glide.with(mContext).load(list[p1].imagePathString).centerCrop().into(p0.imageView)

            //后端返回的文字
            val spannable = SpannableString(list[p1].name)
            //需要高亮显示的文字
            val pattern = Pattern.compile(this@FragmentSearch.editText.text.toString().trim())
            val matcher = pattern.matcher(this@FragmentSearch.editText.text.toString().trim())
            while (matcher.find()) {
                val span = ForegroundColorSpan(activity?.let { ContextCompat.getColor(it, R.color.colorPrimary) }!!)
                spannable.setSpan(span, matcher.start() , matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            p0.movieDetails.text = list[p1].details
            p0.movieName.text = spannable
            p0.downloadButton.setOnClickListener({
                DownLoadManager.downloadTorrent(list[p1].torrentPathString, list[p1].name)
            })
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView = view.findViewById<ImageView>(R.id.movie_cav)!!
            val movieName = view.findViewById<TextView>(R.id.movie_title)!!
            val movieDetails = view.findViewById<TextView>(R.id.movie_detail)!!
            val playButton = view.findViewById<Button>(R.id.movie_play)!!
            val downloadButton = view.findViewById<Button>(R.id.movie_download)!!

        }
    }


}
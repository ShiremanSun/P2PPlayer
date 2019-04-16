package com.example.sunday.p2pplayer.search

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import com.example.sunday.p2pplayer.MainActivity
import com.example.sunday.p2pplayer.model.Movie
import com.example.sunday.p2pplayer.model.MyViewModel
import com.example.sunday.p2pplayer.R
import java.util.ArrayList

/**
 * Created by Sunday on 2019/4/15
 */
class FragmentSearch : Fragment() {
    companion object {
        const val MOVIE_URL = "movieUrl"
    }


    private lateinit var mViewModel : MyViewModel

    private val mList = ArrayList<Movie>()
    private var mAdapter = SearchAdapter(mList)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mViewModel = ViewModelProviders.of(this).get(MyViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val toolbar = view.findViewById<Toolbar>(R.id.toolBar)
        (activity as MainActivity).setSupportActionBar(toolbar)

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
        val editText = view.findViewById<EditText>(R.id.search_editText)


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
}
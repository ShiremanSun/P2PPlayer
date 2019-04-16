package com.example.sunday.p2pplayer

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.example.sunday.p2pplayer.downloaded.FragmentDownloaded
import com.example.sunday.p2pplayer.downloading.FragmentDownloading
import com.example.sunday.p2pplayer.search.FragmentSearch

/**
 * Created by Sunday on 2019/4/15
 */
class MyFragmentPageAdapter(fm:FragmentManager) : FragmentPagerAdapter(fm) {

    val fragmentSearch = FragmentSearch()
    val fragmentDownloading = FragmentDownloading()
    val fragmentDownloaded = FragmentDownloaded()

    override fun getItem(p0: Int): Fragment {
        var fragment: Fragment? = null
        when (p0) {
            MainActivity.PAGE_ONE -> fragment = fragmentSearch
            MainActivity.PAGE_TWO -> fragment = fragmentDownloading
            MainActivity.PAGE_THREE -> fragment = fragmentDownloaded
        }
        return fragment!!
    }

    override fun getCount(): Int {
        return 3
    }
}
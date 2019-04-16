package com.example.sunday.p2pplayer;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.alibaba.android.arouter.facade.annotation.Route;

@Route(path = "showmovie/mainactivity")
public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, ViewPager.OnPageChangeListener{

    public static final int PAGE_ONE = 0;
    public static final int PAGE_TWO = 1;
    public static final int PAGE_THREE = 2;

    private RadioButton mSearch;
    private RadioButton mDownloaded;
    private RadioButton mDownloading;

    private ViewPager mViewPager;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);


        RadioGroup mRadiaGroup = findViewById(R.id.tab_bar);
        mSearch = findViewById(R.id.tab_search);
        mDownloading = findViewById(R.id.tab_downloading);
        mDownloaded = findViewById(R.id.tab_downloaded);
        mViewPager = findViewById(R.id.viewPager);
        mRadiaGroup.setOnCheckedChangeListener(this);

        mSearch.setChecked(true);

        MyFragmentPageAdapter fragmentPageAdapter = new MyFragmentPageAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(fragmentPageAdapter);
        mViewPager.setCurrentItem(0);

        mViewPager.addOnPageChangeListener(this);



    }





    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

        switch (checkedId) {
            case R.id.tab_search:
                mViewPager.setCurrentItem(PAGE_ONE);
                break;
            case R.id.tab_downloading:
                mViewPager.setCurrentItem(PAGE_TWO);
                break;
            case R.id.tab_downloaded:
                mViewPager.setCurrentItem(PAGE_THREE);
                break;
        }
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {

    }

    @Override
    public void onPageScrollStateChanged(int i) {

        //如果i==2，表示滑动结束
        if (i == 2) {
            switch (mViewPager.getCurrentItem()) {
                case PAGE_ONE:
                    mSearch.setChecked(true);
                    break;
                case PAGE_TWO:
                    mDownloading.setChecked(true);
                    break;
                case PAGE_THREE:
                    mDownloaded.setChecked(true);
                    break;
            }
        }

    }
}

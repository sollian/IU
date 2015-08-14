package com.aiyou.viewLargeImage;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MyPagerAdapter extends FragmentPagerAdapter {
    private List<ViewLargeImageFragment> mFragList = new ArrayList<ViewLargeImageFragment>();
    private FragmentManager mFragMgr;

    public MyPagerAdapter(FragmentManager fm, List<String> urlList) {
        super(fm);
        mFragMgr = fm;
        initFragList(urlList);
    }

    private void initFragList(List<String> urlList) {
        List<Fragment> fragList = mFragMgr.getFragments();
        int flsize = -1;
        if (fragList != null) {
            flsize = fragList.size();
        }
        if (urlList != null && !urlList.isEmpty()) {
            int size = urlList.size();
            for (int i = 0; i < size; i++) {
                ViewLargeImageFragment fragment;
                if (flsize > i) {
                    fragment = (ViewLargeImageFragment) fragList.get(i);
                } else {
                    fragment = new ViewLargeImageFragment();
                }
                fragment.setUrl(urlList.get(i));
                mFragList.add(fragment);
            }
        }
    }

    @Override
    public Fragment getItem(int position) {
        return mFragList.get(position);
    }

    @Override
    public int getCount() {
        return mFragList.size();
    }

    public void showStill() {
        for (ViewLargeImageFragment frag : mFragList) {
            frag.showStill();
        }
    }

    public void showDynamic() {
        for (ViewLargeImageFragment frag : mFragList) {
            frag.showDynamic();
        }
    }

}

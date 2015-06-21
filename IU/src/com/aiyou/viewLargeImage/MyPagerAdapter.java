package com.aiyou.viewLargeImage;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MyPagerAdapter extends FragmentPagerAdapter {
    private List<ViewLargeImageFragment> mFragList = new ArrayList<ViewLargeImageFragment>();

    public MyPagerAdapter(FragmentManager fm, List<String> urlList) {
        super(fm);
        initFragList(urlList);
    }

    private void initFragList(List<String> urlList) {
        if(urlList != null && !urlList.isEmpty()) {
            for(String url : urlList) {
                ViewLargeImageFragment fragment = new ViewLargeImageFragment(url);
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
        for(ViewLargeImageFragment frag : mFragList) {
            frag.showStill();
        }
    }
    
    public void showDynamic() {
        for(ViewLargeImageFragment frag : mFragList) {
            frag.showDynamic();
        }
    }

}

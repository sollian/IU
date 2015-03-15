
package com.aiyou.bbs.adapter;

import java.util.ArrayList;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * 表情viewpager的adapter
 * 
 * @author sollian
 */
public class ViewPagerAdapter extends PagerAdapter {
    public ArrayList<View> mListViews = null;

    public ViewPagerAdapter(ArrayList<View> mListViews) {
        this.mListViews = mListViews;
    }

    @Override
    public void destroyItem(View container, int position, Object arg2) {
        ((ViewPager) container).removeView(mListViews.get(position));
    }

    @Override
    public void finishUpdate(View arg0) {
    }

    @Override
    public int getCount() {
        return mListViews.size();
    }

    @Override
    public Object instantiateItem(View container, int position) {
        ((ViewPager) container).addView(mListViews.get(position), 0);
        return mListViews.get(position);
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == (arg1);
    }

    @Override
    public void restoreState(Parcelable arg0, ClassLoader arg1) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void startUpdate(View arg0) {
    }
}

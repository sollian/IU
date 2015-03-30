package com.aiyou.electricity;

import java.util.List;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

class ElectricityPagerAdapter extends PagerAdapter {
    private List<View> mList;
    
    public ElectricityPagerAdapter(Context context, List<View> list) {
        mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
    
    @Override  
    public void destroyItem(ViewGroup container, int position,  
            Object object) {  
        container.removeView(mList.get(position));  
    }  

    @Override  
    public int getItemPosition(Object object) {  

        return super.getItemPosition(object);  
    }  

    @Override  
    public Object instantiateItem(ViewGroup container, int position) {  
        container.addView(mList.get(position));  
        return mList.get(position);  
    }  

}

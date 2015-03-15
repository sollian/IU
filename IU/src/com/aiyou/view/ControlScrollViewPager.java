
package com.aiyou.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 可锁定使其不滚动的viewpager
 */
public class ControlScrollViewPager extends ViewPager {

    private boolean mScrollable = true;

    public ControlScrollViewPager(Context context) {
        super(context);
    }

    public ControlScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 设置是否可以滚动
     * 
     * @param enable
     */
    public void setScrollable(boolean enable) {
        mScrollable = enable;
    }

    /**
     * 查询是否可以滚动
     * 
     * @return
     */
    public boolean getScrollable() {
        return mScrollable;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mScrollable) {
            return super.onInterceptTouchEvent(event);
        } else {
            return false;
        }
    }
}

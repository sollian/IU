
package com.aiyou.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * 可以获取滚动距离的scrollView
 * 
 * @author sollian
 */
public class GetScrollDistanceScrollView extends ScrollView {
    private OnScrollListener mOnScrollListener = null;

    public GetScrollDistanceScrollView(Context context) {
        super(context);
    }

    public GetScrollDistanceScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GetScrollDistanceScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public int computeVerticalScrollRange() {
        return super.computeVerticalScrollRange();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(t, oldt);
        }
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.mOnScrollListener = onScrollListener;
    }

    /**
     * 滚动的回调接口
     * 
     * @author sollian
     */
    public interface OnScrollListener {
        /**
         * 回调方法，返回scrollview滑动的Y方向距离
         * 
         * @param scrollY
         */
        public void onScroll(int y, int oldY);
    }
}

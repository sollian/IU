
package external.PullToRefresh;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.View;

import com.aiyou.R;
import com.aiyou.view.GetScrollDistanceScrollView;

public class PullToRefreshMyScrollView extends PullToRefreshBase<GetScrollDistanceScrollView> {

    public PullToRefreshMyScrollView(Context context) {
        super(context);
    }

    public PullToRefreshMyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullToRefreshMyScrollView(Context context, Mode mode) {
        super(context, mode);
    }

    public PullToRefreshMyScrollView(Context context, Mode mode,
            AnimationStyle style) {
        super(context, mode, style);
    }

    @Override
    public final Orientation getPullToRefreshScrollDirection() {
        return Orientation.VERTICAL;
    }

    @Override
    protected GetScrollDistanceScrollView createRefreshableView(Context context,
            AttributeSet attrs) {
        GetScrollDistanceScrollView scrollView;
        if (VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD) {
            scrollView = new InternalScrollViewSDK9(context, attrs);
        } else {
            scrollView = new GetScrollDistanceScrollView(context, attrs);
        }

        scrollView.setId(R.id.scrollview);
        return scrollView;
    }

    @Override
    protected boolean isReadyForPullStart() {
        return mRefreshableView.getScrollY() == 0;
    }

    @Override
    protected boolean isReadyForPullEnd() {
        View scrollViewChild = mRefreshableView.getChildAt(0);
        if (null != scrollViewChild) {
            return mRefreshableView.getScrollY() >= (scrollViewChild
                    .getHeight() - getHeight());
        }
        return false;
    }

    @TargetApi(9)
    public class InternalScrollViewSDK9 extends GetScrollDistanceScrollView {

        public InternalScrollViewSDK9(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
                int scrollY, int scrollRangeX, int scrollRangeY,
                int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {

            final boolean returnValue = super.overScrollBy(deltaX, deltaY,
                    scrollX, scrollY, scrollRangeX, scrollRangeY,
                    maxOverScrollX, maxOverScrollY, isTouchEvent);

            // Does all of the hard work...
            OverscrollHelper.overScrollBy(PullToRefreshMyScrollView.this,
                    deltaX, scrollX, deltaY, scrollY, getScrollRange(),
                    isTouchEvent);

            return returnValue;
        }

        /**
         * Taken from the AOSP ScrollView source
         */
        private int getScrollRange() {
            int scrollRange = 0;
            if (getChildCount() > 0) {
                View child = getChildAt(0);
                scrollRange = Math.max(0, child.getHeight()
                        - (getHeight() - getPaddingBottom() - getPaddingTop()));
            }
            return scrollRange;
        }
    }
}

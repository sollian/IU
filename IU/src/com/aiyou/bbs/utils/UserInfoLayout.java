
package com.aiyou.bbs.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

import com.aiyou.R;
import com.aiyou.bbs.bean.User;
import com.aiyou.utils.SwitchManager;
import com.aiyou.view.ScrollTextView;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.view.ViewHelper;

import external.OtherView.CircleImageView;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class UserInfoLayout extends ScrollView {

    private int mStartColor;
    private int mEndColor;

    private FrameLayout mTopFLayout;
    private CircleImageView mFaceCIV;
    private LinearLayout mTextLLayout;
    private ScrollTextView mIdTV, mNameTV;
    private LinearLayout mContentLLayout;
    // 基本信息
    private TextView mGenderTV, mAstroTV, mQQTV, mMsnTV, mHomePageTV;
    // 论坛属性
    private TextView mLevelTV, mOnlineTV, mPostCountTV, mScoreTV, mLifeTV, mLastLoginTimeTV,
            mLastLoginIpTV, mFirstLoginTimeTV;

    private ObjectAnimator mObjectAnim;
    private int mWidth;
    private int mFaceHeight, mFaceWidth, mFaceMarginTop;
    private int mTextHeight;
    private int mTextPaddingTop;

    private int mRange1, mRange2;

    private boolean mIsTouchOrRunning = false;
    private boolean mIsActionCancel = true;
    private Status mStatus;
    private float mDetalY;
    private float mLastY;
    private float mMinScale;

    public UserInfoLayout(Context context) {
        super(context);
    }

    public UserInfoLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UserInfoLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        if (SwitchManager.getInstance(getContext()).isNightModeEnabled()) {
            mStartColor = Color.parseColor("#00222222");
            mEndColor = Color.parseColor("#ff222222");
        } else {
            mStartColor = Color.parseColor("#000099cc");
            mEndColor = Color.parseColor("#ff0099cc");
        }

        init();

        this.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                UserInfoLayout.this.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                mWidth = getWidth();
            }
        });

        mFaceCIV.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mFaceCIV.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                mFaceHeight = mFaceCIV.getHeight();
                mFaceWidth = mFaceCIV.getWidth();
                mFaceMarginTop = ((MarginLayoutParams) (mFaceCIV.getLayoutParams())).topMargin;
                mRange2 = mFaceHeight + mFaceMarginTop;
            }
        });

        mTextLLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mTextLLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                mTextHeight = mTextLLayout.getHeight();
                mTextPaddingTop = mTextLLayout.getPaddingTop();
                mContentLLayout.setPadding(0, mTextHeight, 0, 0);
            }
        });

        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mFaceHeight > 0) {
                    mMinScale = (float) (mTextHeight - mTextPaddingTop) / mFaceHeight;
                    mRange1 = 2 * mFaceHeight - mTextHeight + mFaceMarginTop;
                    if (mRange1 < 0) {
                        mRange1 = 0;
                    }
                } else {
                    handler.post(this);
                }
            }
        });
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mWidth > 0 && mFaceWidth > 0) {
                    ViewHelper.setTranslationX(mFaceCIV, (mWidth - mFaceWidth) / 2);
                } else {
                    handler.post(this);
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (Build.VERSION.SDK_INT < 11) {
            return super.onInterceptTouchEvent(ev);
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsActionCancel = false;
                mIsTouchOrRunning = true;
                mLastY = ev.getY();
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (Build.VERSION.SDK_INT < 11) {
            return super.onTouchEvent(ev);
        }
        if (mObjectAnim != null && mObjectAnim.isRunning()) {
            ev.setAction(MotionEvent.ACTION_UP);
            mIsActionCancel = true;
        }
        if (mIsActionCancel && ev.getAction() != MotionEvent.ACTION_DOWN) {
            return false;
        }
        if (ev.getActionIndex() != 0 && getScrollY() < mRange2) {
            ev.setAction(MotionEvent.ACTION_UP);
            mIsActionCancel = true;
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                mIsTouchOrRunning = true;
                if (getScrollY() != 0) {
                    mDetalY = 0;
                    mLastY = ev.getY();
                } else {
                    mDetalY = ev.getY() - mLastY;
                    if (mDetalY > 0) {
                        setT((int) -mDetalY / 5);
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mIsTouchOrRunning = false;
                if (getScrollY() < mRange2) {
                    if (mDetalY != 0) {
                        reset();
                    } else {
                        toggle();
                    }
                    return true;
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (Build.VERSION.SDK_INT < 11) {
            return;
        }
        if (t > mRange2) {
            updateTopPadding(t);
            ViewHelper.setTranslationX(mFaceCIV, -mFaceWidth * (1 - mMinScale) / 2f);
            ViewHelper.setTranslationY(mFaceCIV, mFaceHeight / 2 + mFaceHeight
                    * (1 - mMinScale) / 2);
            ViewHelper.setScaleX(mFaceCIV, mMinScale);
            ViewHelper.setScaleY(mFaceCIV, mMinScale);
            mTopFLayout.setBackgroundColor(mEndColor);
        } else if (!mIsTouchOrRunning && t != mRange2) {
            scrollTo(0, mRange2);
            ViewHelper.setTranslationY(mTopFLayout, 0);
        } else if (t > mRange1) {
            animateScroll2(t);
            ViewHelper.setTranslationY(mTopFLayout, 0);
            ViewHelper.setScaleX(mFaceCIV, mMinScale);
            ViewHelper.setScaleY(mFaceCIV, mMinScale);
        } else {
            animateScroll1(t);
            ViewHelper.setTranslationY(mTopFLayout, 0);
            ViewHelper.setTranslationX(mFaceCIV, (mWidth - mFaceWidth) / 2);
            mTopFLayout.setBackgroundColor(mStartColor);
        }
    }

    private void updateTopPadding(int t) {
        ViewHelper.setTranslationY(mTopFLayout, t - mRange2);
    }

    private void animateScroll1(int t) {
        float percent = (float) t / mRange1;
        percent = mMinScale + (1 - percent) * (1 - mMinScale);
        ViewHelper.setScaleX(mFaceCIV, percent);
        ViewHelper.setScaleY(mFaceCIV, percent);
        ViewHelper.setTranslationY(mFaceCIV, mFaceHeight * (1 - percent) / 2f);

    }

    private void animateScroll2(int t) {
        float percent = (float) (mRange2 - t) / (mRange2 - mRange1);

        ViewHelper.setTranslationX(mFaceCIV, ((mWidth - mFaceWidth) / 2) * percent - (1 - percent)
                * mFaceWidth * (1 - mMinScale) / 2f);
        ViewHelper.setTranslationY(mFaceCIV, mFaceHeight * (1 - percent) / 2 + mFaceHeight
                * (1 - mMinScale) / 2);
        mTopFLayout.setBackgroundColor(evaluate(1 - percent, mStartColor, mEndColor));
    }

    private Integer evaluate(float fraction, Object startValue, Integer endValue) {
        int startInt = (Integer) startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;
        int endInt = (Integer) endValue;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;
        return (int) ((startA + (int) (fraction * (endA - startA))) << 24)
                | (int) ((startR + (int) (fraction * (endR - startR))) << 16)
                | (int) ((startG + (int) (fraction * (endG - startG))) << 8)
                | (int) ((startB + (int) (fraction * (endB - startB))));
    }

    public void setT(int t) {
        scrollTo(0, t);
        if (t < 0) {
            animatePull(t);
        }
    }

    private void animatePull(int t) {
        float percent = (float) t / mRange2;
        ViewHelper.setScaleX(mFaceCIV, 1 - percent);
        ViewHelper.setScaleY(mFaceCIV, 1 - percent);
        ViewHelper.setTranslationY(mFaceCIV, mFaceHeight * -percent / 2f);

        int padding = mTextHeight - t;
        mTextLLayout.setPadding(0, mTextPaddingTop - t, 0, 0);
        mContentLLayout.setPadding(0, padding, 0, 0);
    }

    public void toggle() {
        if (isOpen()) {
            close();
        } else {
            open();
        }
    }

    public enum Status {
        Open, Close;
    }

    public boolean isOpen() {
        return mStatus == Status.Open;
    }

    private void reset() {
        if (mObjectAnim != null && mObjectAnim.isRunning()) {
            return;
        }
        mObjectAnim = ObjectAnimator.ofInt(this, "t", (int) -mDetalY / 5, 0);
        mObjectAnim.setDuration(150);
        mObjectAnim.start();
    }

    public void close() {
        if (mObjectAnim != null && mObjectAnim.isRunning()) {
            return;
        }
        mObjectAnim = ObjectAnimator.ofInt(this, "t", getScrollY(), mRange2);
        mObjectAnim.setInterpolator(new DecelerateInterpolator());
        mObjectAnim.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator arg0) {
                mIsTouchOrRunning = true;
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {
            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                mIsTouchOrRunning = false;
                mStatus = Status.Close;
            }

            @Override
            public void onAnimationCancel(Animator arg0) {

            }
        });
        mObjectAnim.setDuration(250);
        mObjectAnim.start();
    }

    public void open() {
        if (mObjectAnim != null && mObjectAnim.isRunning()) {
            return;
        }
        mObjectAnim = ObjectAnimator
                .ofInt(this, "t", getScrollY(), (int) (-getScrollY() / 2.2f), 0);
        mObjectAnim.setInterpolator(new DecelerateInterpolator());
        mObjectAnim.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator arg0) {
                mIsTouchOrRunning = true;
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {
            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                mIsTouchOrRunning = false;
                mStatus = Status.Open;
            }

            @Override
            public void onAnimationCancel(Animator arg0) {

            }
        });
        mObjectAnim.setDuration(400);
        mObjectAnim.start();
    }

    public void setUser(User user) {
        // 头像
        if (null != user.face_url) {
            mFaceCIV.setImageURL(user.face_url);
        }
        // id
        if (null != user.id) {
            mIdTV.setText(user.id);
        }
        // 昵称
        if (null != user.user_name) {
            mNameTV.setText(user.user_name);
        }
        // 性别
        if (null != user.gender) {
            if ("m".equals(user.gender)) {
                mGenderTV.setText("帅哥");
            } else if ("f".equals(user.gender)) {
                mGenderTV.setText("美女");
            } else {
                mGenderTV.setText("保密");
            }
        }
        // 星座
        if (null != user.astro) {
            if ("".equals(user.astro)) {
                mAstroTV.setText("保密");
            } else {
                mAstroTV.setText(user.astro);
            }
        }
        // qq
        if (null != user.qq) {
            if (!"".equals(user.qq)) {
                mQQTV.setText(user.qq);
            }
        }
        // msn
        if (null != user.msn) {
            if (!"".equals(user.msn)) {
                mMsnTV.setText(user.msn);
            }
        }
        // 主页
        if (null != user.home_page) {
            if (!"".equals(user.home_page)) {
                mHomePageTV.setText(user.home_page);
            }
        }
        // 论坛等级
        if (null != user.level) {
            mLevelTV.setText(user.level);
        }
        // 是否在线
        if (false == user.is_online) {
            mOnlineTV.setText("否");
        } else {
            mOnlineTV.setText("是");
        }
        // 发文数
        if (-1 != user.post_count) {
            mPostCountTV.setText(user.post_count + "篇");
        }
        // 积分
        if (-1 != user.score) {
            mScoreTV.setText(user.score + "");
        }
        // 生命力
        if (-1 != user.life) {
            mLifeTV.setText(user.life + "");
        }
        // 上次登录时间
        if (-1 != user.last_login_time) {
            mLastLoginTimeTV.setText(formatTime(user.last_login_time));
        }
        // 上次登录IP
        if (null != user.last_login_ip) {
            mLastLoginIpTV.setText(user.last_login_ip);
        }
        // 注册时间
        if (-1 != user.first_login_time) {
            mFirstLoginTimeTV.setText(formatTime(user.first_login_time));
        }
    }

    private String formatTime(long timeStamp) {
        timeStamp *= 1000;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.CHINA);
        String date = sdf.format(timeStamp);
        return date;
    }

    private void init() {
        mTopFLayout = (FrameLayout) findViewById(R.id.fl_top);
        mTopFLayout.setBackgroundColor(mStartColor);
        mTopFLayout.bringToFront();
        mFaceCIV = (CircleImageView) findViewById(R.id.civ_face);
        mFaceCIV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
            }
        });
        mTextLLayout = (LinearLayout) findViewById(R.id.ll_text);
        mContentLLayout = (LinearLayout) findViewById(R.id.ll_content);
        mIdTV = (ScrollTextView) findViewById(R.id.tv_id);
        mNameTV = (ScrollTextView) findViewById(R.id.tv_name);

        mGenderTV = (TextView) findViewById(R.id.tv_gender);
        mAstroTV = (TextView) findViewById(R.id.tv_astro);
        mQQTV = (TextView) findViewById(R.id.tv_qq);
        mMsnTV = (TextView) findViewById(R.id.tv_msn);
        mHomePageTV = (TextView) findViewById(R.id.tv_home_page);

        mLevelTV = (TextView) findViewById(R.id.tv_level);
        mOnlineTV = (TextView) findViewById(R.id.tv_is_online);
        mPostCountTV = (TextView) findViewById(R.id.tv_post_count);
        mScoreTV = (TextView) findViewById(R.id.tv_score);
        mLifeTV = (TextView) findViewById(R.id.tv_life);
        mLastLoginTimeTV = (TextView) findViewById(R.id.tv_last_login_time);
        mLastLoginIpTV = (TextView) findViewById(R.id.tv_last_login_ip);
        mFirstLoginTimeTV = (TextView) findViewById(R.id.tv_first_login_time);

        if (Build.VERSION.SDK_INT >= 11) {
            initDivider();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initDivider() {
        LinearLayout[] array = new LinearLayout[15];
        array[0] = (LinearLayout) findViewById(R.id.ll1);
        array[1] = (LinearLayout) findViewById(R.id.ll2);
        array[2] = (LinearLayout) findViewById(R.id.ll3);
        array[3] = (LinearLayout) findViewById(R.id.ll4);
        array[4] = (LinearLayout) findViewById(R.id.ll5);
        array[5] = (LinearLayout) findViewById(R.id.ll6);
        array[6] = (LinearLayout) findViewById(R.id.ll7);
        array[7] = (LinearLayout) findViewById(R.id.ll8);
        array[8] = (LinearLayout) findViewById(R.id.ll9);
        array[9] = (LinearLayout) findViewById(R.id.ll10);
        array[10] = (LinearLayout) findViewById(R.id.ll11);
        array[11] = (LinearLayout) findViewById(R.id.ll12);
        array[12] = (LinearLayout) findViewById(R.id.ll13);
        array[13] = (LinearLayout) findViewById(R.id.ll14);
        array[14] = (LinearLayout) findViewById(R.id.ll15);
        Drawable drawableH, drawableV;
        if (SwitchManager.getInstance(getContext()).isNightModeEnabled()) {
            drawableH = getResources().getDrawable(R.drawable.divider_white_h_night);
            drawableV = getResources().getDrawable(R.drawable.divider_white_v_night);
        } else {
            drawableH = getResources().getDrawable(R.drawable.divider_white_h_day);
            drawableV = getResources().getDrawable(R.drawable.divider_white_v_day);
        }
        if (drawableH != null && drawableV != null) {
            for (int i = 0; i < array.length; i++) {
                if (i == 0 || i == 6) {
                    array[i].setDividerDrawable(drawableH);
                } else {
                    array[i].setDividerDrawable(drawableV);
                }
            }
        }
    }

}


package com.aiyou.bbs.FaceViewListener;

import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

/**
 * 表情ViewPager监听器
 * 
 * @author sollian
 */
public class MyOnPageChangeListener implements OnPageChangeListener {

    int mOne = 0;// 页卡1 -> 页卡2 偏移量
    int mTwo = 0;// 页卡1 -> 页卡3 偏移量
    int mThree = 0;// 页卡1 -> 页卡4 偏移量
    int mOffset = 0;
    int mCurIndex = 0;
    ImageView mImageView;

    public MyOnPageChangeListener(ImageView iv, int offset, int bmpW,
            int currIndex) {
        mImageView = iv;
        mOffset = offset;
        mOne = offset * 3 + bmpW;// 页卡1 -> 页卡2 偏移量
        mTwo = offset * 5 + bmpW * 2;// 页卡1 -> 页卡3 偏移量
        mThree = offset * 7 + bmpW * 3;
        mCurIndex = currIndex;
    }

    @Override
    public void onPageSelected(int arg0) {
        Animation animation = null;
        switch (arg0) {
            case 0:
                if (mCurIndex == 1) {
                    animation = new TranslateAnimation(mOne, 0, 0, 0);
                } else if (mCurIndex == 2) {
                    animation = new TranslateAnimation(mTwo, 0, 0, 0);
                } else if (mCurIndex == 3) {
                    animation = new TranslateAnimation(mThree, 0, 0, 0);
                }
                break;
            case 1:
                if (mCurIndex == 0) {
                    animation = new TranslateAnimation(mOffset, mOne, 0, 0);
                } else if (mCurIndex == 2) {
                    animation = new TranslateAnimation(mTwo, mOne, 0, 0);
                } else if (mCurIndex == 3) {
                    animation = new TranslateAnimation(mThree, mOne, 0, 0);
                }
                break;
            case 2:
                if (mCurIndex == 0) {
                    animation = new TranslateAnimation(mOffset, mTwo, 0, 0);
                } else if (mCurIndex == 1) {
                    animation = new TranslateAnimation(mOne, mTwo, 0, 0);
                } else if (mCurIndex == 3) {
                    animation = new TranslateAnimation(mThree, mTwo, 0, 0);
                }
                break;
            case 3:
                if (mCurIndex == 0) {
                    animation = new TranslateAnimation(mOffset, mThree, 0, 0);
                } else if (mCurIndex == 1) {
                    animation = new TranslateAnimation(mOne, mThree, 0, 0);
                } else if (mCurIndex == 2) {
                    animation = new TranslateAnimation(mTwo, mThree, 0, 0);
                }
                break;
        }
        mCurIndex = arg0;
        animation.setFillAfter(true);// True:图片停在动画结束位置
        animation.setDuration(300);
        mImageView.startAnimation(animation);
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }
}

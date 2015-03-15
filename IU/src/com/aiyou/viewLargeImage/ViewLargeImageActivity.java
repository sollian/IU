
package com.aiyou.viewLargeImage;

import java.util.ArrayList;
import java.util.List;
import com.aiyou.BaseActivity;
import com.aiyou.R;
import com.aiyou.utils.NetWorkManager;
import com.aiyou.utils.SwitchManager;
import com.aiyou.utils.FileCache.FileManager;
import com.aiyou.utils.image.ImageFactory;
import com.aiyou.utils.share.ShareTask;
import com.aiyou.utils.share.ShareTask.ShareListener;
import com.aiyou.viewLargeImage.GetLargeImgTask.ProgressListener;
import external.GifImageViewEx.net.frakbot.imageviewex.ImageViewEx;
import external.OtherView.SinkingView;
import external.OtherView.TouchImageView;
import external.OtherView.Win8ProgressBar;
import external.SmartImageView.SmartImageView;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 查看大图
 * 
 * @author sollian
 */
public class ViewLargeImageActivity extends BaseActivity implements ProgressListener {
    public static final String KEY_NEWS = "news";
    public static final String KEY_URL = "url";
    public static final String KEY_URL_LIST = "url_list";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DEGREE = "degree";

    private boolean mFlag = false;
    /**
     * 图片旋转角度，查看大图时用到
     */
    private int mDegree = 0;
    /**
     * 获取大图的异步任务
     */
    private GetLargeImgTask mTask = null;
    /**
     * 保存图片地址的list，用户展示大图时能够切换
     */
    private List<String> mUrlList = new ArrayList<String>();
    /**
     * 控件
     */
    // 进度条
    private FrameLayout mProgressFLayout;
    private Win8ProgressBar mProgressBar;
    // sinkingview
    private SinkingView mSinkView;
    private SmartImageView mSmartIV;
    // 查看大图
    private FrameLayout mTIVFLayout;
    private TouchImageView mTouchImageView;
    private ImageViewEx mImageViewEx;
    private TextView mTitleTV;
    private ImageView mRotateLeftIV, mRotateRightIV;
    private ImageView mPreviousIV, mNextIV;
    private Button mDynamicBtn;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SwitchManager.getInstance(getBaseContext()).isNightModeEnabled()) {
            // 夜间模式
            this.setTheme(R.style.ThemeNight);
        } else {
            // 日间模式
            this.setTheme(R.style.ThemeDay);
        }
        setContentView(R.layout.activity_view_large_image);

        if (null == savedInstanceState) {
            Intent intent = getIntent();
            mFlag = intent.getBooleanExtra(KEY_NEWS, false);
            init(mFlag);
            mTouchImageView.setTag(intent.getStringExtra(KEY_URL));
            List<String> tempList = (ArrayList<String>) intent
                    .getSerializableExtra(KEY_URL_LIST);
            int size = tempList.size();
            String url;
            for (int i = 0; i < size; i++) {
                url = tempList.get(i);
                url = i + "+" + url;
                mUrlList.add(url);
            }
        } else {
            mFlag = savedInstanceState.getBoolean(KEY_NEWS, false);
            init(mFlag);
            mTouchImageView.setTag(savedInstanceState.getString(KEY_URL));
            String list = savedInstanceState.getString(KEY_URL_LIST);
            String arr[] = list.split(",");
            mUrlList.clear();
            for (int i = 0; i < arr.length; i++) {
                mUrlList.add(arr[i]);
            }
            mTitleTV.setText(savedInstanceState.getString(KEY_TITLE));
            mDegree = savedInstanceState.getInt(KEY_DEGREE);
        }
        showLargeImage();
    }

    private void init(boolean flag) {
        /**
         * 进度条
         */
        mProgressFLayout = (FrameLayout) findViewById(R.id.activity_view_large_image_fl_progress);
        mProgressBar = (Win8ProgressBar) findViewById(R.id.progress_bar);
        /**
         * sinkingview
         */
        mSinkView = (SinkingView) findViewById(R.id.activity_view_large_image_sv);
        mSmartIV = (SmartImageView) findViewById(R.id.activity_view_large_image_siv);
        /**
         * 大图
         */
        mTIVFLayout = (FrameLayout) findViewById(R.id.activity_view_large_image_fl_tiv);
        mTouchImageView = (TouchImageView) findViewById(R.id.activity_view_large_image_tiv);
        mImageViewEx = (ImageViewEx) findViewById(R.id.activity_view_large_image_ive);
        mTitleTV = (TextView) findViewById(R.id.activity_view_large_image_tv_title);
        mRotateLeftIV = (ImageView) findViewById(R.id.activity_view_large_image_btl);
        mRotateRightIV = (ImageView) findViewById(R.id.activity_view_large_image_btr);
        mPreviousIV = (ImageView) findViewById(R.id.activity_view_large_image_previous);
        mNextIV = (ImageView) findViewById(R.id.activity_view_large_image_next);
        mDynamicBtn = (Button) findViewById(R.id.activity_view_large_image_bt);

        if (Build.VERSION.SDK_INT < 11) {
            mRotateLeftIV.setVisibility(View.INVISIBLE);
            mRotateRightIV.setVisibility(View.INVISIBLE);
        }

        // 设置缩放比例
        mTouchImageView.setMaxZoom(10f);

        if (SwitchManager.getInstance(getBaseContext()).isNightModeEnabled()) {
            mSmartIV.setColorFilter(Color.GRAY,
                    android.graphics.PorterDuff.Mode.MULTIPLY);
            mTouchImageView.setColorFilter(Color.GRAY,
                    android.graphics.PorterDuff.Mode.MULTIPLY);
            mImageViewEx.setColorFilter(Color.GRAY,
                    android.graphics.PorterDuff.Mode.MULTIPLY);
        }

        if (flag && !SwitchManager.getInstance(getBaseContext()).isNightModeEnabled()) {
            mTitleTV.setBackgroundColor(Color.parseColor("#aae46600"));
            mRotateLeftIV
                    .setBackgroundResource(R.drawable.background_large_image_orange);
            mRotateRightIV
                    .setBackgroundResource(R.drawable.background_large_image_orange);
            mPreviousIV
                    .setBackgroundResource(R.drawable.background_large_image_orange);
            mNextIV.setBackgroundResource(R.drawable.background_large_image_orange);
            mDynamicBtn
                    .setBackgroundResource(R.drawable.background_large_image_orange);
        }
    }

    public void onClick(View view) {
        if (view == mDynamicBtn) {
            // 查看大图->动图|静图切换
            if (View.GONE == mImageViewEx.getVisibility()) {
                mImageViewEx.setVisibility(View.VISIBLE);
                mTouchImageView.setVisibility(View.GONE);
                mDynamicBtn.setText("返回");
                mRotateLeftIV.setVisibility(View.INVISIBLE);
                mRotateRightIV.setVisibility(View.INVISIBLE);
                if (mImageViewEx.canPlay()) {
                    mImageViewEx.play();
                }
            } else {
                mImageViewEx.setVisibility(View.GONE);
                mTouchImageView.setVisibility(View.VISIBLE);
                mDynamicBtn.setText(R.string.dynamic_bmp);
                mRotateLeftIV.setVisibility(View.VISIBLE);
                mRotateRightIV.setVisibility(View.VISIBLE);
                if (mImageViewEx.isPlaying()) {
                    mImageViewEx.stop();
                }
            }
        }
    }

    public void onShare(View view) {
        // 分享
        String urlImg = (String) mTouchImageView.getTag();
        if (null != urlImg) {
            int index = urlImg.indexOf("+") + 1;
            urlImg = urlImg.substring(index);
        }
        ShareTask task = new ShareTask(ViewLargeImageActivity.this, urlImg, new ShareListener() {
            @Override
            public void onShareStart() {
                showProgress(true);
            }

            @Override
            public void onShareFinish(Boolean success) {
                showProgress(false);
            }
        });
        task.execute();
    }

    /**
     * 切换图片的方法
     * 
     * @param view
     */
    public void onSwapLargeImage(View view) {
        String url = (String) mTouchImageView.getTag();
        int position = mUrlList.indexOf(url);
        if (position < 0 || position >= mUrlList.size()) {
            return;
        }
        if (view == mPreviousIV) {
            // 上一张
            if (position == 0) {
                Toast.makeText(getBaseContext(), "已是第一张", Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            url = mUrlList.get(position - 1);
            mTouchImageView.setTag(url);
            int index = url.indexOf("+") + 1;
            url = url.substring(index);
        } else if (view == mNextIV) {
            // 下一张
            if (position == mUrlList.size() - 1) {
                Toast.makeText(getBaseContext(), "最后一张了", Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            url = mUrlList.get(position + 1);
            mTouchImageView.setTag(url);
            int index = url.indexOf("+") + 1;
            url = url.substring(index);
        }
        showLargeImage();
    }

    /**
     * 旋转大图
     * 
     * @param view
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onRotateLargeImage(View view) {
        if (null != mTask) {
            if (Status.FINISHED != mTask.getStatus()) {
                Toast.makeText(getBaseContext(), "下载中", Toast.LENGTH_SHORT)
                        .show();
                return;
            }
        }
        int nId = view.getId();
        FileManager imgch = new FileManager(FileManager.DIR_LARGEIMG);
        String url = (String) mTouchImageView.getTag();
        int index = url.indexOf("+") + 1;
        url = url.substring(index);
        Bitmap bmp = ImageFactory.getMaxBmp(imgch.getImage(url), false);
        if (null != bmp) {
            Matrix matrix = new Matrix();
            if (R.id.activity_view_large_image_btl == nId) {
                // 逆时针旋转
                matrix.setRotate(90 * (--mDegree));
                rotate(mNextIV, 90 * mDegree, 90 * (mDegree + 1));
                rotate(mPreviousIV, 90 * mDegree, 90 * (mDegree + 1));
                rotate(mRotateLeftIV, 90 * mDegree, 90 * (mDegree + 1));
                rotate(mRotateRightIV, 90 * mDegree, 90 * (mDegree + 1));
                rotate(mDynamicBtn, 90 * mDegree, 90 * (mDegree + 1));
            } else if (R.id.activity_view_large_image_btr == nId) {
                // 顺时针旋转
                matrix.setRotate(90 * (++mDegree));
                rotate(mNextIV, 90 * mDegree, 90 * (mDegree - 1));
                rotate(mPreviousIV, 90 * mDegree, 90 * (mDegree - 1));
                rotate(mRotateLeftIV, 90 * mDegree, 90 * (mDegree - 1));
                rotate(mRotateRightIV, 90 * mDegree, 90 * (mDegree - 1));
                rotate(mDynamicBtn, 90 * mDegree, 90 * (mDegree - 1));
            }
            try {
                bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
                        bmp.getHeight(), matrix, true);
            } catch (OutOfMemoryError e) {
                Toast.makeText(getBaseContext(), "图片太大,转不动了",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            mTouchImageView.setImageBitmap(bmp);
            mSinkView.setRotation(90 * mDegree);

        }
    }

    /**
     * 左上角返回按钮
     */
    public void selfFinish(View view) {
        scrollToFinishActivity();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 按下键盘上返回按钮
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            selfFinish(null);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mTask) {
            if (Status.FINISHED != mTask.getStatus()) {
                mTask.cancel(true);
            }
            mTask = null;
        }
        mUrlList.clear();
        mUrlList = null;
    }

    /**
     * 旋转动画
     * 
     * @param view
     * @param toDegree
     * @param fromDegree
     */
    public void rotate(View view, int toDegree, int fromDegree) {
        view.clearAnimation();
        RotateAnimation anim = new RotateAnimation(fromDegree, toDegree,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        anim.setDuration(500);
        anim.setFillAfter(true);
        view.startAnimation(anim);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {// 记录旧状态
        super.onSaveInstanceState(outState);
        // 保存tag
        String url = (String) mTouchImageView.getTag();
        outState.putString(KEY_URL, url);
        // 保存nDegree
        outState.putInt(KEY_DEGREE, mDegree);
        // 保存list
        String list = "";
        int size = mUrlList.size();
        for (int i = 0; i < size; i++) {
            if (i < size - 1) {
                list += mUrlList.get(i) + ",";
            } else {
                list += mUrlList.get(i);
            }
        }
        outState.putString(KEY_URL_LIST, list);
        // 保存title
        outState.putString(KEY_TITLE, mTitleTV.getText().toString());
        // 保存flag
        outState.putBoolean(KEY_NEWS, mFlag);
    }

    /**
     * 设置cpb_progress的状态和是否显示
     * 
     * @param flag
     */
    private void showProgress(boolean flag) {
        if (flag) {
            mProgressFLayout.setVisibility(View.VISIBLE);
            mProgressBar.start();
        } else {
            mProgressFLayout.setVisibility(View.GONE);
            mProgressBar.stop();
        }
    }

    /**
     * 显示查看大图
     */
    private void showLargeImage() {
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            Toast.makeText(getBaseContext(), NetWorkManager.MSG_NONET, Toast.LENGTH_LONG).show();
            return;
        }
        if (null != mTask) {
            switch (mTask.getStatus()) {
                case RUNNING:
                    mTask.cancel(true);
                    break;
                case PENDING:
                    mTask.cancel(false);
                    break;
                default:
                    break;
            }
        }

        String url = (String) mTouchImageView.getTag();
        int index = url.indexOf("+") + 1;
        mTask = new GetLargeImgTask(getBaseContext(), url.substring(index), this);
        mTask.execute();
        mTouchImageView.setImageResource(R.drawable.touch_image_view);
        mImageViewEx.setImageBitmap(null);

        mPreviousIV.setVisibility(View.VISIBLE);
        mNextIV.setVisibility(View.VISIBLE);
        int position = mUrlList.indexOf(url);
        mTitleTV.setText("查看大图    " + (position + 1) + "/"
                + mUrlList.size());
    }

    @Override
    public void onStartProgress() {
        mTIVFLayout.clearAnimation();
        mTIVFLayout.setVisibility(View.GONE);
        mSinkView.clearAnimation();
        mSinkView.setVisibility(View.VISIBLE);

        String url = (String) mTouchImageView.getTag();
        int index = url.indexOf("+") + 1;
        url = url.substring(index);
        mSmartIV.setImageUrl(url, R.drawable.iu_default_gray,
                R.drawable.iu_default_green);
    }

    @Override
    public void onProgress(int progress) {
        float percent = progress / 100.0f;
        mSinkView.setPercent(percent);
    }

    @Override
    public void onFinishProgress(byte[] result) {
        if (result == null) {
            Toast.makeText(getBaseContext(), "下载失败", Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap bmp = null;
//        bmp = ImageFactory.getMaxBmp(result, false);
        bmp = ImageFactory.getFixedBmp(result, 2000, 3000, false);
        if (bmp == null) {
            return;
        }
        Matrix matrix = new Matrix();
        matrix.setRotate(90 * mDegree);
        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
                bmp.getHeight(), matrix, true);
        mTouchImageView.setImageBitmap(bmp);
        try {
            mImageViewEx.setSource(result);
        } catch (OutOfMemoryError e) {
        }
        mImageViewEx.pause();
        Animation animIn = AnimationUtils.loadAnimation(getBaseContext(),
                android.R.anim.fade_in);
        Animation animOut = AnimationUtils.loadAnimation(getBaseContext(),
                android.R.anim.fade_out);
        if (null != result) {
            mTIVFLayout.setVisibility(View.VISIBLE);
        }
        mSinkView.clear();
        mTIVFLayout.startAnimation(animIn);
        mSinkView.startAnimation(animOut);
        animOut.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                mSinkView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationStart(Animation arg0) {
            }
        });
    }
}

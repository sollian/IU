package com.aiyou.viewLargeImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.aiyou.BaseActivity;
import com.aiyou.R;
import com.aiyou.utils.SwitchManager;
import com.aiyou.utils.share.ShareTask;
import com.aiyou.utils.share.ShareTask.ShareListener;

import external.otherview.Win8ProgressBar;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * 查看大图
 *
 * @author sollian
 */
public class ViewLargeImageActivity extends BaseActivity implements
        ViewPager.OnPageChangeListener {
    public static final String KEY_NEWS = "news";
    public static final String KEY_CUR_SEL = "current_selected";
    public static final String KEY_URL_LIST = "url_list";

    private boolean mFlag = false;
    /**
     * 保存图片地址的list，用户展示大图时能够切换
     */
    private List<String> mUrlList = new ArrayList<>();
    /**
     * 控件
     */
    private FrameLayout mTitleFL;
    // 进度条
    private FrameLayout mProgressFLayout;
    private Win8ProgressBar mProgressBar;

    private TextView mTitleTV;
    private Button mDynamicBtn;

    private ViewPager mViewPager;
    private MyPagerAdapter mAdapter;

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

        int curSel;

        if (null == savedInstanceState) {
            Intent intent = getIntent();
            @SuppressWarnings("unchecked")
            List<String> tempList = (ArrayList<String>) intent
                    .getSerializableExtra(KEY_URL_LIST);
            if (tempList == null || tempList.isEmpty()) {
                selfFinish(null);
                return;
            }
            mUrlList.clear();
            mUrlList.addAll(tempList);
            curSel = Integer.parseInt(intent.getStringExtra(KEY_CUR_SEL));
            mFlag = intent.getBooleanExtra(KEY_NEWS, false);
            init(mFlag);
        } else {
            mFlag = savedInstanceState.getBoolean(KEY_NEWS, false);
            init(mFlag);
            String list = savedInstanceState.getString(KEY_URL_LIST);
            String arr[] = new String[0];
            if (list != null) {
                arr = list.split(",");
            }
            mUrlList.clear();
            mUrlList.addAll(Arrays.asList(arr));
            curSel = Integer
                    .parseInt(savedInstanceState.getString(KEY_CUR_SEL));
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mTitleFL.setVisibility(View.GONE);
                ((FrameLayout.LayoutParams) mViewPager.getLayoutParams()).topMargin = 0;
            } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                mTitleFL.setVisibility(View.VISIBLE);
            }
        }
        if (curSel < 0 || curSel >= mUrlList.size()) {
            selfFinish(null);
        }
        setSelTitle(curSel + 1);
        final int cs = curSel;
        mAdapter = new MyPagerAdapter(getSupportFragmentManager(), mUrlList);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(cs);
    }

    private void init(boolean flag) {
        mTitleFL = (FrameLayout) findViewById(R.id.fl_title);
        /**
         * 进度条
         */
        mProgressFLayout = (FrameLayout) findViewById(R.id.activity_view_large_image_fl_progress);
        mProgressBar = (Win8ProgressBar) findViewById(R.id.progress_bar);
        mTitleTV = (TextView) findViewById(R.id.activity_view_large_image_tv_title);
        mDynamicBtn = (Button) findViewById(R.id.activity_view_large_image_bt);

        mViewPager = (ViewPager) findViewById(R.id.activity_view_large_image_vp);
        mViewPager.setOnPageChangeListener(this);

        if (flag
                && !SwitchManager.getInstance(getBaseContext())
                .isNightModeEnabled()) {
            mTitleTV.setBackgroundColor(Color.parseColor("#aae46600"));
            mDynamicBtn
                    .setBackgroundResource(R.drawable.background_large_image_orange);
        }
    }

    public void onClick(View view) {
        if (view == mDynamicBtn) {
            // 查看大图->动图|静图切换
            if (mDynamicBtn.getText().equals(
                    getBaseContext().getResources().getString(
                            R.string.dynamic_bmp))) {
                mDynamicBtn.setText("返回");
                mAdapter.showDynamic();
            } else {
                mDynamicBtn.setText(R.string.dynamic_bmp);
                mAdapter.showStill();
            }
        }
    }

    public void onShare(View view) {
        // 分享
        String urlImg = mUrlList.get(mViewPager.getCurrentItem());
        if (null != urlImg) {
            int index = urlImg.indexOf("+") + 1;
            urlImg = urlImg.substring(index);
        }
        ShareTask task = new ShareTask(ViewLargeImageActivity.this, urlImg,
                new ShareListener() {
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
        mUrlList.clear();
        mUrlList = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {// 记录旧状态
        super.onSaveInstanceState(outState);
        // 保存tag
        outState.putString(KEY_CUR_SEL, mViewPager.getCurrentItem() + "");
        // 保存list
        StringBuilder sb = new StringBuilder();
        int size = mUrlList.size();
        for (int i = 0; i < size; i++) {
            if (i < size - 1) {
                sb.append(mUrlList.get(i)).append(",");
            } else {
                sb.append(mUrlList.get(i));
            }
        }
        outState.putString(KEY_URL_LIST, sb.toString());
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

    private void setSelTitle(int select) {
        mTitleTV.setText("查看大图      " + select + "/" + mUrlList.size());
    }

    @Override
    public void onPageScrolled(int position, float positionOffset,
                               int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        setSelTitle(position + 1);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}

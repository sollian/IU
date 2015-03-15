
package com.aiyou.news;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aiyou.BaseActivity;
import com.aiyou.R;
import com.aiyou.news.adapter.NewsListAdapter;
import com.aiyou.news.utils.News;
import com.aiyou.news.utils.News.NewsType;
import com.aiyou.news.utils.NewsManager;
import com.aiyou.utils.ActivityFunc;
import com.aiyou.utils.AiYouManager;
import com.aiyou.utils.NetWorkManager;
import com.aiyou.utils.SwitchManager;
import com.aiyou.utils.FileCache.FileManager;
import com.aiyou.utils.http.HttpManager;
import com.aiyou.utils.logcat.Logcat;
import com.aiyou.utils.thread.ThreadUtils;

import external.ListviewAnimations.swinginadapters.AnimationAdapter;
import external.ListviewAnimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import external.OtherView.ActivitySplitAnimationUtil;
import external.OtherView.Win8ProgressBar;
import external.PullToRefresh.PullToRefreshBase;
import external.PullToRefresh.PullToRefreshListView;
import external.PullToRefresh.PullToRefreshBase.OnRefreshListener2;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

/**
 * 显示新闻列表的Activity
 * 
 * @author sollian
 */
public class NewsListActivity extends BaseActivity implements
        OnItemClickListener, OnRefreshListener2<ListView>, OnScrollListener {
    private static final String TAG = NewsListActivity.class.getSimpleName();

    private static final int MSG_ERROR = -1;
    protected static final int MSG_DOWNLOAD_ERROR = 0;
    private static final int MSG_LIST = 1;
    private static final int MSG_VIEW_CONTENT = 2;

    private static final String KEY_OPENSCREEN = "open_screen";
    public static final String KEY_DATA = "data";
    public static final String KEY_MODE = "mode";

    private SwitchManager mSwitchMgr;
    private HttpManager mHttpMgr;
    /**
     * 辅助变量
     */
    private int mCurrentItem = 1;
    private boolean mFlagLoadMore = true;
    // 存储新闻列表
    private List<News> mNewsList = new ArrayList<News>();
    // 当前页数
    private int mCurPage = 1;
    // 总页数
    private int mTotalPage = 1;
    // 列表的adapter
    private BaseAdapter mAdapter;
    /**
     * 新闻类型标志位
     */
    private NewsType mMode;
    /**
     * 是否应用开屏动画
     */
    private boolean mIsOpenScreen = true;
    /**
     * 控件
     */
    // 标题
    private LinearLayout mTitleLLayout;
    // 选择按钮
    private Button mNewsBtn, mInformBtn, mHeadlineBtn;
    // 列表
    private PullToRefreshListView mPTRListView;
    private ListView mListView;
    // 菜单按钮
    private FrameLayout mMenuFLayout;
    // 进度条
    private Win8ProgressBar mProgressBar;

    // 处理UI线程的handler
    public Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == MSG_LIST) {
                Bundle data = msg.getData();
                News news = (News) data.getSerializable(KEY_DATA);
                if (news != null) {
                    // 更新列表
                    mNewsList.addAll(news.list);
                    mAdapter.notifyDataSetChanged();
                }
                mFlagLoadMore = true;
            } else if (msg.what == MSG_VIEW_CONTENT) {
                Bundle data = msg.getData();
                News news = (News) data.getSerializable(KEY_DATA);
                if (news != null) {
                    if (news.content.equals(News.IS_FILE)) {
                        showFile(news.url);
                    } else {
                        Intent intent = new Intent(NewsListActivity.this,
                                NewsContentActivity.class);
                        intent.putExtra(KEY_DATA, news);
                        intent.putExtra(KEY_MODE, mMode);
                        ActivityFunc.startActivity(NewsListActivity.this, intent);
                    }
                }
            } else if (msg.what == MSG_DOWNLOAD_ERROR) {
                Toast.makeText(NewsListActivity.this, "文件下载失败",
                        Toast.LENGTH_SHORT).show();
            }
            else if (msg.what == MSG_ERROR) {
                if (mMode == NewsType.headline) {
                    Toast.makeText(NewsListActivity.this, NetWorkManager.MSG_NONET,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(NewsListActivity.this, NetWorkManager.MSG_NONET + "（暂不支持外网访问）",
                            Toast.LENGTH_SHORT).show();
                }
                mFlagLoadMore = true;
            }
            mPTRListView.onRefreshComplete();
            showProgress(false);
            return true;
        }
    });

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
        setContentView(R.layout.activity_news_list);
        int delay = 0;
        mIsOpenScreen = getIntent().getBooleanExtra(KEY_OPENSCREEN, true);
        if (mIsOpenScreen && ActivitySplitAnimationUtil.canPlay() && Build.VERSION.SDK_INT >= 14) {
            // 中心打开动画
            ActivitySplitAnimationUtil.prepareAnimation(this);
            ActivitySplitAnimationUtil.animate(this, 1000);
            delay = 1000;
        }

        // 获取新闻类型的标志位
        mMode = (NewsType) getIntent().getSerializableExtra(KEY_MODE);
        if (mMode == null) {
            mMode = NewsType.headline;
        }

        init();

        mHandler.postDelayed(new Runnable() {
            public void run() {
                /**
                 * 查询用户信息
                 */
                selectTag(mMode);
            }
        }, delay);
    }

    /**
     * 初始化控件
     */
    private void init() {
        mSwitchMgr = SwitchManager.getInstance(getBaseContext());
        mHttpMgr = HttpManager.getInstance(getBaseContext());

        mInformBtn = (Button) findViewById(R.id.activity_newslist_bt_inform);
        mNewsBtn = (Button) findViewById(R.id.activity_newslist_bt_news);
        mHeadlineBtn = (Button) findViewById(R.id.activity_newslist_bt_headline);

        mTitleLLayout = (LinearLayout) findViewById(R.id.activity_newslist_ll_title);

        mPTRListView = (PullToRefreshListView) findViewById(R.id.activity_newslist_lv);
        mPTRListView.setOnRefreshListener(this);
        mPTRListView.setShowIndicator(false);
        mListView = mPTRListView.getRefreshableView();
        mListView.setOnItemClickListener(this);
        mListView.setOnScrollListener(this);
        mAdapter = new NewsListAdapter(NewsListActivity.this, mNewsList);
        if (!mSwitchMgr.isSimpleModeEnabled() && Build.VERSION.SDK_INT >= 11) {
            // listview动画
            AnimationAdapter animAdapter = new SwingBottomInAnimationAdapter(
                    mAdapter);
            animAdapter.setAbsListView(mListView);
            mListView.setAdapter(animAdapter);
        } else {
            mListView.setAdapter(mAdapter);
        }
        /**
         * menu
         */
        mMenuFLayout = (FrameLayout) findViewById(R.id.activity_newslist_fl_menu);
        /**
         * 进度条
         */
        mProgressBar = (Win8ProgressBar) findViewById(R.id.progress_bar);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        News news = (News) parent.getItemAtPosition(position);
        if (!TextUtils.isEmpty(news.content)) {
            Message msg = mHandler.obtainMessage(MSG_VIEW_CONTENT);
            Bundle data = msg.getData();
            data.putSerializable(KEY_DATA, news);
            mHandler.sendMessage(msg);
        } else {
            if (isHtmlFile(news.url)) {
                if (!FileManager.checkSDCard()) {
                    Toast.makeText(getBaseContext(), "SD卡未安装或空间不足，无法下载文件", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                if (!showFile(news.url)) {
                    downloadFile(news);
                }
            } else {
                getNewsContent(news);
            }
        }
    }

    private void downloadFile(final News news) {
        showProgress(true);
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            if (null != mHandler) {
                mHandler.sendEmptyMessage(MSG_ERROR);
            }
            return;
        }

        ThreadUtils.execute(new Runnable() {
            @SuppressWarnings("deprecation")
            @Override
            public void run() {
                String dir = Environment.getExternalStorageDirectory().toString()
                        + FileManager.DIR_FILE;
                File folder = new File(dir);
                if (!folder.exists()) {
                    folder.mkdir();
                }
                String[] strs = news.url.split("/");
                int length = strs.length;
                String fileName = strs[length - 1];
                dir += "/" + fileName;
                // 重新编码地址（可能含有中文）
                news.url = "";
                for (int i = 0; i < length - 1; i++) {
                    news.url += strs[i] + "/";
                }
                news.url += URLEncoder.encode(strs[length - 1]);
                FileOutputStream fos = null;
                try {
                    File file = new File(dir);
                    if (file.exists()) {
                    }
                    file.createNewFile();
                    fos = new FileOutputStream(file);

                    byte[] buffer = HttpManager.getInstance(getBaseContext()).getHttpByte(
                            getBaseContext(),
                            news.url);
                    if (buffer != null) {
                        fos.write(buffer, 0, buffer.length);
                    }
                    news.content = News.IS_FILE;
                    if (mHandler != null) {
                        Message msg = mHandler.obtainMessage(MSG_VIEW_CONTENT);
                        Bundle data = msg.getData();
                        data.putSerializable(KEY_DATA, news);
                        mHandler.sendMessage(msg);
                        return;
                    }
                } catch (FileNotFoundException e) {
                    Logcat.e(TAG, "downloadFile FileNotFoundException");
                } catch (IOException e) {
                    Logcat.e(TAG, "downloadFile IOException");
                } finally {
                    FileManager.close(fos);
                }
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(MSG_DOWNLOAD_ERROR);
                }
            }
        });

    }

    private void getNewsContent(final News news) {
        showProgress(true);
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            if (null != mHandler) {
                mHandler.sendEmptyMessage(MSG_ERROR);
            }
            return;
        }
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                String strInfo = mHttpMgr.getHttp(getBaseContext(), news.url);
                if (TextUtils.isEmpty(strInfo)) {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(MSG_ERROR);
                    }
                    return;
                }
                String content = null;
                if (mMode == NewsType.headline) {
                    // 北邮要闻
                    content = NewsManager.getHeadlineContent(strInfo);
                } else {
                    // 信息门户
                    content = NewsManager.getNewsContent(strInfo);
                }
                if (null != mHandler) {
                    if (content != null) {
                        news.content = content;
                        Message msg = mHandler.obtainMessage(MSG_VIEW_CONTENT);
                        Bundle data = msg.getData();
                        data.putSerializable(KEY_DATA, news);
                        mHandler.sendMessage(msg);
                    } else {
                        mHandler.sendEmptyMessage(MSG_ERROR);
                    }
                }
            }
        });
    }

    /**
     * 获取新闻列表
     */
    private void getNewsList(boolean showProgress) {
        if (showProgress) {
            showProgress(true);
        }
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            if (null != mHandler) {
                mHandler.sendEmptyMessage(MSG_ERROR);
            }
            return;
        }
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                String url = null;
                switch (mMode) {
                    case inform:
                        url = NewsManager.URL_INFO_INFORM;
                        break;
                    case news:
                        url = NewsManager.URL_INFO_NEWS;
                        break;
                    case headline:
                        url = NewsManager.URL_HEADLINE;
                        break;
                }
                Logcat.e(TAG, "page:" + mCurPage);
                String strInfo = mHttpMgr.getHttp(getBaseContext(), url + mCurPage);
                if (TextUtils.isEmpty(strInfo)) {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(MSG_ERROR);
                    }
                    return;
                }
                News news = null;
                getTotalPage(strInfo);
                if (mMode == NewsType.headline) {
                    news = NewsManager.getHeadlineTitle(strInfo);
                } else {
                    news = NewsManager.getNewsTitle(strInfo);
                }
                if (null != mHandler) {
                    if (null != news && news.list != null) {
                        Message msg = mHandler.obtainMessage(MSG_LIST);
                        Bundle data = msg.getData();
                        data.putSerializable(KEY_DATA, news);
                        mHandler.sendMessage(msg);
                    } else {
                        mHandler.sendEmptyMessage(MSG_ERROR);
                    }
                }
            }
        });
    }

    public void onClick(View view) {
        final int nId = view.getId();
        if (R.id.activity_newslist_iv_menu == nId) {
            // 打开菜单
            showPopMenu(true);
        } else if (R.id.activity_newslist_menu_iv_back == nId
                || R.id.activity_newslist_fl_menu == nId) {
            // 关闭菜单
            showPopMenu(false);
        } else if (R.id.activity_newslist_bt_inform == nId) {
            // 校园通告
            selectTag(NewsType.inform);
        } else if (R.id.activity_newslist_bt_news == nId) {
            // 校园新闻
            selectTag(NewsType.news);
        } else if (R.id.activity_newslist_bt_headline == nId) {
            // 北邮要闻
            selectTag(NewsType.headline);
        } else if (R.id.activity_newslist_menu_bt_night == nId) {
            // 日间|夜间模式切换
            if (mSwitchMgr.isNightModeEnabled()) {
                mSwitchMgr.enableNightMode(false);
            } else {
                mSwitchMgr.enableNightMode(true);
            }
            mIsOpenScreen = false;
            finish();
            Intent intent = new Intent(NewsListActivity.this,
                    NewsListActivity.class);
            intent.putExtra(KEY_MODE, mMode);
            intent.putExtra(KEY_OPENSCREEN, mIsOpenScreen);
            ActivityFunc.startActivity(this, intent);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 按下键盘上返回按键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (View.VISIBLE == mMenuFLayout.getVisibility()) {
                showPopMenu(false);
                return true;
            }
            selfFinish(null);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (View.VISIBLE == mMenuFLayout.getVisibility()) {
                showPopMenu(false);
            } else {
                showPopMenu(true);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mNewsList.clear();
        mNewsList = null;
        ActivitySplitAnimationUtil.cancel();
        System.gc();
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        refresh();
    }

    private void refresh() {
        clearListView();
        getNewsList(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        if (!loadMore(true)) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mPTRListView.onRefreshComplete();
                }
            });
        }
    }

    private boolean loadMore(boolean showProgress) {
        if (!mFlagLoadMore) {
            return false;
        }
        mFlagLoadMore = false;
        if (mCurPage < mTotalPage) {
            mCurPage++;
            getNewsList(showProgress);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem <= 1) {
            mCurrentItem = 1;
            mTitleLLayout.clearAnimation();
            mTitleLLayout.setTag(null);
            if (View.VISIBLE != mTitleLLayout.getVisibility()) {
                showTitle(true);
            }
        } else {
            if (mTitleLLayout.getTag() == null) {
                if (mCurrentItem < firstVisibleItem
                        && View.VISIBLE == mTitleLLayout.getVisibility()) {
                    showTitle(false);
                } else if (mCurrentItem > firstVisibleItem
                        && View.VISIBLE != mTitleLLayout.getVisibility()) {
                    showTitle(true);
                }
                mCurrentItem = firstVisibleItem;
            }
        }
        // 自动加载
        if (firstVisibleItem + visibleItemCount * 2 >= totalItemCount) {
            loadMore(false);
        }
    }

    /**
     * 显示/隐藏Menu
     * 
     * @param flag
     */
    @SuppressWarnings("deprecation")
    private void showPopMenu(boolean flag) {
        Animation anim = null;
        if (flag) {
            if (View.VISIBLE == mMenuFLayout.getVisibility()) {
                return;
            }
            if (mSwitchMgr.isSimpleModeEnabled()) {
                if (mSwitchMgr.isNightModeEnabled()) {
                    mMenuFLayout.setBackgroundColor(getResources().getColor(
                            R.color.news_background_night));
                } else {
                    mMenuFLayout.setBackgroundColor(getResources().getColor(
                            R.color.news_background_day));
                }
            } else {
                Drawable drawable = AiYouManager.getBlurBg(NewsListActivity.this);
                if (null != drawable) {
                    mMenuFLayout.setBackgroundDrawable(drawable);
                } else {
                    if (mSwitchMgr.isNightModeEnabled()) {
                        mMenuFLayout.setBackgroundColor(getResources().getColor(
                                R.color.news_background_night));
                    } else {
                        mMenuFLayout.setBackgroundColor(getResources().getColor(
                                R.color.news_background_day));
                    }
                }
            }
            anim = AnimationUtils.loadAnimation(getBaseContext(),
                    android.R.anim.fade_in);
            mMenuFLayout.setVisibility(View.VISIBLE);
        } else {
            if (View.VISIBLE != mMenuFLayout.getVisibility()) {
                return;
            }
            anim = AnimationUtils.loadAnimation(getBaseContext(),
                    android.R.anim.fade_out);
            anim.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mMenuFLayout.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
        if (null != anim) {
            mMenuFLayout.startAnimation(anim);
        }
    }

    /**
     * 是否显示标题栏
     * 
     * @param flag
     */
    private void showTitle(final boolean flag) {
        Animation anim;
        int duration = 1000;
        mTitleLLayout.setTag("anim");
        if (flag) {
            anim = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_top);
            anim.setDuration(duration);
            mTitleLLayout.setVisibility(View.VISIBLE);
            mTitleLLayout.startAnimation(anim);
        } else {
            anim = AnimationUtils.loadAnimation(this, R.anim.slide_out_to_top);
            anim.setDuration(duration);
            mTitleLLayout.startAnimation(anim);
        }
        anim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                if (!flag) {
                    mTitleLLayout.setVisibility(View.GONE);
                }
                mTitleLLayout.setTag(null);
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationStart(Animation arg0) {
            }
        });
    }

    /**
     * 重置listview
     */
    private void clearListView() {
        mFlagLoadMore = false;
        mCurPage = 1;
        mTotalPage = 1;
        mNewsList.clear();
        mAdapter.notifyDataSetChanged();
    }

    private void getTotalPage(String strHtml) {
        if (TextUtils.isEmpty(strHtml)) {
            return;
        }
        Pattern p;
        Matcher m;
        switch (mMode) {
            case inform:
            case news:
                p = Pattern.compile("分<b>(.*?)</b>页");
                m = p.matcher(strHtml);
                while (m.find()) {
                    MatchResult mr = m.toMatchResult();
                    try {
                        mTotalPage = Integer.parseInt(mr.group(1));
                    } catch (NumberFormatException e) {
                        Logcat.e(TAG, "getTotalPage NumberFormatException 1");
                    }
                }
                break;
            case headline:
                p = Pattern.compile("下一页.*?href=\"(.*?)\">最后一页");
                m = p.matcher(strHtml);
                while (m.find()) {
                    MatchResult mr = m.toMatchResult();
                    String page = mr.group(1);
                    String[] arr = page.split("_");
                    try {
                        mTotalPage = Integer.parseInt(arr[arr.length - 1]);
                    } catch (NumberFormatException e) {
                        Logcat.e(TAG, "getTotalPage NumberFormatException 2");
                    }
                }
                break;
        }
    }

    private void selectTag(NewsType type) {
        mHttpMgr.disconnect(getBaseContext());
        mMode = type;
        refresh();
        switch (type) {
            case headline:
                if (!mSwitchMgr.isNightModeEnabled()) {
                    mInformBtn
                            .setBackgroundResource(R.drawable.background_news_title_left_unpressed_day);
                    mNewsBtn.setBackgroundResource(R.drawable.background_news_title_middle_unpressed_day);
                    mHeadlineBtn
                            .setBackgroundResource(R.drawable.background_news_title_right_pressed_day);
                } else {
                    mInformBtn
                            .setBackgroundResource(R.drawable.background_news_title_left_unpressed_night);
                    mNewsBtn.setBackgroundResource(R.drawable.background_news_title_middle_unpressed_night);
                    mHeadlineBtn
                            .setBackgroundResource(R.drawable.background_news_title_right_pressed_night);
                }
                break;
            case inform:
                if (!mSwitchMgr.isNightModeEnabled()) {
                    mInformBtn
                            .setBackgroundResource(R.drawable.background_news_title_left_pressed_day);
                    mNewsBtn.setBackgroundResource(R.drawable.background_news_title_middle_unpressed_day);
                    mHeadlineBtn
                            .setBackgroundResource(R.drawable.background_news_title_right_unpressed_day);
                } else {
                    mInformBtn
                            .setBackgroundResource(R.drawable.background_news_title_left_pressed_night);
                    mNewsBtn.setBackgroundResource(R.drawable.background_news_title_middle_unpressed_night);
                    mHeadlineBtn
                            .setBackgroundResource(R.drawable.background_news_title_right_unpressed_night);
                }
                break;
            case news:
                if (!mSwitchMgr.isNightModeEnabled()) {
                    mInformBtn
                            .setBackgroundResource(R.drawable.background_news_title_left_unpressed_day);
                    mNewsBtn.setBackgroundResource(R.drawable.background_news_title_middle_pressed_day);
                    mHeadlineBtn
                            .setBackgroundResource(R.drawable.background_news_title_right_unpressed_day);
                } else {
                    mInformBtn
                            .setBackgroundResource(R.drawable.background_news_title_left_unpressed_night);
                    mNewsBtn.setBackgroundResource(R.drawable.background_news_title_middle_pressed_night);
                    mHeadlineBtn
                            .setBackgroundResource(R.drawable.background_news_title_right_unpressed_night);
                }
                break;
        }
    }

    /**
     * 关闭Activity
     * 
     * @param view
     */
    public void selfFinish(View view) {
        if (Build.VERSION.SDK_INT >= 14) {
            ActivitySplitAnimationUtil.finish(this);
        } else {
            scrollToFinishActivity();
        }
    }

    /**
     * 设置cpb_progress的状态和是否显示
     * 
     * @param flag
     */
    private void showProgress(boolean flag) {
        if (flag) {
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.start();
        } else {
            mProgressBar.setVisibility(View.GONE);
            mProgressBar.stop();
        }
    }

    /**
     * 判断url地址是否为pdf/doc/xls/ppt/txt文件地址
     * 
     * @param urlPath
     * @return
     */
    @SuppressLint("DefaultLocale")
    private boolean isHtmlFile(String urlPath) {
        String url = urlPath.toLowerCase();
        if (url.contains(".pdf") || url.contains(".doc")
                || url.contains(".docx") || url.contains(".xls")
                || url.contains(".xlsx") || url.contains(".ppt")
                || url.contains(".pptx") || url.contains(".txt")) {
            return true;
        }
        return false;
    }

    /**
     * 将下载的文件交给其他应用来显示
     * 
     * @param fileURL 文件的网络地址
     */
    @SuppressLint("DefaultLocale")
    private boolean showFile(String fileURL) {
        String dir = Environment.getExternalStorageDirectory().toString()
                + FileManager.DIR_FILE;
        File folder = new File(dir);
        if (!folder.exists()) {
            return false;
        }
        String[] strs = fileURL.split("/");
        dir += "/" + strs[strs.length - 1];
        File file = new File(dir);
        if (!file.exists()) {
            return false;
        }
        String[] arr = fileURL.split("\\.");
        String type = arr[arr.length - 1].toLowerCase();
        if (type == "pdf") {
            type = "application/pdf";
        } else if (type.contains("doc")) {
            type = "application/msword";
        } else if (type.contains("xls")) {
            type = "application/vnd.ms-excel";
        } else if (type.contains("ppt")) {
            type = "application/vnd.ms-powerpoint";
        } else {
            type = "text/plain";
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(file);
        intent.setDataAndType(uri, type);
        ActivityFunc.startActivity(this, intent);
        return true;
    }
}

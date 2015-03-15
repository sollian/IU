
package com.aiyou.bbs;

import java.util.ArrayList;

import com.aiyou.BaseActivity;
import com.aiyou.R;
import com.aiyou.bbs.bean.Article;
import com.aiyou.bbs.bean.Board;
import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.utils.ActivityFunc;
import com.aiyou.utils.AiYouManager;
import com.aiyou.utils.JsonHelper;
import com.aiyou.utils.NetWorkManager;
import com.aiyou.utils.SwitchManager;
import com.aiyou.utils.FileCache.FileManager;
import com.aiyou.utils.http.HttpManager;
import com.aiyou.utils.thread.ThreadUtils;
import com.aiyou.view.DarkImageView;

import external.GifImageViewEx.net.frakbot.imageviewex.ImageViewEx;
import external.OtherView.CircleImageView;
import external.OtherView.Win8ProgressBar;
import external.PullToRefresh.PullToRefreshBase;
import external.PullToRefresh.PullToRefreshListView;
import external.PullToRefresh.PullToRefreshBase.OnRefreshListener2;
import external.foldablelist.item.Painting;
import external.foldablelist.item.PaintingsAdapter;
import external.foldablelist.item.PaintingsAdapter.OpenDetailsListener;
import external.foldablelist.lib.UnfoldableView;
import external.foldablelist.lib.UnfoldableView.OnFoldingListener;
import external.foldablelist.lib.shading.GlanceFoldShading;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebSettings.PluginState;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 贴图秀
 * 
 * @author sollian
 */
public class BBSPhotoShowActivity extends BaseActivity implements
        OnRefreshListener2<ListView>, OnFoldingListener, OpenDetailsListener {
    private static final int MSG_OK = 0;
    private static final int MSG_ERROR = -1;
    private static final String KEY_DATA = "data";
    private SwitchManager mSwitchMgr;
    private BBSManager mBBSMgr;
    /**
     * painting
     */
    private Painting mPainting;
    /**
     * flag
     */
    private boolean mFlagLoad = true;
    private boolean mFlagFolded = true;
    /**
     * 
     */
    private ArrayList<ImageViewEx> mIVEList = new ArrayList<ImageViewEx>();
    /**
     * adapter
     */
    private PaintingsAdapter mAdapter;
    private ArrayList<Painting> mList = new ArrayList<Painting>();
    private Board mBoard;
    /**
     * 控件
     */
    private UnfoldableView mUnfoldableView;
    private View mView;
    private LinearLayout mDetailsLLayout;

    private PullToRefreshListView mPTRLV;
    private ListView mListView;
    // 进度条
    private Win8ProgressBar mProgressBar;
    // 帮助
    private ImageView mHelpIV;

    private Handler mHandler = new Handler(new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            if (msg.what == MSG_OK) {
                if (mFlagLoad) {
                    mFlagLoad = false;
                    // 内容全部获取
                    for (int i = 0; i < mBoard.articles.length; i++) {
                        if (mBoard.articles[i].is_top
                                || mBoard.articles[i].content == null) {
                            continue;
                        }
                        Painting painting = new Painting(getBaseContext(),
                                mBoard.articles[i]);
                        if (painting.getImageUrl() == null) {
                            continue;
                        }
                        mList.add(painting);
                    }
                    mAdapter.notifyDataSetChanged();
                }
            } else if (msg.what == MSG_ERROR) {
                Bundle data = msg.getData();
                String strError = data.getString(KEY_DATA);
                if (TextUtils.isEmpty(strError)) {
                    strError = NetWorkManager.MSG_NONET;
                }
                Toast.makeText(getBaseContext(), strError, Toast.LENGTH_SHORT)
                        .show();
            }
            mPTRLV.onRefreshComplete();
            showProgress(false);
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSwitchMgr = SwitchManager.getInstance(getBaseContext());
        if (mSwitchMgr.isNightModeEnabled()) {
            // 夜间模式
            this.setTheme(R.style.ThemeNight);
        } else {
            // 日间模式
            this.setTheme(R.style.ThemeDay);
        }
        setContentView(R.layout.activity_bbs_photoshow);

        init();

        threadGetList(1);
    }

    /**
     * 获取贴图列表的线程
     * 
     * @param page 页数
     */
    private void threadGetList(final int page) {
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            if (null != mHandler) {
                mHandler.sendEmptyMessage(MSG_ERROR);
            }
            return;
        }
        showProgress(true);
        mFlagLoad = true;
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                String strJson = Board.getBoard(BBSPhotoShowActivity.this, "Picture", page);
                if (TextUtils.isEmpty(strJson)) {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(MSG_ERROR);
                    }
                    return;
                }
                // 获取内容
                // 检查返回的是否是错误信息
                String strError = JsonHelper.checkError(strJson);
                if (null != strError) {
                    // 是 错误信息
                    if (mHandler != null) {
                        Message msg = mHandler.obtainMessage(MSG_ERROR);
                        Bundle data = msg.getData();
                        data.putString(KEY_DATA, strError);
                        mHandler.sendMessage(msg);
                    }
                    return;
                }
                mBoard = new Board(strJson);
                if (mBoard.articles == null) {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(MSG_ERROR);
                    }
                    return;
                }
                for (int i = 0; i < mBoard.articles.length; i++) {
                    if (mBoard.articles[i].is_top) {
                        continue;
                    }
                    GetContentTask task = new GetContentTask(i,
                            mBoard.articles[i]);
                    ThreadUtils.execute(task);
                }
            }
        });
    }

    private void init() {
        mBBSMgr = BBSManager.getInstance(getBaseContext());
        /**
         * 控件
         */
        mUnfoldableView = (UnfoldableView) findViewById(R.id.activity_bbs_photoshow_uv);
        Bitmap glance = ((BitmapDrawable) getResources().getDrawable(
                R.drawable.foldable_unfold_glance)).getBitmap();
        mUnfoldableView.setFoldShading(new GlanceFoldShading(this, glance));
        mUnfoldableView.setOnFoldingListener(this);

        mView = (View) findViewById(R.id.activity_bbs_photoshow_view);
        mView.setClickable(false);

        mDetailsLLayout = (LinearLayout) findViewById(R.id.activity_bbs_photoshow_ll_details);
        mDetailsLLayout.setVisibility(View.INVISIBLE);

        mPTRLV = (PullToRefreshListView) findViewById(R.id.activity_bbs_photoshow_lv);
        mPTRLV.setOnRefreshListener(this);
        mPTRLV.setShowIndicator(false);
        mListView = mPTRLV.getRefreshableView();
        mAdapter = new PaintingsAdapter(this, mList, this);
        mListView.setAdapter(mAdapter);
        // 进度条
        mProgressBar = (Win8ProgressBar) findViewById(R.id.progress_bar);
        // 帮助
        mHelpIV = (ImageView) findViewById(R.id.activity_bbs_photoshow_iv_help);
    }

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
     * 查看帖子内容
     * 
     * @param coverView
     * @param painting
     */
    public void openDetails(View coverView, Painting painting) {
        this.mPainting = painting;
        mDetailsLLayout.setBackgroundColor(painting.getColor());
        mDetailsLLayout.setId(painting.getColor());
        mUnfoldableView.unfold(coverView, mDetailsLLayout);
    }

    public void onClick(View v) {
        int nId = v.getId();
        if (nId == R.id.activity_bbs_photoshow_iv_go_source) {
            /**
             * 打开Article对应的Threads
             */
            Intent intent = new Intent(this, BBSContentActivity.class);
            intent.putExtra(BBSContentActivity.KEY_ARTICLE, mPainting.getArticle());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ActivityFunc.startActivity(this, intent);
        } else if (nId == R.id.activity_bbs_photoshow_civ_face) {
            // 用户信息
            Article article = mPainting.getArticle();
            if (null != article.user) {
                ActivityFunc.startActivity(this, BBSUserInfoActivity.class,
                        article.user, false);
            }
        } else if (v == mHelpIV) {
            mHelpIV.setVisibility(View.GONE);
        }
    }

    /**
     * 装载内容
     * 
     * @param painting
     */
    private void loadContent(Painting painting) {
        CircleImageView civ = (CircleImageView) mDetailsLLayout
                .findViewById(R.id.activity_bbs_photoshow_civ_face);
        TextView tv_author = (TextView) mDetailsLLayout
                .findViewById(R.id.activity_bbs_photoshow_tv_author);
        TextView tv_date = (TextView) mDetailsLLayout
                .findViewById(R.id.activity_bbs_photoshow_tv_date);
        LinearLayout ll_content = (LinearLayout) mDetailsLLayout
                .findViewById(R.id.activity_bbs_photoshow_ll_content);
        // 设置头像
        if (!mSwitchMgr.isFaceEnabled()) {
            civ.setVisibility(View.GONE);
        } else {
            civ.setVisibility(View.VISIBLE);
            String url = painting.getFaceUrl();
            if (null != url) {
                civ.setImageUrl(url, R.drawable.iu_default_gray,
                        R.drawable.iu_default_green);
            } else {
                civ.setImageResource(R.drawable.iu_default_green);
            }
        }
        // 设置作者
        tv_author.setText(painting.getAuthor());
        // 设置日期
        tv_date.setText(painting.getDate());
        // 设置内容
        ll_content.removeAllViews();
        processContent(ll_content, JsonHelper.toHtml(painting.getArticle(), true));
    }

    /**
     * 显示内容
     * 
     * @param ll
     * @param html
     */
    private void processContent(LinearLayout ll, String[] html) {
        String[] array = html[0].split("<image");
        int length = array.length;

        String str = null;
        int index = 0;
        String strImg = null;
        String strHtml = null;
        for (int i = 0; i < length; i++) {
            str = array[i].trim();
            if (str.startsWith("=")) {
                index = str.indexOf(">");
                if (index > 0) {
                    strImg = str.substring(1, index);
                    // 图片
                    processImage(ll, strImg);
                    strHtml = str.substring(index + 1).trim();
                    if (!"".equals(strHtml)) {
                        // 网页
                        processWebView(ll, strHtml);
                    }
                } else {
                    // 网页
                    processWebView(ll, str);
                }
            } else {
                // 网页
                processWebView(ll, str);
            }
        }
        ll.invalidate();
    }

    /**
     * 显示图片
     * 
     * @param ll
     * @param url
     */
    private void processImage(LinearLayout ll, String url) {
        LinearLayout lLayout = new LinearLayout(this);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        lLayout.setLayoutParams(params);
        lLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        ll.addView(lLayout);
        GetImageTask task = new GetImageTask(lLayout, url);
        task.execute();
    }

    /**
     * 显示webview
     * 
     * @param ll
     * @param html
     */
    @SuppressWarnings("deprecation")
    @SuppressLint("SetJavaScriptEnabled")
    private void processWebView(LinearLayout ll, String html) {
        html = html.trim().replaceAll("\n", "<br/>");
        html = "<body style=\"text-align:justify;text-justify:distribute-all-lines;\">"
                + html + "</body>";
        if (mSwitchMgr.isNightModeEnabled()) {
            html = "<style type=\"text/css\">body{color:#888888}a:link{color:#00aaaa}</style>"
                    + html;
        } else {
            html = "<style type=\"text/css\">body{color:#000000}</style>"
                    + html;
        }
        WebView wv = new WebView(this);
        WebSettings setting = wv.getSettings();
        setting.setJavaScriptEnabled(true);
        setting.setAllowFileAccess(true);
        setting.setAppCacheEnabled(true);
        setting.setLoadsImagesAutomatically(true);
        setting.setPluginState(PluginState.ON);
        // 设置缩放比例
        wv.setInitialScale(mBBSMgr.getWebViewScaleSize());
        // 设置背景色
        wv.setBackgroundColor(mDetailsLLayout.getId());
        // 水平滚动条不显示
        wv.setHorizontalScrollBarEnabled(false);
        wv.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
        ll.addView(wv);
    }

    @Override
    public void onBackPressed() {
        if (mUnfoldableView != null
                && (mUnfoldableView.isUnfolded() || mUnfoldableView.isUnfolding())) {
            mUnfoldableView.foldBack();
            return;
        }
        scrollToFinishActivity();
        super.onBackPressed();
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        mList.clear();
        mAdapter.notifyDataSetChanged();
        threadGetList(1);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        if (mBoard == null || mBoard.pagination == null) {
            completeRefresh();
            return;
        }
        int page = mBoard.pagination.page_current_count + 1;
        if (page > mBoard.pagination.page_all_count) {
            completeRefresh();
        } else {
            threadGetList(page);
        }
    }

    private void completeRefresh() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mPTRLV.onRefreshComplete();
            }
        });
    }

    @Override
    public void onUnfolding(UnfoldableView unfoldableView) {
        mView.setClickable(true);
        mDetailsLLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onUnfolded(UnfoldableView unfoldableView) {
        mView.setClickable(false);
        if (mFlagFolded) {
            mFlagFolded = false;
            loadContent(mPainting);
            if (mSwitchMgr.needShowPhotoShowHelp()) {
                mHelpIV.setVisibility(View.VISIBLE);
                mSwitchMgr.disableShowPhotoShowHelp();
            }
        }
    }

    @Override
    public void onFoldingBack(UnfoldableView unfoldableView) {
        mView.setClickable(true);
    }

    @Override
    public void onFoldedBack(UnfoldableView unfoldableView) {
        mView.setClickable(false);
        mDetailsLLayout.setVisibility(View.INVISIBLE);
        ((LinearLayout) mDetailsLLayout
                .findViewById(R.id.activity_bbs_photoshow_ll_content))
                .removeAllViews();
        mIVEList.clear();
        mFlagFolded = true;
    }

    @Override
    public void onFoldProgress(UnfoldableView unfoldableView, float progress) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (ImageViewEx ive : mIVEList) {
            if (ive != null && !ive.isPlaying() && ive.canPlay()) {
                ive.play();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        for (ImageViewEx ive : mIVEList) {
            if (ive != null && ive.isPlaying()) {
                ive.stop();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mIVEList.clear();
        mList.clear();
        System.gc();
    }

    /**
     * 获取帖子内容的任务类
     * 
     * @author sollian
     */
    class GetContentTask implements Runnable {
        private Article article;
        private int position = 0;

        public GetContentTask(int position, Article article) {
            this.position = position;
            this.article = article;
        }

        @Override
        public void run() {
            String strJson = Article
                    .getArticle(BBSPhotoShowActivity.this, article.board_name, article.id);
            if (TextUtils.isEmpty(strJson)) {
                return;
            }
            // 获取内容
            // 检查返回的是否是错误信息
            String strError = JsonHelper.checkError(strJson);
            if (null != strError) {
                // 是 错误信息
                return;
            }
            article = new Article(strJson);
            mBoard.articles[position] = article;
            boolean flagGo = true;
            for (int i = 0; i < mBoard.articles.length; i++) {
                if (mBoard.articles[i].is_top) {
                    continue;
                }
                if (mBoard.articles[i].content == null) {
                    flagGo = false;
                }
            }
            if (flagGo && mHandler != null) {
                mHandler.sendEmptyMessage(MSG_OK);
            }
        }
    }

    /**
     * 获取大图片的异步线程类
     * 
     * @author sollian
     */
    class GetImageTask extends AsyncTask<Void, Integer, byte[]> {
        private LinearLayout ll = null;
        private String url = null;
        private DarkImageView iv = null;

        private FileManager imgch = new FileManager(FileManager.DIR_LARGEIMG);

        public GetImageTask(LinearLayout ll, String url) {
            this.ll = ll;
            this.url = url;
            iv = new DarkImageView(getBaseContext());
            iv.setAdjustViewBounds(true);
            iv.setImageResource(R.drawable.iu_default_green);
            ll.addView(iv);
        }

        @Override
        protected byte[] doInBackground(Void... params) {
            byte[] data = null;
            try {
                // 判断是否是论坛图片
                String strUrl = url;
                if (strUrl.endsWith("middle") || strUrl.endsWith("small")) {
                    strUrl = strUrl.substring(0, strUrl.lastIndexOf('/'));
                }
                if (mHandler == null) {
                    return null;
                }
                // 优先从SD卡加载图片
                data = imgch.getImage(strUrl);
                if (mHandler == null) {
                    return null;
                }
                // 若不存在，则从网络下载，然后存储到本地
                if (data == null) {
                    try {
                        data = getBmpByUrl(strUrl);
                    } catch (OutOfMemoryError e) {
                        return null;
                    }
                    imgch.saveWebBmpToSd(data, strUrl);
                }
            } catch (Exception e) {

                return null;
            }
            if (mHandler == null) {
                return null;
            }
            return data;
        }

        @Override
        protected void onPostExecute(byte[] result) {
            if (result != null) {
                if (mHandler == null) {
                    return;
                }
                Options option = new Options();
                option.inJustDecodeBounds = true;
                Bitmap bmp = BitmapFactory.decodeByteArray(result, 0,
                        result.length, option);
                int height = option.outHeight;
                int width = option.outWidth;
                height = height * AiYouManager.getScreenWidth() / width;
                width = AiYouManager.getScreenWidth();
                LayoutParams param = new LayoutParams(width, height);

                option.inJustDecodeBounds = false;
                if (option.outHeight > 1000 || option.outWidth > 1000) {
                    int scalew = (int) (option.outWidth / 1000.0);
                    int scaleh = (int) (option.outHeight / 1000.0);
                    option.inSampleSize = Math.max(scalew, scaleh) + 1;
                    bmp = BitmapFactory.decodeByteArray(result, 0,
                            result.length, option);
                    iv.setImageBitmap(bmp);
                    return;
                }
                if (mHandler == null) {
                    return;
                }
                try {
                    ImageViewEx ive = new ImageViewEx(getBaseContext());
                    ive.setLayoutParams(param);
                    ive.setAdjustViewBounds(true);
                    ive.setSource(result);
                    mIVEList.add(ive);
                    ll.removeAllViews();
                    ll.addView(ive);
                } catch (OutOfMemoryError e) {

                    bmp = BitmapFactory.decodeByteArray(result, 0,
                            result.length, option);
                    iv.setImageBitmap(bmp);
                }
            } else {
                if (mHandler == null) {
                    return;
                }
                iv.setImageResource(R.drawable.iu_default_gray);
            }
        }

        /**
         * 获取图片的方法
         * 
         * @param url
         * @return
         * @throws Exception
         */
        public byte[] getBmpByUrl(String url) {
            // 判断是否是论坛图片
            if (url.contains(BBSManager.API_HEAD)) {
                url += BBSManager.FORMAT + "?appkey=" + BBSManager.APPKEY;
            }
            byte[] data = HttpManager.getInstance(getBaseContext()).getHttpByte(getBaseContext(),
                    url);
            return data;
        }
    }

    @Override
    public void onOpenDetails(View v, Painting item) {
        openDetails(v, item);
    }

}

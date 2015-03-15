
package com.aiyou.bbs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.aiyou.BaseActivity;
import com.aiyou.R;
import com.aiyou.bbs.FaceViewListener.MyGridViewListener;
import com.aiyou.bbs.FaceViewListener.MyOnPageChangeListener;
import com.aiyou.bbs.adapter.FaceGridViewAdapter;
import com.aiyou.bbs.adapter.PageListAdapter;
import com.aiyou.bbs.adapter.ViewPagerAdapter;
import com.aiyou.bbs.bean.Article;
import com.aiyou.bbs.bean.Refer;
import com.aiyou.bbs.bean.Threads;
import com.aiyou.bbs.bean.User;
import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.utils.ActivityFunc;
import com.aiyou.utils.AiYouManager;
import com.aiyou.utils.JsonHelper;
import com.aiyou.utils.NetWorkManager;
import com.aiyou.utils.SwitchManager;
import com.aiyou.utils.share.ShareTask;
import com.aiyou.utils.thread.ThreadUtils;
import com.aiyou.utils.time.TimeUtils;
import com.aiyou.view.ControlScrollViewPager;
import com.aiyou.view.CustomDialog;
import com.aiyou.view.DarkImageView;
import com.aiyou.view.GetScrollDistanceScrollView;
import com.aiyou.view.GetScrollDistanceScrollView.OnScrollListener;
import com.aiyou.view.ScrollTextView;
import com.aiyou.viewLargeImage.ViewLargeImageActivity;

import external.ArcMenu.RayMenu;
import external.GifImageViewEx.net.frakbot.imageviewex.Converters;
import external.GifImageViewEx.net.frakbot.imageviewex.ImageViewEx;
import external.OtherView.CircleImageView;
import external.OtherView.SizeAdjustingTextView;
import external.OtherView.Win8ProgressBar;
import external.PullToRefresh.PullToRefreshBase;
import external.PullToRefresh.PullToRefreshMyScrollView;
import external.PullToRefresh.PullToRefreshBase.Mode;
import external.PullToRefresh.PullToRefreshBase.OnRefreshListener2;
import external.SmartImageView.SmartImageView;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebSettings.PluginState;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

/**
 * 查看文章内容
 * 
 * @author sollian
 */
@SuppressWarnings("deprecation")
public class BBSContentActivity extends BaseActivity implements
        OnRefreshListener2<GetScrollDistanceScrollView>, OnItemClickListener,
        OnItemLongClickListener, OnTouchListener, OnScrollListener {
    public static final String KEY_ARTICLE = "article";
    public static final String KEY_REFER = "refer";

    private static final int REQUESTCODE_SECTION = 0x1113;
    private static final int MSG_CONTENT_ARTICLE = 0;
    private static final int MSG_OPERATION = 1;
    private static final int MSG_ARTICLE_SEND = 2;
    private static final int MSG_FORWARD_CROSS = 3;
    private static final int MSG_ERROR = -1;
    private static final String KEY_DATA = "data";

    private SwitchManager mSwitchMgr;
    private AiYouManager mIUMgr;
    private BBSManager mBBSMgr;
    /**
     * 主题帖
     */
    private Threads mThreads = null;
    // 保存上一页内容
    private List<Threads> mHistoryList = new ArrayList<Threads>();
    /**
     * 存放含有音频的webview
     */
    private List<WebView> mWebViewList = new ArrayList<WebView>();
    /**
     * 只看此ID存放的用户ID
     */
    private String mThisID = null;
    /**
     * 是否显示Refer
     */
    private boolean mIsRefer = false;
    /**
     * 动态表情相关
     */
    private int mVPLoc[] = new int[2];
    private List<HashMap<String, String>> mDynamicFaceList = new ArrayList<HashMap<String, String>>();
    /**
     * 保存图片地址的list，用户展示大图时能够切换
     */
    private ArrayList<String> mImgUrlList = new ArrayList<String>();
    private int mImgId = 0;
    /**
     * 转载文章
     */
    private Article mForwardArticle = null;
    /**
     * 控件
     */
    // popmenu
    private FrameLayout mMenuFLayout;
    // title
    private LinearLayout mTitleLLayout;
    private ScrollTextView mTitleSTV;
    // 内容
    private LinearLayout mListLLayout;
    private PullToRefreshMyScrollView mPTRScrollView;
    private GetScrollDistanceScrollView mScrollView;
    // 进度条
    private FrameLayout mProgressFLayout;
    private Win8ProgressBar mProgressBar;
    // pagedrawer
    private SlidingDrawer mSlidingDrawer;
    private ImageView mHandleIV;
    private ListView mPageLV;
    private PageListAdapter mPageAdapter;
    private ArrayList<String> mPageList = new ArrayList<String>();
    // 回复
    private LinearLayout mReplyLLayout;
    private LinearLayout mFaceLLayout;
    private ControlScrollViewPager mReplyVP;
    private ImageView mCursorIV;
    private TextView mClassicTV, mOnionTV,
            mTuzkiTV, mYociTV;
    private GridView gv_classic, gv_onion, gv_tuzki,
            gv_yoci;
    private ImageView mFaceIV;
    private EditText mReplyET;
    private ImageView mSendReplyIV;
    /*
     * 查看动态表情的view
     */
    private LinearLayout mDynamicFaceLLayout;
    private ImageViewEx mDynamicFaceIVE;
    // help
    private ImageView mHelpIV;

    private boolean mIsReplyEnabled = true;

    private Handler mHandler = new Handler(new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            if (MSG_CONTENT_ARTICLE == msg.what) {
                // 将json数据解析为元数据
                if (!mIsRefer) {
                    mTitleSTV.setText(mThreads.title);
                }
                showContent();
            } else if (MSG_OPERATION == msg.what) {
                // 删除文章
                Toast.makeText(getBaseContext(), "删除成功", Toast.LENGTH_SHORT)
                        .show();
                startThread(mThreads.board_name, mThreads.group_id, mThisID, 1);
            } else if (MSG_ARTICLE_SEND == msg.what) {
                // 回复成功
                Toast.makeText(getBaseContext(), "回复成功", Toast.LENGTH_SHORT)
                        .show();
                mReplyET.setText("");
            } else if (MSG_FORWARD_CROSS == msg.what) {
                // 转载文章|转寄文章
                Bundle data = msg.getData();
                String info = data.getString(KEY_DATA);
                if (!TextUtils.isEmpty(info)) {
                    Toast.makeText(getBaseContext(), info, Toast.LENGTH_SHORT)
                            .show();
                }
                return true;
            } else if (MSG_ERROR == msg.what) {
                Bundle data = msg.getData();
                String strError = data.getString(KEY_DATA);
                if (TextUtils.isEmpty(strError)) {
                    strError = NetWorkManager.MSG_NONET;
                }
                Toast.makeText(getBaseContext(), strError, Toast.LENGTH_SHORT)
                        .show();
            }
            showProgress(false);
            mPTRScrollView.onRefreshComplete();
            mIsReplyEnabled = true;
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSwitchMgr = SwitchManager.getInstance(getBaseContext());
        if (mSwitchMgr.isNightModeEnabled()) {
            // 夜间模式
            setTheme(R.style.ThemeNight);
        } else {
            // 日间模式
            setTheme(R.style.ThemeDay);
        }
        setContentView(R.layout.activity_bbs_content_article);

        init();
        /**
         * 获取intent对象
         */
        Intent intent = getIntent();
        mThreads = new Threads();
        Article article = (Article) intent.getSerializableExtra(KEY_ARTICLE);
        if (null != article) {
            // 主题帖模式
            mThreads.title = article.title;
            mThreads.board_name = article.board_name;
            mThreads.group_id = article.group_id;
        } else {
            // 提醒模式
            Refer refer = (Refer) intent.getSerializableExtra(KEY_REFER);
            mThreads.title = refer.title;
            mThreads.board_name = refer.board_name;
            mThreads.group_id = refer.id;
            changeReferMode(true);
        }

        mTitleSTV.setText(mThreads.title);
        /**
         * 开启获取主题帖的线程
         */
        startThread(mThreads.board_name, mThreads.group_id, mThisID, 1);
    }

    /**
     * 排版显示内容的方法
     */
    @SuppressLint("NewApi")
    private void showContent() {
        if (null == mThreads || null == mThreads.articles) {
            return;
        }
        // 滚动到顶部
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.fullScroll(ScrollView.FOCUS_UP);
            }
        });
        // 更新页数列表
        updatePageDrawer();
        int nChildCount = mListLLayout.getChildCount();
        // 删除多余的布局
        while (mThreads.articles.length < nChildCount) {
            mListLLayout.removeViewAt(0);
            nChildCount = mListLLayout.getChildCount();
        }
        // 清空图片地址list
        mImgUrlList.clear();
        mImgId = 0;

        Article article = null;
        ViewHolder holder = null;
        View convertView = null;

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int position = 0; position < mThreads.articles.length; position++) {
            article = mThreads.articles[position];
            if (nChildCount > position) {
                convertView = mListLLayout.getChildAt(position);
                holder = (ViewHolder) convertView.getTag();
            } else {
                convertView = inflater.inflate(
                        R.layout.list_item_bbs_content_article, null);
                holder = new ViewHolder();
                holder.ll_root = (LinearLayout) convertView
                        .findViewById(R.id.list_item_bbcontent_article_ll_root);
                holder.civ_face = (CircleImageView) convertView
                        .findViewById(R.id.list_item_bbcontent_article_civ_face);
                holder.tv_author = (TextView) convertView
                        .findViewById(R.id.list_item_bbcontent_article_tv_author);
                holder.tv_date = (TextView) convertView
                        .findViewById(R.id.list_item_bbcontent_article_tv_date);
                holder.tv_layor = (SizeAdjustingTextView) convertView
                        .findViewById(R.id.list_item_bbscontent_article_tv_layor);
                holder.ll_content = (LinearLayout) convertView
                        .findViewById(R.id.list_item_bbcontent_article_ll_content);
                holder.ll = (LinearLayout) convertView
                        .findViewById(R.id.list_item_bbcontent_article_ll);
                holder.rm = (RayMenu) convertView
                        .findViewById(R.id.list_item_bbcontent_article_rm);
                convertView.setTag(holder);
                mListLLayout.addView(convertView);
            }
            if (position == 0) {
                holder.ll_root.setPadding(0, mIUMgr.dip2px(50), 0, 0);
            } else {
                holder.ll_root.setPadding(0, 0, 0, 0);
            }
            if (mSwitchMgr.isNightModeEnabled()) {
                holder.ll
                        .setBackgroundResource(R.drawable.background_list_night);
                holder.tv_author.setTextColor(Color.parseColor("#00aaaa"));
            } else {
                holder.ll.setBackgroundResource(R.drawable.background_list_day);
                holder.tv_author.setTextColor(Color.BLUE);
            }

            holder.ll_content.removeAllViews();
            /**
             * 设置头像
             */
            if (mSwitchMgr.isFaceEnabled()) {
                if (null != article.user) {
                    // 头像
                    if (null != article.user.face_url) {
                        holder.civ_face.setImageUrl(article.user.face_url);
                    } else {
                        holder.civ_face
                                .setImageResource(R.drawable.iu_default_green);
                    }
                    holder.civ_face.setTag(article.user);
                    holder.civ_face.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            User user = (User) v.getTag();
                            ActivityFunc.startActivity(BBSContentActivity.this,
                                    BBSUserInfoActivity.class, user, false);
                        }
                    });
                }
            } else {
                holder.civ_face.setVisibility(View.GONE);
            }
            /**
             * 设置用户ID
             */
            if (article.user != null) {
                holder.tv_author.setText(article.user.id);
            }
            if (!mIsRefer) {
                if (article.user.id.equals(mThreads.user.id)) {
                    // 楼主名字显示为红色
                    holder.tv_author.setTextColor(Color.parseColor("#cc0000"));
                }
            }
            /**
             * 时间
             */
            holder.tv_date.setText(TimeUtils.getLocalTime(article.post_time));
            /**
             * 楼层
             */
            if (!mIsRefer) {
                int nLayor = (mThreads.pagination.page_current_count - 1) * 10
                        + position;
                if (0 == nLayor) {
                    holder.tv_layor.setText("楼主");
                    holder.tv_layor.setTextColor(Color.parseColor("#cc0000"));
                } else if (1 == nLayor) {
                    holder.tv_layor.setText("沙发");
                    holder.tv_layor.setTextColor(Color.parseColor("#ff6600"));
                } else if (2 == nLayor) {
                    holder.tv_layor.setText("板凳");
                    holder.tv_layor.setTextColor(Color.parseColor("#cc00cc"));
                } else if (9 == nLayor) {
                    holder.tv_layor.setText("酒楼");
                    holder.tv_layor.setTextColor(Color.parseColor("#008800"));
                } else {
                    holder.tv_layor.setText(nLayor + "楼");
                    holder.tv_layor.setTextColor(Color.GRAY);
                }
            }

            /**
             * RayMenu
             */
            // 重置
            holder.rm.reset();
            int[] rmItems;
            String strId = mBBSMgr.getUserId();
            if (!mIsRefer) {
                // 阅读文章
                if (BBSManager.GUEST.equals(strId)) {
                    // guest
                    rmItems = new int[1];
                } else if (article.user.id.equals(strId)) {
                    // 用户即是作者
                    rmItems = new int[5];
                    rmItems[1] = R.drawable.main_write;
                    rmItems[2] = R.drawable.main_clear;
                    rmItems[3] = R.drawable.main_cross;
                    rmItems[4] = R.drawable.main_forward;
                } else {
                    // 用户不是作者
                    rmItems = new int[4];
                    rmItems[1] = R.drawable.main_send;
                    rmItems[2] = R.drawable.main_cross;
                    rmItems[3] = R.drawable.main_forward;
                }
                if (null == mThisID) {
                    // 只看此ID
                    rmItems[0] = R.drawable.main_this_id;
                } else {
                    // 查看全部
                    rmItems[0] = R.drawable.main_this_id_back;
                }
            } else {
                // 提醒模式
                rmItems = new int[4];
                rmItems[0] = R.drawable.main_source;
                rmItems[1] = R.drawable.main_send;
                rmItems[2] = R.drawable.main_cross;
                rmItems[3] = R.drawable.main_forward;
            }
            int length = rmItems.length;
            for (int i = 0; i < length; i++) {
                DarkImageView item = new DarkImageView(BBSContentActivity.this);
                item.setTag(article);
                item.setId(rmItems.length * 10 + i);
                if (Build.VERSION.SDK_INT >= 11) {
                    item.setRotationY(180);
                }
                item.setImageResource(rmItems[i]);
                holder.rm.addItem(item, new RayMenuListener());
            }
            /**
             * 显示内容
             */
            processContent(holder.ll_content, JsonHelper.toHtml(article, true),
                    article);
            mListLLayout.invalidate();
        }
    }

    /**
     * 显示内容
     * 
     * @param ll
     * @param html
     */
    private void processContent(LinearLayout ll, String[] html, Article article) {
        String[] array = html[0].split("<image");
        int length = array.length;
        String str = null;
        int index = 0;
        String strImg = null;
        String strHtml = null;
        for (int i = 0; i < length; i++) {
            str = array[i].trim();
            if (i != 0 && str.startsWith("=")) {
                index = str.indexOf(">");
                if (index > 0) {
                    strImg = str.substring(1, index);
                    // 图片
                    processImage(ll, strImg);
                    strHtml = str.substring(index + 1).trim();
                    if (!"".equals(strHtml)) {
                        // 网页
                        processWebView(ll, strHtml, article);
                    }
                } else {
                    // 网页
                    processWebView(ll, str, article);
                }
            } else {
                // 网页
                processWebView(ll, str, article);
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
        SmartImageView siv = new SmartImageView(this);
        siv.setScaleType(ScaleType.CENTER_CROP);
        LayoutParams param = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        siv.setMaxWidth(mIUMgr.dip2px(200));
        siv.setMaxHeight(mIUMgr.dip2px(300));
        siv.setBackgroundColor(Color.WHITE);
        siv.setLayoutParams(param);
        siv.setAdjustViewBounds(true);
        
        siv.setImageUrl(url, R.drawable.iu_default_gray,
                R.drawable.iu_default_green);
        siv.setTag(mImgId++ + "+" + url);
        // 将图片地址添加到listImgSrc中
        mImgUrlList.add(url);

        ll.addView(siv);
        // 单击图片查看大图
        siv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BBSContentActivity.this,
                        ViewLargeImageActivity.class);
                intent.putExtra(ViewLargeImageActivity.KEY_URL, (String) view.getTag());
                intent.putExtra(ViewLargeImageActivity.KEY_URL_LIST, mImgUrlList);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                ActivityFunc.startActivity(BBSContentActivity.this, intent);
            }
        });
    }

    /**
     * 显示webview
     * 
     * @param ll
     * @param html
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void processWebView(LinearLayout ll, String html, Article article) {
        html = html.trim().replaceAll("\n", "<br/>");
        html = "<body "
                // +
                // "style=\"text-align:justify;text-justify:distribute-all-lines;"
                + "\">"
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            wv.setBackgroundColor(0x01000000);
        } else {
            if (mSwitchMgr.isNightModeEnabled()) {
                wv.setBackgroundColor(Color.parseColor("#111111"));
            } else {
                wv.setBackgroundColor(Color.parseColor("#ffffff"));
            }
        }
        // 水平滚动条不显示
        wv.setHorizontalScrollBarEnabled(false);
        // if (html.contains("<audio")) {
        mWebViewList.add(wv);
        // }
        wv.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
        wv.setTag(article);
        if (!BBSManager.GUEST.equals(mBBSMgr.getUserId())) {
            wv.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Article article = (Article) v.getTag();
                    mReplyET.setHint("回复" + article.user.id);
                    mSendReplyIV.setTag(article);

                    if (mIsReplyEnabled) {
                        if (View.VISIBLE == mReplyLLayout.getVisibility()) {
                            // 隐藏回复窗口
                            showReply(false);
                        } else {
                            // 显示回复窗口
                            showReply(true);
                        }
                    }
                    return false;
                }
            });
        }
        ll.addView(wv);
    }

    @SuppressLint("NewApi")
    private void init() {
        mIUMgr = AiYouManager.getInstance(getBaseContext());
        mBBSMgr = BBSManager.getInstance(getBaseContext());
        /**
         * 背景图片
         */
        ImageView iv_background = (ImageView) findViewById(R.id.activity_bbscontent_article_iv_background);

        FrameLayout fl_container = (FrameLayout) findViewById(R.id.activity_bbscontent_article_fl_container);
        // 是否是简约模式
        if (mSwitchMgr.isSimpleModeEnabled()) {
            iv_background.setVisibility(View.GONE);
            if (mSwitchMgr.isNightModeEnabled()) {
                fl_container.setBackgroundColor(getResources().getColor(
                        R.color.bbs_background_night));
            } else {
                fl_container.setBackgroundColor(getResources().getColor(
                        R.color.bbs_background_day));
            }
        }
        /**
         * popmenu
         */
        mMenuFLayout = (FrameLayout) findViewById(R.id.activity_bbscontent_article_fl_menu);
        /**
         * title
         */
        mTitleLLayout = (LinearLayout) findViewById(R.id.activity_bbscontent_article_ll_title);
        mTitleSTV = (ScrollTextView) findViewById(R.id.activity_bbscontent_article_stv_title);
        /**
         * PullToRefreshScrollView
         */
        mPTRScrollView = (PullToRefreshMyScrollView) findViewById(R.id.activity_bbscontent_article_sv);
        mPTRScrollView.setScrollingWhileRefreshingEnabled(false);// 刷新时禁止滚动
        mPTRScrollView.setOnRefreshListener(this);
        mScrollView = mPTRScrollView.getRefreshableView();
        mScrollView.setOnScrollListener(this);

        /**
         * 设置
         */
        setHeaderFooter();

        mListLLayout = (LinearLayout) findViewById(R.id.activity_bbscontent_article_ll_list);
        /**
         * 进度条
         */
        mProgressFLayout = (FrameLayout) findViewById(R.id.activity_bbscontent_article_fr_progress);
        mProgressBar = (Win8ProgressBar) findViewById(R.id.progress_bar);
        /**
         * help
         */
        mHelpIV = (ImageView) findViewById(R.id.activity_bbscontent_article_iv_help);
        /**
         * 动态表情
         */
        mDynamicFaceLLayout = (LinearLayout) findViewById(R.id.activity_bbscontent_article_ll_ive);
        mDynamicFaceIVE = (ImageViewEx) findViewById(R.id.activity_bbscontent_article_ive);
        /**
         * pagedrawer
         */
        mSlidingDrawer = (SlidingDrawer) findViewById(R.id.pagedrawer_sd);
        mHandleIV = (ImageView) findViewById(R.id.pagedrawer_iv_handle);
        mPageLV = (ListView) findViewById(R.id.pagedrawer_lv);
        mPageAdapter = new PageListAdapter(this, mPageList);
        mPageLV.setAdapter(mPageAdapter);
        mPageLV.setOnItemClickListener(this);
        mSlidingDrawer
                .setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {
                    @Override
                    public void onDrawerOpened() {
                        if (Build.VERSION.SDK_INT >= 11) {
                            mHandleIV.setRotation(180);
                        }
                    }
                });
        mSlidingDrawer
                .setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {
                    @Override
                    public void onDrawerClosed() {
                        if (Build.VERSION.SDK_INT >= 11) {
                            mHandleIV.setRotation(0);
                        }
                    }
                });
        /**
         * 回复
         */
        mReplyLLayout = (LinearLayout) findViewById(R.id.reply_ll_reply);
        mFaceLLayout = (LinearLayout) findViewById(R.id.reply_ll_face);
        mReplyVP = (ControlScrollViewPager) findViewById(R.id.reply_vp);
        mCursorIV = (ImageView) findViewById(R.id.reply_iv_cursor);
        mClassicTV = (TextView) findViewById(R.id.reply_tv_classic);
        mOnionTV = (TextView) findViewById(R.id.reply_tv_onion);
        mTuzkiTV = (TextView) findViewById(R.id.reply_tv_tuzki);
        mYociTV = (TextView) findViewById(R.id.reply_tv_yoci);
        mFaceIV = (ImageView) findViewById(R.id.reply_iv_face);
        mReplyET = (EditText) findViewById(R.id.reply_et_reply);
        mSendReplyIV = (ImageView) findViewById(R.id.reply_iv_reply);

        mFaceIV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 打开|关闭表情栏
                if (View.VISIBLE == mFaceLLayout.getVisibility()) {
                    mFaceLLayout.setVisibility(View.GONE);
                } else {
                    mFaceLLayout.setVisibility(View.VISIBLE);
                    if (mSwitchMgr.needShowFaceHelp()) {
                        mHelpIV.setVisibility(View.VISIBLE);
                        mHelpIV.bringToFront();
                        mSwitchMgr.disableShowFaceHelp();
                    }
                }
            }
        });
        mSendReplyIV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendReply();
            }
        });

        mCursorIV = (ImageView) findViewById(R.id.reply_iv_cursor);

        // 动画图片的宽度
        int bmpW = BitmapFactory.decodeResource(getResources(),
                R.drawable.face_cursor).getWidth();// 获取图片宽度
        // 动画图片偏移量
        int offset = ((AiYouManager.getScreenWidth() - mIUMgr.dip2px(20)) / 4 - bmpW) / 2;// 计算偏移量

        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);
        mCursorIV.setImageMatrix(matrix);// 设置动画初始位置
        // 页卡
        mClassicTV.setOnClickListener(new FaceTypeListener(0));
        mOnionTV.setOnClickListener(new FaceTypeListener(1));
        mTuzkiTV.setOnClickListener(new FaceTypeListener(2));
        mYociTV.setOnClickListener(new FaceTypeListener(3));
        // viewpager
        // tag页面列表
        ArrayList<View> tabList = new ArrayList<View>();
        LayoutInflater mInflater = getLayoutInflater();
        gv_classic = (GridView) mInflater.inflate(R.layout.face_classic, null);
        gv_onion = (GridView) mInflater.inflate(R.layout.face_onion, null);
        gv_tuzki = (GridView) mInflater.inflate(R.layout.face_tuzki, null);
        gv_yoci = (GridView) mInflater.inflate(R.layout.face_yoci, null);
        gv_classic.setAdapter(new FaceGridViewAdapter(getBaseContext(), 0));
        gv_onion.setAdapter(new FaceGridViewAdapter(getBaseContext(), 1));
        gv_tuzki.setAdapter(new FaceGridViewAdapter(getBaseContext(), 2));
        gv_yoci.setAdapter(new FaceGridViewAdapter(getBaseContext(), 3));
        gv_classic.setOnItemClickListener(new MyGridViewListener(
                getBaseContext(), 0, mReplyET));
        gv_onion.setOnItemClickListener(new MyGridViewListener(
                getBaseContext(), 1, mReplyET));
        gv_tuzki.setOnItemClickListener(new MyGridViewListener(
                getBaseContext(), 2, mReplyET));
        gv_yoci.setOnItemClickListener(new MyGridViewListener(getBaseContext(),
                3, mReplyET));
        if (Build.VERSION.SDK_INT >= 11) {
            gv_classic.setOnItemLongClickListener(this);
            gv_onion.setOnItemLongClickListener(this);
            gv_tuzki.setOnItemLongClickListener(this);
            gv_yoci.setOnItemLongClickListener(this);
            gv_classic.setOnTouchListener(this);
            gv_onion.setOnTouchListener(this);
            gv_tuzki.setOnTouchListener(this);
            gv_yoci.setOnTouchListener(this);
        }
        tabList.add(gv_classic);
        tabList.add(gv_onion);
        tabList.add(gv_tuzki);
        tabList.add(gv_yoci);
        mReplyVP.setAdapter(new ViewPagerAdapter(tabList));
        mReplyVP.setCurrentItem(0);
        mReplyVP.setOnPageChangeListener(new MyOnPageChangeListener(mCursorIV,
                offset, bmpW, 0));
    }

    private class FaceTypeListener implements OnClickListener {
        private int index = 0;

        public FaceTypeListener(int index) {
            this.index = index;
        }

        @Override
        public void onClick(View v) {
            mReplyVP.setCurrentItem(index);
        }
    }

    /**
     * 获取主题帖的方法
     * 
     * @param board 版面名称
     * @param id 主题ID
     * @param author 只看此author的帖子,null——查看全部
     * @param page 页数
     */
    private void startThread(final String board, final int id,
            final String author, final int page) {
        /**
         * 设置进度条状态
         */
        showProgress(true);
        threadGetThreads(board, id, author, page);
    }

    /**
     * 删帖的方法
     * 
     * @param board 版面名称
     * @param id 文章ID
     */
    private void startOperationThread(final String board, final int id) {
        mHistoryList.clear();
        showProgress(true);
        threadDeleteArticle(board, id);
    }

    /**
     * 分享
     * 
     * @param view
     */
    public void onShare(View view) {
        String urlArticle = BBSManager.BBS_URL + "/#!article/" + mThreads.board_name
                + "/" + mThreads.group_id;
        ShareTask task = new ShareTask(BBSContentActivity.this, mThreads.title, urlArticle,
                new ShareTask.ShareListener() {
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
        mMenuFLayout.setVisibility(View.GONE);
    }

    /**
     * 网址复制
     * 
     * @param view
     */
    public void onCopy(View view) {
        // 将网址复制到剪贴板
        ClipboardManager copy = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        copy.setText(BBSManager.BBS_URL + "/article/" + mThreads.board_name + "/"
                + mThreads.group_id);
        Toast.makeText(getBaseContext(), "复制成功", Toast.LENGTH_SHORT).show();
        mMenuFLayout.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mListLLayout.removeAllViews();
        mHistoryList.clear();
        mHistoryList = null;
        clearWebView();
        mThreads = null;
        mWebViewList = null;
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        System.gc();
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<GetScrollDistanceScrollView> refreshView) {
        if (null != mThreads.pagination) {
            if (-1 != mThreads.pagination.page_current_count) {
                for (Threads temp : mHistoryList) {
                    if (null != temp.pagination) {
                        if (-1 != temp.pagination.page_current_count
                                && temp.pagination.page_current_count == mThreads.pagination.page_current_count - 1) {
                            mThreads = temp;
                            showContent();
                            mPTRScrollView.onRefreshComplete();
                            return;
                        }
                    } else {
                        mHistoryList.remove(temp);
                    }
                }
                int page = mThreads.pagination.page_current_count - 1;
                if (page > 0) {
                    startThread(mThreads.board_name, mThreads.group_id,
                            mThisID, page);
                }
            }
        }
        mPTRScrollView.onRefreshComplete();
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<GetScrollDistanceScrollView> refreshView) {
        if (null != mThreads.pagination) {
            if (-1 != mThreads.pagination.page_current_count) {
                for (Threads temp : mHistoryList) {
                    if (null != temp.pagination) {
                        if (-1 != temp.pagination.page_current_count
                                && temp.pagination.page_current_count == mThreads.pagination.page_current_count + 1) {
                            mThreads = temp;
                            showContent();
                            mPTRScrollView.onRefreshComplete();
                            return;
                        }
                    } else {
                        mHistoryList.remove(temp);
                    }
                }
                int page = mThreads.pagination.page_current_count + 1;
                if (page <= mThreads.pagination.page_all_count) {
                    startThread(mThreads.board_name, mThreads.group_id,
                            mThisID, page);
                }
            }
        }
        mPTRScrollView.onRefreshComplete();
    }

    /**
     * 点击事件
     * 
     * @param view
     */
    public void onClick(View view) {
        if (R.id.activity_bbscontent_article_iv_help == view.getId()) {
            mHelpIV.setVisibility(View.GONE);
        } else if (view == mMenuFLayout) {
            mMenuFLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 是否显示回复窗口
     * 
     * @param flag
     */
    private void showReply(boolean flag) {
        Animation anim = null;
        if (!flag) {
            anim = AnimationUtils.loadAnimation(this, R.anim.search_exit);
            mReplyLLayout.startAnimation(anim);
            mIsReplyEnabled = false;
            anim.setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mReplyLLayout.setVisibility(View.GONE);
                    mFaceLLayout.setVisibility(View.GONE);
                    mIsReplyEnabled = true;
                    mReplyLLayout.clearAnimation();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        } else {
            anim = AnimationUtils.loadAnimation(this, R.anim.search_enter);
            mReplyLLayout.setVisibility(View.VISIBLE);
            mReplyLLayout.startAnimation(anim);
            mIsReplyEnabled = false;
            anim.setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mIsReplyEnabled = true;
                    mReplyLLayout.clearAnimation();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUESTCODE_SECTION) {
                if (mForwardArticle != null) {
                    String board = mForwardArticle.board_name;
                    int id = mForwardArticle.id;
                    // 分区列表返回的结果
                    String target = (String) data.getStringExtra(BBSSectionActivity.KEY_NAME);
                    // 开启转载文章线程
                    threadForwardCross(board, id, target, true);
                }
            }
        }
    }

    /**
     * pagedrawer点击选择页数
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        startThread(mThreads.board_name, mThreads.group_id, mThisID,
                position + 1);
    }

    /**
     * 更新pagedrawer
     */
    private void updatePageDrawer() {
        if (mIsRefer) {
            return;
        }
        mPageList.clear();
        for (int i = 0; i < mThreads.pagination.page_all_count; i++) {
            if (i != mThreads.pagination.page_current_count - 1) {
                mPageList.add("false");
            } else {
                mPageList.add("true");
            }
        }
        mPageAdapter.notifyDataSetChanged();
        if (mThreads.pagination.page_current_count - 6 >= 0) {
            mPageLV.setSelection(mThreads.pagination.page_current_count - 6);
        } else {
            mPageLV.setSelection(0);
        }
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
     * 页面缩放按钮点击事件
     * 
     * @param view
     */
    public void onZoomClick(View view) {
        int nId = view.getId();
        int size = mBBSMgr.getWebViewScaleSize();
        if (R.id.pagedrawer_iv_zoomin == nId) {
            // 缩小
            if (size <= 100) {
                Toast.makeText(getBaseContext(), "已达到最小级别", Toast.LENGTH_SHORT)
                        .show();
            } else {
                mBBSMgr.setWebViewScaleSize(size - 25);
                showContent();
            }
        } else if (R.id.pagedrawer_iv_zoomout == nId) {
            // 放大
            if (size >= 700) {
                Toast.makeText(getBaseContext(), "已达到最大级别", Toast.LENGTH_SHORT)
                        .show();
            } else {
                mBBSMgr.setWebViewScaleSize(size + 25);
                showContent();
            }
        }
    }

    /**
     * 左上角返回按钮
     */
    public void selfFinish(View view) {
        scrollToFinishActivity();
    }

    /**
     * 转载|转寄文章的线程
     * 
     * @param board 文章所在版面
     * @param id 文章id
     * @param target 目标
     * @param isCross 是否是转载
     */
    private void threadForwardCross(final String board, final int id,
            final String target, final boolean isCross) {
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            if (null != mHandler) {
                mHandler.sendEmptyMessage(MSG_ERROR);
            }
            return;
        }
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                String strJson = null;
                if (isCross) {
                    // 转载文章
                    strJson = Article.crossArticle(BBSContentActivity.this, board, id, target);
                } else {
                    // 转寄文章
                    strJson = Article.forwardArticle(BBSContentActivity.this, board, id, target);
                }
                if (TextUtils.isEmpty(strJson)) {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(MSG_ERROR);
                    }
                    return;
                }
                // 转载文章|转寄文章
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
                String info = null;
                // 转载|转寄成功
                if (isCross) {
                    info = "转载成功";
                } else {
                    info = "转寄成功";
                }
                if (null != mHandler) {
                    Message msg = mHandler.obtainMessage(MSG_FORWARD_CROSS);
                    Bundle data = msg.getData();
                    data.putString(KEY_DATA, info);
                    mHandler.sendMessage(msg);
                }
            }
        });
    }

    /**
     * 获取主题帖的线程 由 {@link #startThread(String, int, String, int)} 启动
     * 
     * @param board 版面名称
     * @param id 主题ID
     * @param author 只看此author的帖子
     * @param page 页数
     */
    private void threadGetThreads(final String board, final int id,
            final String author, final int page) {
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            if (null != mHandler) {
                mHandler.sendEmptyMessage(MSG_ERROR);
            }
            return;
        }
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                String strJson = null;
                if (!mIsRefer) {
                    if (-1 == page) {
                        if (null == author) {
                            strJson = Threads.getThreads(BBSContentActivity.this, board, id);
                        } else {
                            strJson = Threads.getThreads(BBSContentActivity.this, board, id,
                                    author);
                        }
                    } else {
                        if (null == author) {
                            strJson = Threads.getThreads(BBSContentActivity.this, board, id,
                                    page);
                        } else {
                            strJson = Threads.getThreads(BBSContentActivity.this, board, id,
                                    author,
                                    page);
                        }
                    }
                } else {
                    strJson = Article.getArticle(BBSContentActivity.this, board, id);
                }
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
                // 将json数据解析为元数据
                if (mIsRefer) {
                    Article article = new Article(strJson);
                    mThreads.articles = new Article[1];
                    mThreads.articles[0] = article;
                    mThreads.group_id = article.group_id;
                } else {
                    if (null != mThreads) {
                        if (null != mThreads.pagination) {
                            if (-1 != mThreads.pagination.page_current_count) {
                                while (mHistoryList.size() > 15) {
                                    // listThreadsPre最大长度为15
                                    mHistoryList.remove(0);
                                }
                                ArrayList<Threads> listTemp = new ArrayList<Threads>();
                                for (Threads temp : mHistoryList) {
                                    if (null != temp.pagination) {
                                        if (-1 != temp.pagination.page_current_count) {
                                            if (mThreads.pagination.page_current_count == temp.pagination.page_current_count) {
                                                listTemp.add(temp);
                                            }
                                        }
                                    }
                                }
                                mHistoryList.removeAll(listTemp);
                                if (mThreads.pagination.page_current_count != mThreads.pagination.page_all_count) {
                                    Threads newThreads = mThreads;
                                    mHistoryList.add(newThreads);
                                }
                            }
                        }
                    }
                    mThreads = new Threads(strJson);
                }
                if (null != mHandler) {
                    mHandler.sendEmptyMessage(MSG_CONTENT_ARTICLE);
                }
            }
        });
    }

    /**
     * 删帖的线程 由 {@link #startOperationThread(String, int)} 启动
     * 
     * @param board 版面名称
     * @param id 主题ID
     */
    private void threadDeleteArticle(final String board, final int id) {
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            if (null != mHandler) {
                mHandler.sendEmptyMessage(MSG_ERROR);
            }
            return;
        }
        showProgress(true);
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                String strJson = Article.deleteArticle(BBSContentActivity.this, board, id);
                if (TextUtils.isEmpty(strJson)) {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(MSG_ERROR);
                    }
                    return;
                }
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
                if (null != mHandler) {
                    mHandler.sendEmptyMessage(MSG_OPERATION);
                }
            }
        });
    }

    /**
     * 回复线程
     * 
     * @param board
     * @param title
     * @param content
     * @param reid
     */
    private void threadReplyArticle(final String board, final String title,
            final String content, final String reid) {
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            if (null != mHandler) {
                mHandler.sendEmptyMessage(MSG_ERROR);
            }
            return;
        }
        showProgress(true);
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                String strJson = Article.sendArticle(BBSContentActivity.this, board, title,
                        content, reid);
                if (TextUtils.isEmpty(strJson)) {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(MSG_ERROR);
                    }
                    return;
                }
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
                if (null != mHandler) {
                    mHandler.sendEmptyMessage(MSG_ARTICLE_SEND);
                }
            }
        });
    }

    /**
     * 下拉时，显示刷新还是上一页
     * 
     * @param flag
     */
    private void setHeaderFooter() {
        mPTRScrollView.setPullLabel("上一页", Mode.PULL_FROM_START);
        mPTRScrollView.setRefreshingLabel("正在加载...", Mode.PULL_FROM_START);
        mPTRScrollView.setReleaseLabel("松开加载", Mode.PULL_FROM_START);
        mPTRScrollView.setPullLabel("下一页", Mode.PULL_FROM_END);
        mPTRScrollView.setRefreshingLabel("正在加载...", Mode.PULL_FROM_END);
        mPTRScrollView.setReleaseLabel("松开加载", Mode.PULL_FROM_END);
    }

    /**
     * 清空webview
     */
    @SuppressLint("NewApi")
    private void clearWebView() {
        if (0 != mWebViewList.size()) {
            for (WebView wv : mWebViewList) {
                if (null != wv) {
                    if (Build.VERSION.SDK_INT >= 11) {
                        wv.onPause();
                    }
                    wv.removeAllViews();
                    wv.destroy();
                }
            }
        }
        mWebViewList.clear();
    }

    class ViewHolder {
        public LinearLayout ll_root;
        public CircleImageView civ_face;
        public TextView tv_author;
        public TextView tv_date;
        public SizeAdjustingTextView tv_layor;
        public LinearLayout ll_content;
        public LinearLayout ll;
        public RayMenu rm;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 按下键盘上返回按钮
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (View.VISIBLE == mHelpIV.getVisibility()) {
                mHelpIV.setVisibility(View.GONE);
                return true;
            } else if (mSlidingDrawer.isOpened()) {
                mSlidingDrawer.close();
                return true;
            }
            selfFinish(null);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            showPopmenu(null);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 切换Article|Refer模式
     * 
     * @param isReferMode
     */
    private void changeReferMode(boolean isReferMode) {
        mIsRefer = isReferMode;
        ImageView iv = (ImageView) findViewById(R.id.activity_bbscontent_article_iv_popmenu);
        if (isReferMode) {
            iv.setVisibility(View.INVISIBLE);
            mSlidingDrawer.setVisibility(View.GONE);
            mPTRScrollView.setMode(Mode.DISABLED);
        } else {
            iv.setVisibility(View.VISIBLE);
            mSlidingDrawer.setVisibility(View.VISIBLE);
            mPTRScrollView.setMode(Mode.BOTH);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
            int position, long id) {
        // 禁用滑动结束Activity
        getSwipeBackLayout().setEnableGesture(false);
        // 震动
        mIUMgr.vibrate(300);
        // 获取viewpager的屏幕坐标
        mReplyVP.getLocationInWindow(mVPLoc);
        // 获取可见的gif图位置、名称
        mDynamicFaceList.clear();

        int start = parent.getFirstVisiblePosition();
        int end = parent.getLastVisiblePosition();

        View v = null;
        for (int i = 0; i <= end - start; i++) {
            HashMap<String, String> map = new HashMap<String, String>();
            v = parent.getChildAt(i);
            int location[] = new int[2];
            if (null == v) {
                continue;
            }
            v.getLocationInWindow(location);
            if (v == view) {
                mDynamicFaceIVE.setId(i);
            }
            map.put("x", location[0] + "");
            map.put("y", location[1] + "");
            map.put("imgName", parent.getItemAtPosition(i + start).toString());
            mDynamicFaceList.add(map);
        }

        // 锁定vp，使不能滚动
        mReplyVP.setScrollable(false);
        // 获取图片名称
        String imgName = parent.getItemAtPosition(position).toString();
        mDynamicFaceIVE.setSource(Converters.assetToByteArray(getAssets(),
                "face/" + imgName));
        // 获取控件在窗口中的绝对位置，不包括最顶部的状态栏
        int location[] = new int[2];
        view.getLocationInWindow(location);
        mDynamicFaceLLayout.setX(location[0] - mIUMgr.dip2px(10));
        mDynamicFaceLLayout.setY(location[1] - mIUMgr.dip2px(120));
        mDynamicFaceLLayout.setVisibility(View.VISIBLE);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (mReplyVP.getScrollable()) {
                    return false;
                }
                // 获取触摸点相对于屏幕的坐标，不包含顶部的通知栏
                float eventX = mVPLoc[0] + event.getX();
                float eventY = mVPLoc[1] + event.getY();

                int x = 0,
                y = 0;
                HashMap<String, String> map = null;
                String imgName = null;
                for (int i = 0; i < mDynamicFaceList.size(); i++) {
                    map = mDynamicFaceList.get(i);
                    x = Integer.parseInt(map.get("x"));
                    y = Integer.parseInt(map.get("y"));
                    if (eventX >= x && eventX <= x + mIUMgr.dip2px(40) && eventY >= y
                            && eventY <= y + mIUMgr.dip2px(40)) {
                        if (i == mDynamicFaceIVE.getId()) {
                            // 如果是正在播放的gif则返回
                            return false;
                        }
                        // 更新显示的gif
                        imgName = map.get("imgName").toString();
                        mDynamicFaceIVE.setSource(Converters.assetToByteArray(
                                getAssets(), "face/" + imgName));
                        mDynamicFaceIVE.setId(i);
                        mDynamicFaceLLayout.setX(x - mIUMgr.dip2px(10));
                        mDynamicFaceLLayout.setY(y - mIUMgr.dip2px(120));
                        break;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                // 重新初始化滑动结束Activity
                initSwipeOut();
                mReplyVP.setScrollable(true);
                mDynamicFaceLLayout.setVisibility(View.GONE);
                break;
        }
        return false;
    }

    @Override
    public void onScroll(int y, int oldY) {
        if (y < mIUMgr.dip2px(70)) {
            mTitleLLayout.clearAnimation();
            mTitleLLayout.setTag(null);
            if (View.VISIBLE != mTitleLLayout.getVisibility()) {
                showTitle(true);
            }
        } else {
            if (mTitleLLayout.getTag() == null) {
                if (y > oldY && View.VISIBLE == mTitleLLayout.getVisibility()) {
                    showTitle(false);
                } else if (y < oldY && View.VISIBLE != mTitleLLayout.getVisibility()) {
                    showTitle(true);
                }
            }
        }
    }

    /**
     * 是否显示标题栏
     * 
     * @param flag
     */
    private void showTitle(final boolean flag) {
        Animation anim = null;
        mTitleLLayout.setTag("anim");
        if (flag) {
            anim = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_top);
            anim.setDuration(1000);
            mTitleLLayout.setVisibility(View.VISIBLE);
            mTitleLLayout.startAnimation(anim);
        } else {
            anim = AnimationUtils.loadAnimation(this, R.anim.slide_out_to_top);
            anim.setDuration(1000);
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

    public void showPopmenu(View view) {
        if (View.VISIBLE == mMenuFLayout.getVisibility()) {
            mMenuFLayout.setVisibility(View.GONE);
        } else {
            mMenuFLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 回复
     */
    private void sendReply() {
        // 发送
        if (BBSManager.GUEST.equals(mBBSMgr.getUserId())) {
            Toast.makeText(getBaseContext(), "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }
        String message = mReplyET.getText().toString().trim();
        if ("".equals(message)) {
            Toast.makeText(getBaseContext(), "回复内容为空", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        // 关闭输入法
        AiYouManager.viewInputMethod(BBSContentActivity.this, false, mReplyET);
        // 关闭表情
        mFaceLLayout.setVisibility(View.GONE);

        Article article = (Article) mSendReplyIV.getTag();
        String reid = article.id + "";
        String title = "Re:" + article.title;
        String strContent = "";
        if (article.id != article.group_id) {
            String[] arr = JsonHelper.toHtml(article, true);
            String strReply = arr[1];
            // 去掉帖子中回复他人的部分
            strContent = article.content.replace(strReply, "").trim();
            // 去除多余的尾巴
            while (strContent.endsWith("-") || strContent.endsWith("\n")) {
                strContent = strContent.substring(0, strContent.length() - 1);
            }
            if (strContent.length() > 500) {
                strContent = strContent.substring(0, 500);
                strContent += "\n...................";
            }
            String[] array = strContent.split("\n");
            strContent = "";
            for (int i = 0; i < array.length; i++) {
                array[i] = AiYouManager.getTxtWithoutNTSRElement(array[i], "");
                array[i] = array[i].trim();
                if (!array[i].equals("")) {
                    if (i < array.length - 1) {
                        strContent += array[i] + "\n: ";
                    } else {
                        strContent += array[i];
                    }
                }
            }
            message = message + "\n【 在 " + article.user.id + " 的大作中提到: 】\n: "
                    + strContent;
        }
        String tail = mBBSMgr.getAppTail();
        if (null != tail) {
            message += "\n\n" + tail;
        }
        mReplyLLayout.setVisibility(View.GONE);
        mIsReplyEnabled = false;

        threadReplyArticle(article.board_name, title, message, reid);
    }

    private class RayMenuListener implements OnClickListener {
        @Override
        public void onClick(final View v) {
            int nId = v.getId();
            if (nId == 10 || nId == 40 || nId == 50) {
                if (null == mThisID && !mIsRefer) {
                    // 只看此ID
                    Article article = (Article) v.getTag();
                    mThisID = article.user.id;
                    Toast.makeText(getBaseContext(),
                            "只看" + mThisID + "的帖子",
                            Toast.LENGTH_SHORT).show();
                    mHistoryList.clear();
                } else {
                    // 查看全部
                    mThisID = null;
                    Toast.makeText(getBaseContext(), "查看全部帖子",
                            Toast.LENGTH_SHORT).show();
                }
                mHistoryList.clear();
                changeReferMode(false);
                startThread(mThreads.board_name, mThreads.group_id,
                        mThisID, 1);
            } else if (nId == 41) {
                // 回复
                Article article = (Article) v.getTag();
                Intent intent = new Intent(BBSContentActivity.this,
                        BBSWriteActivity.class);
                intent.putExtra(BBSWriteActivity.REPLY_ARTICLE, article);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, 0);
                Toast.makeText(getBaseContext(), "回复该贴",
                        Toast.LENGTH_SHORT).show();
            } else if (nId == 51) {
                // 编辑
                Article article = (Article) v.getTag();
                Intent intent = new Intent(BBSContentActivity.this,
                        BBSWriteActivity.class);
                intent.putExtra(BBSWriteActivity.EDIT_ARTICLE, article);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, 0);
                Toast.makeText(getBaseContext(), "编辑该贴",
                        Toast.LENGTH_SHORT).show();
            } else if (nId == 52) {
                // 删帖
                final CustomDialog dialog = new CustomDialog(BBSContentActivity.this);
                dialog.setMessage("确定删除该贴吗？");
                dialog.setCancelButton(null)
                        .setOKButton(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                                Article article = (Article) v.getTag();
                                startOperationThread(article.board_name, article.id);
                            }
                        }).show();

            } else if (nId == 42 || nId == 53) {
                // 转载文章
                Toast.makeText(getBaseContext(), "转载文章",
                        Toast.LENGTH_SHORT).show();
                final CustomDialog dialog = new CustomDialog(BBSContentActivity.this);
                dialog.setMessage("确定转载该文章吗？");
                dialog.setCancelButton(null)
                        .setOKButton(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                                mForwardArticle = (Article) v.getTag();
                                // 转载文章
                                Toast.makeText(getBaseContext(), "请选择要转载的版面",
                                        Toast.LENGTH_SHORT).show();
                                // 打开分区列表
                                ActivityFunc.startActivityForResult(
                                        BBSContentActivity.this,
                                        BBSSectionActivity.class,
                                        null, REQUESTCODE_SECTION);
                            }
                        }).show();
            } else if (nId == 43 || nId == 54) {
                // 转寄文章
                Toast.makeText(getBaseContext(), "转寄文章",
                        Toast.LENGTH_SHORT).show();
                final CustomDialog dialog = new CustomDialog(BBSContentActivity.this);
                dialog.setMessage("请输入收件人ID")
                        .setCancelButton(null)
                        .setOKGetEditTextContentListener(
                                new CustomDialog.GetEditTextContentListener() {
                                    @Override
                                    public void onClick(View view, String userId) {
                                        // 转寄文章
                                        if (TextUtils.isEmpty(userId)) {
                                            Toast.makeText(getBaseContext(),
                                                    "请输入收件人ID",
                                                    Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        dialog.dismiss();
                                        Article article = (Article) v.getTag();
                                        // 开启线程
                                        threadForwardCross(article.board_name,
                                                article.id,
                                                userId, false);
                                    }
                                }).show();
            }
        }
    }
}

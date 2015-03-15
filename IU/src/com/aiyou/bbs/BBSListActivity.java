
package com.aiyou.bbs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.aiyou.BaseActivity;
import com.aiyou.R;
import com.aiyou.bbs.PullRefer.BBSService;
import com.aiyou.bbs.PullRefer.NotificationMgr;
import com.aiyou.bbs.PullRefer.NotificationMgr.NotifyType;
import com.aiyou.bbs.adapter.BBSListAdapter;
import com.aiyou.bbs.adapter.BBSListAdapter.OnRayMenuClickListener;
import com.aiyou.bbs.adapter.FavoriteAdapter;
import com.aiyou.bbs.adapter.FavoriteAdapter.DeleteFavoriteListener;
import com.aiyou.bbs.bean.Article;
import com.aiyou.bbs.bean.Board;
import com.aiyou.bbs.bean.Favorite;
import com.aiyou.bbs.bean.Mail;
import com.aiyou.bbs.bean.Mailbox;
import com.aiyou.bbs.bean.Refer;
import com.aiyou.bbs.bean.Mailbox.MailboxType;
import com.aiyou.bbs.bean.Refer.ReferType;
import com.aiyou.bbs.bean.VoteList.VoteType;
import com.aiyou.bbs.bean.Search;
import com.aiyou.bbs.bean.User;
import com.aiyou.bbs.bean.Vote;
import com.aiyou.bbs.bean.VoteList;
import com.aiyou.bbs.bean.Widget;
import com.aiyou.bbs.bean.helper.AdapterInterface;
import com.aiyou.bbs.utils.BBSListHelper;
import com.aiyou.bbs.utils.BBSListHelper.BeanType;
import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.utils.ActivityFunc;
import com.aiyou.utils.AiYouManager;
import com.aiyou.utils.JsonHelper;
import com.aiyou.utils.NetWorkManager;
import com.aiyou.utils.SwitchManager;
import com.aiyou.utils.http.HttpManager;
import com.aiyou.utils.logcat.Logcat;
import com.aiyou.utils.thread.ThreadUtils;
import com.aiyou.view.CustomDialog;
import com.aiyou.view.ScrollTextView;
import external.OtherView.ActivitySplitAnimationUtil;
import external.OtherView.BadgeView;
import external.OtherView.CircleImageView;
import external.OtherView.Win8ProgressBar;
import external.PullToRefresh.PullToRefreshBase;
import external.PullToRefresh.PullToRefreshListView;
import external.PullToRefresh.PullToRefreshBase.Mode;
import external.PullToRefresh.PullToRefreshBase.OnRefreshListener2;
import external.ResideMenu.ResideMenu;
import external.ResideMenu.ResideMenuItem;
import external.ResideMenu.ResideMenu.OnMenuListener;
import external.mesh.BitmapMesh;
import external.mesh.BitmapMesh.MeshView;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 呈现论坛文章列表的Activity
 * 
 * @author sollian
 */
public class BBSListActivity extends BaseActivity implements OnClickListener,
        OnRefreshListener2<ListView>, OnMenuListener, OnItemClickListener,
        OnRayMenuClickListener, OnScrollListener {
    private static final String TAG = BBSListActivity.class.getSimpleName();

    private static final String RESIDE_MENU_DIVIDER = "#cccccc";

    private static final int REQUESTCODE_LOGIN = 0x1111;
    private static final int REQUESTCODE_SECTION = 0x1112;
    private static final int MSG_USER_QUERY = 0;
    private static final int MSG_GET_LIST = 1;
    private static final int MSG_OPERATION = 2;
    protected static final int MSG_FAVORITE = 3;
    private static final int MSG_ERROR = -1;

    private static final String ACTION_SET_READ = "setRead";
    private static final String ACTION_DELETE = "delete";

    private static final String KEY_OPENSCREEN = "open_screen";
    private static final String KEY_REFRESH_DATA = "needRefreshData";
    private static final String KEY_INDEX = "index";
    private static final String KEY_TYPE = "type";
    private static final String KEY_DATA = "data";

    private BBSManager mBBSMgr;
    private AiYouManager mIUMgr;
    private SwitchManager mSwitchMgr;

    /**
     * 辅助变量
     */
    private int mCurrentItem = 1;
    private boolean mFlagLoadMore = false;
    /**
     * 辅助类
     */
    private BBSListHelper mMember;
    /**
     * 广播接收类
     */
    private MyReceiver mReceiver;
    /**
     * 存储item的list
     */
    private List<AdapterInterface> mList = new ArrayList<AdapterInterface>();
    /**
     * BaseAdapter
     */
    private BaseAdapter mAdapter;
    /**
     * 收藏版面（右滑菜单）
     */
    private List<Board> mFavoriteList = new ArrayList<Board>();
    private BaseAdapter mFavoriteAdapter;
    /**
     * ResideMenu相关
     */
    private ResideMenu mResideMenu;
    private ResideMenuItem mUserInfoItem;
    private ResideMenuItem mReplyItem;
    private ResideMenuItem mAiItem;
    private ResideMenuItem mMailItem;
    private ResideMenuItem mLoginItem;
    private ResideMenuItem mVoteItem;
    private ResideMenuItem mHomepageItem;
    private ResideMenuItem mPhotoShowItem;
    // 日间|夜间模式
    private ResideMenuItem mModeItem;
    /**
     * BadgeView
     */
    private BadgeView mReplyBV, mAtBV, mMailBV, mFaceBV;
    /**
     * 控件
     */
    // 左上角头像
    private CircleImageView mFaceIV;
    //
    private ImageView mSectionIV;
    // 装载meshview的布局
    private LinearLayout mMeshLLayout;
    // 动画视图
    private MeshView mMeshView;
    // 标题
    private LinearLayout mTitleLLayout;
    private ScrollTextView mTitleSTV;
    // 显示文章列表的listview
    private PullToRefreshListView mPTRListView;
    private ListView mListView;
    // 进度条、重试
    private Win8ProgressBar mProgressBar;
    // help
    private ImageView mHelpImageView;
    /**
     * 菜单布局
     */
    private FrameLayout mMenuFLayout;
    private LinearLayout mArticleMenu, mReferMenu, mMailMenu;
    private FrameLayout mVoteMenu;
    private ImageView mDeletetIV;

    private ImageView mSerchTitleIV, mSearchAuthorIV,
            mWriteArticleIV;
    /**
     * 搜索
     */
    private LinearLayout mSearchLLayout;
    private EditText mSearchET;

    /**
     * 当前用户
     */
    private User mUser;
    /**
     * 是否应用开屏动画
     */
    private boolean mIsOpenScreen = true;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @SuppressWarnings("unchecked")
        public boolean handleMessage(Message msg) {
            if (MSG_USER_QUERY == msg.what) {
                Bundle data = msg.getData();
                mUser = (User) data.getSerializable(KEY_DATA);
                data.clear();
                // 更新用户信息
                updateUserWindow(mUser);
                /**
                 * 显示分区按钮
                 */
                ((ImageView) findViewById(R.id.activity_bbslist_iv_section))
                        .setVisibility(View.VISIBLE);

                startThread(false, 1, true);
                return true;
            } else if (MSG_GET_LIST == msg.what) {
                if (mResideMenu.isOpened()) {
                    mResideMenu.closeMenu();
                }
                @SuppressWarnings("rawtypes")
                List list = mMember.getList();
                if (null != list) {
                    mList.addAll(list);
                    mAdapter.notifyDataSetChanged();
                }
                if (mList.isEmpty()) {
                    Toast.makeText(getBaseContext(), "无数据",
                            Toast.LENGTH_SHORT).show();
                }
                mFlagLoadMore = true;
            } else if (MSG_OPERATION == msg.what) {
                // 操作——refer全删、标为已读；mail全删
                Bundle data = msg.getData();
                boolean needRefreshData = data.getBoolean(KEY_REFRESH_DATA);
                int index = data.getInt(KEY_INDEX);
                String type = data.getString(KEY_TYPE);
                data.clear();

                return updateBadgeView(needRefreshData, index, type);
            } else if (msg.what == MSG_FAVORITE) {
                Bundle data = msg.getData();
                String strJson = data.getString(KEY_DATA);
                data.clear();
                Favorite.mFavorite = new Favorite(strJson);
                refreshFavorite();
            } else if (MSG_ERROR == msg.what) {
                refreshFavorite();
                Bundle data = msg.getData();
                String strError = data.getString(KEY_DATA);
                data.clear();
                if (strError == null) {
                    strError = NetWorkManager.MSG_NONET;
                }
                // 连接服务器失败
                Toast.makeText(getBaseContext(), strError, Toast.LENGTH_SHORT)
                        .show();
                mFlagLoadMore = true;
            }
            showProgress(false);
            mPTRListView.onRefreshComplete();
            return true;
        }

        private boolean updateBadgeView(boolean needRefreshData, int index, String type) {
            if (mMember.beanType == BeanType.REFER) {
                if (mMember.referType == ReferType.REPLY) {
                    int count = 0;
                    if (-1 != index) {
                        if (type.equals(ACTION_DELETE)) {
                            Refer refer = null;
                            for (Object obj : mList) {
                                refer = (Refer) obj;
                                if (index == refer.index) {
                                    break;
                                }
                                refer = null;
                            }
                            if (refer == null || refer.is_read) {
                                if (needRefreshData) {
                                    startThread(false, 1, true);
                                }
                                return true;
                            }
                        }
                        count = mBBSMgr
                                .getBBSNotificationRefer(ReferType.REPLY) - 1;
                    }
                    mBBSMgr.setBBSNotificationRefer(
                            ReferType.REPLY, count);
                    NotificationMgr.getInstance().cancel(NotifyType.REPLY);
                    if (count > 0) {
                        mReplyBV.setText(count + "");
                    } else {
                        mReplyBV.hide(true);
                    }
                } else if (mMember.referType == ReferType.AT) {
                    int count = 0;
                    if (-1 != index) {
                        if (type.equals(ACTION_DELETE)) {
                            Refer refer = null;
                            for (Object obj : mList) {
                                refer = (Refer) obj;
                                if (index == refer.index) {
                                    break;
                                }
                                refer = null;
                            }
                            if (refer == null || refer.is_read) {
                                if (needRefreshData) {
                                    startThread(false, 1, true);
                                }
                                return true;
                            }
                        }
                        count = mBBSMgr
                                .getBBSNotificationRefer(ReferType.AT) - 1;
                    }
                    mBBSMgr.setBBSNotificationRefer(ReferType.AT,
                            count);
                    NotificationMgr.getInstance().cancel(NotifyType.AT);
                    if (count > 0) {
                        mAtBV.setText(count + "");
                    } else {
                        mAtBV.hide(true);
                    }
                }
            } else if (mMember.beanType == BeanType.MAILBOX) {
                if (type.equals(ACTION_DELETE)) {
                    boolean flag = true;
                    if (-1 != index) {
                        flag = false;
                        Mail mail = null;
                        for (Object obj : mList) {
                            mail = (Mail) obj;
                            if (index == mail.index) {
                                if (!mail.is_read) {
                                    flag = true;
                                }
                                break;
                            }
                        }
                    }
                    if (flag) {
                        NotificationMgr.getInstance().cancel(NotifyType.MAIL);
                        mBBSMgr.setBBSNotificationMail(false);
                        mMailBV.hide(true);
                    }
                }
            }

            if (needRefreshData) {
                startThread(false, 1, true);
            } else {
                showProgress(false);
                mPTRListView.onRefreshComplete();
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBBSMgr = BBSManager.getInstance(this);
        mIUMgr = AiYouManager.getInstance(this);
        mSwitchMgr = SwitchManager.getInstance(this);

        if (mSwitchMgr.isNightModeEnabled()) {
            // 夜间模式
            setTheme(R.style.ThemeNight);
        } else {
            // 日间模式
            setTheme(R.style.ThemeDay);
        }
        int delay = 0;
        mIsOpenScreen = getIntent().getBooleanExtra(KEY_OPENSCREEN, true);
        if (mIsOpenScreen && ActivitySplitAnimationUtil.canPlay()
                && Build.VERSION.SDK_INT > 14) {
            // 中心打开动画
            ActivitySplitAnimationUtil.prepareAnimation(this);
            ActivitySplitAnimationUtil.animate(this, 1000);
            delay = 1000;
        }
        setContentView(R.layout.activity_bbs_list);

        init();

        Intent intent = getIntent();
        boolean bReply = intent.getBooleanExtra(NotifyType.REPLY.getTag(), false);
        boolean bAt = intent.getBooleanExtra(NotifyType.AT.getTag(), false);
        boolean bMail = intent.getBooleanExtra(NotifyType.MAIL.getTag(), false);

        if (bReply) {
            mMember.beanType = BeanType.REFER;
            mMember.referType = ReferType.REPLY;
            showFooter(true);
            mMember.title = getResources().getString(R.string.reply_me);
        } else if (bAt) {
            mMember.beanType = BeanType.REFER;
            mMember.referType = ReferType.AT;
            showFooter(true);
            mMember.title = getResources().getString(R.string.at_me);
        } else if (bMail) {
            mMember.beanType = BeanType.MAILBOX;
            mMember.mailboxType = MailboxType.INBOX;
            showFooter(true);
            mMember.title = getResources().getString(R.string.inbox);
        } else if (mMember.beanType == BeanType.WIDGET) {
            showFooter(false);
        } else {
            showFooter(true);
        }
        updateTitle();
        mHandler.postDelayed(new Runnable() {
            public void run() {
                /**
                 * 查询用户信息
                 */
                startThread(true, 1, true);
            }
        }, delay);
    }

    @SuppressLint("RtlHardcoded")
    private void init() {
        FrameLayout fl = (FrameLayout) findViewById(R.id.activity_bbslist_ll);
        if (mSwitchMgr.isNightModeEnabled()) {
            fl.setBackgroundColor(Color.parseColor("#11000000"));
        } else {
            fl.setBackgroundColor(Color.parseColor("#11ffffff"));
        }

        mSectionIV = (ImageView) findViewById(R.id.activity_bbslist_iv_section);
        mMeshLLayout = (LinearLayout) findViewById(R.id.activity_bbslist_ll_mesh);

        mMember = BBSListHelper.getInstance();

        mTitleLLayout = (LinearLayout) findViewById(R.id.activity_bbslist_ll_title);
        mTitleSTV = (ScrollTextView) findViewById(R.id.activity_bbslist_stv_title);
        mPTRListView = (PullToRefreshListView) findViewById(R.id.activity_bbslist_lv);

        mPTRListView.setOnRefreshListener(this);
        mPTRListView.setShowIndicator(false);
        mListView = mPTRListView.getRefreshableView();

        mListView.setOnItemClickListener(this);
        mListView.setOnScrollListener(this);

        mAdapter = new BBSListAdapter(this, mList, this);
        mListView.setAdapter(mAdapter);

        /**
         * 菜单
         */
        mMenuFLayout = (FrameLayout) findViewById(R.id.activity_bbslist_fl_menu);
        mArticleMenu = (LinearLayout) findViewById(R.id.activity_bbslist_menu_ll_article);
        mReferMenu = (LinearLayout) findViewById(R.id.activity_bbslist_menu_ll_refer);
        mMailMenu = (LinearLayout) findViewById(R.id.activity_bbslist_menu_ll_mail);
        mVoteMenu = (FrameLayout) findViewById(R.id.activity_bbslist_menu_fl_vote);

        mDeletetIV = (ImageView) findViewById(R.id.activity_bbslist_menu_iv_delete_collect);

        mSerchTitleIV = (ImageView) findViewById(R.id.activity_bbslist_menu_iv_search_title);
        mSearchAuthorIV = (ImageView) findViewById(R.id.activity_bbslist_menu_iv_search_author);
        mWriteArticleIV = (ImageView) findViewById(R.id.activity_bbslist_menu_iv_write_article);
        /**
         * 进度条、重试
         */
        mProgressBar = (Win8ProgressBar) findViewById(R.id.progress_bar);
        /**
         * help
         */
        mHelpImageView = (ImageView) findViewById(R.id.activity_bbslist_iv_help);
        if (mSwitchMgr.needShowPageHelp()) {
            mHelpImageView.setVisibility(View.VISIBLE);
            mHelpImageView.bringToFront();
            mSwitchMgr.disableShowPageHelp();
        }
        mFaceIV = (CircleImageView) findViewById(R.id.activity_bbs_list_civ_face);
        mFaceBV = new BadgeView(this, mFaceIV);
        mFaceBV.setText("");
        mFaceBV.setUseDefaultParams(false);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                mIUMgr.dip2px(10), mIUMgr.dip2px(10));
        params.gravity = Gravity.RIGHT | Gravity.TOP;
        params.setMargins(0, 0, 0, 0);
        mFaceBV.setLayoutParams(params);

        /**
         * 搜索
         */
        mSearchLLayout = (LinearLayout) findViewById(R.id.activity_bbslist_ll_search);
        mSearchET = (EditText) findViewById(R.id.activity_bbslist_et_search);
        mSearchET.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                    KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH:
                        onSearchClick(null);
                        break;
                }
                return true;
            }
        });
        /**
         * ResideMenu
         */
        mResideMenu = new ResideMenu(this);
        mResideMenu.setMenuListener(this);
        // 背景设置
        if (mSwitchMgr.isNightModeEnabled()) {
            if (mSwitchMgr.isSimpleModeEnabled()) {
                mResideMenu.setBackgroundColor(getResources().getColor(
                        R.color.bbs_background_night));
            } else {
                try {
                    mResideMenu.setBackground(R.drawable.background_night);
                } catch (OutOfMemoryError e) {
                    mResideMenu.setBackgroundColor(getResources().getColor(
                            R.color.bbs_background_night));
                    Logcat.e(TAG, "OutOfMemoryError:" + e.getMessage());
                }
            }
        } else {
            if (mSwitchMgr.isSimpleModeEnabled()) {
                mResideMenu.setBackgroundColor(getResources().getColor(
                        R.color.bbs_background_day));
            } else {
                try {
                    mResideMenu.setBackground(R.drawable.background_day);
                } catch (OutOfMemoryError e) {
                    mResideMenu.setBackgroundColor(getResources().getColor(
                            R.color.bbs_background_day));
                }
            }
        }
        mResideMenu.attachToActivity(this);
        mResideMenu.setScaleValue(0.6f);
        mResideMenu.setShadowVisible(false);
        // mResideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);

        mHomepageItem = new ResideMenuItem(this, R.drawable.icon_home_page,
                getString(R.string.topten));
        mLoginItem = new ResideMenuItem(this, R.drawable.icon_login, getString(R.string.login));
        if (mSwitchMgr.isNightModeEnabled()) {
            mModeItem = new ResideMenuItem(this, R.drawable.icon_sun, "日间模式");
        } else {
            mModeItem = new ResideMenuItem(this, R.drawable.icon_moon, "夜间模式");
        }
        // 登录后的
        mUserInfoItem = new ResideMenuItem(this);
        mReplyItem = new ResideMenuItem(this, R.drawable.icon_reply_me, "回我");
        mAiItem = new ResideMenuItem(this, R.drawable.icon_at_me, "@我");
        mMailItem = new ResideMenuItem(this, R.drawable.icon_mail, "邮箱");
        mVoteItem = new ResideMenuItem(this, R.drawable.icon_vote, "投票");
        mPhotoShowItem = new ResideMenuItem(this, R.drawable.icon_photo, "贴图秀");

        mHomepageItem.setOnClickListener(this);
        mLoginItem.setOnClickListener(this);
        mModeItem.setOnClickListener(this);

        mUserInfoItem.setOnClickListener(this);
        mReplyItem.setOnClickListener(this);
        mAiItem.setOnClickListener(this);
        mMailItem.setOnClickListener(this);
        mVoteItem.setOnClickListener(this);
        mPhotoShowItem.setOnClickListener(this);
        mUserInfoItem.setOnClickListener(this);

        mResideMenu.addMenuItem(mUserInfoItem);
        mResideMenu.addMenuItem(mReplyItem);
        mResideMenu.addMenuItem(mAiItem);
        mResideMenu.addMenuItem(mMailItem);
        mResideMenu.addMenuItem(mLoginItem);
        mResideMenu.addSeparator(this, 5, RESIDE_MENU_DIVIDER);
        mResideMenu.addMenuItem(mVoteItem);
        if (Build.VERSION.SDK_INT >= 11) {
            mResideMenu.addMenuItem(mPhotoShowItem);
        }
        mResideMenu.addMenuItem(mHomepageItem);
        mResideMenu.addSeparator(this, 5, RESIDE_MENU_DIVIDER);
        mResideMenu.addMenuItem(mModeItem);

        mReplyBV = new BadgeView(this, mReplyItem);
        mAtBV = new BadgeView(this, mAiItem);
        mMailBV = new BadgeView(this, mMailItem);

        /**
         * 广播接收类
         */
        mReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BBSManager.REFER_RECEIVER_ACTION);
        // 注册
        registerReceiver(mReceiver, filter);
        /**
         * 收藏版面
         */
        initFavorite();
    }

    /**
     * 点击右上角按钮，打开弹出菜单
     * 
     * @param view
     */
    public void openPopmenu(View view) {
        // 全部隐藏
        mArticleMenu.setVisibility(View.GONE);
        mReferMenu.setVisibility(View.GONE);
        mMailMenu.setVisibility(View.GONE);
        mVoteMenu.setVisibility(View.GONE);

        mDeletetIV.setVisibility(View.GONE);

        mSerchTitleIV.setVisibility(View.VISIBLE);
        mSearchAuthorIV.setVisibility(View.VISIBLE);
        mWriteArticleIV.setVisibility(View.VISIBLE);

        switch (mMember.beanType) {
            case BOARD:
                mArticleMenu.setVisibility(View.VISIBLE);
                // guest禁用发帖
                if (BBSManager.GUEST.equals(mUser.id)) {
                    mWriteArticleIV.setVisibility(View.INVISIBLE);
                }
                break;
            case COLLECT:
                mDeletetIV.setVisibility(View.VISIBLE);
                break;
            case MAILBOX:
                mMailMenu.setVisibility(View.VISIBLE);
                break;
            case REFER:
                mReferMenu.setVisibility(View.VISIBLE);
                break;
            case SEARCH:
                Toast.makeText(getBaseContext(), "无可用菜单", Toast.LENGTH_SHORT)
                        .show();
                return;
            case VOTELIST:
                mVoteMenu.setVisibility(View.VISIBLE);
                break;
            case WIDGET:
                mArticleMenu.setVisibility(View.VISIBLE);
                mSerchTitleIV.setVisibility(View.GONE);
                mSearchAuthorIV.setVisibility(View.GONE);
                mWriteArticleIV.setVisibility(View.GONE);
                break;
            default:
                Toast.makeText(getBaseContext(), "无可用菜单", Toast.LENGTH_SHORT)
                        .show();
                return;

        }
        showPopMenu(true);
    }

    @Override
    public void onClick(View v) {
        if (v == mReplyItem) {
            // 回我
            selectTag(BeanType.REFER, ReferType.REPLY);
        } else if (v == mAiItem) {
            // @我
            selectTag(BeanType.REFER, ReferType.AT);
        } else if (v == mMailItem) {
            // 邮箱
            selectTag(BeanType.MAILBOX, MailboxType.INBOX);
        } else if (v == mVoteItem) {
            // 投票
            selectTag(BeanType.VOTELIST, VoteType.ALL);
        } else if (v == mPhotoShowItem) {
            // 贴图秀
            startPhotoShow();
        } else if (v == mLoginItem) {
            // 登录
            ActivityFunc.startActivityForResult(this, BBSLoginActivity.class, null,
                    REQUESTCODE_LOGIN);
        } else if (v == mModeItem) {
            // 日间|夜间模式切换
            if (mSwitchMgr.isNightModeEnabled()) {
                mSwitchMgr.enableNightMode(false);
            } else {
                mSwitchMgr.enableNightMode(true);
            }
            mIsOpenScreen = false;
            setRelease(false);
            finish();
            Intent intent = new Intent(BBSListActivity.this,
                    BBSListActivity.class);
            intent.putExtra(KEY_OPENSCREEN, mIsOpenScreen);
            ActivityFunc.startActivity(BBSListActivity.this, intent);
        } else if (v == mUserInfoItem) {
            // 用户信息
            if (null != mUser) {
                ActivityFunc.startActivity(this, BBSUserInfoActivity.class, mUser,
                        false);
            }
        } else if (v == mHomepageItem) {
            // 主页，即十大热点
            selectTag(BeanType.WIDGET, null);
        } else if (v.getId() == R.id.activity_bbslist_iv_section) {
            // 打开分区列表
            ActivityFunc.startActivityForResult(this, BBSSectionActivity.class,
                    null, REQUESTCODE_SECTION);
        } else if (v == mHelpImageView) {
            // 帮助
            mHelpImageView.setVisibility(View.GONE);
        }
    }

    /**
     * 打开贴图秀Activity
     */
    private void startPhotoShow() {
        NetWorkManager.NetStatus netStatus = NetWorkManager.getInstance(getBaseContext())
                .getNetworkType();
        switch (netStatus) {
            case NOTHING:
            case NONE:
                Toast.makeText(getBaseContext(), NetWorkManager.MSG_NONET, Toast.LENGTH_SHORT)
                        .show();
                break;
            case NETTYPE_WIFI:
                Intent intent = new Intent(this, BBSPhotoShowActivity.class);
                ActivityFunc.startActivity(BBSListActivity.this, intent);
                break;
            case NETTYPE_CMWAP:
            case NETTYPE_CMNET:
                final CustomDialog dialog = new CustomDialog(this);
                dialog.setMessage("非wifi网络，会产生大量流量。\n继续吗？")
                        .setOKButton(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                // 打开贴图秀
                                Intent intent = new Intent(BBSListActivity.this,
                                        BBSPhotoShowActivity.class);
                                ActivityFunc.startActivity(BBSListActivity.this, intent);
                            }

                        }).setCancelButton(null).show();
                break;
        }
    }

    private void selectTag(BeanType type, Object subType) {
        HttpManager.getInstance(getBaseContext()).disconnect(getBaseContext());
        mMember.beanType = type;
        switch (mMember.beanType) {
            case BOARD:
                mMember.beanType = BeanType.BOARD;
                Board board = (Board) subType;
                mMember.boardName = board.name;
                mMember.title = board.description;
                updateTitle();
                showFooter(true);
                break;
            case COLLECT:
                mMember.title = getString(R.string.collect);
                showFooter(false);
                break;
            case MAILBOX:
                mMember.mailboxType = (MailboxType) subType;
                switch (mMember.mailboxType) {
                    case DELETED:
                        mMember.title = getString(R.string.deleted);
                        break;
                    case INBOX:
                        mMember.title = getString(R.string.inbox);
                        NotificationMgr.getInstance().cancel(NotifyType.MAIL);
                        break;
                    case OUTBOX:
                        mMember.title = getString(R.string.outbox);
                        break;
                }
                showFooter(true);
                break;
            case REFER:
                mMember.referType = (ReferType) subType;
                switch (mMember.referType) {
                    case AT:
                        mMember.title = getString(R.string.at_me);
                        NotificationMgr.getInstance().cancel(NotifyType.AT);
                        break;
                    case REPLY:
                        mMember.title = getString(R.string.reply_me);
                        NotificationMgr.getInstance().cancel(NotifyType.REPLY);
                        break;
                }
                showFooter(true);
                break;
            case SEARCH:
                break;
            case VOTELIST:
                mMember.voteType = (VoteType) subType;
                switch (mMember.voteType) {
                    case ALL:
                        mMember.title = getString(R.string.vote_all);
                        break;
                    case HOT:
                        mMember.title = getString(R.string.vote_hot);
                        break;
                    case JOIN:
                        mMember.title = getString(R.string.vote_join);
                        break;
                    case ME:
                        mMember.title = getString(R.string.vote_me);
                        break;
                    case NEW:
                        mMember.title = getString(R.string.vote_newest);
                        break;
                }
                showFooter(true);
                break;
            case WIDGET:
                mMember.title = getString(R.string.topten);
                showFooter(false);
                break;
        }
        updateTitle();
        startThread(false, 1, true);
    }

    /**
     * 弹出菜单的点击事件
     * 
     * @param view
     */
    public void onMenuClick(View view) {
        int nId = view.getId();
        if (R.id.activity_bbslist_fl_menu == nId) {
            // 关闭menu
        } else if (R.id.activity_bbslist_menu_iv_collect == nId) {
            // 查看收藏
            selectTag(BeanType.COLLECT, null);
        } else if (R.id.activity_bbslist_menu_iv_write_article == nId) {
            // 撰写文章
            Intent intent = new Intent(BBSListActivity.this,
                    BBSWriteActivity.class);
            intent.putExtra(BBSWriteActivity.WRITE_ARTICLE, mMember.getBoard());
            ActivityFunc.startActivity(BBSListActivity.this, intent);
        } else if (R.id.activity_bbslist_menu_iv_search_title == nId) {
            // 查主题
            showSearch(true, true);
        } else if (R.id.activity_bbslist_menu_iv_search_author == nId) {
            // 查作者
            showSearch(true, false);
        } else if (R.id.activity_bbslist_menu_iv_delete_refer == nId) {
            // 删除所有提醒
            final CustomDialog dialog = new CustomDialog(this);
            dialog.setMessage("确定删除所有提醒吗？")
                    .setOKButton(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            showPopMenu(false);
                            startOperationThread(-1, ACTION_DELETE);
                        }
                    }).setCancelButton(null).show();
            return;
        } else if (R.id.activity_bbslist_menu_bt_read == nId) {
            // 所有提醒标为已读
            startOperationThread(-1, ACTION_SET_READ);
        } else if (R.id.activity_bbslist_menu_iv_delete_mail == nId) {
            // 删除所有邮件
            final CustomDialog dialog = new CustomDialog(this);
            dialog.setMessage("确定删除当前所有邮件吗？")
                    .setOKButton(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            showPopMenu(false);
                            startOperationThread(-1, ACTION_DELETE);
                        }
                    }).setCancelButton(null).show();
            return;
        } else if (R.id.activity_bbslist_menu_iv_write_mail == nId) {
            // 发邮件
            Intent intent = new Intent(BBSListActivity.this,
                    BBSWriteActivity.class);
            ActivityFunc.startActivity(BBSListActivity.this, intent);
        } else if (R.id.activity_bbslist_menu_bt_all == nId) {
            // 所有投票
            selectTag(BeanType.VOTELIST, VoteType.ALL);
        } else if (R.id.activity_bbslist_menu_bt_new == nId) {
            // 最新投票
            selectTag(BeanType.VOTELIST, VoteType.NEW);
        } else if (R.id.activity_bbslist_menu_bt_hot == nId) {
            // 热门投票
            selectTag(BeanType.VOTELIST, VoteType.HOT);
        } else if (R.id.activity_bbslist_menu_bt_me == nId) {
            // 我的投票
            selectTag(BeanType.VOTELIST, VoteType.ME);
        } else if (R.id.activity_bbslist_menu_bt_join == nId) {
            // 我参与的投票
            selectTag(BeanType.VOTELIST, VoteType.JOIN);
        } else if (R.id.activity_bbslist_menu_iv_delete_collect == nId) {
            // 删除收藏
            final CustomDialog dialog = new CustomDialog(this);
            dialog.setMessage("确定清空收藏吗？")
                    .setOKButton(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            showPopMenu(false);
                            BBSManager.clearArticleCollect();
                            startThread(false, 1, true);
                        }
                    }).setCancelButton(null).show();
            return;
        }
        showPopMenu(false);
    }

    /**
     * 点击搜索
     * 
     * @param view
     */
    public void onSearchClick(View view) {
        int nId = 0;
        if (view != null) {
            nId = view.getId();
        }
        if (R.id.activity_bbslist_iv_search == nId || null == view) {
            mMember.searchTitle = mSearchET.getText().toString();
            if (TextUtils.isEmpty((mMember.searchTitle))) {
                Toast.makeText(getBaseContext(), "请输入搜索内容", Toast.LENGTH_SHORT).show();
            } else {
                // 隐藏 输入法
                AiYouManager.viewInputMethod(BBSListActivity.this, false, mSearchET);
                startThread(false, 1, true);
            }
        } else if (R.id.activity_bbslist_iv_clearsearch == nId) {
            showSearch(false, false);
        }
    }

    /**
     * 启动线程
     * 
     * @param isUserQuery
     * @param page
     * @param showProgress 是否显示进度条
     */
    private void startThread(boolean isUserQuery, int page, boolean showProgress) {
        /**
         * 刷新而非加载更多时，设置进度条状态，清空列表
         */
        if (page == 1) {
            if (showProgress) {
                showProgress(true);
            }
            clearListView();
        }

        if (isUserQuery) {
            threadUserQuery();
        } else {
            /**
             * 收藏
             */
            if (mMember.beanType == BeanType.COLLECT) {
                if (showProgress) {
                    showProgress(true);
                }
                clearListView();
                Article[] article = BBSManager.getArticleCollect();
                if (null == article || article.length == 0) {
                    Toast.makeText(getBaseContext(), "还没有收藏", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    mList.addAll(Arrays.asList(article));
                    mAdapter.notifyDataSetChanged();
                }
                // 后续工作
                showProgress(false);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mPTRListView.onRefreshComplete();
                    }
                });
            } else {
                threadGetList(page);
            }
        }
    }

    /**
     * 启动操作线程
     * 
     * @param index -1删除所有
     * @param type 操作类型:delete——删除；setRead——标记为已读
     */
    private void startOperationThread(final int index, final String type) {
        if (index > -1 && type.equals(ACTION_SET_READ)) {
        } else {
            showProgress(true);
        }
        threadOperation(index, type);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        Intent intent = null;
        switch (mMember.beanType) {
            case WIDGET:
            case SEARCH:
            case COLLECT:
            case BOARD:
                Article article = (Article) mList.get(position - 1);
                intent = new Intent(this, BBSContentActivity.class);
                intent.putExtra(BBSContentActivity.KEY_ARTICLE, article);
                break;
            case REFER:
                Refer refer = (Refer) mList.get(position - 1);
                if (!refer.is_read) {
                    mFaceBV.hide(true);
                    refer.is_read = true;
                    final TextView tv = (TextView) view
                            .findViewById(R.id.list_item_bbslist_board_tv_title);
                    if (mSwitchMgr.isNightModeEnabled()) {
                        tv.setTextColor(getResources().getColor(R.color.font_night));
                    } else {
                        tv.setTextColor(getResources().getColor(
                                R.color.font_black_day));
                    }
                    int count = 0;
                    if (mMember.referType == ReferType.REPLY) {
                        count = mBBSMgr
                                .getBBSNotificationRefer(ReferType.REPLY) - 1;
                        mBBSMgr.setBBSNotificationRefer(ReferType.REPLY,
                                count);
                        if (count > 0) {
                            mReplyBV.setText(count + "");
                        } else {
                            mReplyBV.hide(true);
                        }
                    } else if (mMember.referType == ReferType.AT) {
                        count = mBBSMgr
                                .getBBSNotificationRefer(ReferType.AT) - 1;
                        mBBSMgr.setBBSNotificationRefer(ReferType.AT, count);
                        if (count > 0) {
                            mAtBV.setText(count + "");
                        } else {
                            mAtBV.hide(true);
                        }
                    }
                    startOperationThread(refer.index, ACTION_SET_READ);
                }
                intent = new Intent(this, BBSContentActivity.class);
                intent.putExtra(BBSContentActivity.KEY_REFER, refer);
                break;
            case VOTELIST:
                Vote vote = (Vote) mList.get(position - 1);
                intent = new Intent(this, BBSVoteActivity.class);
                intent.putExtra(BBSVoteActivity.KEY_VOTE, vote);
                break;
            case MAILBOX:
                Mail mail = (Mail) mList.get(position - 1);
                if (!mail.is_read) {
                    mFaceBV.hide(true);
                    mail.is_read = true;
                    final TextView tv = (TextView) view
                            .findViewById(R.id.list_item_bbslist_board_tv_title);
                    if (mSwitchMgr.isNightModeEnabled()) {
                        tv.setTextColor(getResources().getColor(R.color.font_night));
                    } else {
                        tv.setTextColor(getResources().getColor(
                                R.color.font_black_day));
                    }
                    mBBSMgr.setBBSNotificationMail(false);
                    mMailBV.hide(true);
                }
                intent = new Intent(this, BBSMailActivity.class);
                intent.putExtra(BBSMailActivity.KEY_MAIL, mail);
                break;
        }
        if (null != intent) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ActivityFunc.startActivity(BBSListActivity.this, intent);
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        startThread(false, 1, true);
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
        int page = mMember.getPageCurrent() + 1;
        if (page > mMember.getPageTotal()) {
            return false;
        } else {
            startThread(false, page, showProgress);
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUESTCODE_LOGIN) {
                // 用户登录返回的结果
                User userTmp = (User) data.getSerializableExtra(BBSLoginActivity.KEY_USER);
                if (null != userTmp) {
                    if (!userTmp.id.equals(mUser.id)) {
                        // 设置为当前用户
                        mUser = userTmp;
                        // 左侧用户窗口
                        updateUserWindow(mUser);
                    } else {
                    }
                }
            } else if (requestCode == REQUESTCODE_SECTION) {
                // 分区列表返回的结果
                Board board = new Board();
                board.name = data.getStringExtra(BBSSectionActivity.KEY_NAME);
                board.description = data.getStringExtra(BBSSectionActivity.KEY_DESC);
                selectTag(BeanType.BOARD, board);
            }
        }
    }

    /**
     * 查询用户信息的线程,由 {@link #startThread(boolean, int)} 启动
     */
    private void threadUserQuery() {
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            if (null != mHandler) {
                mHandler.sendEmptyMessage(MSG_ERROR);
            }
            return;
        }
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                String strJson = User.query(BBSListActivity.this, mBBSMgr.getUserId());
                if (TextUtils.isEmpty(strJson)) {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(MSG_ERROR);
                    }
                    return;
                }
                String strError = JsonHelper.checkError(strJson);
                if (null != strError && mHandler != null) {
                    // 是 错误信息
                    if (mHandler != null) {
                        Message msg = mHandler.obtainMessage(MSG_ERROR);
                        Bundle data = msg.getData();
                        data.putString(KEY_DATA, strError);
                        mHandler.sendMessage(msg);
                    }
                    return;
                }
                // 将json数据解析为User元数据
                User user = new User(strJson);
                if (mHandler != null) {
                    Message msg = mHandler.obtainMessage(MSG_USER_QUERY);
                    Bundle data = msg.getData();
                    data.putSerializable(KEY_DATA, user);
                    mHandler.sendMessage(msg);
                }
            }
        });
    }

    /**
     * 查询信息的线程，由 {@link #startThread(boolean, int)} 启动
     * 
     * @param page
     */
    private void threadGetList(final int page) {
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
                Logcat.e(TAG, "page:" + page);
                switch (mMember.beanType) {
                    case WIDGET:
                        strJson = Widget.getTopten(BBSListActivity.this);
                        break;
                    case BOARD:
                        strJson = Board.getBoard(BBSListActivity.this, mMember.boardName, page);
                        break;
                    case REFER:
                        strJson = Refer.getRefer(BBSListActivity.this, mMember.referType, page);
                        break;
                    case SEARCH:
                        strJson = Search.getSearch(BBSListActivity.this, mMember.boardName,
                                mMember.searchTitle, mMember.isSearchTitle, page);
                        break;
                    case VOTELIST:
                        strJson = VoteList.getVoteList(BBSListActivity.this, mMember.voteType,
                                page);
                        break;
                    case MAILBOX:
                        strJson = Mailbox.getMailBox(BBSListActivity.this, mMember.mailboxType,
                                page);
                        break;
                    case COLLECT:
                        break;
                }
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
                // 将json数据解析为元数据
                updateBBSListMember(strJson);
                if (null != mHandler) {
                    mHandler.sendEmptyMessage(MSG_GET_LIST);
                }
            }
        });
    }

    /**
     * 操作的线程，由 {@link #startOperationThread(int, String)} 启动
     * 
     * @param index -1删除所有
     * @param type 操作类型:ACTION_DELETE——删除；ACTION_SET_READ——标记为已读
     */
    private void threadOperation(final int index, final String type) {
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            if (null != mHandler) {
                mHandler.sendEmptyMessage(MSG_ERROR);
            }
            return;
        }
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                Bundle data = new Bundle();
                data.putBoolean(KEY_REFRESH_DATA, true);
                data.putInt(KEY_INDEX, index);
                data.putString(KEY_TYPE, type);

                String strJson = null;
                switch (mMember.beanType) {
                    case REFER:
                        if (ACTION_DELETE.equals(type)) {
                            if (-1 == index) {
                                // 全部删除
                                strJson = Refer.deleteRefer(BBSListActivity.this,
                                        mMember.referType);
                            } else {
                                // 删除一条
                                strJson = Refer.deleteRefer(BBSListActivity.this,
                                        mMember.referType,
                                        index);
                            }
                        } else if (ACTION_SET_READ.equals(type)) {
                            if (-1 == index) {
                                // 全部标记为已读
                                strJson = Refer
                                        .setRead(BBSListActivity.this, mMember.referType);
                            } else {
                                data.putBoolean(KEY_REFRESH_DATA, false);
                                strJson = Refer
                                        .setRead(BBSListActivity.this, mMember.referType, index);
                            }
                        }
                        break;
                    case MAILBOX:
                        if (-1 == index) {
                            // 删除list中所有邮件
                            Mail mail = null;
                            for (AdapterInterface adapterInterface : mList) {
                                mail = (Mail) adapterInterface;
                                if (null != mail) {
                                    strJson = Mail.deleteMail(BBSListActivity.this,
                                            MailboxType.INBOX,
                                            mail.index);
                                }
                            }
                        } else {
                            // 删除一封邮件
                            strJson = Mail.deleteMail(BBSListActivity.this, MailboxType.INBOX,
                                    index);
                        }
                        break;
                    default:
                        break;
                }
                if (TextUtils.isEmpty(strJson)) {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(MSG_ERROR);
                    }
                }
                String strError = JsonHelper.checkError(strJson);
                if (null != strError) {
                    // 是 错误信息
                    if (mHandler != null) {
                        Message msg = mHandler.obtainMessage(MSG_ERROR);
                        Bundle bundle = msg.getData();
                        bundle.putString(KEY_DATA, strError);
                        mHandler.sendMessage(msg);
                    }
                    return;
                }
                if (null != mHandler) {
                    Message msg = mHandler.obtainMessage(MSG_OPERATION);
                    msg.setData(data);
                    mHandler.sendMessage(msg);
                }
            }
        });
    }

    /**
     * 更新用户信息窗口
     * 
     * @param user
     */
    private void updateUserWindow(User user) {
        mReplyItem.setVisibility(View.GONE);
        mAiItem.setVisibility(View.GONE);
        mMailItem.setVisibility(View.GONE);
        mVoteItem.setVisibility(View.GONE);
        mLoginItem.setVisibility(View.GONE);
        if (null != user.id && !BBSManager.GUEST.equals(user.id)) {
            mReplyItem.setVisibility(View.VISIBLE);
            mAiItem.setVisibility(View.VISIBLE);
            mMailItem.setVisibility(View.VISIBLE);
            mVoteItem.setVisibility(View.VISIBLE);

            mMailBV.setText("新");
            int count = 0;
            count = mBBSMgr.getBBSNotificationRefer(ReferType.REPLY);
            if (0 != count) {
                mReplyBV.setText(count + "");
                mReplyBV.show(true);
                mFaceBV.show(true);
            }
            count = mBBSMgr.getBBSNotificationRefer(ReferType.AT);
            if (0 != count) {
                mAtBV.setText(count + "");
                mAtBV.show(true);
                mFaceBV.show(true);
            }
            if (mBBSMgr.getBBSNotificationMail()) {
                mMailBV.show(true);
                mFaceBV.show(true);
            }
        } else {
            mLoginItem.setVisibility(View.VISIBLE);
        }
        mUserInfoItem.setUserInfo(user.face_url, user.id, user.user_name);
        mFaceIV.setImageUrl(user.face_url, R.drawable.iu_default_gray,
                R.drawable.iu_default_green);
    }

    /**
     * 更新BeanType变量
     */
    private void updateBBSListMember(String strJson) {
        switch (mMember.beanType) {
            case WIDGET:
                Widget widget = new Widget(strJson);
                mMember.updateWidget(widget);
                break;
            case BOARD:
                Board board = new Board(strJson);
                mMember.updateBoard(board);
                break;
            case REFER:
                Refer refer = new Refer(strJson);
                mMember.updateRefer(refer);
                break;
            case SEARCH:
                Search search = new Search(strJson);
                mMember.updateSearch(search);
                break;
            case VOTELIST:
                VoteList voteList = new VoteList(strJson);
                mMember.updateVoteList(voteList);
                break;
            case MAILBOX:
                Mailbox mailbox = new Mailbox(strJson);
                mMember.updateMailbox(mailbox);
                break;
            default:
                break;
        }
    }

    /**
     * 关闭Activity
     * 
     * @param view
     */
    public void selfFinish() {
        if (Build.VERSION.SDK_INT >= 14) {
            ActivitySplitAnimationUtil.finish(this);
        } else {
            scrollToFinishActivity();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 按下键盘上返回按钮
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (View.VISIBLE == mMenuFLayout.getVisibility()) {
                showPopMenu(false);
                return true;
            } else if (mResideMenu.isOpened()) {
                mResideMenu.closeMenu();
                return true;
            }
            selfFinish();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (View.VISIBLE == mMenuFLayout.getVisibility()) {
                showPopMenu(false);
            } else {
                openPopmenu(null);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void openMenu() {
        mFaceBV.hide(true);
        mUserInfoItem.startShimmer();
        showPopMenu(false);
    }

    @Override
    public void closeMenu() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /**
         * 注销广播接收器
         */
        unregisterReceiver(mReceiver);
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mList.clear();
        mList = null;
        mAdapter = null;
        // mUser = null;
        ActivitySplitAnimationUtil.cancel();
        // 设置版面类型
        if (mMember.beanType == BeanType.SEARCH) {
            mMember.beanType = BeanType.BOARD;
        }
        System.gc();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mResideMenu.dispatchTouchEvent(ev);
    }

    /**
     * 清空listview,由 {@link #startThread(boolean, int)} 调用
     */
    private void clearListView() {
        mFlagLoadMore = false;
        mList.clear();
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 设置是否显示lv_article的footer
     * 
     * @param flag
     */
    @SuppressWarnings("deprecation")
    private void showFooter(boolean flag) {
        if (flag) {
            mPTRListView.setPullLabel("加载", Mode.PULL_FROM_END);
            mPTRListView.setRefreshingLabel("正在加载...", Mode.PULL_FROM_END);
            mPTRListView.setReleaseLabel("松开加载", Mode.PULL_FROM_END);
            Drawable drawable = getResources().getDrawable(
                    R.drawable.default_ptr_rotate);
            mPTRListView.setLoadingDrawable(drawable, Mode.PULL_FROM_END);
        } else {
            mPTRListView.setPullLabel("", Mode.PULL_FROM_END);
            mPTRListView.setRefreshingLabel("", Mode.PULL_FROM_END);
            mPTRListView.setReleaseLabel("", Mode.PULL_FROM_END);
            mPTRListView.setLoadingDrawable(null, Mode.PULL_FROM_END);
        }
    }

    /**
     * 按左上角返回键调用的方法
     * 
     * @param view
     */
    public void openLeftMenu(View view) {
        mResideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
    }

    /**
     * 设置进度条可见
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
     * 接口方法
     */
    @Override
    public void onRayMenuClick(final int position) {
        View v = mListView.getChildAt(position + 1
                - mListView.getFirstVisiblePosition());
        FrameLayout fl_cache = (FrameLayout) v.findViewById(R.id.list_item_bbslist_board_fl_cache);
        fl_cache.setDrawingCacheEnabled(true);
        Bitmap bmp = Bitmap.createBitmap(fl_cache.getDrawingCache());
        fl_cache.setDrawingCacheEnabled(false);

        int[] location = new int[2];
        fl_cache.getLocationOnScreen(location);
        /**
         * 播放吸入动画
         */
        startMesh(bmp, location, fl_cache.getHeight());
        /**
         * 将文章加入收藏
         */
        if (BeanType.BOARD == mMember.beanType
                || BeanType.WIDGET == mMember.beanType
                || BeanType.SEARCH == mMember.beanType) {
            Article article = (Article) mList.get(position);
            BBSManager.putArticleCollect(article);
            if (Build.VERSION.SDK_INT < 11) {
                Toast.makeText(getBaseContext(), "收藏成功", Toast.LENGTH_SHORT).show();
            }
        }
        int delay = 800;
        if (Build.VERSION.SDK_INT < 11) {
            delay = 0;
        }
        /**
         * 删除该条收藏
         */
        if (BeanType.COLLECT == mMember.beanType) {
            Article article = (Article) mList.get(position);
            BBSManager.deleteArticleCollect(article);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startThread(false, 1, true);
                }
            }, delay);
        }
        /**
         * 删除该条refer
         */
        if (BeanType.REFER == mMember.beanType) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Refer refer = (Refer) mList.get(position);
                    startOperationThread(refer.index, ACTION_DELETE);
                }
            }, delay);
        }
        /**
         * 删除该条mail
         */
        if (BeanType.MAILBOX == mMember.beanType) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Mail mail = (Mail) mList.get(position);
                    startOperationThread(mail.index, ACTION_DELETE);
                }
            }, delay);
        }
    }

    /**
     * 播放吸入动画的方法
     * 
     * @param bmp
     * @param location
     * @param viewHeight
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startMesh(Bitmap bmp, int[] location, int viewHeight) {
        if (Build.VERSION.SDK_INT < 11) {
            return;
        }
        // 显示标题栏
        mTitleLLayout.clearAnimation();
        mSectionIV.clearAnimation();
        mTitleLLayout.setTag(null);
        if (View.VISIBLE != mTitleLLayout.getVisibility()) {
            mTitleLLayout.setVisibility(View.VISIBLE);
            mSectionIV.setVisibility(View.VISIBLE);
        }
        /**
         * 计算y轴偏移量
         */
        mMeshLLayout.setPadding(0, 0, 0, AiYouManager.getScreenHeight() - location[1]
                - viewHeight);
        if (null == mMeshView) {
            // 初始化
            mMeshView = new BitmapMesh.MeshView(this, bmp);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            mMeshView.setLayoutParams(params);
            mMeshView.setRotation(180);
            mMeshLLayout.addView(mMeshView);
        } else {
            // 设置Bitmap
            mMeshView.setBitmap(bmp);
        }
        mMeshView.startAnimation(false);
    }

    /**
     * 设置标题
     * 
     * @param title
     */
    private void updateTitle() {
        mTitleSTV.setText(mMember.title);
        if (View.VISIBLE == mSearchLLayout.getVisibility()) {
            showSearch(false, false);
        } else {
            mTitleSTV.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 显示/隐藏搜索栏
     * 
     * @param flag true：显示；false：隐藏
     * @param searchIsTitle 搜主题还是搜作者
     */
    private void showSearch(boolean flag, boolean searchIsTitle) {
        if (flag) {
            // 设置版面类型
            mMember.beanType = BeanType.SEARCH;
            // 设置搜索类型
            mMember.isSearchTitle = searchIsTitle;
            if (searchIsTitle) {
                mSearchET.setHint("搜主题");
            } else {
                mSearchET.setHint("搜作者");
            }
            // 显示搜索栏
            Animation anim = AnimationUtils.loadAnimation(this,
                    R.anim.search_enter);
            mSearchLLayout.setVisibility(View.VISIBLE);
            mTitleSTV.setVisibility(View.GONE);
            mSearchLLayout.startAnimation(anim);
            anim.setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationEnd(Animation arg0) {
                    mSearchLLayout.clearAnimation();
                    // 打开输入法
                    AiYouManager.viewInputMethod(BBSListActivity.this, true, mSearchET);
                }

                @Override
                public void onAnimationRepeat(Animation arg0) {
                }

                @Override
                public void onAnimationStart(Animation arg0) {
                }
            });
        } else {
            // 设置版面类型
            if (mMember.beanType == BeanType.SEARCH) {
                mMember.beanType = BeanType.BOARD;
            }
            // 隐藏 输入法
            AiYouManager.viewInputMethod(BBSListActivity.this, false, mSearchET);
            // 隐藏搜索栏
            Animation anim = AnimationUtils.loadAnimation(this,
                    R.anim.search_exit);
            mSearchLLayout.startAnimation(anim);
            anim.setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationEnd(Animation arg0) {
                    mSearchLLayout.setVisibility(View.GONE);
                    mTitleSTV.setVisibility(View.VISIBLE);
                    mSearchLLayout.clearAnimation();
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

    /**
     * 广播接收器
     * 
     * @author user
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            NotifyType type = (NotifyType) intent.getSerializableExtra(BBSService.KEY_TYPE);
            if (type == null) {
                return;
            }
            int count = 0;
            switch (type) {
                case AT:
                    count = intent.getIntExtra(BBSService.KEY_NEW_COUNT, 0);
                    mAtBV.setText(count + "");
                    mAtBV.show(true);
                    break;
                case MAIL:
                    mMailBV.show(true);
                    break;
                case REPLY:
                    count = intent.getIntExtra(BBSService.KEY_NEW_COUNT, 0);
                    mReplyBV.setText(count + "");
                    mReplyBV.show(true);
                    break;
                default:
                    break;

            }
            mFaceBV.show(true);
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
            mSectionIV.clearAnimation();
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
        if (mMember.beanType == BeanType.REFER) {
            return;
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
                            R.color.bbs_background_night));
                } else {
                    mMenuFLayout.setBackgroundColor(getResources().getColor(
                            R.color.bbs_background_day));
                }
            } else {
                Drawable drawable = AiYouManager.getBlurBg(BBSListActivity.this);
                if (null != drawable) {
                    mMenuFLayout.setBackgroundDrawable(drawable);
                } else {
                    if (mSwitchMgr.isNightModeEnabled()) {
                        mMenuFLayout.setBackgroundColor(getResources().getColor(
                                R.color.bbs_background_night));
                    } else {
                        mMenuFLayout.setBackgroundColor(getResources().getColor(
                                R.color.bbs_background_day));
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
        Animation anim = null;
        Animation anim2 = null;
        mTitleLLayout.setTag("anim");
        if (flag) {
            anim = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_top);
            anim.setDuration(1000);
            mTitleLLayout.setVisibility(View.VISIBLE);
            mTitleLLayout.startAnimation(anim);

            anim2 = AnimationUtils.loadAnimation(this,
                    R.anim.slide_in_from_bottom);
            anim2.setDuration(1000);
            mSectionIV.setVisibility(View.VISIBLE);
            mSectionIV.startAnimation(anim2);
        } else {
            anim = AnimationUtils.loadAnimation(this, R.anim.slide_out_to_top);
            anim.setDuration(1000);
            mTitleLLayout.startAnimation(anim);

            anim2 = AnimationUtils.loadAnimation(this,
                    R.anim.slide_out_to_bottom);
            anim2.setDuration(1000);
            mSectionIV.startAnimation(anim2);
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
        anim2.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                if (!flag) {
                    mSectionIV.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationStart(Animation arg0) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Favorite.mFavorite == null) {
            threadGetFavorite();
        } else {
            refreshFavorite();
        }
    }

    private void refreshFavorite() {
        mFavoriteList.clear();
        mFavoriteList.addAll(Favorite.mFavorite.boards);
        mFavoriteAdapter.notifyDataSetChanged();
    }

    private void initFavorite() {
        mFavoriteAdapter = new FavoriteAdapter(this, mFavoriteList,
                new DeleteFavoriteListener() {
                    @Override
                    public void onDelete(Board board) {
                        threadDeleteFavorite(board);
                    }
                });
        mResideMenu.setAdapter(mFavoriteAdapter);
        mResideMenu.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Board board = mFavoriteList.get(position);
                selectTag(BeanType.BOARD, board);
            }
        });
    }

    private void threadDeleteFavorite(final Board board) {
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            return;
        }
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                String strJson = Favorite.deleteFavorite(BBSListActivity.this, 0, board.name);
                if (TextUtils.isEmpty(strJson)) {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(MSG_ERROR);
                    }
                    return;
                }
                String strError = JsonHelper.checkError(strJson);
                if (strError != null) {
                    if (mHandler != null) {
                        Message msg = mHandler.obtainMessage(MSG_ERROR);
                        Bundle data = msg.getData();
                        data.putString(KEY_DATA, strError);
                        mHandler.sendMessage(msg);
                    }
                    return;
                }
                if (mHandler != null) {
                    Message msg = mHandler.obtainMessage(MSG_FAVORITE);
                    Bundle data = msg.getData();
                    data.putString(KEY_DATA, strJson);
                    mHandler.sendMessage(msg);
                }
            }
        });
    }

    private void threadGetFavorite() {
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            return;
        }
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                String strJson = Favorite.getFavorite(BBSListActivity.this, 0);
                if (!TextUtils.isEmpty(strJson) && JsonHelper.checkError(strJson) == null) {
                    if (mHandler != null) {
                        Message msg = mHandler.obtainMessage(MSG_FAVORITE);
                        Bundle data = msg.getData();
                        data.putString(KEY_DATA, strJson);
                        mHandler.sendMessage(msg);
                    }
                }
            }
        });
    }
}

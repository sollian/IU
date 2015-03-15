
package com.aiyou.bbs;

import java.util.ArrayList;

import com.aiyou.BaseActivity;
import com.aiyou.R;
import com.aiyou.bbs.bean.Mail;
import com.aiyou.bbs.bean.Mailbox.MailboxType;
import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.utils.ActivityFunc;
import com.aiyou.utils.AiYouManager;
import com.aiyou.utils.JsonHelper;
import com.aiyou.utils.NetWorkManager;
import com.aiyou.utils.SwitchManager;
import com.aiyou.utils.thread.ThreadUtils;
import com.aiyou.utils.time.TimeUtils;
import com.aiyou.view.ScrollTextView;
import com.aiyou.viewLargeImage.ViewLargeImageActivity;

import external.OtherView.CircleImageView;
import external.OtherView.Win8ProgressBar;
import external.SmartImageView.SmartImageView;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebSettings.PluginState;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

/**
 * 显示Mail内容
 * 
 * @author sollian
 */
public class BBSMailActivity extends BaseActivity {
    public static final String KEY_MAIL = "mail";

    private static final int MSG_MAIL_FORWARD = 0;
    private static final int MSG_MAIL = 1;
    private static final int MSG_ERROR = -1;
    private static final String KEY_DATA = "data";

    private SwitchManager mSwitchMgr;
    private AiYouManager mIUMgr;

    private ArrayList<String> mImgUrlList = new ArrayList<String>();
    private int mImgId = 0;
    /**
     * mail变量
     */
    private Mail mMail;
    /**
     * 存放webview
     */
    private ArrayList<WebView> mWebViewList = new ArrayList<WebView>();

    private ScrollTextView mTitleSTV;
    private CircleImageView mFaceCIV;
    private TextView mAuthorTV, mDateTV;
    private LinearLayout mContentLLayout;
    // 进度条
    private FrameLayout mProgressFLayout;
    private Win8ProgressBar mProgressBar;
    // 转寄
    private EditText mForwardET;

    private Handler mHandler = new Handler(new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            if (MSG_MAIL == msg.what) {
                if (mMail != null) {
                    showContent();
                }
            } else if (MSG_MAIL_FORWARD == msg.what) {
                Toast.makeText(getBaseContext(), "转发成功", Toast.LENGTH_SHORT)
                        .show();
            } else if (MSG_ERROR == msg.what) {
                Bundle data = msg.getData();
                String strError = data.getString(KEY_DATA);
                if (strError == null) {
                    strError = NetWorkManager.MSG_NONET;
                }
                // 连接服务器失败
                Toast.makeText(getBaseContext(), strError, Toast.LENGTH_SHORT)
                        .show();
            }
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
        setContentView(R.layout.activity_bbs_mail);

        init();

        Intent intent = getIntent();
        mMail = (Mail) intent.getSerializableExtra(KEY_MAIL);
        mTitleSTV.setText(mMail.title);

        startThread(mMail.index, null);
    }

    /**
     * 启动线程的方法
     * 
     * @param index
     * @param userId
     */
    private void startThread(int index, String userId) {
        showProgress(true);
        if (null == userId) {
            // 获取邮件
            threadGetMail(index);
        } else {
            // 转发邮件
            threadForwardMail(index, userId);
        }
    }

    /**
     * 显示内容的方法
     */
    private void showContent() {
        /**
         * 设置头像
         */
        if (mSwitchMgr.isFaceEnabled()) {
            if (null != mMail.user) {
                // 头像
                if (null != mMail.user.face_url) {
                    mFaceCIV.setImageUrl(mMail.user.face_url);
                } else {
                    mFaceCIV.setImageResource(R.drawable.iu_default_green);
                }
                mFaceCIV.setTag(mMail.user);
            }
        } else {
            mFaceCIV.setVisibility(View.GONE);
        }
        /**
         * 设置用户ID
         */
        if (mMail.user != null) {
            mAuthorTV.setText(mMail.user.id);
        }
        if (mSwitchMgr.isNightModeEnabled()) {
            mAuthorTV.setTextColor(Color.parseColor("#00aaaa"));
        } else {
            mAuthorTV.setTextColor(Color.BLUE);
        }
        /**
         * 时间
         */
        mDateTV.setText(TimeUtils.getLocalTime(mMail.post_time));
        /**
         * 内容
         */
        processContent(mContentLLayout, JsonHelper.toHtml(mMail, false));
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

        mImgUrlList.clear();

        String str = null;
        String strHtml = null;
        int index = 0;
        for (int i = 0; i < length; i++) {
            str = array[i].trim();
            if (i != 0 && str.startsWith("=")) {
                index = str.indexOf(">");
                String strImg = str.substring(1, index);
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
        siv.setLayoutParams(param);
        siv.setBackgroundColor(Color.WHITE);
        siv.setAdjustViewBounds(true);
        if (mSwitchMgr.isNightModeEnabled()) {
            siv.setColorFilter(Color.GRAY,
                    android.graphics.PorterDuff.Mode.MULTIPLY);
        }

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
                Intent intent = new Intent(BBSMailActivity.this,
                        ViewLargeImageActivity.class);
                intent.putExtra(ViewLargeImageActivity.KEY_URL, (String) view.getTag());
                intent.putExtra(ViewLargeImageActivity.KEY_URL_LIST, mImgUrlList);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                ActivityFunc.startActivity(BBSMailActivity.this, intent);
            }
        });
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
        wv.setInitialScale(BBSManager.getInstance(getBaseContext()).getWebViewScaleSize());
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

        mWebViewList.add(wv);

        wv.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
        ll.addView(wv);
    }

    /**
     * 回复
     * 
     * @param view
     */
    public void onReply(View view) {
        Intent intent = new Intent(BBSMailActivity.this, BBSWriteActivity.class);
        intent.putExtra(BBSWriteActivity.NEW_MAIL, mMail);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, 0);
    }

    /**
     * 转寄
     * 
     * @param view
     */
    public void onForward(View view) {
        if (View.GONE == mForwardET.getVisibility()) {
            mForwardET.setVisibility(View.VISIBLE);
        } else {
            // 转寄
            String strId = mForwardET.getText().toString().trim();
            if ("".equals(strId)) {
                Toast.makeText(getBaseContext(), "好友为空", Toast.LENGTH_SHORT)
                        .show();
            } else {
                AiYouManager.viewInputMethod(BBSMailActivity.this, false, mForwardET);
                startThread(mMail.index, strId);
                mForwardET.setVisibility(View.GONE);
            }
        }
    }

    public void onClick(View view) {
        int nId = view.getId();
        if (R.id.activity_bbsmail_civ_face == nId) {
            // 点击头像，查看用户信息
            ActivityFunc.startActivity(BBSMailActivity.this,
                    BBSUserInfoActivity.class, mMail.user, false);
        }
    }

    /**
     * 获取邮件的线程 由 {@link #startThread(int, String)} 启动
     * 
     * @param index
     */
    private void threadGetMail(final int index) {
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            if (null != mHandler) {
                mHandler.sendEmptyMessage(MSG_ERROR);
            }
            return;
        }
        ThreadUtils.execute(new Runnable() {
            public void run() {
                String strJson = Mail.getMail(BBSMailActivity.this, MailboxType.INBOX, index);
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
                mMail = new Mail(strJson);
                if (null != mHandler) {
                    mHandler.sendEmptyMessage(MSG_MAIL);
                }
            }
        });
    }

    /**
     * 转寄邮件的线程 由 {@link #startThread(int, String)} 启动
     * 
     * @param index
     * @param userId
     */
    private void threadForwardMail(final int index, final String userId) {
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            if (null != mHandler) {
                mHandler.sendEmptyMessage(MSG_ERROR);
            }
            return;
        }
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                String strJson = Mail.forwardMail(BBSMailActivity.this, index, userId);
                if (TextUtils.isEmpty(strJson)) {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(MSG_ERROR);
                    }
                    return;
                }
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
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(MSG_MAIL_FORWARD);
                }
            }
        });
    }

    private void init() {
        mIUMgr = AiYouManager.getInstance(getBaseContext());
        /**
         * 背景图片
         */
        ImageView iv_background = (ImageView) findViewById(R.id.activity_bbsmail_iv_background);

        LinearLayout ll_container = (LinearLayout) findViewById(R.id.activity_bbsmail_ll_container);
        // 是否是简约模式
        if (mSwitchMgr.isSimpleModeEnabled()) {
            iv_background.setVisibility(View.GONE);
            if (mSwitchMgr.isNightModeEnabled()) {
                ll_container.setBackgroundColor(getResources().getColor(
                        R.color.bbs_background_night));
            } else {
                ll_container.setBackgroundColor(getResources().getColor(
                        R.color.bbs_background_day));
            }
        }

        mTitleSTV = (ScrollTextView) findViewById(R.id.activity_bbsmail_stv_title);
        mFaceCIV = (CircleImageView) findViewById(R.id.activity_bbsmail_civ_face);
        mAuthorTV = (TextView) findViewById(R.id.activity_bbsmail_tv_author);
        mDateTV = (TextView) findViewById(R.id.activity_bbsmail_tv_date);
        mContentLLayout = (LinearLayout) findViewById(R.id.activity_bbsmail_ll_content);

        /**
         * 进度条
         */
        mProgressFLayout = (FrameLayout) findViewById(R.id.fl_progress);
        mProgressBar = (Win8ProgressBar) findViewById(R.id.progress_bar);
        /**
         * 转寄
         */
        mForwardET = (EditText) findViewById(R.id.activity_bbsmail_et_forward);
        mForwardET.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                    KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEND:
                        onForward(null);
                        break;
                }
                return true;
            }
        });
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
        mImgUrlList.clear();
        mImgUrlList = null;
        mContentLLayout.removeAllViews();
        clearWebView();
        mMail = null;
        mWebViewList = null;
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        System.gc();
    }

    /**
     * 清理webview
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
}

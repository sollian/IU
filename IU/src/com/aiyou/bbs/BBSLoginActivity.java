
package com.aiyou.bbs;

import com.aiyou.AiYouApplication;
import com.aiyou.BaseActivity;
import com.aiyou.R;
import com.aiyou.bbs.bean.Favorite;
import com.aiyou.bbs.bean.User;
import com.aiyou.bbs.bean.Refer.ReferType;
import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.utils.ActivityFunc;
import com.aiyou.utils.AiYouManager;
import com.aiyou.utils.JsonHelper;
import com.aiyou.utils.NetWorkManager;
import com.aiyou.utils.SwitchManager;
import com.aiyou.utils.thread.ThreadUtils;

import external.OtherView.CircleImageView;
import external.OtherView.FloatLabeledEditText;
import external.OtherView.Win8ProgressBar;
import external.shimmer.Shimmer;
import external.shimmer.ShimmerTextView;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

/**
 * 用户登录页面
 * 
 * @author sollian
 */
public class BBSLoginActivity extends BaseActivity implements OnClickListener {
    public static final String KEY_USER = "user";

    private static final int MSG_USER_QUERY = 1;
    private static final int MSG_ERROR = -1;
    private static final String KEY_ERROR = "errorMsg";

    private static String LOGIN;
    private static String LOGOUT;

    private BBSManager mBBSMgr;
    /**
     * 控件
     */
    private FloatLabeledEditText mIdEditText, mPasswordEditText;
    private Button mLoginBtn;
    private Win8ProgressBar mProgressBar;
    // 显示用户信息
    private CircleImageView mFaceImageView;
    private ShimmerTextView mIdTextView;
    private TextView mNameTextView;
    private String mId;
    private String mPassword;
    /**
     * 用户id扫光特效
     */
    private Shimmer mShimmer;
    /**
     * 用户
     */
    private User mUser;

    private Handler mHandler = new Handler(new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            if (MSG_USER_QUERY == msg.what) {
                // 登录|注销成功
                /**
                 * 设置返回数据
                 */
                // 使用Intent返回数据
                Intent intent = new Intent();
                // 把返回数据存入Intent
                if (null != mUser) {
                    intent.putExtra(KEY_USER, mUser);
                }
                // 设置返回数据
                setResult(RESULT_OK, intent);

                // 更新用户信息
                updateUserWindow(mUser);
            } else if (MSG_ERROR == msg.what) {
                // 出错
                Bundle data = msg.getData();
                String error = data.getString(KEY_ERROR);
                msg.recycle();
                if (error == null) {
                    error = NetWorkManager.MSG_NONET;
                }
                Toast.makeText(getBaseContext(), error, Toast.LENGTH_SHORT)
                        .show();
            }
            showProgressBar(false);
            return true;
        }
    });

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bbs_login);

        LOGIN = getString(R.string.login);
        LOGOUT = getString(R.string.logout);

        init();

        verifyUserInfo();
    }

    @Override
    public void onClick(View view) {
        int nId = view.getId();
        if (R.id.login_btn == nId) {
            String text = mLoginBtn.getText().toString();
            if (text.equals(LOGIN)) {
                login();
            } else {
                logout();
            }
        } else if (R.id.activity_bbslogin_civ_face == nId) {
            // 显示用户详细信息
            if (null != mUser) {
                ActivityFunc.startActivity(this, BBSUserInfoActivity.class, mUser,
                        false);
            }
        }
    }

    private void login() {
        // 验证用户名、密码
        mId = mIdEditText.getTextString().trim();
        mPassword = mPasswordEditText.getTextString().trim();
        if ("".equals(mId) || "".equals(mPassword)) {
            Toast.makeText(getBaseContext(), "用户名或密码不能为空", Toast.LENGTH_SHORT)
                    .show();
            return;
        } else {
            mBBSMgr.setUserInfo(mId, mPassword);
            AiYouManager.viewInputMethod(BBSLoginActivity.this, false, mPasswordEditText);
            verifyUserInfo();
        }
    }

    private void logout() {
        mBBSMgr.clearUserInfo();
        verifyUserInfo();
    }

    /**
     * 验证用户ID和密码
     * 
     * @param strId
     * @param strPassword
     */
    private void verifyUserInfo() {
        threadUserVerify();
    }

    /**
     * 更新用户信息
     * 
     * @param user
     */
    private void updateUserWindow(User user) {
        if (null != user.face_url) {
            mFaceImageView.setImageURL(user.face_url);
        }
        if (null != user.id) {
            if (user.id.equals(BBSManager.GUEST)) {
                mLoginBtn.setText(LOGIN);
            } else {
                mLoginBtn.setText(LOGOUT);
            }
            mIdTextView.setText(user.id);
            if (Build.VERSION.SDK_INT >= 11) {
                if (null != mShimmer && mShimmer.isAnimating()) {
                    mShimmer.cancel();
                }
                mShimmer = new Shimmer();
                mShimmer.setRepeatCount(2);
                mShimmer.setDuration(800);
                mShimmer.start(mIdTextView);
            }
        }
        if (null != user.user_name) {
            mNameTextView.setText(user.user_name);
        }
    }

    /**
     * 验证用户信息的线程
     * 
     * @param strId
     * @param strPassword
     */
    private void threadUserVerify() {
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            if (null != mHandler) {
                mHandler.sendEmptyMessage(MSG_ERROR);
            }
            return;
        }
        showProgressBar(true);
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                String strJson = User.login(getBaseContext());
                if (TextUtils.isEmpty(strJson)) {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(MSG_ERROR);
                    }
                    return;
                }
                String strError = JsonHelper.checkError(strJson);
                if (null != strError) {
                    // 是 错误信息
                    if (null != mHandler) {
                        Message msg = mHandler.obtainMessage(MSG_ERROR);
                        Bundle data = msg.getData();
                        data.putString(KEY_ERROR, strError);
                        msg.setData(data);
                        mHandler.sendMessage(msg);
                    }
                    return;
                }
                initFavorite();
                // 不是 错误信息
                mBBSMgr.setBBSNotificationMail(false);
                mBBSMgr.setBBSNotificationRefer(ReferType.REPLY, 0);
                mBBSMgr.setBBSNotificationRefer(ReferType.AT, 0);
                // 将json数据解析为User元数据
                mUser = new User(strJson);
                /**
                 * 重置分区列表
                 */
                BBSManager.initTreeViewData(getBaseContext(),
                        BBSSectionActivity.mTreeListElements);
                if (null != mHandler) {
                    mHandler.sendEmptyMessage(MSG_USER_QUERY);
                }
            }
        });
    }
    
    private void initFavorite() {
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            return;
        }
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                    String strJson = Favorite.getFavorite(AiYouApplication.getInstance(), 0);
                    if(!TextUtils.isEmpty(strJson) && JsonHelper.checkError(strJson) == null) {
                        Favorite.mFavorite = new Favorite(strJson);
                    }
                }
        });
    }

    private void showProgressBar(boolean flag) {
        if (flag) {
            mLoginBtn.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mLoginBtn.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
        }
    }

    /**
     * 初始化
     */
    @SuppressWarnings("deprecation")
    private void init() {
        mBBSMgr = BBSManager.getInstance(getBaseContext());

        LinearLayout ll_user_info = (LinearLayout) findViewById(R.id.activity_bbslogin_ll_user_info);
        // 设置背景
        Bitmap bmp = (Bitmap) (getIntent().getParcelableExtra(ActivityFunc.KEY_BACKGROUND));
        if (null != bmp) {
            Drawable drawable = new BitmapDrawable(bmp);
            ll_user_info.setBackgroundDrawable(drawable);
        } else {
            if (SwitchManager.getInstance(getBaseContext()).isNightModeEnabled()) {
                ll_user_info.setBackgroundColor(getResources().getColor(
                        R.color.bbs_background_night));
            } else {
                ll_user_info.setBackgroundColor(getResources().getColor(
                        R.color.bbs_background_day));
            }
        }

        mIdEditText = (FloatLabeledEditText) findViewById(R.id.activity_bbslogin_flet_user_id);
        mPasswordEditText = (FloatLabeledEditText) findViewById(R.id.activity_bbslogin_flet_user_password);
        mPasswordEditText.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                    KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_DONE:
                        login();
                        break;
                }
                return true;
            }
        });

        mLoginBtn = (Button) findViewById(R.id.login_btn);
        mLoginBtn.setText(LOGIN);
        mProgressBar = (Win8ProgressBar) findViewById(R.id.progress_bar);

        mFaceImageView = (CircleImageView) findViewById(R.id.activity_bbslogin_civ_face);
        mIdTextView = (ShimmerTextView) findViewById(R.id.activity_bbslogin_shtv_id);
        mNameTextView = (TextView) findViewById(R.id.activity_bbslogin_tv_name);
    }

    /**
     * 注册新账号
     * 
     * @param view
     */
    public void onRegister(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(BBSManager.BBS_URL));
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, 0);
    }

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

        mUser = null;
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        if (Build.VERSION.SDK_INT >= 11) {
            if (null != mShimmer && mShimmer.isAnimating()) {
                mShimmer.cancel();
            }
            mShimmer = null;
        }
        System.gc();
    }
}

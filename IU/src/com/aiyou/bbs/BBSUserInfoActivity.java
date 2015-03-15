
package com.aiyou.bbs;

import com.aiyou.BaseActivity;
import com.aiyou.R;
import com.aiyou.bbs.bean.User;
import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.bbs.utils.UserInfoLayout;
import com.aiyou.utils.ActivityFunc;
import com.aiyou.utils.SwitchManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * 用户信息
 * 
 * @author sollian
 */
public class BBSUserInfoActivity extends BaseActivity {
    /**
     * 用户
     */
    private User mUser;
    private UserInfoLayout mUserInfoLayout;

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
        setContentView(R.layout.activity_user_info);

        init();
    }

    /**
     * 返回按钮点击事件
     * 
     * @param view
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

    /**
     * 发送邮件
     * 
     * @param view
     */
    public void onSendMail(View view) {
        if (BBSManager.GUEST.equals(BBSManager.getInstance(getBaseContext()).getUserId())) {
            Toast.makeText(getBaseContext(), "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(BBSUserInfoActivity.this,
                BBSWriteActivity.class);
        intent.putExtra(BBSWriteActivity.MAIL_TO, mUser.id);
        ActivityFunc.startActivity(this, intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUser = null;
        System.gc();
    }

    @SuppressWarnings("deprecation")
    private void init() {
        mUserInfoLayout = (UserInfoLayout) findViewById(R.id.userinfolayout);

        FrameLayout root = (FrameLayout) findViewById(R.id.rootview);
        /**
         * 设置背景
         */
        Intent intent = getIntent();
        Bitmap bmp = (Bitmap) intent.getParcelableExtra(ActivityFunc.KEY_BACKGROUND);
        if (null != bmp) {
            Drawable drawable = new BitmapDrawable(bmp);
            root.setBackgroundDrawable(drawable);
        } else {
            int color = 0;
            if (SwitchManager.getInstance(getBaseContext()).isNightModeEnabled()) {
                color = getResources().getColor(R.color.bbs_background_night);
            } else {
                color = getResources().getColor(R.color.bbs_background_day);
            }
            root.setBackgroundColor(color);
        }
        mUser = (User) intent.getSerializableExtra(ActivityFunc.KEY_USER);
        mUserInfoLayout.setUser(mUser);
    }

}

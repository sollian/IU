
package com.aiyou;

import com.aiyou.bbs.BBSLoginActivity;
import com.aiyou.bbs.BBSWriteActivity;
import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.utils.ActivityFunc;
import com.aiyou.utils.AiYouManager;
import com.aiyou.utils.SwitchManager;
import com.aiyou.utils.FileCache.ClearCacheTask;
import com.aiyou.utils.FileCache.ClearCacheTask.ClearCacheListener;
import com.aiyou.view.CustomDialog;
import com.aiyou.view.SwitchPreferences;

import external.OtherView.ActivitySplitAnimationUtil;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 设置Activity
 * 
 * @author sollian
 */
public class SetActivity extends BaseActivity implements
        SwitchPreferences.OnPrefChangeListener, OnItemSelectedListener {

    private SwitchManager mSwitchMgr;
    /**
     * 控件
     */
    private SwitchPreferences mFacePref,
            mLargeImgPref, mSimpleModePref, mShakeSharePref, mUpdatePref;
    private Spinner mSpinner;
    // 关于
    private FrameLayout mAboutFLayout;
    private TextView mAboutTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ActivitySplitAnimationUtil.canPlay()
                && Build.VERSION.SDK_INT >= 14) {
            // 中心打开动画
            ActivitySplitAnimationUtil.prepareAnimation(this);
            ActivitySplitAnimationUtil.animate(this, 1000);
        }
        setContentView(R.layout.activity_set);

        init();
    }

    @SuppressWarnings("deprecation")
    private void init() {
        mSwitchMgr = SwitchManager.getInstance(getBaseContext());
        /**
         * 设置
         */
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.activity_set_fl);
        // 设置背景
        Bitmap bmp = (Bitmap) (getIntent().getParcelableExtra(ActivityFunc.KEY_BACKGROUND));
        if (null != bmp) {
            Drawable drawable = new BitmapDrawable(bmp);
            frameLayout.setBackgroundDrawable(drawable);
        } else {
            if (mSwitchMgr.isNightModeEnabled()) {
                frameLayout.setBackgroundColor(getResources().getColor(
                        R.color.bbs_background_night));
            } else {
                frameLayout.setBackgroundColor(getResources().getColor(
                        R.color.bbs_background_day));
            }
        }

        mFacePref = (SwitchPreferences) findViewById(R.id.face_pref);
        mLargeImgPref = (SwitchPreferences) findViewById(R.id.large_image_pref);
        mSimpleModePref = (SwitchPreferences) findViewById(R.id.simple_mode_pref);
        mShakeSharePref = (SwitchPreferences) findViewById(R.id.shake_share_pref);
        mUpdatePref = (SwitchPreferences) findViewById(R.id.update_pref);
        mFacePref.setChecked(mSwitchMgr.isFaceEnabled());
        mLargeImgPref.setChecked(mSwitchMgr.isLargeImageEnabled());
        mSimpleModePref.setChecked(mSwitchMgr.isSimpleModeEnabled());
        mShakeSharePref.setChecked(mSwitchMgr.isShakeShareEnabled());
        mUpdatePref.setChecked(mSwitchMgr.getUpdateOnlyWifi());
        mFacePref.setOnPrefChangeListener(this);
        mLargeImgPref.setOnPrefChangeListener(this);
        mSimpleModePref.setOnPrefChangeListener(this);
        mShakeSharePref.setOnPrefChangeListener(this);
        mUpdatePref.setOnPrefChangeListener(this);
        
        mSpinner = (Spinner)findViewById(R.id.spinner);
        mSpinner.setSelection(mSwitchMgr.getSwipeOut());
        mSpinner.setOnItemSelectedListener(this);
        /**
         * 关于
         */
        mAboutFLayout = (FrameLayout) findViewById(R.id.activity_set_fl_about);
        mAboutTV = (TextView) findViewById(R.id.activity_set_tv_about);
    }

    public void onClick(View v) {
        int nId = v.getId();
        if (R.id.activity_set_ll_account == nId) {
            // 账号设置
            ActivityFunc.startActivity(this, BBSLoginActivity.class, null, false);
        } else if (R.id.activity_set_tv_clear_cache == nId) {
            // 清空缓存
            clearCache();
        } else if (R.id.activity_set_tv_advice == nId) {
            // 意见或建议
            writeAdavice();
        } else if (R.id.activity_set_tv_set_about == nId) {
            // 关于
            showAbout(true);
        } else if (R.id.activity_set_tv_help == nId) {
            // 帮助
            Intent intent = new Intent(SetActivity.this, HelpActivity.class);
            ActivityFunc.startActivity(this, intent);
        } else if (R.id.activity_set_fl_about == nId) {
            // 关闭查看关于
            showAbout(false);
        }
    }

    /**
     * 提意见或建议
     */
    private void writeAdavice() {
        if (BBSManager.GUEST.equals(BBSManager.getInstance(getBaseContext()).getUserId())) {
            Toast.makeText(getBaseContext(), "请先登录", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        Intent intent = new Intent(SetActivity.this, BBSWriteActivity.class);
        intent.putExtra(BBSWriteActivity.MAIL_TO, AiYouManager.AUTHOR);
        ActivityFunc.startActivity(this, intent);
    }

    /**
     * 打开|关闭 关于窗口
     * 
     * @param flag
     */
    @SuppressWarnings("deprecation")
    private void showAbout(boolean flag) {
        if (flag) {
            Drawable drawable = null;
            if (!mSwitchMgr.isSimpleModeEnabled()
                    && (drawable = AiYouManager.getBlurBg(SetActivity.this)) != null) {
                mAboutFLayout.setBackgroundDrawable(drawable);
            } else {
                if (mSwitchMgr.isNightModeEnabled()) {
                    mAboutFLayout.setBackgroundColor(getResources().getColor(
                            R.color.bbs_background_night));
                } else {
                    mAboutFLayout.setBackgroundColor(getResources().getColor(
                            R.color.bbs_background_day));
                }
            }
            mAboutFLayout.setVisibility(View.VISIBLE);
            mAboutTV.setText(R.string.other_about);
        } else {
            mAboutFLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 清理缓存
     */
    private void clearCache() {
        final CustomDialog dialog = new CustomDialog(this);
        dialog.setMessage("确定清空缓存吗？")
                .setCancelButton(null)
                .setOKButton(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.startProgress();
                        // 清理缓存
                        ClearCacheTask task = new ClearCacheTask(new ClearCacheListener() {
                            @Override
                            public void onStartClear() {
                                dialog.setMessage("正在准备……");
                            }

                            @Override
                            public void onProgressUpdate(String progress) {
                                dialog.setMessage(progress);
                            }

                            @Override
                            public void onEndClear(int fileCount) {
                                dialog.dismiss();
                                Toast.makeText(getBaseContext(),
                                        "共清理" + fileCount + "个文件", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
                        task.execute();
                    }
                }).show();
    }

    @Override
    public void onPrefChanged(SwitchPreferences pref, boolean isChecked) {
        pref.setEnabled(false);
        int nId = pref.getId();
        if (R.id.face_pref == nId) {
            // 设置用户头像是是否显示
            if (isChecked) {
                mSwitchMgr.enableFace(true);
            } else {
                mSwitchMgr.enableFace(false);
            }
        } else if (R.id.large_image_pref == nId) {
            // 设置高清缩略图是否打开
            if (isChecked) {
                mSwitchMgr.enableLargeImage(true);
            } else {
                mSwitchMgr.enableLargeImage(false);
            }
        } else if (R.id.simple_mode_pref == nId) {
            // 是否开启简约模式
            if (isChecked) {
                mSwitchMgr.enableSimpleMode(true);
            } else {
                mSwitchMgr.enableSimpleMode(false);
            }
        } else if (R.id.shake_share_pref == nId) {
            // 是否开启摇一摇分享
            if (isChecked) {
                mSwitchMgr.enableShakeShare(true);
            } else {
                mSwitchMgr.enableShakeShare(false);
            }
        } else if(R.id.update_pref == nId) {
            //仅wifi下更新
            if (isChecked) {
                mSwitchMgr.setUpdateOnlyWifi(true);
            } else {
                mSwitchMgr.setUpdateOnlyWifi(false);
            }
        }
        pref.setEnabled(true);
    }

    public void selfFinish(View view) {
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
            selfFinish(null);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivitySplitAnimationUtil.cancel();
        System.gc();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mSwitchMgr.setSwipeOut(position);
        initSwipeOut();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

}

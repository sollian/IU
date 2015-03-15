
package com.aiyou.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author sollian
 */
public class SwitchManager {
    private static SwitchManager mInstance;

    private SharedPreferences mSharedPref;
    private static final String SPNAME = "switch";

    private static final String KEY_SHOW_WELCOME = "show_welcome_2_4";
    private static final String KEY_SIMPLE_MODE = "simple_mode_enabled";
    private static final String KEY_SHAKE_SHARE = "shake_share";
    private static final String KEY_FACE_HELP = "face_help";
    private static final String KEY_PAGE_HELP = "page_help_2_3";
    private static final String KEY_PHOTOSHOW_HELP = "photoshow_help";
    private static final String KEY_NIGHT_MODE = "is_night_mode";
    private static final String KEY_FACE = "bbs_face";
    private static final String KEY_LARGE_IMAGE = "bbs_large_image";
    private static final String KEY_SWIPE_OUT = "swipe_out";
    private static final String KEY_UPDATE_WIFI = "update_only_wifi";
    
    public static final int SWIPE_CLOSE = 0;
    public static final int SWIPE_LEFT = 1;
    public static final int SWIPE_RIGHT = 2;
    public static final int SWIPE_BOTH = 3;

    private SwitchManager(Context context) {
        mSharedPref = context.getSharedPreferences(SPNAME, Context.MODE_PRIVATE);
    }

    public static SwitchManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (SwitchManager.class) {
                if (mInstance == null) {
                    mInstance = new SwitchManager(context);
                }
            }
        }
        return mInstance;
    }

    /**
     * 是否显示欢迎页
     * 
     * @return
     */
    public boolean isFirstRun() {
        return mSharedPref.getBoolean(KEY_SHOW_WELCOME, true);
    }

    /**
     * 设置不显示欢迎页
     */
    public void disableFirstRun() {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(KEY_SHOW_WELCOME, false);
        editor.commit();
    }

    /**
     * 是否开启简约模式
     * 
     * @param flag
     */
    public void enableSimpleMode(boolean flag) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(KEY_SIMPLE_MODE, flag);
        editor.commit();
    }

    /**
     * 查询简约模式是否开启
     * 
     * @return
     */
    public boolean isSimpleModeEnabled() {
        return mSharedPref.getBoolean(KEY_SIMPLE_MODE, false);
    }

    /**
     * 是否开启摇一摇分享
     * 
     * @return
     */
    public boolean isShakeShareEnabled() {
        return mSharedPref.getBoolean(KEY_SHAKE_SHARE, false);
    }

    /**
     * 设置是否开启摇一摇分享
     * 
     * @param flag
     */
    public void enableShakeShare(boolean flag) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(KEY_SHAKE_SHARE, flag);
        editor.commit();
    }

    /**
     * 是否显示face_help
     * 
     * @return
     */
    public boolean needShowFaceHelp() {
        return mSharedPref.getBoolean(KEY_FACE_HELP, true);
    }

    /**
     * 设置不显示face_help
     */
    public void disableShowFaceHelp() {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(KEY_FACE_HELP, false);
        editor.commit();
    }

    /**
     * 是否显示page_help
     * 
     * @return
     */
    public boolean needShowPageHelp() {
        return mSharedPref.getBoolean(KEY_PAGE_HELP, true);
    }

    /**
     * 设置不显示page_help
     */
    public void disableShowPageHelp() {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(KEY_PAGE_HELP, false);
        editor.commit();
    }

    /**
     * 是否显示贴图秀的帮助
     * 
     * @return
     */
    public boolean needShowPhotoShowHelp() {
        return mSharedPref.getBoolean(KEY_PHOTOSHOW_HELP, true);
    }

    /**
     * 设置不显示贴图秀的帮助
     */
    public void disableShowPhotoShowHelp() {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(KEY_PHOTOSHOW_HELP, false);
        editor.commit();
    }

    /**
     * 设置夜间模式
     * 
     * @param flag
     */
    public void enableNightMode(boolean flag) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(KEY_NIGHT_MODE, flag);
        editor.commit();
    }

    /**
     * 查询夜间模式是否开启
     * 
     * @return
     */
    public boolean isNightModeEnabled() {
        return mSharedPref.getBoolean(KEY_NIGHT_MODE, false);
    }

    /**
     * 设置用户头像是否显示
     * 
     * @param flag
     */
    public void enableFace(boolean flag) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(KEY_FACE, flag);
        editor.commit();
    }

    /**
     * 查询用户头像是否显示
     * 
     * @return
     */
    public boolean isFaceEnabled() {
        return mSharedPref.getBoolean(KEY_FACE, false);
    }

    /**
     * 设置图片是否显示
     * 
     * @param flag
     */
    public void enableLargeImage(boolean flag) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(KEY_LARGE_IMAGE, flag);
        editor.commit();
    }

    /**
     * 查询图片是否显示
     * 
     * @return
     */
    public boolean isLargeImageEnabled() {
        return mSharedPref.getBoolean(KEY_LARGE_IMAGE, false);
    }
    
    /**
     * 设置滑动结束当前Activity
     * 
     * @param edge
     */
    public void setSwipeOut(int edge) {
        if(edge < 0) {
            edge = 0;
        }
        if(edge > 3) {
            edge = 3;
        }
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putInt(KEY_SWIPE_OUT, edge);
        editor.commit();
    }
    
    /**
     * 
     * @return
     */
    public int getSwipeOut() {
        return mSharedPref.getInt(KEY_SWIPE_OUT, SWIPE_BOTH);
    }
    
    /**
     * 设置仅wifi下更新
     * @param flag
     */
    public void setUpdateOnlyWifi(boolean flag) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(KEY_UPDATE_WIFI, flag);
        editor.commit();
    }
    
    public boolean getUpdateOnlyWifi() {
        return mSharedPref.getBoolean(KEY_UPDATE_WIFI, true);
    }
}

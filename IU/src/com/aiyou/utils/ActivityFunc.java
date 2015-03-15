
package com.aiyou.utils;

import com.aiyou.bbs.bean.User;

import external.OtherView.ActivitySplitAnimationUtil;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Activity跳转函数类
 * 
 * @author sollian
 */
public class ActivityFunc {

    public static final String KEY_BACKGROUND = "background";
    public static final String KEY_USER = "user";

    public static void startActivity(Activity activity, Intent intent) {
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, 0);
    }

    /**
     * @param activity
     * @param cls 要打开的Activity
     * @param user 用户
     * @param needAnim 是否需要中心打开动画
     */
    public static void startActivity(Activity activity, Class<?> cls,
            User user, boolean needAnim) {
        Intent intent = new Intent(activity, cls);
        if (!SwitchManager.getInstance(activity).isSimpleModeEnabled()) {
            Drawable drawable = AiYouManager.getBlurBg(activity);
            if (null != drawable) {
                Bitmap bmp = ((BitmapDrawable) drawable).getBitmap();
                intent.putExtra(KEY_BACKGROUND, bmp);
            }
        }
        if (null != user) {
            intent.putExtra(KEY_USER, user);
        }
        // 如果这个activity已经启动了，就不产生新的activity，而只是把这个activity实例加到栈顶来就可以了。
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (needAnim && !SwitchManager.getInstance(activity).isSimpleModeEnabled()) {
            ActivitySplitAnimationUtil.startActivity(activity, intent, 0.875);
        } else {
            activity.startActivity(intent);
            activity.overridePendingTransition(android.R.anim.fade_in, 0);
        }
    }

    /**
     * 启动新的Activity
     * 
     * @param activity
     * @param cls 要启动的Activity
     * @param user 用户
     * @param requestCode
     */
    public static void startActivityForResult(Activity activity, Class<?> cls,
            User user, int requestCode) {
        Intent intent = new Intent(activity, cls);
        if (!SwitchManager.getInstance(activity).isSimpleModeEnabled()) {
            Drawable drawable = AiYouManager.getBlurBg(activity);
            if (null != drawable) {
                Bitmap bmp = ((BitmapDrawable) drawable).getBitmap();
                intent.putExtra(KEY_BACKGROUND, bmp);
            }
        }
        if (null != user) {
            intent.putExtra(KEY_USER, user);
        }
        // 如果这个activity已经启动了，就不产生新的activity，而只是把这个activity实例加到栈顶来就可以了。
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivityForResult(intent, requestCode);
        activity.overridePendingTransition(android.R.anim.fade_in, 0);
    }
}

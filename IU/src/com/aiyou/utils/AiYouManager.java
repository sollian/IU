
package com.aiyou.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aiyou.utils.image.ImageFactory;
import com.aiyou.utils.logcat.Logcat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

/**
 * @author sollian
 */
public class AiYouManager {
    public static final String AUTHOR = "sollian";
    private static final String TAG = AiYouManager.class.getSimpleName();
    private static AiYouManager mInstance;
    private Context mContext;

    private static int mScreenWidth, mScreenHeight;

    private AiYouManager(Context context) {
        mContext = context;
        init();
    }

    public static AiYouManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (AiYouManager.class) {
                if (mInstance == null) {
                    mInstance = new AiYouManager(context);
                }
            }
        }
        return mInstance;
    }

    @SuppressWarnings("deprecation")
    private void init() {
        /**
         * 屏幕宽高
         */
        WindowManager manager = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        // 获取状态栏高度
        mScreenWidth = display.getWidth();
        mScreenHeight = display.getHeight();
    }

    public static int getScreenWidth() {
        return mScreenWidth;
    }

    public static int getScreenHeight() {
        return mScreenHeight;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     * 
     * @param dpValue
     * @return
     */
    public int dip2px(float dpValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     * 
     * @param pxValue
     * @return
     */
    public int px2dip(float pxValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     * 
     * @param spValue
     * @param fontScale （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public int sp2px(float spValue) {
        final float fontScale = mContext.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 震动
     * 
     * @param duration 持续时间
     */
    public void vibrate(long duration) {
        Vibrator vibrator = (Vibrator) mContext
                .getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {
                0, duration
        };
        vibrator.vibrate(pattern, -1);
    }

    /**
     * 去除字符串中的空格、回车、换行符、制表符、问号 和非法字符（非Asccll码）
     * 
     * @param str 要处理的字符串
     * @param strReplace 用该字符替换
     * @return 处理过的字符串
     */
    public static String getTxtWithoutNTSRElement(String str, String strReplace) {
        String dest = "";
        if (str != null) {
            // Pattern p =
            // Pattern.compile("[\\s]|[\t]|[\r]|[\n]|[?]|[^\\p{ASCII}]");
            Pattern p = Pattern.compile("[\\s]|[\t]|[\r]|[\n]|[?]");
            Matcher m = p.matcher(str);
            dest = m.replaceAll(strReplace);
        }
        return dest;
    }

    /**
     * 显示|隐藏输入法
     * 
     * @param activity
     * @param state 要设置的状态
     * @param view
     */
    public static void viewInputMethod(Activity activity, boolean state,
            View view) {
        InputMethodManager inputManager = (InputMethodManager) activity
                .getApplication()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (state) {
            inputManager
                    .showSoftInput(view, InputMethodManager.HIDE_NOT_ALWAYS);
            view.requestFocus();
        } else {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * 获取模糊图像
     * 
     * @param activity
     * @return
     */
    @SuppressWarnings("deprecation")
    public static Drawable getBlurBg(Activity activity) {
        Bitmap bmp = takeScreenShot(activity);
        if (null == bmp) {
            return null;
        }
        bmp = ImageFactory.doBlur(bmp, 20, true);
        return new BitmapDrawable(bmp);
    }

    /**
     * 获取屏幕快照
     * 
     * @param activity
     * @return
     */
    private static Bitmap takeScreenShot(Activity activity) {
        // View是你需要截图的View
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        Bitmap b = view.getDrawingCache();

        // 获取状态栏高度
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        if (b != null) {
            try {
                b = Bitmap.createScaledBitmap(b, 100, 100, true);
                // 去掉标题栏
                statusBarHeight = 100 * statusBarHeight / mScreenHeight;
                b = Bitmap.createBitmap(b, 0, statusBarHeight, 100,
                        100 - statusBarHeight);
            } catch (OutOfMemoryError e) {
                Logcat.e(TAG, "takeScreenShot OOM");
            }
        }
        view.setDrawingCacheEnabled(false);
        return b;
    }
}

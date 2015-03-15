
package external.OtherView;

import com.aiyou.BaseActivity;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

/**
 * Utility class to create a split activity animation
 * 
 * @author Udi Cohen (@udinic)
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class ActivitySplitAnimationUtil {

    public static Bitmap mBitmap = null;
    private static boolean isPlaying = false;
    private static int[] mLoc1;
    private static int[] mLoc2;
    private static ImageView mLeftImage;
    private static ImageView mRightImage;
    private static AnimatorSet mSetAnim;

    /**
     * Start a new Activity with a Split animation
     * 
     * @param currActivity The current Activity
     * @param intent The Intent needed tot start the new Activity
     * @param splitXPercent The X percentage where we want to split the Activity
     *            on the animation. -1 will split the Activity equally
     */
    public static void startActivity(Activity currActivity, Intent intent,
            double splitXPercent) {
        // Preparing the bitmaps that we need to show
        prepare(currActivity, splitXPercent);

        currActivity.startActivity(intent);
        currActivity.overridePendingTransition(0, 0);
    }

    /**
     * Start a new Activity with a Split animation right in the middle of the
     * Activity
     * 
     * @param currActivity The current Activity
     * @param intent The Intent needed tot start the new Activity
     */
    public static void startActivity(Activity currActivity, Intent intent) {
        startActivity(currActivity, intent, -1);
    }

    /**
     * Preparing the graphics on the destination Activity. Should be called on
     * the destination activity on Activity#onCreate() BEFORE setContentView()
     * 
     * @param destActivity the destination Activity
     */
    public static void prepareAnimation(final Activity destActivity) {
        mLeftImage = createImageView(destActivity, mBitmap, mLoc1);
        mRightImage = createImageView(destActivity, mBitmap, mLoc2);
    }

    /**
     * 判断是否可以进行动画
     * 
     * @return
     */
    public static boolean canPlay() {
        if (mBitmap == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 关闭activity
     * 
     * @param activity
     */
    public static void finish(final BaseActivity activity) {
        if (canPlay()) {
            if (isPlaying) {
                return;
            }
            // 中心关闭动画
            ActivitySplitAnimationUtil.prepareAnimation(activity);
            ActivitySplitAnimationUtil.animate(activity, 1000, false, true);
        } else {
            activity.scrollToFinishActivity();
        }
    }

    /**
     * Start the animation the reveals the destination Activity Should be called
     * on the destination activity on Activity#onCreate() AFTER setContentView()
     * 
     * @param destActivity the destination Activity
     * @param duration The duration of the animation
     * @param interpolator The interpulator to use for the animation. null for
     *            no interpulation.
     */
    public static void animate(final Activity destActivity, final int duration,
            final boolean flagopen, final boolean needCleanBmp, final TimeInterpolator interpolator) {

        // Post this on the UI thread's message queue. It's needed for the items
        // to be already measured
        new Handler().post(new Runnable() {

            @SuppressLint("NewApi")
            @Override
            public void run() {
                mSetAnim = new AnimatorSet();
                mLeftImage.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                mRightImage.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                mSetAnim.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        isPlaying = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isPlaying = false;
                        if (needCleanBmp) {
                            destActivity.finish();
                            destActivity.overridePendingTransition(0, 0);
                            clean(destActivity);
                            mBitmap = null;
                        } else {
                            clean(destActivity);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        isPlaying = false;
                        if (needCleanBmp) {
                            destActivity.finish();
                            destActivity.overridePendingTransition(0, 0);
                            clean(destActivity);
                            mBitmap = null;
                        } else {
                            clean(destActivity);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

                float from1 = 0, from2 = 0, to1 = 0, to2 = 0;
                if (flagopen) {
                    from1 = 0;
                    from2 = 0;
                    to1 = mLeftImage.getWidth() * -1;
                    to2 = mRightImage.getWidth();
                } else {
                    from1 = mLeftImage.getWidth() * -1;
                    from2 = mRightImage.getWidth();
                    to1 = 0;
                    to2 = 0;
                }
                // Animating the 2 parts away from each other
                Animator anim1 = ObjectAnimator.ofFloat(mLeftImage,
                        "translationX", from1, to1);
                Animator anim2 = ObjectAnimator.ofFloat(mRightImage,
                        "translationX", from2, to2);

                if (interpolator != null) {
                    anim1.setInterpolator(interpolator);
                    anim2.setInterpolator(interpolator);
                }

                mSetAnim.setDuration(duration);
                mSetAnim.playTogether(anim1, anim2);
                mSetAnim.start();
            }
        });
    }

    /**
     * Start the animation that reveals the destination Activity Should be
     * called on the destination activity on Activity#onCreate() AFTER
     * setContentView()
     * 
     * @param destActivity the destination Activity
     * @param duration The duration of the animation
     */
    public static void animate(final Activity destActivity, final int duration) {
        animate(destActivity, duration, true, false, new DecelerateInterpolator());
    }

    public static void animate(final Activity destActivity, final int duration,
            final boolean flagopen, final boolean needCleanBmp) {
        animate(destActivity, duration, flagopen, needCleanBmp, new DecelerateInterpolator());
    }

    /**
     * Cancel an in progress animation
     */
    public static void cancel() {
        mBitmap = null;
        if (mSetAnim != null)
            mSetAnim.cancel();
    }

    public static void destroy() {
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
        }
        cancel();
    }

    /**
     * Clean stuff
     * 
     * @param activity The Activity where the animation is occurring
     */
    @SuppressLint("NewApi")
    private static void clean(Activity activity) {
        if (mLeftImage != null) {
            mLeftImage.setLayerType(View.LAYER_TYPE_NONE, null);
            try {
                // If we use the regular removeView() we'll get a small UI
                // glitch
                activity.getWindowManager().removeViewImmediate(mRightImage);
            } catch (Exception ignored) {
            }
        }
        if (mRightImage != null) {
            mRightImage.setLayerType(View.LAYER_TYPE_NONE, null);
            try {
                activity.getWindowManager().removeViewImmediate(mLeftImage);
            } catch (Exception ignored) {
            }
        }
        // mBitmap = null;
    }

    /**
     * Preparing the graphics for the animation
     * 
     * @param currActivity the current Activity from where we start the new one
     * @param splitXPercent The Y percentage where we want to split the
     *            activity. -1 will split the activity equally
     */
    private static void prepare(Activity currActivity, double splitXPercent) {

        // Get the content of the activity and put in a bitmap
        View root = currActivity.getWindow().getDecorView()
                .findViewById(android.R.id.content);
        root.setDrawingCacheEnabled(true);
        mBitmap = root.getDrawingCache();

        // If the split Y coordinate is -1 - We'll split the activity equally
        splitXPercent = (splitXPercent != -1 ? splitXPercent : 0.5);

        if (splitXPercent > 0.9)
            splitXPercent = 0.9;
        // throw new IllegalArgumentException("Split X coordinate ["
        // + splitXPercent + "] exceeds the activity's height ["
        // + mBitmap.getHeight() + "]");
        int splitXCoor = (int) (mBitmap.getWidth() * splitXPercent);

        // Set the location to put the 2 bitmaps on the destination activity
        mLoc1 = new int[] {
                0, splitXCoor, root.getLeft()
        };
        mLoc2 = new int[] {
                splitXCoor, mBitmap.getWidth(), root.getLeft()
        };
    }

    /**
     * Creating the an image, containing one part of the animation on the
     * destination activity
     * 
     * @param destActivity The destination activity
     * @param bmp The Bitmap of the part we want to add to the destination
     *            activity
     * @param loc The location this part should be on the screen
     * @return
     */
    private static ImageView createImageView(Activity destActivity, Bitmap bmp,
            int loc[]) {
        MyImageView imageView = new MyImageView(destActivity);
        imageView.setImageBitmap(bmp);
        imageView.setImageOffsets(bmp.getHeight(), loc[0], loc[1]);

        // 获取状态栏高度
        Rect frame = new Rect();
        destActivity.getWindow().getDecorView()
                .getWindowVisibleDisplayFrame(frame);

        WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
        windowParams.gravity = Gravity.START;
        windowParams.x = loc[2] + loc[0];
        windowParams.y = frame.bottom;
        windowParams.height = bmp.getHeight();
        windowParams.width = loc[1] - loc[0];
        windowParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        windowParams.format = PixelFormat.TRANSLUCENT;
        windowParams.windowAnimations = 0;
        destActivity.getWindowManager().addView(imageView, windowParams);

        return imageView;
    }

    /**
     * MyImageView Extended ImageView that draws just part of an image, base on
     * start/end position
     */
    private static class MyImageView extends ImageView {
        private Rect mSrcRect;
        private Rect mDstRect;
        private Paint mPaint;

        public MyImageView(Context context) {
            super(context);
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }

        /**
         * Setting the bitmap offests to control the visible area
         * 
         * @param width The bitmap image
         * @param bmp The start X position
         * @param loc The end X position
         * @return
         */
        public void setImageOffsets(int height, int startX, int endX) {
            mSrcRect = new Rect(startX, 0, endX, height);
            mDstRect = new Rect(0, 0, endX - startX, height);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            Bitmap bm = null;
            Drawable drawable = getDrawable();
            if (null != drawable && drawable instanceof BitmapDrawable) {
                bm = ((BitmapDrawable) drawable).getBitmap();
            }

            if (null == bm) {
                super.onDraw(canvas);
            } else {
                canvas.drawBitmap(bm, mSrcRect, mDstRect, mPaint);
            }
        }
    }
}

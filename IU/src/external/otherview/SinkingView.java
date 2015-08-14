package external.otherview;

import java.lang.ref.WeakReference;

import com.aiyou.AiYouApplication;
import com.aiyou.R;
import com.aiyou.utils.AiYouManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class SinkingView extends FrameLayout {

    public enum Status {
        RUNNING, NONE
    }

    private static final int DEFAULT_TEXTCOLOT = 0xff0074a2;
    private static final int DEFAULT_TEXTSIZE = AiYouManager.getInstance(
            AiYouApplication.getInstance()).sp2px(20);

    private static WeakReference<Bitmap> mScaledBitmap;
    private static int mRepeatCount = 0;

    private float mPercent;
    private Paint mPaint = new Paint();
    private float mLeft;
    private int mSpeed = 10;
    private Status mFlag = Status.NONE;
    private int mTextColot = DEFAULT_TEXTCOLOT;
    private int mTextSize = DEFAULT_TEXTSIZE;

    public SinkingView(Context context) {
        super(context);
    }

    public SinkingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SinkingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setTextColor(int color) {
        mTextColot = color;
    }

    public void setTextSize(int size) {
        mTextSize = size;
    }

    public float getPercent() {
        return mPercent;
    }

    public void setPercent(float percent) {
        mFlag = Status.RUNNING;
        mPercent = percent;
        postInvalidate();
    }

    public void setStatus(Status status) {
        mFlag = status;
    }

    public void clear() {
        mFlag = Status.NONE;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (mFlag == Status.RUNNING) {
            if (mScaledBitmap == null || mScaledBitmap.get() == null
                    || mScaledBitmap.get().isRecycled()) {
                initScaledBmp();
            }

            for (int idx = 0; idx < mRepeatCount; idx++) {
                canvas.drawBitmap(mScaledBitmap.get(), mLeft + (idx - 1)
                        * mScaledBitmap.get().getWidth(), -mPercent
                        * getHeight(), null);
            }
            String str = (int) (mPercent * 100) + "%";
            mPaint.setColor(mTextColot);
            mPaint.setTextSize(mTextSize);
            canvas.drawText(str, (getWidth() - mPaint.measureText(str)) / 2,
                    getHeight() / 2 + mTextSize / 2, mPaint);
            mLeft += mSpeed;
            if (mLeft >= mScaledBitmap.get().getWidth())
                mLeft = 0;
            postInvalidateDelayed(20);
        }
    }

    private synchronized void initScaledBmp() {
        if (mScaledBitmap != null && mScaledBitmap.get() != null
                && !mScaledBitmap.get().isRecycled()) {
            return;
        }
        Bitmap bmp = null;
        try {
            bmp = BitmapFactory.decodeResource(getContext().getResources(),
                    R.drawable.wave);
            bmp = Bitmap.createScaledBitmap(bmp, bmp.getWidth(), getHeight(),
                    false);
            mRepeatCount = (int) Math.ceil(getWidth() / bmp.getWidth() + 0.5) + 1;
        } catch (OutOfMemoryError e) {
            bmp = BitmapFactory.decodeResource(getContext().getResources(),
                    R.drawable.touch_image_view);
        }
        mScaledBitmap = new WeakReference<Bitmap>(bmp);
    }

}


package external.OtherView;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

/**
 * 圆形进度条
 * 
 * @author planet
 */
public class Win8ProgressBar extends View {
    static final String TAG = Win8ProgressBar.class.getSimpleName();
    /**
     * 帧率=1000/delayMillis 帧率越快,旋转速度也就越快
     */
    private int mDelayMillis = 30;
    private Handler mHandler;
    private ArrayList<Entity> mEntityList;
    private int mWidth = 0;
    // private int height = 0;
    private int mR = 15;
    private int mShift = 20;
    private int mRadius = 3;
    private int mColor = Color.WHITE;
    private long mTime = 0;
    private boolean mStarted = false;

    public Win8ProgressBar(Context context) {
        super(context);
        init(null);
    }

    public Win8ProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public Win8ProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mWidth = getLayoutParams().width;
        // height = getLayoutParams().height;
        if (mWidth > 0) {
            // 根据view宽度定义小球的半径
            if (mWidth < 50) {
                mRadius = 2;
            } else if (mWidth < 80) {
                mRadius = 3;
            } else {
                mRadius = 4;
            }
            // radius = width<80?2:4;
            mR = mWidth / 2 - mRadius * 2;
            if (mR <= 0)
                mR = 15;
            mShift = mWidth / 2;
        }
    }

    private void init(AttributeSet attrs) {
        // 获取设置的background作为小球颜色,然后将view自身背景设置成透明色
        if (attrs != null) {
            String v = attrs.getAttributeValue("http://schemas.android.com/apk/res/android",
                    "background");
            if (v != null) {
                if (v.startsWith("#")) {
                    mColor = Color.parseColor(v);
                } else {
                    mColor = getResources().getColor(Integer.parseInt(v.replaceAll("@", "")));
                }
            }
            setBackgroundResource(android.R.color.transparent);
        }

        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                for (Entity e : mEntityList) {
                    e.update();
                }
                invalidate();
                mHandler.sendEmptyMessageDelayed(0, mDelayMillis);
                mTime += mDelayMillis;
                return false;
            }
        });
    }

    public void setColor(int color) {
        this.mColor = color;
    }

    /**
     * 结束动画
     */
    public void stop() {
        mHandler.removeMessages(0);
        mStarted = false;
        invalidate();
    }

    public boolean isStart() {
        return mStarted;
    }

    /**
     * 重新开始动画
     */
    public void start() {
        if (mStarted)
            return;
        mStarted = true;
        mTime = 0;
        mEntityList = new ArrayList<Win8ProgressBar.Entity>();
        float s = .25f;
        mEntityList.add(new Entity(0, mColor, 0));
        mEntityList.add(new Entity(1 * s, mColor, mDelayMillis * 4));
        mEntityList.add(new Entity(2 * s, mColor, mDelayMillis * 8));
        mEntityList.add(new Entity(3 * s, mColor, mDelayMillis * 12));
        mEntityList.add(new Entity(4 * s, mColor, mDelayMillis * 16));
        mHandler.sendEmptyMessage(0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (Entity e : mEntityList) {
            e.draw(canvas);
        }
        super.onDraw(canvas);
    }

    class Entity {
        private float x;
        private float y;
        private int color;
        private Paint paint;
        private double sp = 0;
        private long delay;
        // 动画一共三个阶段0~2
        private int sec = 0;
        // 每个阶段pec从0~1
        private float pec = 0;
        boolean visiable = true;

        public float getInterpolation(float input) {
            return (float) (Math.cos((input + 1) * Math.PI) / 2.0f) + 0.5f;
        }

        public Entity(float sp, int color, int delay) {
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            this.color = color;
            this.sp = sp;
            this.delay = delay;
            paint.setColor(this.color);
        }

        public void update() {
            if (mTime < delay)
                return;
            visiable = true;
            // pec这个变量是步进值,值越大,旋转速度也越快,可以跟delayMillis配合使用
            pec += 0.03;
            if (pec > 1) {
                pec = 0;
                sec = ++sec == 3 ? 0 : sec;
                delay = sec == 0 ? mTime + mDelayMillis * 22 : mTime + mDelayMillis * 3;
                visiable = sec == 0 ? false : true;
            }
            // sec=0从0.5pi开始,sec=1从1.5pi开始,sec=2从1pi开始
            double θ = Math.PI * .5 + (sec == 0 ? 0 : sec * Math.PI / sec) - (sec == 0 ? 0 : sp)
                    // sec=0,sec=2移动1pi, sec=1移动2pi
                    + (Math.PI * (sec == 1 ? 2 : 1) - (sec == 0 ? sp : 0) + (sec == 2 ? sp : 0))
                    * getInterpolation(pec);
            x = (float) (mR / 2 * Math.cos(θ)) + mShift / 2;
            y = (float) (mR / 2 * Math.sin(θ)) + mShift / 2;
        }

        public void draw(Canvas canvas) {
            if (!visiable || x == 0 || y == 0)
                return;
            canvas.save();
            canvas.translate(x, y);
            canvas.drawCircle(x, y, mRadius, paint);
            canvas.restore();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (getVisibility() == View.VISIBLE) {
            start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stop();
    }
}

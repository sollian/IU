package external.otherview;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import com.aiyou.view.DarkImageView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * 可缩放、旋转、平移的ImageView，可加载超大图片（请使用set方法设置图片来源），完美解决OOM问题
 * 
 * @author sollian
 *
 */
public class MagicImageView extends DarkImageView {
    public interface OnTransformListener {
        public void onDrag(float deltaX, float deltaY);

        /**
         * 当前缩放值
         * 
         * @param scale
         */
        public void onScale(float scale);

        /**
         * 当前图片旋转角度，顺时针为正，只能为0,90,180,270；
         * 
         * @param degree
         *            单位为角度
         */
        public void onRotate(float degree);

        public void onFling(float deltaX, float deltaY);
    }

    private OnTransformListener mListener = null;

    /**
     * 自动缩放、旋转、平移动画的持续时间
     */
    private static final float DEFAULT_ANIM_TIME = 200.0f;
    /**
     * 最大、最小缩放值
     */
    private static final float DEFAULT_MIN_SCALE = 0.75f;
    private static final float DEFAULT_MAX_SCALE = 5.0f;

    private float mMinScale = DEFAULT_MIN_SCALE;
    private float mMaxScale = DEFAULT_MAX_SCALE;
    /**
     * 标准化的缩放倍数
     */
    private float mNormalizedScale = 1;
    /**
     * 适应屏幕时，图片的宽高
     */
    private float mMatchedImageWidth = 0, mMatchedImageHeight = 0;

    private static enum State {
        NONE, ONE_POINT, TWO_POINT,
    };

    private State mState = State.NONE;
    private boolean mIsTranslate = false;
    private boolean mIsRotate = false;
    private boolean mIsScale = false;

    private boolean mIsRotateEnabled = true;
    /**
     * 需要自动旋转的角度
     */
    private float remainDegree = 0f;

    private static int SCREEN_WIDTH = 0, SCREEN_HEIGHT = 0;

    private int mViewHeight, mViewWidth;

    private Matrix mMatrix;

    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleDetector;

    private Fling mFling = null;
    private Rotate mRotate = null;
    private Translate mTranslate = null;
    private Scale mScale = null;

    /**
     * 图片旋转的角度，顺时针方向为正
     */
    private float mDegree = 0;

    public MagicImageView(Context context) {
        super(context);
        init();
    }

    public MagicImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MagicImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("ClickableViewAccessibility")
    @SuppressWarnings("deprecation")
    private void init() {
        if (SCREEN_WIDTH == 0 || SCREEN_HEIGHT == 0) {
            /**
             * 屏幕宽高
             */
            WindowManager manager = (WindowManager) getContext()
                    .getSystemService(Context.WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            SCREEN_WIDTH = display.getWidth();
            SCREEN_HEIGHT = display.getHeight();
        }

        mMatrix = new Matrix();
        mGestureDetector = new GestureDetector(getContext(),
                new GestureListener());
        mScaleDetector = new ScaleGestureDetector(getContext(),
                new ScaleListener());

        setScaleType(ScaleType.MATRIX);
        setOnTouchListener(new TouchListener());
    }

    public void setOnTransformListener(OnTransformListener listener) {
        mListener = listener;
    }

    public void setMaxScale(float scale) {
        mMaxScale = scale;
    }

    /**
     * 获取当前缩放值
     * 
     * @return
     */
    public float getCurrentScale() {
        return mNormalizedScale;
    }

    /**
     * 关闭旋转功能
     */
    public void disableRotate() {
        mIsRotateEnabled = false;
    }

    @Override
    public void setImageResource(int resId) {
        InputStream is = getContext().getResources().openRawResource(resId);
        setInputStream(is);
    }

    public void setByte(byte[] data) {
        if (data == null) {
            return;
        }
        super.setImageBitmap(decodeByte(data));
    }

    public void setInputStream(InputStream is) {
        if (is == null) {
            return;
        }
        byte[] data = null;
        try {
            data = is2Byte(is);
        } catch (Exception e) {
        }
        setByte(data);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        fitImageToView();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        if (bm == null) {
            return;
        }
        bm = decodeByte(bmp2Byte(bm));
        super.setImageBitmap(bm);
        fitImageToView();
    }

    @SuppressWarnings("deprecation")
    private Bitmap decodeByte(byte[] data) {
        if (data == null) {
            return null;
        }
        Bitmap bmp = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inPurgeable = true;
        options.inInputShareable = true;

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        int screenWidth = 0, screenHeight = 0;
        if (options.outWidth > options.outHeight) {
            screenWidth = SCREEN_HEIGHT;
            screenHeight = SCREEN_WIDTH;
        } else {
            screenWidth = SCREEN_WIDTH;
            screenHeight = SCREEN_HEIGHT;
        }
        float scaleX = (float) options.outWidth / screenWidth * 2;
        float scaleY = (float) options.outHeight / screenHeight * 2;
        options.inSampleSize = (int) Math.ceil(Math.max(scaleX, scaleY));

        options.inJustDecodeBounds = false;
        while (true) {
            try {
                bmp = BitmapFactory.decodeByteArray(data, 0, data.length,
                        options);
                break;
            } catch (OutOfMemoryError e) {
                options.inSampleSize++;
            }
        }
        return bmp;
    }

    private byte[] is2Byte(InputStream inStream) throws Exception {
        byte[] buffer = new byte[1024];
        int len = -1;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        byte[] data = outStream.toByteArray();
        outStream.close();
        inStream.close();
        return data;
    }

    private byte[] bmp2Byte(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        mViewWidth = MeasureSpec.getSize(widthMeasureSpec);
        mViewHeight = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(mViewWidth, mViewHeight);

        fitImageToView();
    }

    private void reset() {
        mMatrix.reset();
        mNormalizedScale = 1;
        mState = State.NONE;
        // mDegree = 0;
    }

    /**
     * 使图片适应屏幕
     */
    private void fitImageToView() {
        if (mMatrix == null) {
            return;
        }
        reset();
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();

        float scale1 = (float) mViewWidth / drawableWidth;
        float scale2 = (float) mViewHeight / drawableHeight;
        float scale3 = (float) mViewWidth / drawableHeight;
        float scale4 = (float) mViewHeight / drawableWidth;

        float scale = Math.min(Math.min(scale1, scale2),
                Math.min(scale3, scale4));
        // mMatrix.setScale(scale, scale);
        mMatchedImageWidth = drawableWidth * scale;
        mMatchedImageHeight = drawableHeight * scale;

        mMatrix.setScale(scale, scale);
        mMatrix.postTranslate((mViewWidth - mMatchedImageWidth) / 2,
                (mViewHeight - mMatchedImageHeight) / 2);
        mMatrix.postRotate(mDegree, mViewWidth / 2, mViewHeight / 2);

        setImageMatrix(mMatrix);
    }

    private Size getImageSize() {
        return getImageSize(mNormalizedScale);
    }

    /**
     * 获取当前图片的宽高，考虑了图片的旋转角度
     * 
     * @param scale
     * @return
     */
    private Size getImageSize(float scale) {
        Size size;
        if (mDegree == 0 || mDegree == 180) {
            size = new Size(mMatchedImageWidth * scale, mMatchedImageHeight
                    * scale);
        } else {
            size = new Size(mMatchedImageHeight * scale, mMatchedImageWidth
                    * scale);
        }
        return size;
    }

    private float[] getMatrixValues(Matrix matrix) {
        float[] value = new float[9];
        matrix.getValues(value);
        return value;
    }

    private void fixTranslation() {
        fixTranslation(0, 0);
    }

    /**
     * 移动图片，使适应屏幕
     * 
     * @param deltaX
     * @param deltaY
     */
    private void fixTranslation(float deltaX, float deltaY) {
        float[] matrixValues = getMatrixValues(mMatrix);
        float transX = matrixValues[Matrix.MTRANS_X] + deltaX;
        float transY = matrixValues[Matrix.MTRANS_Y] + deltaY;

        Size size = getImageSize();
        transX = getFixTrans(transX, size.getWidth(), mViewWidth, true);
        transY = getFixTrans(transY, size.getHeight(), mViewHeight, false);
        matrixValues[Matrix.MTRANS_X] = transX;
        matrixValues[Matrix.MTRANS_Y] = transY;
        mMatrix.setValues(matrixValues);
        setImageMatrix(mMatrix);
    }

    private float getFixTrans(float transSize, float contentSize,
            float viewSize, boolean isWidth) {
        return getFixTrans(transSize, contentSize, viewSize, isWidth, true);
    }

    /**
     * 得到合适的偏移量
     * 
     * @param transSize
     * @param contentSize
     * @param viewSize
     * @param isWidth
     * @param fixed
     * @return
     */
    private float getFixTrans(float transSize, float contentSize,
            float viewSize, boolean isWidth, boolean fixed) {
        int degree = 90;
        if (!isWidth) {
            degree = 270;
        }
        if (viewSize >= contentSize) {
            transSize = (viewSize - contentSize) / 2;
            if (mDegree == 180 || mDegree == degree) {
                transSize += contentSize;
            }
        } else {
            float minSize, maxSize;
            minSize = viewSize - contentSize;
            maxSize = 0;
            if (mDegree == 180 || mDegree == degree) {
                minSize += contentSize;
                maxSize += contentSize;
            }
            if (fixed) {
                transSize = transSize > maxSize ? maxSize : transSize;
                transSize = transSize < minSize ? minSize : transSize;
            }
        }
        return transSize;
    }

    /**
     * 得到合适的旋转角度
     * 
     * @param degree
     * @return
     */
    private float ModifyDegree(float degree) {
        while (degree < 0) {
            degree += 360;
        }
        degree %= 360;
        if (degree <= 45 || degree > 315) {
        } else if (degree > 45 && degree <= 135) {
            mDegree += 90;
        } else if (degree > 135 && degree <= 225) {
            mDegree += 180;
        } else {
            mDegree += 270;
        }
        mDegree %= 360;
        if (mListener != null) {
            mListener.onRotate(mDegree);
        }
        degree %= 90;
        if (degree > 45) {
            degree = 90 - degree;
        } else {
            degree = -degree;
        }
        return degree;
    }

    private class TouchListener implements OnTouchListener {
        private PointF lastP1 = new PointF();
        private PointF lastP2 = new PointF();
        private PointF curP1 = new PointF();
        private PointF curP2 = new PointF();

        private float deltaX, deltaY;

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!mIsTranslate && !mIsRotate && !mIsScale) {
                mScaleDetector.onTouchEvent(event);
                mGestureDetector.onTouchEvent(event);
                switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (mFling != null) {
                        mFling.cancelFling();
                    }
                    mState = State.ONE_POINT;
                    lastP1.set(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    switch (mState) {
                    case ONE_POINT:
                        // 拖动
                        performDrag(event);
                        break;
                    case TWO_POINT:
                        // 旋转
                        if (mIsRotateEnabled) {
                            performRotate(event);
                        }
                        break;
                    default:
                        break;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    mState = State.NONE;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    if (mState == State.ONE_POINT) {
                        mState = State.TWO_POINT;
                        lastP2.set(event.getX(1), event.getY(1));
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    mState = State.NONE;
                    if (!mIsRotate) {
                        // 自动旋转图片
                        remainDegree = ModifyDegree(remainDegree);
                        mRotate = new Rotate(remainDegree);
                        mRotate.start();
                        remainDegree = 0;
                    }
                    break;
                }
                setImageMatrix(mMatrix);
            }
            return true;
        }

        /**
         * 拖动图片
         * 
         * @param event
         */
        private void performDrag(MotionEvent event) {
            curP1.set(event.getX(0), event.getY(0));
            deltaX = curP1.x - lastP1.x;
            deltaY = curP1.y - lastP1.y;
            fixTranslation(deltaX, deltaY);
            lastP1.set(curP1);
            if (mListener != null) {
                mListener.onDrag(deltaX, deltaY);
            }
        }

        /**
         * 旋转图片
         * 
         * @param event
         */
        private void performRotate(MotionEvent event) {
            curP1.set(event.getX(0), event.getY(0));
            curP2.set(event.getX(1), event.getY(1));
            float curDegree = calculateDegree(lastP1, lastP2, curP1, curP2);
            lastP1.set(curP1);
            lastP2.set(curP2);
            mMatrix.postRotate(curDegree, mViewWidth / 2, mViewHeight / 2);
            remainDegree += curDegree;
        }

        /**
         * 根据两对坐标点计算旋转角度
         * 
         * @param a1
         * @param b1
         * @param a2
         * @param b2
         * @return
         */
        private float calculateDegree(PointF a1, PointF b1, PointF a2, PointF b2) {
            float degree = 0;
            double d1, d2;
            d1 = Math.atan((b1.y - a1.y) / (b1.x - a1.x));
            d2 = Math.atan((b2.y - a2.y) / (b2.x - a2.x));
            d1 = (float) (d1 * 180 / Math.PI);
            d2 = (float) (d2 * 180 / Math.PI);
            degree = (float) (d2 - d1);
            if (degree > 90) {
                degree = 180 - degree;
            } else if (degree < -90) {
                degree += 180;
            }
            return degree;
        }

    }

    private class ScaleListener extends
            ScaleGestureDetector.SimpleOnScaleGestureListener {
        public boolean onScale(ScaleGestureDetector detector) {
            scaleImage(detector.getScaleFactor(), detector.getFocusX(),
                    detector.getFocusY());
            return true;
        }

        // public void onScaleEnd(ScaleGestureDetector detector) {
        // if (mNormalizedScale < 1 && !mIsScale) {
        // mScale = new Scale(1, mViewWidth / 2, mViewHeight / 2);
        // mScale.start();
        // }
        // }
    }

    private class GestureListener extends
            GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return performClick();
        }

        @Override
        public void onLongPress(MotionEvent e) {
            performLongClick();
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // 双击图片缩放的最大或最小
            boolean consumed = false;
            if (mState == State.NONE && !mIsTranslate && !mIsRotate
                    && !mIsScale) {
                float targetZoom = (mNormalizedScale == 1) ? mMaxScale : 1;
                mScale = new Scale(targetZoom, e.getX(), e.getY(), true);
                mScale.setDuration(500);
                mScale.start();
                consumed = true;
            }
            return consumed;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
            if (mFling != null) {
                mFling.cancelFling();
            }
            mFling = new Fling((int) velocityX, (int) velocityY);
            mFling.start();
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    /**
     * 缩放图片
     * 
     * @param deltaScale
     * @param focusX
     * @param focusY
     */
    private void scaleImage(float deltaScale, float focusX, float focusY) {

        float lowerScale, upperScale;
        lowerScale = mMinScale;
        upperScale = mMaxScale;

        float origScale = mNormalizedScale;
        mNormalizedScale *= deltaScale;
        if (mNormalizedScale > upperScale) {
            mNormalizedScale = upperScale;
            deltaScale = upperScale / origScale;
        } else if (mNormalizedScale < lowerScale) {
            mNormalizedScale = lowerScale;
            deltaScale = lowerScale / origScale;
        }

        if (mListener != null) {
            mListener.onScale(mNormalizedScale);
        }

        mMatrix.postScale((float) deltaScale, (float) deltaScale, focusX,
                focusY);
        setImageMatrix(mMatrix);
    }

    private class Scale implements Runnable {
        private long startTime;
        private float startScale, targetScale;
        private float bitmapX, bitmapY;
        private Interpolator interpolator = new DecelerateInterpolator();
        private PointF startTouch;
        private PointF endTouch;
        private float duration = DEFAULT_ANIM_TIME;
        private boolean isScale = false;
        private boolean fixTrans = false;

        public Scale(float targetScale, float focusX, float focusY,
                boolean fixTrans) {
            startScale = mNormalizedScale;
            this.targetScale = targetScale;
            PointF bitmapPoint = transformCoordTouchToBitmap(focusX, focusY,
                    false);
            bitmapX = bitmapPoint.x;
            bitmapY = bitmapPoint.y;

            startTouch = transformCoordBitmapToTouch(bitmapX, bitmapY);
            endTouch = new PointF(mViewWidth / 2, mViewHeight / 2);
            this.fixTrans = fixTrans;
        }

        public void start() {
            startTime = System.currentTimeMillis();
            mIsScale = true;
            isScale = true;
            postRunnable(this);
        }

        public void setDuration(float duration) {
            this.duration = duration;
        }

        @Override
        public void run() {
            float t = interpolate();
            float deltaScale = calculateDeltaScale(t);
            scaleImage(deltaScale, bitmapX, bitmapY);
            translateImageToCenterTouchPosition(t);
            if (fixTrans) {
                fixTranslation();
            } else {
                setImageMatrix(mMatrix);
            }
            if (mIsScale) {
                if (!isScale) {
                    mIsScale = false;
                }
                postRunnable(this);
            } else {
                if (!fixTrans) {
                    if (!mIsTranslate) {
                        mTranslate = new Translate();
                        mTranslate.start();
                    }
                }
            }
        }

        private PointF transformCoordTouchToBitmap(float x, float y,
                boolean clipToBitmap) {
            float[] m = getMatrixValues(mMatrix);
            float origW = getDrawable().getIntrinsicWidth();
            float origH = getDrawable().getIntrinsicHeight();
            float transX = m[Matrix.MTRANS_X];
            float transY = m[Matrix.MTRANS_Y];
            Size size = getImageSize();
            float finalX = ((x - transX) * origW) / size.getWidth();
            float finalY = ((y - transY) * origH) / size.getHeight();
            if (clipToBitmap) {
                finalX = Math.min(Math.max(finalX, 0), origW);
                finalY = Math.min(Math.max(finalY, 0), origH);
            }
            return new PointF(finalX, finalY);
        }

        private PointF transformCoordBitmapToTouch(float bx, float by) {
            float[] m = getMatrixValues(mMatrix);
            float origW = getDrawable().getIntrinsicWidth();
            float origH = getDrawable().getIntrinsicHeight();
            float px = bx / origW;
            float py = by / origH;
            Size size = getImageSize();
            float finalX = m[Matrix.MTRANS_X] + size.getWidth() * px;
            float finalY = m[Matrix.MTRANS_Y] + size.getHeight() * py;
            return new PointF(finalX, finalY);
        }

        private void translateImageToCenterTouchPosition(float t) {
            float targetX = startTouch.x + t * (endTouch.x - startTouch.x);
            float targetY = startTouch.y + t * (endTouch.y - startTouch.y);
            PointF curr = transformCoordBitmapToTouch(bitmapX, bitmapY);
            mMatrix.postTranslate(targetX - curr.x, targetY - curr.y);
        }

        private float interpolate() {
            long currTime = System.currentTimeMillis();
            float elapsed = (currTime - startTime) / duration;
            elapsed = Math.min(1f, elapsed);
            if (elapsed >= 1) {
                isScale = false;
            }
            return interpolator.getInterpolation(elapsed);
        }

        private float calculateDeltaScale(float t) {
            float zoom = startScale + t * (targetScale - startScale);
            return zoom / mNormalizedScale;
        }
    }

    private class Translate implements Runnable {
        private float beginTransX, beginTransY;
        private float endTransX, endTransY;
        private float curTransX, curTransY;
        private Interpolator interpolator = new DecelerateInterpolator();
        private long startTime;
        private Size size;
        private float duration = DEFAULT_ANIM_TIME;
        private boolean isTranslate = false;

        public Translate() {
            this(mNormalizedScale);
        }

        public Translate(float scaleFactor) {
            size = getImageSize(scaleFactor);
            updateBeginAndEnd();
        }

        public void updateBeginAndEnd() {
            float[] matrixValues = getMatrixValues(mMatrix);
            beginTransX = matrixValues[Matrix.MTRANS_X];
            beginTransY = matrixValues[Matrix.MTRANS_Y];
            endTransX = getFixTrans(beginTransX, size.getWidth(), mViewWidth,
                    true);
            endTransY = getFixTrans(beginTransY, size.getHeight(), mViewHeight,
                    false);
        }

        public void start() {
            if (beginTransX == endTransX && beginTransY == endTransY) {
                return;
            }
            mIsTranslate = true;
            isTranslate = true;
            startTime = System.currentTimeMillis();
            postRunnable(this);
        }

        private void interpolateTrans() {
            float elapsed = (System.currentTimeMillis() - startTime) / duration;
            elapsed = Math.min(1f, elapsed);
            if (elapsed >= 1) {
                isTranslate = false;
            }
            float percent = interpolator.getInterpolation(elapsed);
            curTransX = beginTransX + (endTransX - beginTransX) * percent;
            curTransY = beginTransY + (endTransY - beginTransY) * percent;
        }

        @Override
        public void run() {
            interpolateTrans();
            float[] matrixValues = getMatrixValues(mMatrix);
            matrixValues[Matrix.MTRANS_X] = curTransX;
            matrixValues[Matrix.MTRANS_Y] = curTransY;
            mMatrix.setValues(matrixValues);
            setImageMatrix(mMatrix);
            if (mIsTranslate) {
                if (!isTranslate) {
                    mIsTranslate = false;
                }
                postRunnable(this);
            }
        }
    }

    private class Rotate implements Runnable {
        private float totalDegree;
        private float lastDegree = 0;
        private Interpolator interpolator = new DecelerateInterpolator();
        private long startTime;
        private float focusX, focusY;
        private boolean isRotate = false;

        public Rotate(float d) {
            this(d, mViewWidth / 2, mViewHeight / 2);
        }

        public Rotate(float d, float focusX, float focusY) {
            totalDegree = d;
            this.focusX = focusX;
            this.focusY = focusY;
        }

        public void start() {
            mIsRotate = true;
            isRotate = true;
            startTime = System.currentTimeMillis();
            postRunnable(this);
        }

        private float interpolateDegree() {
            float elapsed = (System.currentTimeMillis() - startTime)
                    / DEFAULT_ANIM_TIME;
            elapsed = Math.min(1f, elapsed);
            if (elapsed >= 1) {
                isRotate = false;
            }
            float curDegree = totalDegree
                    * interpolator.getInterpolation(elapsed);
            float deltaDegree = curDegree - lastDegree;
            lastDegree = curDegree;
            return deltaDegree;
        }

        @Override
        public void run() {
            float deltaDegree = interpolateDegree();
            mMatrix.postRotate(deltaDegree, focusX, focusY);
            setImageMatrix(mMatrix);
            if (mIsRotate) {
                if (!isRotate) {
                    mIsRotate = false;
                }
                postRunnable(this);
            } else {
                if (mNormalizedScale < 1 && !mIsScale) {
                    mScale = new Scale(1, mViewWidth / 2, mViewHeight / 2,
                            false);
                    mScale.start();
                    return;
                }
                if (!mIsTranslate) {
                    mTranslate = new Translate();
                    mTranslate.start();
                }
            }
        }
    }

    private class Fling implements Runnable {
        private Scroller scroller = null;
        private int lastX, lastY;

        public Fling(int velocityX, int velocityY) {
            scroller = new Scroller(getContext());
            float[] mtrixValues = getMatrixValues(mMatrix);

            Size size = getImageSize();
            int startX = (int) mtrixValues[Matrix.MTRANS_X];
            int startY = (int) mtrixValues[Matrix.MTRANS_Y];
            int minX, maxX, minY, maxY;

            if (size.getWidth() > mViewWidth) {
                minX = mViewWidth - (int) size.getWidth();
                maxX = 0;
                if (mDegree == 90 || mDegree == 180) {
                    minX += size.getWidth();
                    maxX += size.getWidth();
                }
            } else {
                minX = maxX = startX;
            }

            if (size.getHeight() > mViewHeight) {
                minY = mViewHeight - (int) size.getHeight();
                maxY = 0;
                if (mDegree == 270 || mDegree == 180) {
                    minY += size.getHeight();
                    maxY += size.getHeight();
                }
            } else {
                minY = maxY = startY;
            }

            scroller.fling(startX, startY, (int) velocityX, (int) velocityY,
                    minX, maxX, minY, maxY);
            lastX = startX;
            lastY = startY;
        }

        public void start() {
            postRunnable(this);
        }

        public void cancelFling() {
            if (scroller != null) {
                scroller.forceFinished(true);
            }
        }

        @Override
        public void run() {
            if (scroller.isFinished()) {
                scroller = null;
                return;
            }

            if (scroller.computeScrollOffset()) {
                int newX = scroller.getCurrX();
                int newY = scroller.getCurrY();
                int deltaX = newX - lastX;
                int deltaY = newY - lastY;
                lastX = newX;
                lastY = newY;
                if (mListener != null) {
                    mListener.onFling(deltaX, deltaY);
                }
                fixTranslation(deltaX, deltaY);
                postRunnable(this);
            } else {
                scroller = null;
            }
        }
    }

    private void postRunnable(Runnable runnable) {
        postDelayed(runnable, 1000 / 60);
    }

    private class Size {
        private float mWidth;
        private float mHeight;

        public Size(float width, float height) {
            mWidth = width;
            mHeight = height;
        }

        public float getWidth() {
            return mWidth;
        }

        public float getHeight() {
            return mHeight;
        }
    }
}

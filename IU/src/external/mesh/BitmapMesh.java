/**
 * Copyright © 2013 CVTE. All Rights Reserved.
 */

package external.mesh;

import com.aiyou.AiYouApplication;
import com.aiyou.utils.AiYouManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;

public class BitmapMesh {
    /**
     * 对图像进行mesh动画的视图
     * 
     * @author sollian
     */
    public static class MeshView extends View {

        private static final int WIDTH = 40;
        private static final int HEIGHT = 40;

        private Bitmap mBitmap = null;
        private boolean mIsDebug = false;
        private Paint mPaint = null;
        private float[] mInhalePoint = null;
        private InhaleMesh mInhaleMesh = null;

        public MeshView(Context context, Bitmap bmp) {
            super(context);
            init(bmp);
        }

        public MeshView(Context context, AttributeSet attrs, Bitmap bmp) {
            super(context, attrs);
            init(bmp);
        }

        private void init(Bitmap bmp) {
            setFocusable(true);

            Matrix matrix = new Matrix();
            // 旋转180度
            matrix.postRotate(180);
            int width = bmp.getWidth();
            int height = bmp.getHeight();
            mBitmap = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix,
                    true);

            mPaint = new Paint();
            mInhalePoint = new float[] {
                    0, 0
            };
            mInhaleMesh = new InhaleMesh(WIDTH, HEIGHT);
            mInhaleMesh.setBitmapSize(mBitmap.getWidth(), mBitmap.getHeight());
        }

        public void setBitmap(Bitmap bmp) {
            Matrix matrix = new Matrix();
            matrix.postRotate(180); /* 翻转180度 */
            int width = bmp.getWidth();
            int height = bmp.getHeight();
            mBitmap = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix,
                    true);
        }

        public void setIsDebug(boolean isDebug) {
            mIsDebug = isDebug;
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            float bitmapWidth = mBitmap.getWidth();
            float bitmapHeight = mBitmap.getHeight();

            buildPaths(AiYouManager.getInstance(AiYouApplication.getInstance()).dip2px(20),
                    h - AiYouManager.getInstance(AiYouApplication.getInstance()).dip2px(25));
            buildMesh(bitmapWidth, bitmapHeight);
        }

        public boolean startAnimation(boolean reverse) {
            Animation anim = this.getAnimation();
            if (null != anim && !anim.hasEnded()) {
                return false;
            }

            PathAnimation animation = new PathAnimation(0, HEIGHT + 1, reverse,
                    new PathAnimation.IAnimationUpdateListener() {
                        @Override
                        public void onAnimUpdate(int index) {
                            mInhaleMesh.buildMeshes(index);
                            invalidate();
                        }
                    });

            if (null != animation) {
                animation.setDuration(1000);
                this.startAnimation(animation);
            }
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(0x00CCCCCC);
            canvas.drawBitmapMesh(mBitmap, mInhaleMesh.getWidth(),
                    mInhaleMesh.getHeight(), mInhaleMesh.getVertices(), 0,
                    null, 0, mPaint);
            // Draw the target point.
            // mPaint.setColor(Color.RED);
            // mPaint.setStrokeWidth(2);
            // mPaint.setAntiAlias(true);
            // mPaint.setStyle(Paint.Style.FILL);
            // canvas.drawCircle(mInhalePoint[0], mInhalePoint[1], 5, mPaint);

            if (mIsDebug) {
                // Draw the mesh vertices.
                canvas.drawPoints(mInhaleMesh.getVertices(), mPaint);
                // Draw the paths
                mPaint.setColor(Color.BLUE);
                mPaint.setStyle(Paint.Style.STROKE);
                Path[] paths = mInhaleMesh.getPaths();
                for (Path path : paths) {
                    canvas.drawPath(path, mPaint);
                }
            }
        }

        private void buildMesh(float w, float h) {
            mInhaleMesh.buildMeshes(w, h);
        }

        public void buildPaths(float endX, float endY) {
            mInhalePoint[0] = endX;
            mInhalePoint[1] = endY;
            mInhaleMesh.buildPaths(endX, endY);
        }

        // private int mLastPointX = 0;
        // private int mLastPointY = 0;

        // @Override
        // public boolean onTouchEvent(MotionEvent event) {
        // float[] pt = { event.getX(), event.getY() };
        //
        // if (event.getAction() == MotionEvent.ACTION_UP) {
        // int x = (int) pt[0];
        // int y = (int) pt[1];
        // if (mLastPointX != x || mLastPointY != y) {
        // mLastPointX = x;
        // mLastPointY = y;
        // buildPaths(pt[0], pt[1]);
        // invalidate();
        // }
        // }
        // return true;
        // }
    }

    private static class PathAnimation extends Animation {

        public interface IAnimationUpdateListener {
            public void onAnimUpdate(int index);
        }

        private int mFromIndex;
        private int mEndIndex;
        private boolean mReverse;
        private IAnimationUpdateListener mListener;

        public PathAnimation(int fromIndex, int endIndex, boolean reverse,
                IAnimationUpdateListener listener) {
            mFromIndex = fromIndex;
            mEndIndex = endIndex;
            mReverse = reverse;
            mListener = listener;
        }

        public boolean getTransformation(long currentTime,
                Transformation outTransformation) {
            return super.getTransformation(currentTime, outTransformation);
        }

        @Override
        protected void applyTransformation(float interpolatedTime,
                Transformation t) {
            Interpolator interpolator = this.getInterpolator();
            if (null != interpolator) {
                float value = interpolator.getInterpolation(interpolatedTime);
                interpolatedTime = value;
            }
            if (mReverse) {
                interpolatedTime = 1.0f - interpolatedTime;
            }
            int currentIndex = (int) (mFromIndex + (mEndIndex - mFromIndex)
                    * interpolatedTime);

            if (null != mListener) {
                mListener.onAnimUpdate(currentIndex);
            }
        }
    }
}

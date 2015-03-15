
package com.aiyou;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.aiyou.utils.AiYouManager;
import com.aiyou.utils.SwitchManager;
import com.aiyou.utils.FileCache.FileManager;
import com.aiyou.utils.http.HttpManager;
import com.aiyou.utils.thread.ThreadUtils;
import com.umeng.analytics.MobclickAgent;

import external.SwipeBackLayout.SwipeBackLayout;
import external.SwipeBackLayout.app.SwipeBackActivity;

public class BaseActivity extends SwipeBackActivity implements
        SensorEventListener {
    /**
     * 滑动结束Activity的布局
     */
    private SwipeBackLayout mSwipeBackLayout;

    // 传感器对象
    private Sensor mSensor;
    // 传感器管理者
    private SensorManager mSensorManager;
    // 坐标
    private float mLastX = 0;
    private float mLastY = 0;
    private float mLastZ = 0;
    // 时间阀值
    private static final int UPDATE_INTERVAL_TIME = 100;
    // 速度阀值
    private static final int SPEED_SHRESHOLD = 2000;
    // 最后时间
    private long mLastDate = 0;

    private boolean mFlagRelease = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * 滑动结束Activity的布局
         */
        mSwipeBackLayout = getSwipeBackLayout();

        // 实例化对象
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initSwipeOut();

        // 友盟应用统计
        MobclickAgent.onResume(this);
        if (SwitchManager.getInstance(this).isShakeShareEnabled()) {
            // 注册
            mSensorManager.registerListener(this, mSensor,
                    SensorManager.SENSOR_DELAY_GAME);
        }
    }
    
    public void initSwipeOut() {
        int edge = SwitchManager.getInstance(getBaseContext()).getSwipeOut();
        if (edge == SwitchManager.SWIPE_CLOSE) {
            mSwipeBackLayout.setEnableGesture(false);
        } else {
            mSwipeBackLayout.setEnableGesture(true);
            if (edge == SwitchManager.SWIPE_LEFT) {
                mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
            } else if (edge == SwitchManager.SWIPE_RIGHT) {
                mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_RIGHT);
            } else {
                mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT
                        | SwipeBackLayout.EDGE_RIGHT);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 友盟应用统计
        MobclickAgent.onPause(this);
        // 停止注册
        try {
            mSensorManager.unregisterListener(this);
        } catch (Exception e) {
        }
    }

    public void setRelease(boolean flag) {
        mFlagRelease = flag;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFlagRelease) {
            HttpManager.getInstance(this).disconnect(this);
            ThreadUtils.shutDown();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // 获取当前时间
        long currentDate = System.currentTimeMillis();
        // 时间变化
        long timeDate = currentDate - mLastDate;
        // 如果时间间隔小于时间阀值
        if (timeDate < UPDATE_INTERVAL_TIME) {
            return;

        }
        // 当前时间赋值给最后时间
        mLastDate = currentDate;

        // 获得当前的坐标x,y,z
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        // 获得x,y,z的变化值
        float deltaX = x - mLastX;
        float deltaY = y - mLastY;
        float deltaZ = z - mLastZ;
        // 将现在的坐标变为last坐标
        mLastX = x;
        mLastY = y;
        mLastZ = z;
        // 达到速度阀值，发出振动
        double speed = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ
                * deltaZ)
                / timeDate * 10000;
        if (speed > SPEED_SHRESHOLD) {
            share();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * 分享
     */
    private void share() {
        AiYouManager.getInstance(this).vibrate(500);

        Bitmap bmp = getSnapshot();

        File file = saveBmp(bmp);

        if (file != null) {
            Uri uri = Uri.fromFile(file);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/jpeg");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(intent,
                    "来自" + getResources().getString(R.string.app_name) + "的分享"));
        } else {
            Toast.makeText(this, "分享失败 ", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取屏幕截图
     * 
     * @return
     */
    private Bitmap getSnapshot() {
        View view = getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        Bitmap bmp = view.getDrawingCache();
        // 获取状态栏高度
        Rect frame = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        try {
            bmp = Bitmap.createBitmap(bmp, 0, statusBarHeight, bmp.getWidth(),
                    bmp.getHeight() - statusBarHeight);
        } catch (OutOfMemoryError e) {
        }
        view.setDrawingCacheEnabled(false);
        return bmp;
    }

    /**
     * 将图片保存到本地
     * 
     * @param bmp
     * @return
     */
    private File saveBmp(Bitmap bmp) {
        String dir = FileManager.getDirectory(FileManager.DIR_SNAPSHOT);
        // 注意HH：24小时制；hh：12小时制
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmss",
                Locale.getDefault());
        String fileName = sDateFormat.format(new java.util.Date()) + FileManager.BMP_SUFFIX;

        boolean result = FileManager.saveBmpToSd(bmp, dir, fileName);
        if (result) {
            Toast.makeText(this, "截图已保存：" + dir + "/" + fileName, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "保存截图失败", Toast.LENGTH_SHORT).show();
            return null;
        }
        File file = new File(dir + "/" + fileName);
        return file;
    }
}

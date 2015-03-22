
package com.aiyou.iptv;

import com.aiyou.R;
import com.aiyou.iptv.bean.Chanel;
import com.aiyou.utils.AiYouManager;
import com.aiyou.utils.SwitchManager;
import com.aiyou.utils.logcat.Logcat;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class IptvViewBufferActivity extends Activity implements OnInfoListener,
        OnBufferingUpdateListener {
    public static final String KEY_CHANEL = "chanel";

    private int mFlag = 0;
    private float mY = 0, mRawY = 0;
    private float mThreshold = 2;

    private VideoView mVideoView;
    private MediaController mMediaController;
    private ProgressBar mProgressBar;
    private TextView mDownloadRateTV, mLoadRateTV, mIndicateTV;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SwitchManager.getInstance(getBaseContext()).isNightModeEnabled()) {
            // 夜间模式
            this.setTheme(R.style.ThemeNight);
        } else {
            // 日间模式
            this.setTheme(R.style.ThemeDay);
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 取消标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 全屏
        setContentView(R.layout.activity_viewbuffer);

        init();
    }

    @SuppressLint({
            "NewApi", "ClickableViewAccessibility"
    })
    private void init() {
        if (!LibsChecker.checkVitamioLibs(this))
            return;

        mVideoView = (VideoView) findViewById(R.id.buffer);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        mDownloadRateTV = (TextView) findViewById(R.id.download_rate);
        mLoadRateTV = (TextView) findViewById(R.id.load_rate);
        mIndicateTV = (TextView) findViewById(R.id.tv_indicate);
        
        if(SwitchManager.getInstance(getBaseContext()).needShowIptvHelp()) {
            final LinearLayout ll_help = (LinearLayout)findViewById(R.id.ll_help);
            ll_help.setVisibility(View.VISIBLE);
            ll_help.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setVisibility(View.GONE);
                }
            });
            SwitchManager.getInstance(getBaseContext()).disableShowIptvHelp();
        }

        Chanel chanel = (Chanel) getIntent().getSerializableExtra(KEY_CHANEL);
        Logcat.e(chanel.name, chanel.url);
        Uri uri = Uri.parse(chanel.url);
        mVideoView.setVideoURI(uri);
        mMediaController = new MediaController(this);
        mVideoView.setMediaController(mMediaController);
        mVideoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_HIGH);
        mVideoView.setOnInfoListener(this);
        mVideoView.setBufferSize(5120);
        mVideoView.setOnBufferingUpdateListener(this);

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                // optional need Vitamio 4.0
                mediaPlayer.setPlaybackSpeed(1.0f);
                int vWidth = mediaPlayer.getVideoWidth();
                int vHeight = mediaPlayer.getVideoHeight();
                if (vWidth > AiYouManager.getScreenWidth()
                        || vHeight > AiYouManager.getScreenHeight()) {
                    // 如果video的宽或者高超出了当前屏幕的大小，则要进行缩放
                    float wRatio = (float) vWidth / (float) AiYouManager.getScreenWidth();
                    float hRatio = (float) vHeight / (float) AiYouManager.getScreenHeight();
                    // 选择大的一个进行缩放
                    float ratio = Math.max(wRatio, hRatio);
                    vWidth = (int) Math.ceil((float) vWidth / ratio);
                    vHeight = (int) Math.ceil((float) vHeight / ratio);
                    // 设置surfaceView的布局参数
                    mVideoView.setLayoutParams(new LinearLayout.LayoutParams(vWidth, vHeight));
                    // 然后开始播放视频
                    mediaPlayer.start();
                }
            }
        });
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (mVideoView.isPlaying()) {
                    mVideoView.pause();
                    showProgress(true);
                    mDownloadRateTV.setText("");
                    mLoadRateTV.setText("");
                    mDownloadRateTV.setVisibility(View.VISIBLE);
                    mLoadRateTV.setVisibility(View.VISIBLE);

                }
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                mVideoView.start();
                showProgress(false);
                mDownloadRateTV.setVisibility(View.GONE);
                mLoadRateTV.setVisibility(View.GONE);
                break;
            case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
                mDownloadRateTV.setText("" + extra + "kb/s" + "  ");
                break;
        }
        return true;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        mLoadRateTV.setText(percent + "%");
    }

    /**
     * 设置cpb_progress的状态和是否显示
     * 
     * @param flag
     */
    private void showProgress(boolean flag) {
        if (flag) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        touchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    private void touchEvent(MotionEvent event) {
        Logcat.e("touch", "true");
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mY = event.getY();
                mRawY = mY;
                if (event.getX() < AiYouManager.getScreenHeight() / 2.0f) {
                    mFlag = 1;
                } else {
                    mFlag = 0;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float y = event.getY();
                float yy = (mY - y) / mThreshold;
                if (Math.abs(yy) >= 1) {
                    mY = y;
                    if (mFlag == 0) {
                        // 调节音量
                        setVolumn(yy);
                    } else if (mFlag == 1) {
                        // 调节亮度
                        setBrightness(yy);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                showIndicate(null);
                if(Math.abs(mRawY - event.getY()) < 10) {
                    mMediaController.show(2000);
                }
                break;
        }
    }

    private void setVolumn(float volumn) {
        AudioManager audioMgr = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
        if (volumn > 0) {
            audioMgr.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        } else if (volumn < 0) {
            audioMgr.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        }
        showIndicate("声音：" + audioMgr.getStreamVolume(AudioManager.STREAM_MUSIC));
        Logcat.e("volumn", audioMgr.getStreamVolume(AudioManager.STREAM_MUSIC) + "");
    }

    private void setBrightness(float brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = lp.screenBrightness + brightness / 100.0f;
        if (lp.screenBrightness > 1) {
            lp.screenBrightness = 1;
        } else if (lp.screenBrightness < 0.1) {
            lp.screenBrightness = 0.1f;
        }
        showIndicate(String.format("亮度：%.0f", lp.screenBrightness * 100));
        Logcat.e("bright", lp.screenBrightness + "");
        getWindow().setAttributes(lp);
    }

    private void showIndicate(String str) {
        if (TextUtils.isEmpty(str)) {
            mIndicateTV.setVisibility(View.GONE);
        } else {
            mIndicateTV.setText(str);
            mIndicateTV.setVisibility(View.VISIBLE);
        }
    }
}

package com.aiyou.viewLargeImage;

import com.aiyou.AiYouApplication;
import com.aiyou.R;
import com.aiyou.utils.NetWorkManager;
import com.aiyou.utils.SwitchManager;
import com.aiyou.viewLargeImage.GetLargeImgTask.ProgressListener;
import external.GifImageViewEx.net.frakbot.imageviewex.ImageViewEx;
import external.otherview.MagicImageView;
import external.otherview.SinkingView;
import external.smartimageview.SmartImageView;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.AsyncTask.Status;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.Toast;

public class ViewLargeImageFragment extends Fragment implements
        ProgressListener {
    /**
     * 获取大图的异步任务
     */
    private GetLargeImgTask mTask = null;

    private String mUrl;
    private boolean mShowStill = true;
    // sinkingview
    private SinkingView mSinkView;
    private SmartImageView mSmartIV;
    // 查看大图
    private FrameLayout mMIVFLayout;
    private MagicImageView mMagicImageView;
    private ImageViewEx mImageViewEx;

    public ViewLargeImageFragment(String url) {
        mUrl = url;
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_large_image, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        /**
         * sinkingview
         */
        mSinkView = (SinkingView) view
                .findViewById(R.id.activity_view_large_image_sv);
        mSmartIV = (SmartImageView) view
                .findViewById(R.id.activity_view_large_image_siv);
        /**
         * 大图
         */
        mMIVFLayout = (FrameLayout) view
                .findViewById(R.id.activity_view_large_image_fl_tiv);
        mMagicImageView = (MagicImageView) view
                .findViewById(R.id.activity_view_large_image_tiv);
        mImageViewEx = (ImageViewEx) view
                .findViewById(R.id.activity_view_large_image_ive);

        // 设置缩放比例
        mMagicImageView.setMaxScale(10f);

        if (SwitchManager.getInstance(getActivity()).isNightModeEnabled()) {
            mSmartIV.setColorFilter(Color.GRAY,
                    android.graphics.PorterDuff.Mode.MULTIPLY);
            mImageViewEx.setColorFilter(Color.GRAY,
                    android.graphics.PorterDuff.Mode.MULTIPLY);
        }
        if (mShowStill) {
            showStill();
        } else {
            showDynamic();
        }
        loadLargeImage();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mTask) {
            if (Status.FINISHED != mTask.getStatus()) {
                mTask.cancel(true);
            }
            mTask = null;
        }
    }

    public void setUrl(String url) {
        mUrl = url;
        loadLargeImage();
    }

    public void showStill() {
        mShowStill = true;
        if (mImageViewEx != null && mMagicImageView != null) {
            mImageViewEx.setVisibility(View.GONE);
            mMagicImageView.setVisibility(View.VISIBLE);
            if (mImageViewEx.isPlaying()) {
                mImageViewEx.stop();
            }
        }
    }

    public void showDynamic() {
        mShowStill = false;
        if (mImageViewEx != null && mMagicImageView != null) {
            mImageViewEx.setVisibility(View.VISIBLE);
            mMagicImageView.setVisibility(View.GONE);
            if (mImageViewEx.canPlay()) {
                mImageViewEx.play();
            }
        }
    }

    public MagicImageView getStillView() {
        return mMagicImageView;
    }

    public ImageViewEx getDynamicView() {
        return mImageViewEx;
    }

    public SinkingView getLoadingView() {
        return mSinkView;
    }

    public FrameLayout getLargeImageLayout() {
        return mMIVFLayout;
    }

    public void clearAnim() {
        mMIVFLayout.clearAnimation();
        mSinkView.clearAnimation();
    }

    private void loadLargeImage() {
        if (!NetWorkManager.getInstance(getActivity()).isNetAvailable()) {
            Toast.makeText(getActivity(), NetWorkManager.MSG_NONET,
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(mUrl)) {
            Toast.makeText(getActivity(), "图片地址为空", Toast.LENGTH_LONG).show();
            return;
        }

        if (null != mTask) {
            switch (mTask.getStatus()) {
            case RUNNING:
                mTask.cancel(true);
                break;
            case PENDING:
                mTask.cancel(false);
                break;
            default:
                break;
            }
        }

        mTask = new GetLargeImgTask(getActivity(), mUrl, this);
        mTask.execute();
        mMagicImageView.setImageResource(R.drawable.touch_image_view);
        mImageViewEx.setImageBitmap(null);
    }

    @Override
    public void onStartProgress() {
        mMIVFLayout.clearAnimation();
        mMIVFLayout.setVisibility(View.GONE);
        mSinkView.clearAnimation();
        mSinkView.setVisibility(View.VISIBLE);

        mSmartIV.setImageUrl(mUrl, R.drawable.iu_default_gray,
                R.drawable.iu_default_green);
    }

    @Override
    public void onProgress(int progress) {
        float percent = progress / 100.0f;
        mSinkView.setPercent(percent);
    }

    @Override
    public void onFinishProgress(byte[] result) {
        if (result == null) {
            Toast.makeText(getActivity(), "下载失败", Toast.LENGTH_SHORT).show();
            return;
        }
        mMagicImageView.setByte(result);
        try {
            mImageViewEx.setSource(result);
        } catch (OutOfMemoryError e) {
        }
        mImageViewEx.pause();
        if (mImageViewEx.getVisibility() == View.VISIBLE
                && mImageViewEx.canPlay()) {
            mImageViewEx.play();
        }
        if (null != result) {
            mMIVFLayout.setVisibility(View.VISIBLE);
        }
        Animation animIn = AnimationUtils.loadAnimation(
                AiYouApplication.getInstance(), android.R.anim.fade_in);
        Animation animOut = AnimationUtils.loadAnimation(
                AiYouApplication.getInstance(), android.R.anim.fade_out);
        mSinkView.clear();
        mMIVFLayout.startAnimation(animIn);
        mSinkView.startAnimation(animOut);
        animOut.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                mSinkView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationStart(Animation arg0) {
            }
        });
    }

}

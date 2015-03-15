
package external.SmartImageView;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

import com.aiyou.utils.thread.ThreadUtils;
import com.aiyou.view.DarkImageView;

import external.SmartImageView.SmartImageTask.OnCompleteHandler;
import external.SmartImageView.SmartImageTask.OnCompleteListener;

public class SmartImageView extends DarkImageView {
    private static WebImageCache webImageCache;

    private SmartImageTask mCurTask;

    public SmartImageView(Context context) {
        super(context);
    }

    public SmartImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SmartImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // Helpers to set image by URL
    public void setImageUrl(String url) {
        setImage(new WebImage(url));
    }

    public void setImageUrl(String url,
            OnCompleteListener completeListener) {
        setImage(new WebImage(url), completeListener);
    }

    public void setImageUrl(String url, final Integer fallbackResource) {
        setImage(new WebImage(url), fallbackResource);
    }

    public void setImageUrl(String url, final Integer fallbackResource,
            OnCompleteListener completeListener) {
        setImage(new WebImage(url), fallbackResource, completeListener);
    }

    public void setImageUrl(String url, final Integer fallbackResource,
            final Integer loadingResource) {
        setImage(new WebImage(url), fallbackResource, loadingResource);
    }

    public void setImageUrl(String url, final Integer fallbackResource,
            final Integer loadingResource,
            OnCompleteListener completeListener) {
        setImage(new WebImage(url), fallbackResource, loadingResource,
                completeListener);
    }

    // Set image using SmartImage object
    public void setImage(final SmartImage image) {
        setImage(image, null, null, null);
    }

    public void setImage(final SmartImage image,
            final OnCompleteListener completeListener) {
        setImage(image, null, null, completeListener);
    }

    public void setImage(final SmartImage image, final Integer fallbackResource) {
        setImage(image, fallbackResource, fallbackResource, null);
    }

    public void setImage(final SmartImage image,
            final Integer fallbackResource,
            OnCompleteListener completeListener) {
        setImage(image, fallbackResource, fallbackResource, completeListener);
    }

    public void setImage(final SmartImage image,
            final Integer fallbackResource, final Integer loadingResource) {
        setImage(image, fallbackResource, loadingResource, null);
    }

    public void setImage(final SmartImage image,
            final Integer fallbackResource, final Integer loadingResource,
            final OnCompleteListener completeListener) {
        // Set a loading resource
        if (loadingResource != null) {
            setImageResource(loadingResource);
        }

        Bitmap bmp = getBmpFromLocale(image.getUrl());
        if (bmp != null) {
            setImageBitmap(bmp);
            return;
        }

        // Cancel any existing tasks for this image view
        if (mCurTask != null) {
            mCurTask.cancel();
            mCurTask = null;
        }

        // Set up the new task
        mCurTask = new SmartImageTask(getContext(), image);
        mCurTask
                .setOnCompleteHandler(new OnCompleteHandler() {
                    @Override
                    public void onComplete(Bitmap bitmap) {
                        if (bitmap != null) {
                            setImageBitmap(bitmap);
                        } else {
                            // Set fallback resource
                            if (fallbackResource != null) {
                                setImageResource(fallbackResource);
                            }
                        }

                        if (completeListener != null) {
                            completeListener.onComplete();
                        }
                    }
                });

        // Run the task in a threadpool
        ThreadUtils.execute(mCurTask);
    }

    private synchronized Bitmap getBmpFromLocale(String url) {
        if (webImageCache == null) {
            webImageCache = new WebImageCache(getContext());
        }
        Bitmap bmp = webImageCache.get(url);
        return bmp;
    }

}


package external.SmartImageView;


import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.utils.http.HttpManager;
import com.aiyou.utils.image.ImageFactory;
import com.aiyou.utils.logcat.Logcat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;

public class WebImage implements SmartImage {
    private static final String TAG = WebImage.class.getSimpleName();

    private static WebImageCache webImageCache;
    private Context mContext;

    private String mUrl;

    public WebImage(String url) {
        this.mUrl = url;
    }

    public Bitmap getBitmap(Context context) {
        mContext = context;
        // Don't leak context
        if (webImageCache == null) {
            webImageCache = new WebImageCache(context);
        }

        // Try getting bitmap from cache first
        Bitmap bitmap = null;
        if (mUrl != null) {
            // bitmap = webImageCache.get(url);
            if (bitmap == null) {
                bitmap = getBitmapFromUrl(mUrl);
                if (bitmap != null) {
                    webImageCache.put(mUrl, bitmap);
                }
            }
        }
        return bitmap;
    }

    @SuppressLint("NewApi")
    private Bitmap getBitmapFromUrl(String url) {
        Bitmap bitmap = null;
        byte[] data = null;
        if (url.contains(BBSManager.API_HEAD)) {
            // 论坛图片
            url += BBSManager.FORMAT + "?appkey=" + BBSManager.APPKEY;
        }
        data = HttpManager.getInstance(mContext).getHttpByte(mContext, url);
        if (data != null) {
            try {
                // 对图像的大小进行处理
//                bitmap = ImageFactory.getFixedBmp(data, 200, 200, false);
                bitmap = ImageFactory.getMaxBmp(data, false);
            } catch (OutOfMemoryError e) {
                Logcat.e(TAG, "getBitmapFromUrl OOM");
            }
        }
        return bitmap;
    }

    public static void removeFromCache(String url) {
        if (webImageCache != null) {
            webImageCache.remove(url);
        }
    }

    @Override
    public String getUrl() {
        return mUrl;
    }
}

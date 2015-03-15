
package external.SmartImageView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

import com.aiyou.utils.FileCache.FileManager;
import com.aiyou.utils.logcat.Logcat;
import com.aiyou.utils.thread.ThreadUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class WebImageCache {
    private static final String TAG = WebImageCache.class.getSimpleName();

    private ConcurrentHashMap<String, SoftReference<Bitmap>> mMemoryCacheHashMap;
    private String mDiskCachePath;
    private boolean mDiskCacheEnabled = false;

    public WebImageCache(Context context) {
        // Set up in-memory cache store
        mMemoryCacheHashMap = new ConcurrentHashMap<String, SoftReference<Bitmap>>();
        // Set up disk cache store
        mDiskCachePath = FileManager.getDirectory(FileManager.DIR_IMG);
        if (mDiskCachePath != null) {
            File outFile = new File(mDiskCachePath);
            outFile.mkdirs();
            mDiskCacheEnabled = outFile.exists();
        }
    }

    Bitmap get(final String url) {
        Bitmap bitmap = null;
        // Check for image in memory
        bitmap = getBitmapFromMemory(url);
        // Check for image on disk cache
        if (bitmap == null) {
            bitmap = getBitmapFromDisk(url);
            // Write bitmap back into memory cache
            if (bitmap != null) {
                cacheBitmapToMemory(url, bitmap);
            }
        }
        return bitmap;
    }

    void put(String url, Bitmap bitmap) {
        cacheBitmapToMemory(url, bitmap);
        cacheBitmapToDisk(url, bitmap);
    }

    void remove(String url) {
        if (url == null) {
            return;
        }
        // Remove from memory cache
        mMemoryCacheHashMap.remove(getCacheKey(url));
        // Remove from file cache
        if (mDiskCachePath != null) {
            File f = new File(mDiskCachePath, getCacheKey(url));
            if (f.exists() && f.isFile()) {
                f.delete();
            }
        }
    }

    public void clear() {
        // Remove everything from memory cache
        mMemoryCacheHashMap.clear();
        // Remove everything from file cache
        if (mDiskCachePath != null) {
            File cachedFileDir = new File(mDiskCachePath);
            if (cachedFileDir.exists() && cachedFileDir.isDirectory()) {
                File[] cachedFiles = cachedFileDir.listFiles();
                for (File f : cachedFiles) {
                    if (f.exists() && f.isFile()) {
                        f.delete();
                    }
                }
            }
        }
    }

    private void cacheBitmapToMemory(final String url, final Bitmap bitmap) {
        mMemoryCacheHashMap.put(getCacheKey(url), new SoftReference<Bitmap>(bitmap));
    }

    private void cacheBitmapToDisk(final String url, final Bitmap bitmap) {
        if (!FileManager.checkSDCard()) {
            return;
        }
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                if (mDiskCacheEnabled) {
                    BufferedOutputStream ostream = null;
                    try {
                        File file = new File(mDiskCachePath, getCacheKey(url));
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        ostream = new BufferedOutputStream(
                                new FileOutputStream(file), 2 * 1024);
                        bitmap.compress(FileManager.BMP_FORMAT, FileManager.BMP_QUALITY, ostream);
                        ostream.flush();
                    } catch (FileNotFoundException e) {
                        Logcat.e(TAG, "cacheBitmapToDisk FileNotFoundException");
                    } catch (IOException e) {
                        Logcat.e(TAG, "cacheBitmapToDisk IOException:" + e.getMessage());
                    } finally {
                        FileManager.close(ostream);
                    }
                }
            }
        });
    }

    private Bitmap getBitmapFromMemory(String url) {
        Bitmap bitmap = null;
        SoftReference<Bitmap> softRef = mMemoryCacheHashMap.get(getCacheKey(url));
        if (softRef != null) {
            bitmap = softRef.get();
        }
        return bitmap;
    }

    private Bitmap getBitmapFromDisk(String url) {
        if (!FileManager.checkSDCard()) {
            return null;
        }
        Bitmap bitmap = null;
        if (mDiskCacheEnabled) {
            String filePath = getFilePath(url);
            File file = new File(filePath);
            if (file.exists()) {
                FileManager.updateFileTime(filePath);
                bitmap = BitmapFactory.decodeFile(filePath);
            }
        }
        return bitmap;
    }

    private String getFilePath(String url) {
        return mDiskCachePath + "/" + getCacheKey(url);
    }

    private String getCacheKey(String url) {
        return FileManager.getFileNameFromUrl(url);
    }
}

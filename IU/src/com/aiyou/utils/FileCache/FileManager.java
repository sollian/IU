
package com.aiyou.utils.FileCache;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.aiyou.utils.logcat.Logcat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.StatFs;
import android.util.Base64;

public class FileManager {
    private static final String TAG = FileManager.class.getSimpleName();

    /**
     * 文件过期时间为3天
     */
    private static final long FILE_EXPIRE_TIME = 3 * 24 * 60 * 60 * 1000;
    /**
     * SD卡保存目录
     */
    private static final String ROOTDIR = "/AiYou/";
    // 图片存储地址
    public static final String DIR_LARGEIMG = ROOTDIR + "LargeImg";
    // 拍照
    public static final String DIR_CAMERA = ROOTDIR + "CameraImg";
    // 截屏
    public static final String DIR_SNAPSHOT = ROOTDIR + "ScreenSnapshot";
    // 文件存储地址
    public static final String DIR_FILE = ROOTDIR + "Files";
    // SmartImageView、CircleImageView缓存图像存储地址
    public static final String DIR_IMG = ROOTDIR + "CacheImg";

    // 文件名后缀
    private static final String FILESUFFIX = ".gif";
    // 图片格式
    public static final CompressFormat BMP_FORMAT = CompressFormat.JPEG;
    public static final String BMP_SUFFIX = ".jpg";
    public static final int BMP_QUALITY = 80;

    // 缓存空间大小，单位MB
    private static final int FREE_SD_SPACE_NEEDED_TO_CACHE = 50;
    // SD卡是否挂载
    private static boolean mSDMounted = false;

    private String mFileName;
    private String mDirPath;

    public FileManager(String dir) {
        mFileName = dir;
        mDirPath = getDirectory(mFileName);
        if (mDirPath != null) {
            File file = new File(mDirPath);
            if (!file.exists()) {
                // 若不存在，创建目录
                file.mkdirs();
                return;
            }
        }
    }

    /**
     * 从SD卡读取图片
     * 
     * @param url 图片的网络地址，用于提取图片名称
     * @return 读取成功则返回图片，否则返回null
     */
    public byte[] getImage(final String url) {
        String dir = mDirPath;
        if (dir == null) {
            return null;
        }
        final String path = dir + "/" + convertUrlToFileName(url);
        File file = new File(path);
        if (file.exists()) {
            byte[] data = readFileByBytes(path);
            if (data == null) {
                file.delete();
            } else {
                updateFileTime(path);
            }
            return data;
        }
        return null;
    }

    /**
     * 保存图片到SD卡
     * 
     * @param bm 需要保存的图片
     * @param url 图片的网络地址，用于提取图片名称
     * @return 保存成功返回true，否则false
     */
    public boolean saveWebBmpToSd(byte[] buffer, String url) {
        return saveBytesToSd(buffer, mDirPath, convertUrlToFileName(url));
    }

    private boolean saveBytesToSd(byte[] buffer, String dir, String fileName) {
        if (buffer == null) {
            return false;
        }
        if (!isSDSpaceEnough()) {
            return false;
        }
        if (dir == null) {
            return false;
        }
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(dir + "/" + fileName);
        if (file.exists()) {
            file.delete();
        }
        OutputStream os = null;
        try {
            file.createNewFile();
            os = new FileOutputStream(file);
            os.write(buffer, 0, buffer.length);
            os.flush();
            return true;
        } catch (FileNotFoundException e) {
            Logcat.e(TAG, "saveBmpToSd FileNotFoundException");
            return false;
        } catch (IOException e) {
            Logcat.e(TAG, "saveBmpToSd IOException");
            return false;
        } finally {
            close(os);
        }
    }

    public static boolean saveBmpToSd(Bitmap bmp, String dir, String fileName) {
        if (bmp == null) {
            return false;
        }
        if (!checkSDCard()) {
            return false;
        }
        if (dir == null) {
            return false;
        }
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(dir + "/" + fileName);
        if (file.exists()) {
            file.delete();
        }
        OutputStream os = null;
        try {
            file.createNewFile();
            os = new FileOutputStream(file);
            bmp.compress(BMP_FORMAT, BMP_QUALITY, os);
            os.flush();
            return true;
        } catch (FileNotFoundException e) {
            Logcat.e(TAG, "saveBmpToSd FileNotFoundException");
            return false;
        } catch (IOException e) {
            Logcat.e(TAG, "saveBmpToSd IOException");
            return false;
        } finally {
            close(os);
        }
    }

    /**
     * 以字节为单位读取文件，常用于读二进制文件，如图片、声音、影像等文件。
     */
    public static byte[] readFileByBytes(String fileName) {
        InputStream in = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            in = new FileInputStream(fileName);
            byte[] buf = new byte[1024];
            int length = 0;
            while ((length = in.read(buf)) != -1) {
                out.write(buf, 0, length);
            }
        } catch (FileNotFoundException e) {
            Logcat.e(TAG, "readFileByBytes FileNotFoundException");
        } catch (IOException e) {
            Logcat.e(TAG, "readFileByBytes IOException");
        } catch (OutOfMemoryError e) {
            Logcat.e(TAG, "readFileByBytes OOM");
            close(out);
            out = null;
        } finally {
            close(out);
            close(in);
        }
        if (out == null) {
            return null;
        }
        return out.toByteArray();
    }

    /**
     * 将url转成文件名
     * 
     * @param url
     * @return
     */
    public static String convertUrlToFileName(String url) {
        // 判断是否是论坛图片
        if (url.endsWith("middle") || url.endsWith("small")) {
            url = url.substring(0, url.lastIndexOf('/'));
        }
        String fileName = null;
        String arr[] = getFileNameFromUrl(url).split("\\.");
        fileName = arr[0] + FILESUFFIX;
        return fileName;
    }

    /**
     * 删除过期文件
     * 
     * @param dirPath
     * @return 清理的文件个数,-1为找不到路径
     */
    public static int removeExpiredCache(String dirPath) {
        int count = 0;
        String path = getDirectory(dirPath);
        if (path == null) {
            return -1;
        }
        File dir = new File(path);
        File[] files = dir.listFiles();
        if (files != null && files.length > 0) {
            long time = System.currentTimeMillis();
            for (File file : files) {
                if (time - file.lastModified() > FILE_EXPIRE_TIME) {
                    file.delete();
                    count++;
                    Logcat.i(TAG, "清理过期文件：" + file.getAbsolutePath());
                }
            }
            Logcat.i(TAG, "共清理过期文件：" + count);
        }
        return count;
    }

    /**
     * 修改文件的最后修改时间 这里需要考虑,是否将使用的图片日期改为当前日期
     * 
     * @param path
     */
    public static void updateFileTime(String path) {
        File file = new File(path);
        long newModifiedTime = System.currentTimeMillis();
        file.setLastModified(newModifiedTime);
    }

    /**
     * 获取路径
     * 
     * @param dirName {@link #DIR_CAMERA}or{@link #DIR_FILE}or{@link #DIR_IMG}or
     *            {@link #DIR_IMG}or{@link #DIR_LARGEIMG}or{@link #DIR_SNAPSHOT}
     * @return
     */
    public static String getDirectory(String dirName) {
        String dir = null;
        if (isSDMounted() && dirName != null) {
            dir = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + dirName;// 获取根目录
        }
        return dir;
    }

    /**
     * 检查SD卡是否可用，该函数需启动时调用
     * 
     * @param context
     */
    public static boolean checkSDCard() {
        if (!isSDMounted() || !isSDSpaceEnough()) {
            return false;
        }
        return true;
    }

    /**
     * 判断SD卡是否挂载
     * 
     * @return
     */
    private static boolean isSDMounted() {
        if (!mSDMounted) {
            mSDMounted = Environment.getExternalStorageState().equals(
                    android.os.Environment.MEDIA_MOUNTED);
        }
        return mSDMounted;
    }

    /**
     * 判断SD卡是否空间充足
     */
    private static boolean isSDSpaceEnough() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
                .getPath());
        @SuppressWarnings("deprecation")
        double sdFreeMB = ((double) stat.getAvailableBlocks() * (double) stat
                .getBlockSize()) / 1024 / 1024;
        return sdFreeMB > FREE_SD_SPACE_NEEDED_TO_CACHE;
    }

    public static void close(OutputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                Logcat.e(TAG, "close IOException");
            }
        }
    }

    public static void close(InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                Logcat.e(TAG, "close IOException");
            }
        }
    }

    /**
     * 检查是否是图像文件
     * 
     * @param fileName
     * @return
     */
    @SuppressLint("DefaultLocale")
    public static boolean isImage(String fileName) {
        fileName = fileName.toLowerCase();
        return fileName.endsWith("jpg") || fileName.endsWith("jpeg")
                || fileName.endsWith("png") || fileName.endsWith("gif")
                || fileName.endsWith("bmp");
    }

    /**
     * 检查是否是mp3文件
     * 
     * @param fileName
     * @return
     */
    @SuppressLint("DefaultLocale")
    public static boolean isMp3(String fileName) {
        fileName = fileName.toLowerCase();
        return fileName.endsWith("mp3");
    }

    /**
     * 将图片的URL转换为图片名称
     * 
     * @param url
     * @return
     */
    public static String getFileNameFromUrl(String url) {
        String bmpName = null;
        if (url != null) {
            bmpName = Base64.encodeToString(url.getBytes(), Base64.NO_PADDING | Base64.NO_WRAP)
                    + BMP_SUFFIX;
        }
        return bmpName;
    }

    /**
     * 从Assets中读取表情图片
     */
    public static Bitmap getImageFromAssetsFile(String fileName, Context context) {
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        try {
            InputStream is = am.open("face/" + fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            Logcat.e(TAG, "getFaceFromAssetsFile IOException");
        }
        return image;
    }
}

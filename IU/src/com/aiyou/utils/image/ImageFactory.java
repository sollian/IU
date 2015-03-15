
package com.aiyou.utils.image;

import com.aiyou.utils.logcat.Logcat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

/**
 * 图像处理类
 * 
 * @author sollian
 */
public class ImageFactory {
    private static final String TAG = ImageFactory.class.getSimpleName();

    /**
     * 获取保证不发生OOM的最大图片
     * 
     * @param data
     * @param remainAlpha 是否保留Alpha通道
     * @return
     */
    @SuppressWarnings("deprecation")
    public static Bitmap getMaxBmp(byte[] data, boolean remainAlpha) {
        Bitmap bitmap = null;
        if (null == data) {
            return null;
        }
        int length = data.length;
        BitmapFactory.Options options = new BitmapFactory.Options();
        if (!remainAlpha) {
            // 按每像素2字节读取 ,默认argb_8888是4字节
//            options.inPreferredConfig = Bitmap.Config.RGB_565;
        }
        // 应该是共享流，以便因内存不足回收后，再次调用可以后台自动读取
        options.inInputShareable = true;
        // 读取图片时内存不足自动回收本bitmap
        options.inPurgeable = true;
        boolean flag = true;
        int size = 1;
        while (flag) {
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, length, options);
            options.inSampleSize = size;
            options.inJustDecodeBounds = false;
            try {
                bitmap = BitmapFactory
                        .decodeByteArray(data, 0, length, options);
                flag = false;
            } catch (OutOfMemoryError e) {
                Logcat.e(TAG, "getLargeBmp OOM");
                if (++size > 20) {
                    flag = false;
                }
            }
        }
        return bitmap;
    }
    
    @SuppressWarnings("deprecation")
    public static Bitmap getFixedBmp(String filePath, float width, float height, boolean remainAlpha) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        Bitmap bmp = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        if (!remainAlpha) {
            // 按每像素2字节读取 ,默认argb_8888是4字节
            options.inPreferredConfig = Bitmap.Config.RGB_565;
        }
        // 应该是共享流，以便因内存不足回收后，再次调用可以后台自动读取
        options.inInputShareable = true;
        // 读取图片时内存不足自动回收本bitmap
        options.inPurgeable = true;
        
        BitmapFactory.decodeFile(filePath, options);
        int scalew = (int) (options.outWidth / width);
        int scaleh = (int) (options.outHeight / height);

        int size = Math.max(scalew, scaleh) + 1;
        int tempSize = size;
        boolean flag = true;
        while (flag) {
            try {
                options.inJustDecodeBounds = true;
                options.inSampleSize = tempSize;
                // 获取图像
                options.inJustDecodeBounds = false;
                options.inInputShareable = true;
                options.inPurgeable = true;
                bmp = BitmapFactory.decodeFile(filePath, options);
                flag = false;
            } catch (OutOfMemoryError e) {
                Logcat.e(TAG, "getFixedBmp() OOM");
                if (++tempSize - size > 10) {
                    flag = false;
                }
            }
        }
        return bmp;
    }

    @SuppressWarnings("deprecation")
    public static Bitmap getFixedBmp(byte[] data, float width, float height, boolean remainAlpha) {
        if (data == null) {
            return null;
        }
        Bitmap bmp = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        if (!remainAlpha) {
            // 按每像素2字节读取 ,默认argb_8888是4字节
            options.inPreferredConfig = Bitmap.Config.RGB_565;
        }
        // 应该是共享流，以便因内存不足回收后，再次调用可以后台自动读取
        options.inInputShareable = true;
        // 读取图片时内存不足自动回收本bitmap
        options.inPurgeable = true;

        int length = data.length;
        BitmapFactory.decodeByteArray(data, 0, length, options);
        int scalew = (int) (options.outWidth / width);
        int scaleh = (int) (options.outHeight / height);

        int size = Math.max(scalew, scaleh) + 1;
        int tempSize = size;
        boolean flag = true;
        while (flag) {
            try {
                options.inJustDecodeBounds = true;
                options.inSampleSize = tempSize;
                // 获取图像
                options.inJustDecodeBounds = false;
                options.inInputShareable = true;
                options.inPurgeable = true;
                bmp = BitmapFactory.decodeByteArray(data, 0, length,
                        options);
                flag = false;
            } catch (OutOfMemoryError e) {
                Logcat.e(TAG, "getFixedBmp() OOM");
                if (++tempSize - size > 10) {
                    flag = false;
                }
            }
        }
        return bmp;
    }

    /**
     * 模糊图像
     * 
     * @param sentBitmap
     * @param radius
     * @param canReuseInBitmap
     * @return
     */

    public static Bitmap doBlur(Bitmap sentBitmap, int radius,
            boolean canReuseInBitmap) {
        Bitmap bitmap;
        if (canReuseInBitmap) {
            bitmap = sentBitmap;
        } else {
            bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
        }

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16)
                        | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);
        return (bitmap);
    }
}

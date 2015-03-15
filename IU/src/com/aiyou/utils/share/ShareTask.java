
package com.aiyou.utils.share;

import java.io.File;

import com.aiyou.R;
import com.aiyou.utils.FileCache.FileManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

/**
 * 分享异步线程类
 * 
 * @author sollian
 */
public class ShareTask extends AsyncTask<Void, String, Boolean> {
    @SuppressWarnings("unused")
    private static final String TAG = ShareTask.class.getSimpleName();

    private Context mContext;
    private ShareListener mListener;

    private Intent mIntent;

    private String mUrl;
    private String mSubject;
    private String mContent;

    /**
     * 分享图片
     * 
     * @param context
     * @param url 图片地址
     */
    public ShareTask(Activity activity, String url, ShareListener listener) {
        mContext = activity;
        mUrl = url;
        mListener = listener;
        mIntent = new Intent(Intent.ACTION_SEND);

        mSubject = null;
        mContent = null;
    }

    /**
     * 分享文本
     * 
     * @param context
     * @param subject 主题
     * @param content 内容
     */
    public ShareTask(Activity activity, String subject, String content, ShareListener listener) {
        mContext = activity;
        mSubject = subject;
        mContent = content;
        mListener = listener;
        mIntent = new Intent(Intent.ACTION_SEND);

        mUrl = null;
    }

    @Override
    protected void onPreExecute() {
        if (mListener != null) {
            mListener.onShareStart();
        }
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (!FileManager.checkSDCard()) {
            return false;
        }

        if (mUrl != null) {
            // 分享大图片
            mIntent.setType("image/jpeg");
            if (mUrl.endsWith("middle") || mUrl.endsWith("small")) {
                mUrl = mUrl.substring(0, mUrl.lastIndexOf('/'));
            }
            File file = new File(Environment.getExternalStorageDirectory()
                    .getAbsoluteFile()
                    + FileManager.DIR_LARGEIMG
                    + "/"
//                    + FileManager.getFileNameFromUrl(mUrl));\
                    + FileManager.convertUrlToFileName(mUrl));

            if (!file.exists()) {
                return false;
//                String path = Environment.getExternalStorageDirectory()
//                        .getAbsoluteFile()
//                        + FileManager.DIR_LARGEIMG
//                        + "/"
//                        + FileManager.convertUrlToFileName(mUrl);
//                byte[] data = FileManager.readFileByBytes(path);
//                Bitmap bmp = ImageFactory.getMaxBmp(data, false);
//                OutputStream outStream = null;
//                try {
//                    outStream = new FileOutputStream(file);
//                    bmp.compress(FileManager.BMP_FORMAT, FileManager.BMP_QUALITY, outStream);
//                } catch (FileNotFoundException e) {
//                    Logcat.e(TAG, "doInBackground FileNotFoundException:" + e.getMessage());
//                    return false;
//                } finally {
//                    FileManager.close(outStream);
//                    bmp.recycle();
//                }
            }
            Uri uri = Uri.fromFile(file);
            mIntent.putExtra(Intent.EXTRA_STREAM, uri);
        } else {
            if (null == mSubject || null == mContent) {
                return false;
            }
            // 分享网页链接
            mIntent.setType("text/plain");
            mIntent.putExtra(Intent.EXTRA_SUBJECT, mSubject);
            mIntent.putExtra(Intent.EXTRA_TEXT, mContent);
        }
        return true;
    }

    protected void onPostExecute(Boolean result) {
        if (mListener != null) {
            mListener.onShareFinish(result);
        }
        if (!result) {
            if (!FileManager.checkSDCard()) {
                Toast.makeText(mContext, "SD卡未安装或空间不足", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "分享失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            mContext.startActivity(Intent.createChooser(mIntent,
                    "来自" + mContext.getString(R.string.app_name) + "的分享"));
        }
    }

    public interface ShareListener {
        public void onShareStart();

        public void onShareFinish(Boolean success);
    }
}

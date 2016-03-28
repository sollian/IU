
package com.aiyou.viewLargeImage;

import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.utils.filecache.FileManager;
import com.aiyou.utils.http.HttpManager;

import android.content.Context;
import android.os.AsyncTask;

/**
 * 获取大图片的异步线程类
 *
 * @author sollian
 */
public class GetLargeImgTask extends AsyncTask<Void, Integer, byte[]> {
    private FileManager mFileMgr = new FileManager(FileManager.DIR_LARGEIMG);

    private Context mContext;
    private String mUrl;
    private ProgressListener mListener;

    public GetLargeImgTask(Context context, String url, ProgressListener listener) {
        mContext = context;
        mUrl = url;
        mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        if (mListener != null) {
            mListener.onStartProgress();
        }
    }

    @Override
    protected byte[] doInBackground(Void... params) {
        publishProgress(1);
        byte[] data;
        if (isCancelled()) {
            return null;
        }
        // 判断是否是论坛图片
        String strUrl = mUrl;
        if (strUrl.endsWith("middle") || strUrl.endsWith("small")) {
            strUrl = strUrl.substring(0, strUrl.lastIndexOf('/'));
        }
        if (!progress(1, 10)) {
            return null;
        }
        // 优先从SD卡加载图片
        data = mFileMgr.getImage(strUrl);
        if (!progress(11, 25)) {
            return null;
        }
        // 若不存在，则从网络下载，然后存储到本地
        if (data == null) {
            try {
                data = getBmpByUrl(strUrl);
            } catch (OutOfMemoryError e) {
                return null;
            }
            if (!progress(26, 80)) {
                return null;
            }
            mFileMgr.saveWebBmpToSd(data, strUrl);
        } else {
            if (!progress(26, 80)) {
                return null;
            }
        }
        if (!progress(81, 99)) {
            return null;
        }
        if (isCancelled()) {
            return null;
        }
        return data;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (mListener != null) {
            mListener.onProgress(values[0]);
        }
    }

    @Override
    protected void onPostExecute(byte[] result) {
        if (mListener != null) {
            mListener.onFinishProgress(result);
        }
    }

    /**
     * 发送进度信息
     *
     * @param start
     * @param end
     * @return
     */
    public boolean progress(int start, int end) {
        for (int i = start; i <= end; i++) {
            if (isCancelled()) {
                return false;
            }
            publishProgress(i);
//            try {
//                Thread.sleep(1);
//            } catch (InterruptedException e) {
//                return false;
//            }
        }
        return true;
    }

    /**
     * 获取图片的方法
     *
     * @param url
     * @return
     * @throws Exception
     */
    public byte[] getBmpByUrl(String url) {
        byte[] data;
        if (url.contains(BBSManager.API_HEAD)) {
            // 论坛图片
            url += BBSManager.FORMAT + "?appkey=" + BBSManager.APPKEY;
        }
        data = HttpManager.getInstance(mContext).getHttpByte(mContext, url);
        return data;
    }

    public interface ProgressListener {
        void onStartProgress();

        void onProgress(int progress);

        void onFinishProgress(byte[] result);
    }
}

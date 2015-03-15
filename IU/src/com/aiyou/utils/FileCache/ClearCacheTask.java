
package com.aiyou.utils.FileCache;

import java.io.File;

import android.os.AsyncTask;
import android.os.SystemClock;

/**
 * 清理文件的异步线程类
 * 
 * @author sollian
 */
public class ClearCacheTask extends AsyncTask<Void, String, Integer> {
    private String[] directory = new String[] {
            FileManager.DIR_CAMERA,
            FileManager.DIR_FILE, FileManager.DIR_IMG,
            FileManager.DIR_LARGEIMG, FileManager.DIR_SNAPSHOT,
    };

    private ClearCacheListener listener;

    public ClearCacheTask(ClearCacheListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        if (listener != null) {
            listener.onStartClear();
        }
    }

    @Override
    protected Integer doInBackground(Void... params) {
        String strProgress;
        String dirPath;
        int fileCount = 0;
        int nInterval = 100;
        for (String direc : directory) {
            dirPath = FileManager.getDirectory(direc);
            if (dirPath == null) {
                continue;
            }
            File dir = new File(dirPath);
            if (!dir.exists()) {
                continue;
            }
            File[] files = dir.listFiles();
            if (files == null || files.length <= 0) {
                continue;
            }
            nInterval = 2000 / files.length;
            if (nInterval > 400) {
                nInterval = 400;
            }
            for (int j = 0; j < files.length; j++) {
                strProgress = dirPath + ":\n" + j + "/" + (files.length - 1);
                publishProgress(strProgress);
                if (files[j].isFile() && files[j].delete()) {
                    fileCount++;
                }
                SystemClock.sleep(nInterval);
            }
        }
        return fileCount;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        if (listener != null) {
            // 更新进度
            listener.onProgressUpdate(values[0]);
        }
    }

    @Override
    protected void onPostExecute(Integer result) {
        if (listener != null) {
            listener.onEndClear(result);
        }
    }

    public interface ClearCacheListener {
        public void onStartClear();

        public void onProgressUpdate(String progress);

        public void onEndClear(int fileCount);
    }
}

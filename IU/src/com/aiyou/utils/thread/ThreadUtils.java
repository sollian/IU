
package com.aiyou.utils.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadUtils {
    private static ExecutorService mThreadPool;
    private static int mMaxPoolSize = 20;

    public static void execute(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        init();
        mThreadPool.execute(runnable);
    }

    public static Future<?> submit(Callable<?> task) {
        if (task == null) {
            return null;
        }
        init();
        return mThreadPool.submit(task);
    }

    private static void init() {
        if (mThreadPool == null) {
            mThreadPool = new ThreadPoolExecutor(0, mMaxPoolSize, 60L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>());
        }
    }

    public static void setMaxPoolSize(int size) {
        if (size > 0) {
            mMaxPoolSize = size;
            shutDown();
        }
    }

    /**
     * 程序退出时必须调用，其他看情况使用
     */
    public static void shutDown() {
        if (mThreadPool != null && !mThreadPool.isShutdown()) {
            mThreadPool.shutdownNow();
            mThreadPool = null;
        }
    }
}


package com.aiyou.utils.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadUtils {
    private static ExecutorService mThreadPool;

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
            mThreadPool = new ThreadPoolExecutor(0, 10, 60L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>());
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

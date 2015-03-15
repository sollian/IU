
package com.aiyou.utils.logcat;

import android.util.Log;

/**
 * @author sollian
 */
public class Logcat {
    private static final boolean DEBUG = false;

    public static void d(String tag, String error) {
        if (DEBUG) {
            Log.d(tag, error);
        }
    }

    public static void e(String tag, String error) {
        if (DEBUG) {
            Log.e(tag, error);
        }
    }

    public static void i(String tag, String error) {
        if (DEBUG) {
            Log.i(tag, error);
        }
    }

    public static void w(String tag, String error) {
        if (DEBUG) {
            Log.w(tag, error);
        }
    }

    public static void v(String tag, String error) {
        if (DEBUG) {
            Log.v(tag, error);
        }
    }

}

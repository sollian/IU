
package com.aiyou.utils;

import java.util.HashSet;
import java.util.Set;

import com.aiyou.utils.logcat.Logcat;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @author sollian
 */
public class NetWorkManager {
    private static final String TAG = NetWorkManager.class.getSimpleName();
    public static final String MSG_NONET = "网络连接失败";

    private static NetWorkManager mInstance;
    private Context mContext;

    /**
     * 网络链接状态
     */
    public enum NetStatus {
        // 未定义
        NOTHING,
        // 无网络链接
        NONE,
        // wifi链接
        NETTYPE_WIFI,
        // wap链接
        NETTYPE_CMWAP,
        // net链接
        NETTYPE_CMNET,
    };

    private NetStatus NETWORKSTATUS = NetStatus.NOTHING;

    public interface NetStatsChangeListener {
        public void onNetStatsChange();
    }

    private Set<NetStatsChangeListener> mListenerSet = new HashSet<NetStatsChangeListener>();

    private BroadcastReceiver mConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setNetworkType();
            for (NetStatsChangeListener listener : mListenerSet) {
                listener.onNetStatsChange();
            }
        }
    };

    private NetWorkManager(Context context) {
        mContext = context;
        updateNetworkType(mContext);
        /**
         * 注册网络连接状态监听器
         */
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(mConnectionReceiver, intentFilter);
    }

    public static NetWorkManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (NetWorkManager.class) {
                if (mInstance == null) {
                    mInstance = new NetWorkManager(context);
                }
            }
        }
        return mInstance;
    }

    public void registerNetStatsChangeListener(NetStatsChangeListener listener) {
        mListenerSet.add(listener);
    }

    public void unregisterNetStatsChangeListener(NetStatsChangeListener listener) {
        if (mListenerSet.contains(listener)) {
            mListenerSet.remove(listener);
        }
    }

    /**
     * 程序退出时调用
     */
    public void recycle() {
        try {
            mContext.unregisterReceiver(mConnectionReceiver);
        } catch (Exception e) {
            Logcat.e(TAG, "recycle:" + e.getMessage());
        }
        mInstance = null;
    }

    public boolean isNetAvailable() {
        return getNetworkType() != NetStatus.NOTHING;
    }

    /**
     * 获取当前网络类型
     * 
     * @return
     */
    public NetStatus getNetworkType() {
        return getNetworkType(mContext);
    }

    /**
     * 获取当前网络类型
     * 
     * @return
     */
    public NetStatus getNetworkType(Context context) {
        if (NETWORKSTATUS == NetStatus.NOTHING) {
            NETWORKSTATUS = updateNetworkType(context);
        }
        return NETWORKSTATUS;
    }

    /**
     * 设置网络类型
     * 
     * @param status
     */
    public void setNetworkType() {
        NETWORKSTATUS = updateNetworkType(mContext);
    }

    /**
     * 更新当前网络类型
     */
    @SuppressLint("DefaultLocale")
    private NetStatus updateNetworkType(Context context) {
        NetStatus netType = NetStatus.NOTHING;
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return netType;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_MOBILE) {
            String extraInfo = networkInfo.getExtraInfo();
            if (null != extraInfo && extraInfo.toLowerCase().contains("net")) {
                netType = NetStatus.NETTYPE_CMNET;
            } else {
                netType = NetStatus.NETTYPE_CMWAP;
            }
        } else if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = NetStatus.NETTYPE_WIFI;
        }
        return netType;
    }
}

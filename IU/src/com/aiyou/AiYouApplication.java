
package com.aiyou;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.aiyou.bbs.bean.Favorite;
import com.aiyou.bbs.bean.Section;
import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.iptv.utils.IptvManager;
import com.aiyou.map.data.MapHelper;
import com.aiyou.utils.AiYouManager;
import com.aiyou.utils.JsonHelper;
import com.aiyou.utils.NetWorkManager;
import com.aiyou.utils.SwitchManager;
import com.aiyou.utils.filecache.FileManager;
import com.aiyou.utils.thread.ThreadUtils;
import com.baidu.lbsapi.BMapManager;
import com.baidu.lbsapi.MKGeneralListener;
import com.baidu.mapapi.SDKInitializer;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.push.FeedbackPush;
import com.umeng.message.PushAgent;
import com.umeng.update.UmengUpdateAgent;

/**
 * @author sollian
 */
public class AiYouApplication extends Application {

    private static AiYouApplication mInstance;
    /**
     * 百度地图
     */
    public BMapManager mBMapManager;
    /**
     * 友盟推送
     */
    public PushAgent mPushAgent;

    public static AiYouApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        /**
         * 初始化
         */
        AiYouManager.getInstance(this);
        NetWorkManager.getInstance(this);
        SwitchManager.getInstance(this);

        if (FileManager.checkSDCard()) {
            removeExpiredCache();
        } else {
            Toast.makeText(getBaseContext(), "SD卡未安装或空间不足", Toast.LENGTH_SHORT).show();
        }

        try {
            // 百度地图初始化
            SDKInitializer.initialize(this);
            initEngineManager(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /**
         * 友盟推送
         */
        mPushAgent = PushAgent.getInstance(this);
        mPushAgent.setDebugMode(false);
        /**
         * 友盟用户反馈
         */
        FeedbackPush.getInstance(this).init(false);
        /**
         *  友盟自动更新
         */
        // 非wifi环境更新开启，要放在updata()之前调用
        UmengUpdateAgent.setUpdateOnlyWifi(SwitchManager.getInstance(this).getUpdateOnlyWifi());
        UmengUpdateAgent.update(this);
        /**
         * 友盟在线参数—— 获取帖子尾巴
         */
        MobclickAgent.updateOnlineConfig(this);
        BBSManager.getInstance(this).setAppTail(MobclickAgent.getConfigParams(this, "app_tail"));
        //获取iptv频道列表
        IptvManager.getChanelList();

        MapHelper.initMapDatas(this);

        initSections();
        initFavorite();
    }

    /**
     * 清理过期文件
     */
    private void removeExpiredCache() {
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                FileManager.removeExpiredCache(FileManager.DIR_IMG);
                FileManager.removeExpiredCache(FileManager.DIR_LARGEIMG);
            }
        });
    }

    private void initSections() {
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            return;
        }
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                Section.updateRootSection(AiYouApplication.getInstance());
            }
        });
    }

    private void initFavorite() {
        if (Favorite.mFavorite != null) {
            return;
        }
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            return;
        }
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                String strJson = Favorite.getFavorite(AiYouApplication.getInstance(), 0);
                if (!TextUtils.isEmpty(strJson) && JsonHelper.checkError(strJson) == null) {
                    Favorite.mFavorite = new Favorite(strJson);
                }
            }
        });
    }

    private void initEngineManager(Context context) {
        if (mBMapManager == null) {
            mBMapManager = new BMapManager(context);
        }
    }

    public static class MyGeneralListener implements MKGeneralListener {
        @Override
        public void onGetPermissionState(int iError) {
        }
    }
}

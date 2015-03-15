
package com.aiyou;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcelable;
import android.text.TextUtils;
import android.widget.Toast;

import com.aiyou.bbs.bean.Favorite;
import com.aiyou.bbs.bean.Section;
import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.map.data.MapHelper;
import com.aiyou.utils.AiYouManager;
import com.aiyou.utils.JsonHelper;
import com.aiyou.utils.NetWorkManager;
import com.aiyou.utils.SwitchManager;
import com.aiyou.utils.FileCache.FileManager;
import com.aiyou.utils.logcat.Logcat;
import com.aiyou.utils.thread.ThreadUtils;
import com.baidu.lbsapi.BMapManager;
import com.baidu.lbsapi.MKGeneralListener;
import com.baidu.mapapi.SDKInitializer;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

/**
 * @author sollian
 */
public class AiYouApplication extends Application {
    private static String TAG = AiYouApplication.class.getSimpleName();

    private static AiYouApplication mInstance;

    public BMapManager mBMapManager;

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

        if (SwitchManager.getInstance(this).isFirstRun() && !hasShortcut()) {
            createShortCut(this);
        }

        try {
            // 百度地图初始化
            SDKInitializer.initialize(this);
            initEngineManager(this);
        } catch (Exception e) {
            Logcat.e(TAG, "百度地图初始化错误");
        }

        // 友盟自动更新
        // 非wifi环境更新开启，要放在updata()之前调用
        UmengUpdateAgent.setUpdateOnlyWifi(SwitchManager.getInstance(this).getUpdateOnlyWifi());
        UmengUpdateAgent.update(this);
        // 获取帖子尾巴
        MobclickAgent.updateOnlineConfig(this);
        BBSManager.getInstance(this).setAppTail(MobclickAgent.getConfigParams(this, "app_tail"));
        MapHelper.initMapDatas(this);

        initSections();
        initFavorite();
    }

    /**
     * 创建快捷方式
     * 
     * @param act
     */
    private void createShortCut(Context act) {
        Intent shortcutintent = new Intent(
                "com.android.launcher.action.INSTALL_SHORTCUT");
        // 不允许重复创建
        shortcutintent.putExtra("duplicate", false);
        // 需要显示的名称
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
                act.getString(R.string.app_name));
        // 快捷图片
        Parcelable icon = Intent.ShortcutIconResource.fromContext(
                act.getApplicationContext(), R.drawable.ic_launcher);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        // 点击快捷图片，运行的程序主入口
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT,
                new Intent(act.getApplicationContext(), MainActivity.class));
        // 发送广播
        act.sendBroadcast(shortcutintent);
    }

    /**
     * 是否已经创建快捷方式
     * 
     * @return
     */
    private boolean hasShortcut() {
        boolean isInstallShortcut = false;
        final ContentResolver cr = getContentResolver();
        final String AUTHORITY = "com.android.launcher.settings";
        final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/favorites?notify=true");
        Cursor c = cr.query(CONTENT_URI, new String[] {
                "title", "iconResource"
        }, "title=?",
                new String[] {
                    getString(R.string.app_name).trim()
                }, null);
        if (c != null && c.getCount() > 0) {
            isInstallShortcut = true;
        }
        return isInstallShortcut;
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
        if (!mBMapManager.init(new MyGeneralListener())) {
        }
    }

    public static class MyGeneralListener implements MKGeneralListener {
        @Override
        public void onGetPermissionState(int iError) {
            if (iError != 0) {
            } else {
            }
        }
    }
}

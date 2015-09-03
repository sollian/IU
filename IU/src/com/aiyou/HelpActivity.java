
package com.aiyou;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebSettings.PluginState;

/**
 * 版本更新与功能介绍
 *
 * @author sollian
 */
public class HelpActivity extends BaseActivity {
    /**
     * 控件
     */
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        init();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @SuppressWarnings("deprecation")
    private void init() {
        mWebView = (WebView) findViewById(R.id.wv);
        WebSettings setting = mWebView.getSettings();
        setting.setJavaScriptEnabled(true);
        setting.setAllowFileAccess(true);
        setting.setAppCacheEnabled(true);
        setting.setLoadsImagesAutomatically(true);
        setting.setPluginState(PluginState.ON);

        mWebView.loadUrl("file:///android_asset/instruction.html");
    }

    /**
     * 按左上角返回键调用的方法
     *
     * @param view
     */
    public void selfFinish(View view) {
        scrollToFinishActivity();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 按下键盘上返回按钮
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            scrollToFinishActivity();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.destroy();
    }
}

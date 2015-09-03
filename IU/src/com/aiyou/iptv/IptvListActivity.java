
package com.aiyou.iptv;

import java.util.Collections;
import java.util.Comparator;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.aiyou.BaseActivity;
import com.aiyou.R;
import com.aiyou.iptv.adapter.ChanelAdapter;
import com.aiyou.iptv.bean.Chanel;
import com.aiyou.iptv.utils.IptvManager;
import com.aiyou.utils.ActivityFunc;
import com.aiyou.utils.NetWorkManager;
import com.aiyou.utils.SwitchManager;
import com.aiyou.utils.NetWorkManager.NetStatus;

import external.PullToRefresh.PullToRefreshListView;
import external.PullToRefresh.PullToRefreshBase.Mode;
import external.otherview.ActivitySplitAnimationUtil;

public class IptvListActivity extends BaseActivity implements OnItemClickListener {

    private ChanelAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SwitchManager.getInstance(getBaseContext()).isNightModeEnabled()) {
            // 夜间模式
            this.setTheme(R.style.ThemeNight);
        } else {
            // 日间模式
            this.setTheme(R.style.ThemeDay);
        }
        setContentView(R.layout.activity_iptv_list);

        if (ActivitySplitAnimationUtil.canPlay() && Build.VERSION.SDK_INT >= 14) {
            // 中心打开动画
            ActivitySplitAnimationUtil.prepareAnimation(this);
            ActivitySplitAnimationUtil.animate(this, 1000);
        }

        if (NetWorkManager.getInstance(getBaseContext()).getNetworkType() != NetStatus.NETTYPE_WIFI) {
            Toast.makeText(getBaseContext(), "请在wifi环境下观看", Toast.LENGTH_SHORT).show();
        }

        init();
    }

    @SuppressWarnings("deprecation")
    private void init() {
        PullToRefreshListView mPTRLV = (PullToRefreshListView) findViewById(R.id.ptrlv);
        mPTRLV.setShowIndicator(false);
        mPTRLV.setPullLabel("", Mode.BOTH);
        mPTRLV.setRefreshingLabel("", Mode.BOTH);
        mPTRLV.setReleaseLabel("", Mode.BOTH);
        mPTRLV.setLoadingDrawable(null, Mode.BOTH);
        ListView mListView = mPTRLV.getRefreshableView();

        Collections.sort(IptvManager.mChanelList, new Comparator<Chanel>() {
            @Override
            public int compare(Chanel c1, Chanel c2) {
                if (c1.frequency > c2.frequency) {
                    return -1;
                } else if (c1.frequency < c2.frequency) {
                    return 1;
                } else {
                    return 0;
                }
            }

        });

        mAdapter = new ChanelAdapter(getBaseContext(), IptvManager.mChanelList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Chanel chanel = IptvManager.mChanelList.get(position - 1);

        chanel.frequency++;
        IptvManager.getInstance(getBaseContext()).saveChanelFrequency(chanel);

        Intent intent = new Intent(IptvListActivity.this, IptvViewBufferActivity.class);
        intent.putExtra(IptvViewBufferActivity.KEY_CHANEL, chanel);
        ActivityFunc.startActivity(IptvListActivity.this, intent);
    }

    public void selfFinish(View view) {
        if (Build.VERSION.SDK_INT >= 14) {
            ActivitySplitAnimationUtil.finish(this);
        } else {
            scrollToFinishActivity();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 按下键盘上返回按钮
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            selfFinish(null);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivitySplitAnimationUtil.cancel();
        System.gc();
    }
}

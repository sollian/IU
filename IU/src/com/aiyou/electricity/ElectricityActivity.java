
package com.aiyou.electricity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.aiyou.BaseActivity;
import com.aiyou.R;
import com.aiyou.utils.AiYouManager;
import com.aiyou.utils.NetWorkManager;
import com.aiyou.utils.logcat.Logcat;
import com.aiyou.utils.thread.ThreadUtils;

import external.PullToRefresh.PullToRefreshBase;
import external.PullToRefresh.PullToRefreshBase.OnRefreshListener2;
import external.PullToRefresh.PullToRefreshListView;

public class ElectricityActivity extends BaseActivity implements OnRefreshListener2<ListView>,
        OnPageChangeListener {
    private static final String URL = "http://ydcx.bupt.edu.cn/see.aspx?useid=";

    private static final int MSG_ERROR = -1;
    private static final int MSG_MESSAGE = 0;
    private static final int MSG_MESSAGE1 = 1;
    private static final int MSG_MESSAGE2 = 2;
    private static final int MSG_NULL = 3;

    private String mValue1, mValue2;

    private Document mDoc;

    private String mId;

    private PullToRefreshListView mPTRLV1, mPTRLV2;
    private List<ConsumeInfo> mList1 = new ArrayList<ConsumeInfo>();
    private List<BuyEleInfo> mList2 = new ArrayList<BuyEleInfo>();
    private ListAdapter1 mAdapter1;
    private ListAdapter2 mAdapter2;
    private int mPage1 = 1, mPage2 = 1, mTotalPage1 = 1, mTotalPage2 = 1;

    private EditText mIdET;
    private ProgressDialog mProgressDlg;

    private LinearLayout mContentLL;
    private TextView mIdTV, mTypeTV, mAmmeterTV, mIpTV;
    private TextView mTag1TV, mTag2TV;
    private ViewPager mViewPager;

    // 处理UI线程的handler
    public Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == MSG_MESSAGE) {
                try {
                    parseHtml(mDoc);
                    mContentLL.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), "查询失败", Toast.LENGTH_SHORT).show();
                }
            } else if (msg.what == MSG_MESSAGE1) {
                ConsumeInfo.parseHtml(mList1, mDoc);
                mAdapter1.notifyDataSetChanged();
            } else if (msg.what == MSG_MESSAGE2) {
                BuyEleInfo.parseHtml(mList2, mDoc);
                mAdapter2.notifyDataSetChanged();
            } else if (msg.what == MSG_ERROR) {
                Toast.makeText(getBaseContext(), NetWorkManager.MSG_NONET, Toast.LENGTH_SHORT)
                        .show();
            }
            mProgressDlg.dismiss();
            mPTRLV1.onRefreshComplete();
            mPTRLV2.onRefreshComplete();
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_electricity);

        init();
    }

    @SuppressLint("InflateParams")
    private void init() {
        mIdET = (EditText) findViewById(R.id.et_id);
        mIdET.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                    KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_DONE:
                        onQuery(null);
                        break;
                }
                return true;
            }
        });
        mProgressDlg = new ProgressDialog(this);
        mProgressDlg.setCancelable(false);
        mProgressDlg.setCanceledOnTouchOutside(false);
        mProgressDlg.setIndeterminate(true);
        mProgressDlg.setMessage("正在查询");

        mContentLL = (LinearLayout) findViewById(R.id.ll_content);
        mIdTV = (TextView) findViewById(R.id.tv_id);
        mTypeTV = (TextView) findViewById(R.id.tv_type);
        mAmmeterTV = (TextView) findViewById(R.id.tv_ammeter);
        mIpTV = (TextView) findViewById(R.id.tv_ip);

        mTag1TV = (TextView) findViewById(R.id.tv_tag1);
        mTag2TV = (TextView) findViewById(R.id.tv_tag2);
        mViewPager = (ViewPager) findViewById(R.id.vp);

        List<View> viewList = new ArrayList<View>();
        LayoutInflater inflater = getLayoutInflater();
        View view1 = inflater.inflate(R.layout.electricity_vp_list1, null);
        View view2 = inflater.inflate(R.layout.electricity_vp_list2, null);
        mPTRLV1 = (PullToRefreshListView) view1.findViewById(R.id.ptrlv1);
        mPTRLV2 = (PullToRefreshListView) view2.findViewById(R.id.ptrlv2);
        mPTRLV1.setShowIndicator(false);
        mPTRLV2.setShowIndicator(false);
        mAdapter1 = new ListAdapter1(getBaseContext(), mList1);
        mAdapter2 = new ListAdapter2(getBaseContext(), mList2);
        mPTRLV1.setAdapter(mAdapter1);
        mPTRLV2.setAdapter(mAdapter2);
        mPTRLV1.setOnRefreshListener(this);
        mPTRLV2.setOnRefreshListener(this);
        viewList.add(view1);
        viewList.add(view2);
        mViewPager.setAdapter(new ElectricityPagerAdapter(getBaseContext(), viewList));
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setCurrentItem(0);
    }

    public void onQuery(View v) {
        String id = mIdET.getText().toString().trim();
        if (isValid(id)) {
            mId = id;
            query(id);
            AiYouManager.viewInputMethod(ElectricityActivity.this, false, mIdET);
        } else {
            Toast.makeText(getBaseContext(), "输入格式有误", Toast.LENGTH_SHORT).show();
        }
    }

    private void query(final String id) {
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            if (mHandler != null) {
                mHandler.sendEmptyMessage(MSG_ERROR);
            }
            return;
        }
        mProgressDlg.show();
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mDoc = Jsoup.connect(URL + id).timeout(10000).get();
                } catch (IOException e) {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(MSG_ERROR);
                    }
                    return;
                }
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(MSG_MESSAGE);
                }
            }
        });
    }

    private void parseHtml(Document doc) {
        String id = doc.select("span#labfangjianhao").get(0).text();
        String type = doc.select("span#yonghuleixing").get(0).text();
        String ammeter = doc.select("span#Labbiaohao").get(0).text();
        String ip = doc.select("span#onlinenow").get(0).text();
        mIdTV.setText(id);
        mTypeTV.setText(type);
        mAmmeterTV.setText(ammeter);
        mIpTV.setText(ip);
        mList1.clear();
        mList2.clear();
        mTotalPage1 = ConsumeInfo.parseHtml(mList1, doc);
        mAdapter1.notifyDataSetChanged();
        mTotalPage2 = BuyEleInfo.parseHtml(mList2, doc);
        mAdapter2.notifyDataSetChanged();

        mValue1 = doc.select("input#__VIEWSTATE").get(0).attr("value");
        mValue2 = doc.select("input#__EVENTVALIDATION").get(0).attr("value");
    }

    public void onTagClick(View v) {
        final int nId = v.getId();
        if (nId == R.id.tv_tag1) {
            mViewPager.setCurrentItem(0);
        } else if (nId == R.id.tv_tag2) {
            mViewPager.setCurrentItem(1);
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

    public void selfFinish(View view) {
        scrollToFinishActivity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        final int nId = refreshView.getId();
        if (nId == R.id.ptrlv1) {
            if (mPage1 < mTotalPage1) {
                queryNext(mId, ++mPage1, 1);
                return;
            }
        } else if (nId == R.id.ptrlv2) {
            if (mPage2 < mTotalPage2) {
                queryNext(mId, ++mPage2, 2);
                return;
            }
        }
        Toast.makeText(getBaseContext(), "没有了", Toast.LENGTH_SHORT).show();
        if (mHandler != null) {
            mHandler.sendEmptyMessage(MSG_NULL);
        }
    }

    private void queryNext(final String id, final int page, final int target) {
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            if (mHandler != null) {
                mHandler.sendEmptyMessage(MSG_ERROR);
            }
            return;
        }
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                Map<String, String> data = new HashMap<String, String>();
                data.put("__EVENTTARGET", "GridView" + target);
                data.put("__EVENTARGUMENT", "Page$" + page);
                data.put("__VIEWSTATE", mValue1);
                data.put("__EVENTVALIDATION", mValue2);
                try {

                    mDoc = Jsoup.connect(URL + id)
                            .timeout(10000)
                            .data(data)
                            .post();
                } catch (IOException e) {
                    Logcat.e(id, "IOException:" + e.getMessage());
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(MSG_ERROR);
                    }
                    return;
                }
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(MSG_MESSAGE1);
                }
            }
        });
    }

    private boolean isValid(String id) {
        Pattern p = Pattern
                .compile("\\d{1,2}\\-\\d{3,4}");
        Matcher m = p.matcher(id);
        while (m.find()) {
            return true;
        }
        return false;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (position == 0) {
            mTag1TV.setTextColor(0xff0fd451);
            mTag2TV.setTextColor(0xff000000);
        } else if (position == 1) {
            mTag1TV.setTextColor(0xff000000);
            mTag2TV.setTextColor(0xff0fd451);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}

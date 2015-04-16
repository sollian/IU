package com.aiyou.ecard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.aiyou.BaseActivity;
import com.aiyou.R;
import com.aiyou.utils.AiYouManager;
import com.aiyou.utils.NetWorkManager;
import com.aiyou.utils.thread.ThreadUtils;

import external.PullToRefresh.PullToRefreshListView;
import external.SmartImageView.SmartImageView;

public class EcardActivity extends BaseActivity implements
        OnPageChangeListener, OnClickListener {
    private static final String URL_BASIC = "http://ecard.bupt.edu.cn/";
    private static final String URL_BASIC_USER = URL_BASIC + "User/";
    private static final String URL_LOGIN = URL_BASIC + "Login.aspx";
    private static final String URL_BASIC_INFO = URL_BASIC_USER
            + "baseinfo.aspx";
    private static final String URL_CONSUME_INFO = URL_BASIC_USER
            + "ConsumeInfo.aspx";

    private static final int MSG_ERROR = -1;
    private static final int MSG_BASIC_INFO = 1;
    private static final int MSG_CONSUME_INFO = 2;

    private static final String KEY_DATA = "data";

    private Map<String, String> mCookies = null;
    private Map<String, String> mConsumeParams = new HashMap<String, String>();
    private BasicInfo mBasicInfo = null;

    private EditText mIdET, mPasswordET;
    private ProgressDialog mProgressDlg;

    private LinearLayout mContentLL;

    private ViewPager mViewPager;
    private TextView mTag1TV, mTag2TV;

    private SmartImageView mFaceSIV;
    private TextView mIdTV, mNameTV, mSexTV, mNationTV, mMoneyMainTV,
            mMoneyExtraTV, mMoneySpecTV, mRoleTV, mStatusTV, mDepartmentTV;

    private EditText mFromET, mToET;
    private TextView mQueryConsumeTV;
    private PullToRefreshListView mPTRLV;
    private List<ConsumeInfo> mList = new ArrayList<ConsumeInfo>();
    private MyListAdapter mAdapter;
    private int mYear1, mYear2, mMonth1, mMonth2, mDay1, mDay2;

    private int mPage = 1, mTotalPage = 1;

    // 处理UI线程的handler
    public Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == MSG_BASIC_INFO) {
                // 显示基本信息
                showBasicInfo(mBasicInfo);
            } else if (msg.what == MSG_CONSUME_INFO) {
                // 显示消费信息
                Bundle data = msg.getData();
                String html = data.getString(KEY_DATA);
                data.clear();
                Document doc = Jsoup.parse(html);
                parseConsumeInfo(mList, doc);
                mAdapter.notifyDataSetChanged();
                if (mList.isEmpty()) {
                    Toast.makeText(getBaseContext(), "暂无消费记录",
                            Toast.LENGTH_SHORT).show();
                }
                if (mPage < mTotalPage) {
                    queryConsumeInfo(++mPage);
                } else {
                    Toast.makeText(getBaseContext(),
                            "共" + mList.size() + "条记录", Toast.LENGTH_SHORT)
                            .show();
                }
            } else if (msg.what == MSG_ERROR) {
                Bundle data = msg.getData();
                String error = data.getString(KEY_DATA);
                if (TextUtils.isEmpty(error)) {
                    error = NetWorkManager.MSG_NONET;
                }
                Toast.makeText(getBaseContext(), error, Toast.LENGTH_SHORT)
                        .show();
            }
            mProgressDlg.dismiss();
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecard);

        init();
    }

    private void showBasicInfo(BasicInfo info) {
        mIdTV.setText("学    号：" + info.id);
        mNameTV.setText("姓    名：" + info.name);
        String cookie = "";
        if (mCookies != null && !mCookies.isEmpty()) {
            Set<String> keySet = mCookies.keySet();
            for (String key : keySet) {
                cookie += key + "=" + mCookies.get(key) + ";";
            }
        }
        Map<String, String> header = new HashMap<String, String>();
        header.put("Cookie", cookie);
        mFaceSIV.setImageUrl(info.face_url, header);
        mSexTV.setText("性    别：" + info.sex);
        mNationTV.setText("民    族：" + info.nation);
        mMoneyMainTV.setText(info.money_main);
        mMoneyExtraTV.setText(info.money_extra);
        mMoneySpecTV.setText(info.money_spec);
        mRoleTV.setText(info.role);
        mStatusTV.setText(info.status);
        mDepartmentTV.setText(info.department);
        mContentLL.setVisibility(View.VISIBLE);
    }

    @SuppressLint("InflateParams")
    private void init() {
        mProgressDlg = new ProgressDialog(this);
        mProgressDlg.setIndeterminate(true);
        mProgressDlg.setMessage("正在查询");

        mIdET = (EditText) findViewById(R.id.et_id);
        mPasswordET = (EditText) findViewById(R.id.et_password);
        mPasswordET.setOnEditorActionListener(new OnEditorActionListener() {
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

        mContentLL = (LinearLayout) findViewById(R.id.ll_content);

        mTag1TV = (TextView) findViewById(R.id.tv_tag1);
        mTag2TV = (TextView) findViewById(R.id.tv_tag2);
        mViewPager = (ViewPager) findViewById(R.id.vp);

        List<View> viewList = new ArrayList<View>();
        LayoutInflater inflater = getLayoutInflater();
        View view1 = inflater.inflate(R.layout.ecard_vp_page1, null);
        View view2 = inflater.inflate(R.layout.ecard_vp_page2, null);
        mFaceSIV = (SmartImageView) view1.findViewById(R.id.siv);
        mIdTV = (TextView) view1.findViewById(R.id.tv_id);
        mNameTV = (TextView) view1.findViewById(R.id.tv_name);
        mSexTV = (TextView) view1.findViewById(R.id.tv_sex);
        mNationTV = (TextView) view1.findViewById(R.id.tv_nation);
        mMoneyMainTV = (TextView) view1.findViewById(R.id.tv_money_main);
        mMoneyExtraTV = (TextView) view1.findViewById(R.id.tv_money_extra);
        mMoneySpecTV = (TextView) view1.findViewById(R.id.tv_money_spec);
        mRoleTV = (TextView) view1.findViewById(R.id.tv_role);
        mStatusTV = (TextView) view1.findViewById(R.id.tv_status);
        mDepartmentTV = (TextView) view1.findViewById(R.id.tv_department);

        mFromET = (EditText) view2.findViewById(R.id.et_from);
        mToET = (EditText) view2.findViewById(R.id.et_to);
        mQueryConsumeTV = (TextView) view2.findViewById(R.id.tv_seek);
        mQueryConsumeTV.setOnClickListener(this);
        mPTRLV = (PullToRefreshListView) view2.findViewById(R.id.ptrlv);
        mPTRLV.setShowIndicator(false);
        mAdapter = new MyListAdapter(getBaseContext(), mList);
        mPTRLV.setAdapter(mAdapter);

        Calendar c = Calendar.getInstance();
        mYear1 = mYear2 = c.get(Calendar.YEAR);
        mMonth1 = mMonth2 = c.get(Calendar.MONTH);
        mDay2 = c.get(Calendar.DAY_OF_MONTH);
        mDay1 = 1;
        if (mDay1 == mDay2) {
            mMonth1 = mMonth2 - 1;
            if (mMonth1 < 0) {
                mMonth1 = 11;
                mYear1--;
            }
        }
        mFromET.setText(dateFormat(mYear1, mMonth1, mDay1));
        mToET.setText(dateFormat(mYear2, mMonth2, mDay2));

        viewList.add(view1);
        viewList.add(view2);
        mViewPager
                .setAdapter(new EcardPagerAdapter(getBaseContext(), viewList));
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setCurrentItem(0);
    }

    @Override
    public void onClick(View v) {
        if (v == mQueryConsumeTV) {
            // 查询消费记录
            mPage = mTotalPage = 1;
            queryConsumeInfo(mPage);
        }
    }

    public void onTagClick(View v) {
        final int nId = v.getId();
        if (nId == R.id.tv_tag1) {
            mViewPager.setCurrentItem(0);
        } else if (nId == R.id.tv_tag2) {
            mViewPager.setCurrentItem(1);
        }
    }

    public void onDatePick(View v) {
        int nId = v.getId();
        if (nId == R.id.et_from) {
            new DatePickerDialog(EcardActivity.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year,
                                int monthOfYear, int dayOfMonth) {
                            if (year * 10000L + monthOfYear * 100 + dayOfMonth >= mYear2
                                    * 10000 + mMonth2 * 100 + mDay2) {
                                Toast.makeText(EcardActivity.this,
                                        "开始日期要小于结束日期", Toast.LENGTH_SHORT)
                                        .show();
                                return;
                            }
                            mYear1 = year;
                            mMonth1 = monthOfYear;
                            mDay1 = dayOfMonth;
                            mFromET.setText(dateFormat(mYear1, mMonth1, mDay1));
                            clearList();
                        }
                    }, mYear1, mMonth1, mDay1).show();
            ;
        } else if (nId == R.id.et_to) {
            new DatePickerDialog(EcardActivity.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year,
                                int monthOfYear, int dayOfMonth) {
                            if (mYear1 * 10000L + mMonth1 * 100 + mDay1 >= year
                                    * 10000 + monthOfYear * 100 + dayOfMonth) {
                                Toast.makeText(EcardActivity.this,
                                        "开始日期要小于结束日期", Toast.LENGTH_SHORT)
                                        .show();
                                return;
                            }
                            mYear2 = year;
                            mMonth2 = monthOfYear;
                            mDay2 = dayOfMonth;
                            mToET.setText(dateFormat(mYear2, mMonth2, mDay2));
                            clearList();
                        }
                    }, mYear2, mMonth2, mDay2).show();
            ;
        }
    }

    public void onQuery(View v) {
        String id = mIdET.getText().toString().trim();
        String password = mPasswordET.getText().toString().trim();
        if (TextUtils.isEmpty(id) || TextUtils.isEmpty(password)) {
            Toast.makeText(getBaseContext(), "卡号和密码不能为空", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        clear();
        query(id, password);
        AiYouManager.viewInputMethod(EcardActivity.this, false,
                getCurrentFocus());
    }

    private void clear() {
        mFaceSIV.setImageResource(R.drawable.iu_default_green);
        mIdTV.setText("");
        mNameTV.setText("");
        mSexTV.setText("");
        mNationTV.setText("");
        mMoneyMainTV.setText("");
        mMoneyExtraTV.setText("");
        mMoneySpecTV.setText("");
        mRoleTV.setText("");
        mStatusTV.setText("");
        mDepartmentTV.setText("");
        clearList();
    }

    private void clearList() {
        mPage = mTotalPage = 1;
        mList.clear();
        mAdapter.notifyDataSetChanged();
    }

    private void query(final String id, final String password) {
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
                String error = null;
                // 登录
                error = login(id, password);
                if (error != null) {
                    if (mHandler != null) {
                        Message msg = mHandler.obtainMessage(MSG_ERROR);
                        Bundle data = msg.getData();
                        data.putString(KEY_DATA, error);
                        mHandler.sendMessage(msg);
                    }
                    return;
                }
                // 查询基本信息
                error = queryBasicInfo();
                if (error != null) {
                    if (mHandler != null) {
                        Message msg = mHandler.obtainMessage(MSG_ERROR);
                        Bundle data = msg.getData();
                        data.putString(KEY_DATA, error);
                        mHandler.sendMessage(msg);
                    }
                    return;
                }
                // 为查询消费情况准备参数
                error = prepareConsumeInfo();
                if (error != null) {
                    if (mHandler != null) {
                        Message msg = mHandler.obtainMessage(MSG_ERROR);
                        Bundle data = msg.getData();
                        data.putString(KEY_DATA, error);
                        mHandler.sendMessage(msg);
                    }
                    return;
                }

                if (mHandler != null) {
                    mHandler.sendEmptyMessage(MSG_BASIC_INFO);
                }
            }
        });
    }

    private String login(final String id, final String password) {
        Document doc = null;
        /**
         * 访问登录页面，获取需要的键值对
         */
        try {
            doc = Jsoup.connect(URL_LOGIN).timeout(10000).get();
        } catch (IOException e) {
            doc = null;
        }
        if (doc == null) {
            return "查询失败";
        }
        String param1 = "";
        String param2 = "btnLogin";
        String param3 = "";
        String param4 = doc.select("input#__VIEWSTATE").get(0).attr("value");
        String param5 = doc.select("input#__VIEWSTATEGENERATOR").get(0)
                .attr("value");
        String param6 = doc.select("input#__EVENTVALIDATION").get(0)
                .attr("value");

        Map<String, String> params = new HashMap<String, String>();
        params.put("__LASTFOCUS", param1);
        params.put("__EVENTTARGET", param2);
        params.put("__EVENTARGUMENT", param3);
        params.put("__VIEWSTATE", param4);
        params.put("__VIEWSTATEGENERATOR", param5);
        params.put("__EVENTVALIDATION", param6);
        params.put("txtUserName", id);
        params.put("txtPassword", password);
        params.put("hfIsManager", "0");
        /**
         * 登录
         */
        try {
            Connection conn = Jsoup.connect(URL_LOGIN).data(params)
                    .timeout(10000);
            doc = conn.post();
            mCookies = conn.response().cookies();
        } catch (IOException e) {
            doc = null;
        }
        if (doc == null) {
            return "查询失败";
        }
        // 账号、密码是否有误
        if (doc.select("span#lblError").size() > 0) {
            return doc.select("span#lblError").get(0).text();
        }
        return null;
    }

    private String queryBasicInfo() {
        Document doc = null;
        try {
            doc = Jsoup.connect(URL_BASIC_INFO).timeout(10000)
                    .cookies(mCookies).get();
        } catch (IOException e) {
            doc = null;
        }
        if (doc == null) {
            return "查询基本信息失败";
        }
        mBasicInfo = new BasicInfo();
        mBasicInfo.face_url = URL_BASIC_USER
                + doc.select("img").get(0).attr("src");
        mBasicInfo.id = doc.select("span#ContentPlaceHolder1_txtOutID").get(0)
                .text();
        mBasicInfo.name = doc.select("span#ContentPlaceHolder1_txtUserName")
                .get(0).text();
        mBasicInfo.sex = doc.select("span#ContentPlaceHolder1_txtSex").get(0)
                .text();
        mBasicInfo.nation = doc.select("span#ContentPlaceHolder1_txtNation")
                .get(0).text();
        mBasicInfo.money_main = doc
                .select("span#ContentPlaceHolder1_txtOddFare").get(0).text();
        mBasicInfo.money_extra = doc
                .select("span#ContentPlaceHolder1_txtSubsidy").get(0).text();
        mBasicInfo.money_spec = doc
                .select("span#ContentPlaceHolder1_txtSpecialOddfare").get(0)
                .text();
        mBasicInfo.role = doc.select("span#ContentPlaceHolder1_txtCardSF")
                .get(0).text();
        mBasicInfo.status = doc.select("span#ContentPlaceHolder1_txtStatus")
                .get(0).text();
        mBasicInfo.department = doc
                .select("span#ContentPlaceHolder1_stxtCustDeptName").get(0)
                .text();
        return null;
    }

    private String prepareConsumeInfo() {
        Document doc = null;
        try {
            doc = Jsoup.connect(URL_CONSUME_INFO).timeout(10000)
                    .cookies(mCookies).get();
        } catch (IOException e) {
            doc = null;
        }
        if (doc == null) {
            return "查询基本信息失败";
        }
        getConsumeParams(doc);
        return null;
    }

    private void queryConsumeInfo(final int page) {
        String from = dateFormat(mYear1, mMonth1, mDay1);
        String to = dateFormat(mYear2, mMonth2, mDay2);

        // 为查询消费情况准备参数
        final Map<String, String> data = mConsumeParams;
        data.put("__EVENTARGUMENT", page + "");
        // 0查询主钱包，1查询补助钱包
        data.put("ctl00$ContentPlaceHolder1$rbtnType", "0");
        data.put("ctl00$ContentPlaceHolder1$txtStartDate", from);
        data.put("ctl00$ContentPlaceHolder1$txtEndDate", to);
        if (page == 1) {
            data.put("ctl00$ContentPlaceHolder1$btnSearch", "查  询");
        }

        mProgressDlg.show();
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                Document doc = null;
                try {
                    doc = Jsoup.connect(URL_CONSUME_INFO).data(data)
                            .timeout(10000).cookies(mCookies).post();
                } catch (IOException e) {
                    doc = null;
                }
                if (doc == null) {
                    if (mHandler != null) {
                        Message msg = mHandler.obtainMessage(MSG_ERROR);
                        Bundle data = msg.getData();
                        data.putString(KEY_DATA, "查询消费信息失败");
                        mHandler.sendMessage(msg);
                    }
                    return;
                }
                getConsumeParams(doc);
                // 获取总页数
                if (mTotalPage == 1) {
                    try {
                        String page = doc
                                .select("div#ContentPlaceHolder1_AspNetPager1")
                                .select("a").last().attr("href");
                        String[] arr = page.split("'");
                        if (arr.length >= 2) {
                            page = arr[arr.length - 2];
                            mTotalPage = Integer.parseInt(page);
                        }
                    } catch (Exception e) {
                        // 只有一页
                    }
                    if (mTotalPage < 1) {
                        mTotalPage = 1;
                    }
                }
                if (mHandler != null) {
                    Message msg = mHandler.obtainMessage(MSG_CONSUME_INFO);
                    Bundle data = msg.getData();
                    data.putString(KEY_DATA, doc.toString());
                    mHandler.sendMessage(msg);
                }
            }
        });
    }

    private void parseConsumeInfo(List<ConsumeInfo> list, Document doc) {
        Elements tr = doc.select("table#ContentPlaceHolder1_gridView").select(
                "tr");
        tr.remove(0);
        ConsumeInfo info = null;
        Elements td = null;
        String time = null;
        for (Element ele : tr) {
            td = ele.select("td");
            if (td.size() < 7) {
                continue;
            }
            time = td.get(0).text().trim();
            if (TextUtils.isEmpty(time)) {
                continue;
            }
            info = new ConsumeInfo();
            info.time = time;
            info.description = td.get(1).text().trim();
            info.money_deal = td.get(2).text().trim();
            info.money_remain = td.get(3).text().trim();
            info.station = td.get(5).text().trim();
            info.name = td.get(6).text().trim();
            list.add(info);
        }
    }

    private Map<String, String> getConsumeParams(final Document doc) {
        String param1 = "ctl00$ContentPlaceHolder1$AspNetPager1";
        String param2 = doc.select("input#__VIEWSTATE").get(0).attr("value");
        String param3 = doc.select("input#__VIEWSTATEGENERATOR").get(0)
                .attr("value");
        String param4 = doc.select("input#__EVENTVALIDATION").get(0)
                .attr("value");

        mConsumeParams.clear();
        mConsumeParams.put("__EVENTTARGET", param1);
        mConsumeParams.put("__VIEWSTATE", param2);
        mConsumeParams.put("__VIEWSTATEGENERATOR", param3);
        mConsumeParams.put("__EVENTVALIDATION", param4);
        return mConsumeParams;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset,
            int positionOffsetPixels) {
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

    private String dateFormat(int year, int month, int day) {
        String date = year + "-";
        month += 1;
        if (month < 10) {
            date += "0" + month + "-";
        } else {
            date += month + "-";
        }
        if (day < 10) {
            date += "0" + day;
        } else {
            date += day;
        }
        return date;
    }
}

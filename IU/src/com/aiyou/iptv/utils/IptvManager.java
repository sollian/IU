package com.aiyou.iptv.utils;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.aiyou.AiYouApplication;
import com.aiyou.iptv.bean.Chanel;
import com.aiyou.utils.JsonHelper;
import com.aiyou.utils.SwitchManager;
import com.umeng.analytics.MobclickAgent;

public class IptvManager {
    private static final String SPNAME = "iptv";

    public static List<Chanel> mChanelList = new ArrayList<>();

    private SharedPreferences mSharedPref;

    private static IptvManager mInstance;

    private IptvManager(Context context) {
        mSharedPref = context.getSharedPreferences(SPNAME, Context.MODE_PRIVATE);
    }

    public static IptvManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (SwitchManager.class) {
                if (mInstance == null) {
                    mInstance = new IptvManager(context);
                }
            }
        }
        return mInstance;
    }

    public void saveChanelFrequency(Chanel chanel) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putLong(chanel.name, chanel.frequency);
        editor.commit();
    }

    public long getChanelFrequency(Chanel chanel) {
        return mSharedPref.getLong(chanel.name, 0);
    }

    public static List<Chanel> getChanelList() {
        if (mChanelList == null) {
            mChanelList = new ArrayList<>();
        }
        if (!mChanelList.isEmpty()) {
            return mChanelList;
        }
        mChanelList.clear();
        String[] str = new String[7];
        for (int i = 0; i < str.length; i++) {
            str[i] = MobclickAgent.getConfigParams(AiYouApplication.getInstance(), "chanel" + (i + 1));
        }
        String strJson = "";
        for (String aStr : str) {
            if (TextUtils.isEmpty(aStr)) {
                return mChanelList;
            }
            strJson += aStr;
        }
        IptvManager mgr = IptvManager.getInstance(AiYouApplication.getInstance());
        try {
            JSONObject jsonObject = new JSONObject(strJson);
            JSONArray array = JsonHelper.getJSONArray(jsonObject, "chanels");
            if (null != array) {
                int length = array.length();
                Chanel chanel;
                for (int i = 0; i < length; i++) {
                    chanel = new Chanel(array.opt(i).toString());
                    chanel.frequency = mgr.getChanelFrequency(chanel);
                    mChanelList.add(chanel);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mChanelList;
    }
}

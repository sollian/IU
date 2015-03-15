
package com.aiyou.map.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.aiyou.utils.JsonHelper;
import com.aiyou.utils.logcat.Logcat;
import com.umeng.analytics.MobclickAgent;

public class MapHelper {
    private static final String TAG = MapHelper.class.getSimpleName();

    private static MapData[] mMapDatas;

    public static void initMapDatas(Context context) {
        String data1 = MobclickAgent.getConfigParams(context, "map_data1");
        String data2 = MobclickAgent.getConfigParams(context, "map_data2");
        String data3 = MobclickAgent.getConfigParams(context, "map_data3");
        if (!TextUtils.isEmpty(data1) && !TextUtils.isEmpty(data2) && !TextUtils.isEmpty(data3)) {
            parseJson(data1 + data2 + data3);
        }
    }

    public static MapData[] getMapDatas() {
        return mMapDatas;
    }

    private static void parseJson(String strJson) {
        if (TextUtils.isEmpty(strJson)) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(strJson);
            JSONArray jsonArray = JsonHelper.getJSONArray(jsonObject, "data");
            if (null != jsonArray) {
                int length = jsonArray.length();
                mMapDatas = new MapData[length];
                JSONObject object = null;
                for (int i = 0; i < length; i++) {
                    mMapDatas[i] = new MapData();
                    object = jsonArray.optJSONObject(i);
                    mMapDatas[i].setLng(JsonHelper.getDouble(object, "lng"));
                    mMapDatas[i].setLat(JsonHelper.getDouble(object, "lat"));
                    mMapDatas[i].setType(JsonHelper.getInt(object, "type"));
                    mMapDatas[i].setName(JsonHelper.getString(object, "name"));
                }
            }
        } catch (JSONException e) {
            mMapDatas = null;
            Logcat.e(TAG, "getMapDatas JSONException");
        }
    }
}

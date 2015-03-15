
package com.aiyou.bbs.bean;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.utils.JsonHelper;
import com.aiyou.utils.http.HttpManager;

/**
 * widget元数据
 * 
 * @author sollian
 */
public class Widget {
    /**
     * 十大
     */
    private static final String API_WIDGET_TOPTEN = BBSManager.API_HEAD
            + "/widget/topten" + BBSManager.FORMAT;

    public String name;
    public String title;
    /**
     * 附加
     */
    public Article[] articles;

    public Widget(String strJson) {
        if (strJson == null) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(strJson);
            name = JsonHelper.getString(jsonObject, "name");
            title = JsonHelper.getString(jsonObject, "title");
            JSONArray jsonArray = JsonHelper.getJSONArray(jsonObject, "article");
            if (null != jsonArray) {
                int length = jsonArray.length();
                articles = new Article[length];

                JSONObject articleObj = null;
                for (int i = 0; i < length; i++) {
                    articleObj = (JSONObject) jsonArray.opt(i);
                    articles[i] = new Article(articleObj.toString());
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * 获取十大热门话题的信息
     * 
     * @return widget元数据
     */
    public static String getTopten(Context context) {
        return HttpManager.getInstance(context).getHttp(context, API_WIDGET_TOPTEN + "?appkey="
                + BBSManager.APPKEY);
    }
}

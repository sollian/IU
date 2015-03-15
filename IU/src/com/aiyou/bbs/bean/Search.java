
package com.aiyou.bbs.bean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.utils.JsonHelper;
import com.aiyou.utils.http.HttpManager;

/**
 * 查询元数据
 * 
 * @author sollian
 */
public class Search {
    private static final String API_SEARCH = BBSManager.API_HEAD
            + "/search/threads" + BBSManager.FORMAT;

    public Pagination pagination;
    public Article[] articles;

    public Search(String strJson) {
        if (strJson == null) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(strJson);

            pagination = new Pagination(JsonHelper.getJSONObject(jsonObject,
                    "pagination").toString());

            JSONArray articleArray = JsonHelper.getJSONArray(jsonObject, "threads");
            if (null != articleArray) {
                int length = articleArray.length();
                articles = new Article[length];
                for (int i = 0; i < length; i++) {
                    articles[i] = new Article(articleArray.opt(i).toString());
                }
            }
        } catch (JSONException e) {
        }
    }

    /**
     * 搜索单个版面的主题
     * 
     * @param board 单个合法版面
     * @param str 文章标题/作者包含此关键词
     * @param isTitle true：标题；false：作者
     * @return
     */
    public static String getSearch(Context context, String board, String str, boolean isTitle) {
        return getSearch(context, board, str, isTitle, 1);
    }

    /**
     * 搜索单个版面的主题
     * 
     * @param board 单个合法版面
     * @param str 文章标题/作者包含此关键词
     * @param isTitle true：标题；false：作者
     * @param page 文章的页数
     * @return
     */
    public static String getSearch(Context context, String board, String str, boolean isTitle,
            int page) {
        String url = null;
        if (isTitle) {
            url = API_SEARCH + "?appkey="
                    + BBSManager.APPKEY + "&board=" + board + "&title1=" + str
                    + "&page=" + page + "&day=365";
        } else {
            url = API_SEARCH + "?appkey="
                    + BBSManager.APPKEY + "&board=" + board + "&author=" + str
                    + "&page=" + page + "&day=365";
        }
        return HttpManager.getInstance(context).getHttp(context, url);
    }
}

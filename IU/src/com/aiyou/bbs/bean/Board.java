
package com.aiyou.bbs.bean;

import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.utils.JsonHelper;
import com.aiyou.utils.http.HttpManager;

/**
 * 版面元数据
 * 
 * @author sollian
 */
public class Board implements Serializable {
    private static final long serialVersionUID = 11119L;

    // 分区信息
    public static final String API_SECTION = BBSManager.API_HEAD + "/section/";
    private static final String API_BOARD = BBSManager.API_HEAD + "/board/";
    
    public int favorite_level = -1;
    // 版面名称
    public String name;
    // 版面描述——中文名
    public String description;
    // 版面所属根分区号
    public String section;
    // 版面是否不可回复
    public boolean is_no_reply = false;
    // 版面书否允许附件
    public boolean allow_attachment = false;
    // 当前用户是否用发文、回复权限
    public boolean allow_post = false;
    // 版面是否允许匿名发文
    public boolean allow_anonymous = false;
    /**
     * 附加
     */
    public Pagination pagination;
    public Article[] articles;

    public Board() {

    }

    public Board(String strJson) {
        if (strJson == null) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(strJson);
            name = JsonHelper.getString(jsonObject, "name");
            description = JsonHelper.getString(jsonObject, "description");
            section = JsonHelper.getString(jsonObject, "section");
            is_no_reply = JsonHelper.getBoolean(jsonObject, "is_no_reply");
            allow_attachment = JsonHelper.getBoolean(jsonObject, "allow_attachment");
            allow_post = JsonHelper.getBoolean(jsonObject, "allow_post");
            allow_anonymous = JsonHelper.getBoolean(jsonObject, "allow_anonymous");

            JSONObject pagObj = JsonHelper.getJSONObject(jsonObject, "pagination");
            if (pagObj != null) {
                pagination = new Pagination(pagObj.toString());
            }

            JSONArray jsonArray = JsonHelper.getJSONArray(jsonObject, "article");
            if (null != jsonArray) {
                int length = jsonArray.length();
                articles = new Article[length];
                for (int i = 0; i < length; i++) {
                    articles[i] = new Article(jsonArray.opt(i).toString());
                }
            }
        } catch (JSONException e) {
        }
    }

    /**
     * 获取指定版面的信息
     * 
     * @param name 合法的版面名称
     * @return 版面元数据
     */
    public static String getBoard(Context context, String name) {
        return getBoard(context, name, 1);
    }

    /**
     * 获取指定版面的信息
     * 
     * @param name 合法的版面名称
     * @param page 文章的页数
     * @return 版面元数据
     */
    public static String getBoard(Context context, String name, int page) {
        return HttpManager.getInstance(context).getHttp(context,
                API_BOARD + name + BBSManager.FORMAT + "?page=" + page
                        + "&appkey=" + BBSManager.APPKEY);
    }
}

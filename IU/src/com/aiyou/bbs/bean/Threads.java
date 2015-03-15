
package com.aiyou.bbs.bean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.utils.JsonHelper;
import com.aiyou.utils.http.HttpManager;

/**
 * 主题帖元数据
 * 
 * @author sollian
 */
public class Threads {
    private static final String API_THREADS = BBSManager.API_HEAD + "/threads/";

    public int id = -1;
    public int group_id = -1;
    public int reply_id = -1;
    public String flag;
    public boolean is_top = false;
    public boolean is_subject = false;
    public boolean has_attachment = false;
    public boolean is_admin = false;
    public String title;
    public long post_time = -1;
    public String board_name;
    public int reply_count = -1;

    public User user;
    public Article[] articles;
    public Pagination pagination;

    public Threads() {
    }

    public Threads(String strJson) {
        if (strJson == null) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(strJson);
            id = JsonHelper.getInt(jsonObject, "id");
            group_id = JsonHelper.getInt(jsonObject, "group_id");
            reply_id = JsonHelper.getInt(jsonObject, "reply_id");
            flag = JsonHelper.getString(jsonObject, "flag");
            is_top = JsonHelper.getBoolean(jsonObject, "is_top");
            is_subject = JsonHelper.getBoolean(jsonObject, "is_subject");
            has_attachment = JsonHelper.getBoolean(jsonObject, "has_attachment");
            is_admin = JsonHelper.getBoolean(jsonObject, "is_admin");
            title = JsonHelper.getString(jsonObject, "title");
            post_time = JsonHelper.getLong(jsonObject, "post_time");
            board_name = JsonHelper.getString(jsonObject, "board_name");
            reply_count = JsonHelper.getInt(jsonObject, "reply_count");

            JSONObject userObj = JsonHelper.getJSONObject(jsonObject, "user");
            if (null != userObj) {
                user = new User(userObj.toString());
                if (null == user.id) {
                    user.id = JsonHelper.getString(jsonObject, "user");
                }
            } else {
                user = new User();
                user.id = JsonHelper.getString(jsonObject, "user");
            }
            if ("null".equals(user.id)) {
                user.id = "原帖已删除";
            }

            JSONObject paginationObj = JsonHelper.getJSONObject(jsonObject,
                    "pagination");
            if (null != paginationObj) {
                pagination = new Pagination(paginationObj.toString());
            }

            JSONArray articleArray = JsonHelper.getJSONArray(jsonObject, "article");
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
     * 获取指定主题的信息
     * 
     * @param board 合法的版面名称
     * @param id 文章或主题id
     * @return
     */
    public static String getThreads(Context context, String board, int id) {
        return getThreads(context, board, id, 1);
    }

    /**
     * 获取指定主题的信息
     * 
     * @param board 合法的版面名称
     * @param id 文章或主题id
     * @param page 主题文章的页数
     * @return
     */
    public static String getThreads(Context context, String board, int id, int page) {
        return HttpManager.getInstance(context).getHttp(context, API_THREADS + board + "/" + id
                + BBSManager.FORMAT + "?page=" + page + "&appkey=" + BBSManager.APPKEY);
    }

    /**
     * 获取指定主题的信息
     * 
     * @param board 合法的版面名称
     * @param id 文章或主题id
     * @param au 只显示该主题中某一用户的文章，au为该用户的用户名，大小写敏感
     * @return
     */
    public static String getThreads(Context context, String board, int id, String au) {
        return getThreads(context, board, id, au, 1);
    }

    /**
     * 获取指定主题的信息
     * 
     * @param board 合法的版面名称
     * @param id 文章或主题id
     * @param au 只显示该主题中某一用户的文章，au为该用户的用户名，大小写敏感
     * @param page 主题文章的页数
     * @return
     */
    public static String getThreads(Context context, String board, int id, String au, int page) {
        return HttpManager.getInstance(context).getHttp(context,
                API_THREADS + board + "/" + id + BBSManager.FORMAT + "?au="
                        + au + "&page=" + page + "&appkey=" + BBSManager.APPKEY);
    }
}

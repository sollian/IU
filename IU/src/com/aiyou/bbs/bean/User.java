
package com.aiyou.bbs.bean;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.utils.JsonHelper;
import com.aiyou.utils.http.HttpManager;

/**
 * User元数据
 * 
 * @author sollian
 */
public class User implements Serializable {
    private static final long serialVersionUID = 11111L;

    // 获取用户信息
    private static final String API_USER_QUERY = BBSManager.API_HEAD
            + "/user/query/";
    private static final String API_USER_LOGIN = BBSManager.API_HEAD
            + "/user/login";
    /**
     * 基本信息
     */
    // 用户id
    public String id;
    // 用户昵称
    public String user_name;
    // 用户性别：m表示男性，f表示女性，n表示隐藏性别
    public String gender;
    // 用户星座 若隐藏星座则为空
    public String astro;
    // 用户头像地址
    public String face_url;
    // 用户qq
    public String qq;
    // 用户msn
    public String msn;
    // 用户个人主页
    public String home_page;
    /**
     * 论坛属性
     */
    // 论坛等级
    public String level;
    // 用户生命值
    public int life = -1;
    // 用户发文数量
    public int post_count = -1;
    // 积分
    public int score = -1;
    // 用户是否在线
    public boolean is_online = false;
    // 用户注册时间，unixtimestamp,当前登陆用户为 自己或是当前用户具有管理权限
    public long first_login_time = -1;
    // 用户上次登录时间，unixtimestamp
    public long last_login_time = -1;
    // 用户上次登录
    public String last_login_ip;
    // 登录次数,当前登陆用户为 自己或是当前用户具有管理权限
    public long login_count = -1;
    // 用户身份
    public String role;

    public User() {
    }

    public User(String strJson) {
        if (strJson == null) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(strJson);
            id = JsonHelper.getString(jsonObject, "id");
            user_name = JsonHelper.getString(jsonObject, "user_name");
            face_url = JsonHelper.getString(jsonObject, "face_url");
            gender = JsonHelper.getString(jsonObject, "gender");
            astro = JsonHelper.getString(jsonObject, "astro");
            qq = JsonHelper.getString(jsonObject, "qq");
            msn = JsonHelper.getString(jsonObject, "msn");
            home_page = JsonHelper.getString(jsonObject, "home_page");
            level = JsonHelper.getString(jsonObject, "level");
            life = JsonHelper.getInt(jsonObject, "life");
            post_count = JsonHelper.getInt(jsonObject, "post_count");
            score = JsonHelper.getInt(jsonObject, "score");
            is_online = JsonHelper.getBoolean(jsonObject, "is_online");
            last_login_ip = JsonHelper.getString(jsonObject, "last_login_ip");
            role = JsonHelper.getString(jsonObject, "role");
            first_login_time = JsonHelper.getLong(jsonObject, "first_login_time");
            last_login_time = JsonHelper.getLong(jsonObject, "last_login_time");
            login_count = JsonHelper.getLong(jsonObject, "login_count");
        } catch (JSONException e) {
        }
    }

    public JSONObject getJson() {
        JSONObject jsonObject = new JSONObject();
        JsonHelper.put(jsonObject, "id", id);
        JsonHelper.put(jsonObject, "user_name", user_name);
        JsonHelper.put(jsonObject, "gender", gender);
        JsonHelper.put(jsonObject, "astro", astro);
        JsonHelper.put(jsonObject, "face_url", face_url);
        JsonHelper.put(jsonObject, "qq", qq);
        JsonHelper.put(jsonObject, "msn", msn);
        JsonHelper.put(jsonObject, "home_page", home_page);
        JsonHelper.put(jsonObject, "level", level);
        JsonHelper.put(jsonObject, "life", life);
        JsonHelper.put(jsonObject, "post_count", post_count);
        JsonHelper.put(jsonObject, "score", score);
        JsonHelper.put(jsonObject, "is_online", is_online);
        JsonHelper.put(jsonObject, "role", role);
        JsonHelper.put(jsonObject, "last_login_ip", last_login_ip);
        JsonHelper.put(jsonObject, "first_login_time", first_login_time);
        JsonHelper.put(jsonObject, "last_login_time", last_login_time);
        JsonHelper.put(jsonObject, "login_count", login_count);
        return jsonObject;
    }

    public static String query(Context context, String id) {
        return HttpManager.getInstance(context).getHttp(context,
                API_USER_QUERY + id + BBSManager.FORMAT + "?appkey="
                        + BBSManager.APPKEY);
    }

    public static String login(Context context) {
        return HttpManager.getInstance(context).getHttp(context,
                API_USER_LOGIN + BBSManager.FORMAT + "?appkey="
                        + BBSManager.APPKEY);
    }
}

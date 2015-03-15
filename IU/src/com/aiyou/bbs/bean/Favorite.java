package com.aiyou.bbs.bean;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.utils.JsonHelper;
import com.aiyou.utils.http.HttpManager;
import com.aiyou.utils.logcat.Logcat;

import android.content.Context;
import android.text.TextUtils;

public class Favorite {
    private static final String TAG = Favorite.class.getSimpleName();
    private static final String API_FAVORITE = BBSManager.API_HEAD + "/favorite/";

    public static Favorite mFavorite = null;
    
    public int level = -1;
    public boolean has_subfavorites;
    public List<Board> boards;
    
    public Favorite[] subfavorites;
    
    public Favorite(String strJson, int level) {
        this(strJson);
        this.level = level;
    }
    
    public Favorite(String strJson) {
        if(TextUtils.isEmpty(strJson)) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(strJson);
            level = JsonHelper.getInt(jsonObject, "level");
            if(level < 0) {
                level = 0;
            }
            JSONArray subArray = JsonHelper.getJSONArray(jsonObject, "sub_favorite");
            if(subArray != null && subArray.length() > 0) {
                has_subfavorites = true;
                int length = subArray.length();
                subfavorites = new Favorite[length];
                for(int i = 0; i < length; i++) {
                    subfavorites[i] = new Favorite(subArray.opt(i).toString());
                }
            } else {
                has_subfavorites = false;
            }
            JSONArray boardArray = JsonHelper.getJSONArray(jsonObject, "board");
            if(boardArray != null && boardArray.length() > 0) {
                int length = boardArray.length();
                boards = new ArrayList<Board>();
                for(int i = 0; i < length; i++) {
                    boards.add(new Board(boardArray.opt(i).toString()));
                }
            } else {
                boards = null;
            }
        } catch(JSONException e) {
        }
        
    }
    public static String getFavorite(Context context, int level) {
        return HttpManager.getInstance(context).getHttp(context, 
                API_FAVORITE + level + BBSManager.FORMAT + "?appkey="
                 + BBSManager.APPKEY);
    }
    public static String addFavorite(Context context, int level, String name) {
        return addFavorite(context, level, name, 0);
    }
    /**
     * 
     * @param context
     * @param level
     * @param name
     * @param dir 是否为自定义目录：0不是，1是
     * @return
     */
    private static String addFavorite(Context context, int level, String name, int dir) {
        ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();

        params.add(new BasicNameValuePair("name", name));
        params.add(new BasicNameValuePair("dir", dir + ""));

        // 封装请求参数的实体对象
        UrlEncodedFormEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(params, HttpManager.CHARSET);
        } catch (UnsupportedEncodingException e) {
            Logcat.e(TAG, "addFavorite UnsupportedEncodingException");
        }
        return HttpManager.getInstance(context).postHttp(context, 
                API_FAVORITE + "add/" + level + BBSManager.FORMAT
                + "?appkey=" + BBSManager.APPKEY, entity);
    }
    public static String deleteFavorite(Context context, int level, String name) {
        return deleteFavorite(context, level, name, 0);
    }

    private static String deleteFavorite(Context context, int level, String name, int dir) {
        ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();

        params.add(new BasicNameValuePair("name", name));
        params.add(new BasicNameValuePair("dir", dir + ""));

        // 封装请求参数的实体对象
        UrlEncodedFormEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(params, HttpManager.CHARSET);
        } catch (UnsupportedEncodingException e) {
            Logcat.e(TAG, "addFavorite UnsupportedEncodingException");
        }
        return HttpManager.getInstance(context).postHttp(context, 
                API_FAVORITE + "delete/" + level + BBSManager.FORMAT
                + "?appkey=" + BBSManager.APPKEY, entity);
    }
    
    public static List<Board> toArticleList(Favorite favorite, List<Board> list) {
        if(favorite == null || list == null) {
            return null;
        }
        if(favorite.boards != null && favorite.boards.size() > 0) {
            int length = favorite.boards.size();
            for(int i = 0; i < length; i++) {
                favorite.boards.get(i).favorite_level = favorite.level;
                list.add(favorite.boards.get(i));
            }
        }
        if(favorite.has_subfavorites) {
            for(int i = 0; i < favorite.subfavorites.length; i++) {
                toArticleList(favorite.subfavorites[i], list);
            }
        }
        return list;
    }

}

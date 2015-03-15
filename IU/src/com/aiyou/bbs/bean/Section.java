
package com.aiyou.bbs.bean;

import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.utils.JsonHelper;
import com.aiyou.utils.http.HttpManager;

/**
 * 分区元数据
 * 
 * @author sollian
 */
public class Section implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 11133L;

    private static Section ROOT_SECTION = null;

    private static final String API_SECTION = BBSManager.API_HEAD + "/section";

    // 分区数量
    public int section_count = -1;
    // 分区名称
    public String name;
    // 分区表述
    public String description;
    // 是否是根分区
    public boolean is_root = false;
    // 该分区所属根分区名称
    public String parent;
    /**
     * 附加
     */
    // 分区数组
    public Section[] sections;
    // 子目录name数组
    public String[] sub_sections;
    // 子分区数组
    public Board[] boards;

    public Section(String strJson) {
        if (strJson == null) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(strJson);
            section_count = JsonHelper.getInt(jsonObject, "section_count");
            name = JsonHelper.getString(jsonObject, "name");
            description = JsonHelper.getString(jsonObject, "description");
            is_root = JsonHelper.getBoolean(jsonObject, "is_root");
            parent = JsonHelper.getString(jsonObject, "parent");

            JSONArray jsonArray = JsonHelper.getJSONArray(jsonObject, "sub_section");
            if (null != jsonArray) {
                int length = jsonArray.length();
                sub_sections = new String[length];
                for (int i = 0; i < length; i++) {
                    sub_sections[i] = jsonArray.getString(i);
                }
            }

            jsonArray = JsonHelper.getJSONArray(jsonObject, "board");
            if (null != jsonArray) {
                int length = jsonArray.length();
                boards = new Board[length];
                for (int i = 0; i < length; i++) {
                    boards[i] = new Board(jsonArray.opt(i).toString());
                }
            }

            jsonArray = JsonHelper.getJSONArray(jsonObject, "section");
            if (null != jsonArray) {
                int length = jsonArray.length();
                sections = new Section[length];
                for (int i = 0; i < length; i++) {
                    sections[i] = new Section(jsonArray.opt(i).toString());
                }
            }
        } catch (JSONException e) {
        }
    }

    /**
     * 获取所有根分区信息
     * 
     * @param context
     * @return
     */
    public static Section getRootSection(Context context) {
        if (ROOT_SECTION == null) {
            String strJson = BBSManager.getInstance(context).getRootSec();
            if (!TextUtils.isEmpty(strJson)) {
                ROOT_SECTION = new Section(strJson);
            }
        }
        return ROOT_SECTION;
    }

    /**
     * 更新根分区信息
     * 
     * @param context
     */
    public static void updateRootSection(Context context) {
        String strJson = HttpManager.getInstance(context).getHttp(context,
                API_SECTION + BBSManager.FORMAT + "?appkey="
                        + BBSManager.APPKEY);
        if (!TextUtils.isEmpty(strJson)) {
            BBSManager.getInstance(context).saveRootSec(strJson);
            ROOT_SECTION = new Section(strJson);
        }
    }

    /**
     * 获取指定分区的信息
     * 
     * @param section 合法的分区名称
     * @return 分区元数据
     */
    public static String getSection(Context context, String section) {
        return HttpManager.getInstance(context).getHttp(context,
                API_SECTION + "/" + section + BBSManager.FORMAT + "?appkey="
                        + BBSManager.APPKEY);
    }
}

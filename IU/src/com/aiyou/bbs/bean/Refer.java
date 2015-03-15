
package com.aiyou.bbs.bean;

import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.aiyou.AiYouApplication;
import com.aiyou.R;
import com.aiyou.bbs.bean.helper.AdapterInterface;
import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.utils.JsonHelper;
import com.aiyou.utils.http.HttpManager;
import com.aiyou.utils.time.TimeUtils;

/**
 * 提醒元数据
 * 
 * @author sollian
 */
public class Refer implements Serializable, AdapterInterface {
    /**
	 * 
	 */
    private static final long serialVersionUID = 11113L;

    private static final String API_REFER = BBSManager.API_HEAD + "/refer/";

    public enum ReferType {
        AT("at"), // @我消息
        REPLY("reply"); // 回复我的消息

        private String mType;

        private ReferType(String type) {
            mType = type;
        }

        @Override
        public String toString() {
            return mType;
        }
    };

    // 提醒编号，此编号用于提醒的相关操作
    public int index = -1;
    // 提醒文章的id
    public int id = -1;
    // 提醒文章的group id
    public int group_id = -1;
    // 提醒文章的reply id
    public int reply_id = -1;
    // 提醒文章所在版面
    public String board_name;
    // 提醒文章的标题
    public String title;
    // 发出提醒的时间
    public long time = -1;
    // 提醒是否已读
    public boolean is_read = false;
    /**
     * 附加
     */
    // 提醒文章的发信人，此为user元数据，如果user不存在则为用户id
    public User user;

    public String description;
    public Pagination pagination;
    public Refer[] refers;

    // 当前类型的提醒是否启用
    public boolean enable = false;
    // 当前类型的新提醒个数
    public int new_count = -1;

    public Refer(String strJson) {
        if (strJson == null) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(strJson);
            index = JsonHelper.getInt(jsonObject, "index");
            id = JsonHelper.getInt(jsonObject, "id");
            group_id = JsonHelper.getInt(jsonObject, "group_id");
            reply_id = JsonHelper.getInt(jsonObject, "reply_id");
            board_name = JsonHelper.getString(jsonObject, "board_name");
            title = JsonHelper.getString(jsonObject, "title");
            time = JsonHelper.getLong(jsonObject, "time");
            is_read = JsonHelper.getBoolean(jsonObject, "is_read");

            enable = JsonHelper.getBoolean(jsonObject, "enable");
            new_count = JsonHelper.getInt(jsonObject, "new_count");

            description = JsonHelper.getString(jsonObject, "description");

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

            pagination = new Pagination(JsonHelper.getJSONObject(jsonObject,
                    "pagination").toString());
            JSONArray jsonArray = JsonHelper.getJSONArray(jsonObject, "article");
            if (null != jsonArray) {
                int length = jsonArray.length();
                refers = new Refer[length];
                for (int i = 0; i < length; i++) {
                    refers[i] = new Refer(jsonArray.opt(i).toString());
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * 获取指定提醒类型列表
     * 
     * @param type
     * @return
     */
    public static String getRefer(Context context, ReferType type) {
        return getRefer(context, type, 1);
    }

    /**
     * 获取指定提醒类型列表
     * 
     * @param type
     * @param page 提醒列表的页数
     * @return
     */
    public static String getRefer(Context context, ReferType type, int page) {
        return HttpManager.getInstance(context).getHttp(context,
                API_REFER + type.toString() + BBSManager.FORMAT + "?page=" + page
                        + "&appkey=" + BBSManager.APPKEY);

    }

    /**
     * 获取指定类型提醒的属性信息
     * 
     * @param type
     * @return
     */
    public static String getReferInfo(Context context, ReferType type) {
        return HttpManager.getInstance(context).getHttp(context,
                API_REFER + type.toString() + "/info" + BBSManager.FORMAT + "?appkey="
                        + BBSManager.APPKEY);
    }

    /**
     * 设置所有提醒为已读
     * 
     * @param type
     * @return
     */
    public static String setRead(Context context, ReferType type) {
        return HttpManager.getInstance(context).postHttp(context, API_REFER + type.toString()
                + "/setRead" + BBSManager.FORMAT + "?appkey=" + BBSManager.APPKEY, null);
    }

    /**
     * 设置指定提醒为已读
     * 
     * @param type
     * @param index 提醒的索引，为提醒元数据中的index值。如果此参数不存在则设置此类型的所有提醒已读
     * @return
     */
    public static String setRead(Context context, ReferType type, int index) {
        return HttpManager.getInstance(context).postHttp(context,
                API_REFER + type.toString() + "/setRead/"
                        + index + BBSManager.FORMAT + "?appkey=" + BBSManager.APPKEY, null);
    }

    /**
     * 删除指定提醒
     * 
     * @param type
     * @return
     * @throws Exception
     */
    public static String deleteRefer(Context context, ReferType type) {
        return HttpManager.getInstance(context).postHttp(context, API_REFER + type.toString()
                + "/delete" + BBSManager.FORMAT + "?appkey=" + BBSManager.APPKEY, null);
    }

    /**
     * 删除指定提醒
     * 
     * @param type
     * @param index 提醒的索引，为提醒元数据中的index值。如果此参数不存在则删除此类型的所有提醒
     * @return
     */
    public static String deleteRefer(Context context, ReferType type, int index) {
        return HttpManager.getInstance(context).postHttp(context, API_REFER + type + "/delete/"
                + index + BBSManager.FORMAT + "?appkey=" + BBSManager.APPKEY, null);

    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDate() {
        if (-1 != time) {
            return TimeUtils.formatTime(time);
        }
        return null;
    }

    @Override
    public int getTitleColor() {
        if (!is_read) {
            return AiYouApplication.getInstance().getResources().getColor(R.color.refer_notread);
        }
        return -1;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public User getUser() {
        return user;
    }
}

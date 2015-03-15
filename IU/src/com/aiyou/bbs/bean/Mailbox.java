
package com.aiyou.bbs.bean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.utils.JsonHelper;
import com.aiyou.utils.http.HttpManager;

/**
 * 用户信箱元数据
 * 
 * @author sollian
 */
public class Mailbox {
    public enum MailboxType {
        INBOX("inbox"), // 收件箱
        OUTBOX("outbox"), // 发件箱
        DELETED("deleted"); // 回收站

        private String mType;

        private MailboxType(String type) {
            mType = type;
        }

        @Override
        public String toString() {
            return mType;
        }
    };

    // 是否有新邮件
    public boolean new_mail = false;
    // 信箱是否已满
    public boolean full_mail = false;
    // 信箱已用空间
    public String space_used;
    // 当前用户是否能发信
    public boolean can_send = false;

    /**
     * 附加
     */
    // 信箱类型描述，包括：收件箱，发件箱，废纸篓
    public String description;
    // 当前信箱的分页信息
    public Pagination pagination;
    // 当前信箱所包含的信件元数据数组
    public Mail[] mails;

    public Mailbox(String strJson) {
        if (strJson == null) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(strJson);
            new_mail = JsonHelper.getBoolean(jsonObject, "new_mail");
            full_mail = JsonHelper.getBoolean(jsonObject, "full_mail");
            space_used = JsonHelper.getString(jsonObject, "space_used");
            can_send = JsonHelper.getBoolean(jsonObject, "can_send");
            description = JsonHelper.getString(jsonObject, "description");

            JSONObject pagObj = JsonHelper.getJSONObject(jsonObject, "pagination");
            if (pagObj != null) {
                pagination = new Pagination(pagObj.toString());
            }

            JSONArray jsonArray = JsonHelper.getJSONArray(jsonObject, "mail");
            if (null != jsonArray) {
                int length = jsonArray.length();
                mails = new Mail[length];
                for (int i = 0; i < length; i++) {
                    mails[i] = new Mail(jsonArray.opt(i).toString());
                }
            }
        } catch (JSONException e) {
        }
    }

    /**
     * 获取指定信箱信息
     * 
     * @param box
     * @return
     */
    public static String getMailBox(Context context, MailboxType box) {
        return getMailBox(context, box, 1);
    }

    /**
     * 获取指定信箱信息
     * 
     * @param box
     * @param page 信箱的页数
     * @return
     */
    public static String getMailBox(Context context, MailboxType box, int page) {
        return HttpManager.getInstance(context).getHttp(context,
                Mail.API_MAIL + box.toString() + BBSManager.FORMAT + "?appkey="
                        + BBSManager.APPKEY + "&page=" + page);
    }

    /**
     * 信箱属性信息，包括是否有新邮件
     * 
     * @return Mailbox元数据的json数据
     */
    public static String getMailBoxInfo(Context context) {
        return HttpManager.getInstance(context).getHttp(context,
                Mail.API_MAIL + "info" + BBSManager.FORMAT + "?appkey="
                        + BBSManager.APPKEY);
    }

}

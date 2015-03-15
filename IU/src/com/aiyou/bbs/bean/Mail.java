
package com.aiyou.bbs.bean;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.aiyou.AiYouApplication;
import com.aiyou.R;
import com.aiyou.bbs.bean.Mailbox.MailboxType;
import com.aiyou.bbs.bean.helper.AdapterInterface;
import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.utils.JsonHelper;
import com.aiyou.utils.http.HttpManager;
import com.aiyou.utils.logcat.Logcat;
import com.aiyou.utils.time.TimeUtils;

/**
 * 信件元数据
 * 
 * @author sollian
 */
public class Mail implements Serializable, AdapterInterface {
    public static final String TAG = Mail.class.getSimpleName();
    /**
	 * 
	 */
    private static final long serialVersionUID = 11114L;

    protected static final String API_MAIL = BBSManager.API_HEAD + "/mail/";

    // 信件编号，此编号为/mail/:box/:num中的num
    public int index = -1;
    // 是否已读
    public boolean is_read = false;
    // 是否回复
    public boolean is_reply = false;
    // 是否有附件
    public boolean has_attachment = false;
    // 信件标题
    public String title;
    // 发信人，此为user元数据，如果user不存在则为用户id
    public User user;
    // 发信时间
    public long post_time = -1;
    // 所属信箱名
    public String box_name;
    // 信件内容，只存在于/mail/:box/:num中
    public String content;
    // 信件的附件列表,只存在于/mail/:box/:num中
    public Attachment attachment;

    public Mail(String strJson) {
        if (strJson == null) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(strJson);
            index = JsonHelper.getInt(jsonObject, "index");
            is_read = JsonHelper.getBoolean(jsonObject, "is_read");
            is_reply = JsonHelper.getBoolean(jsonObject, "is_reply");
            has_attachment = JsonHelper.getBoolean(jsonObject, "has_attachment");
            title = JsonHelper.getString(jsonObject, "title");
            post_time = JsonHelper.getLong(jsonObject, "post_time");
            box_name = JsonHelper.getString(jsonObject, "box_name");
            content = JsonHelper.getString(jsonObject, "content");

            JSONObject attachmentObj = JsonHelper.getJSONObject(jsonObject,
                    "attachment");
            if (null != attachmentObj) {
                attachment = new Attachment(attachmentObj.toString());
            }

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
        } catch (JSONException e) {
        }
    }

    /**
     * 获取指定信件信息
     * 
     * @param box
     * @param index 信件在信箱的索引,为信箱信息的信件列表中每个信件对象的index值
     * @return 信件元数据的json数据
     */
    public static String getMail(Context context, MailboxType box, int index) {
        return HttpManager.getInstance(context).getHttp(context,
                API_MAIL + box.toString() + "/" + index
                        + BBSManager.FORMAT + "?appkey=" + BBSManager.APPKEY);
    }

    /**
     * 发送新信件
     * 
     * @param subject 主题
     * @param content 内容
     * @param strTo 收信人id
     * @return
     */
    public static String sendMail(Context context, String subject, String content, String strTo) {
        ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();

        params.add(new BasicNameValuePair("title", subject));
        params.add(new BasicNameValuePair("content", content));
        params.add(new BasicNameValuePair("id", strTo));

        // 封装请求参数的实体对象
        UrlEncodedFormEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(params, HttpManager.CHARSET);
        } catch (UnsupportedEncodingException e) {
            Logcat.e(TAG, "sendMail UnsupportedEncodingException");
        }
        return HttpManager.getInstance(context).postHttp(context,
                API_MAIL + "send" + BBSManager.FORMAT + "?appkey=" + BBSManager.APPKEY, entity);
    }

    /**
     * 转寄邮件
     * 
     * @param index
     * @param userId
     * @return
     */
    public static String forwardMail(Context context, int index, String userId) {
        ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("target", userId));
        // 封装请求参数的实体对象
        UrlEncodedFormEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(params, HttpManager.CHARSET);
        } catch (UnsupportedEncodingException e) {
            Logcat.e(TAG, "forwardMail UnsupportedEncodingException");
        }

        return HttpManager.getInstance(context).postHttp(context,
                API_MAIL + "inbox/forward/" + index + BBSManager.FORMAT + "?appkey="
                        + BBSManager.APPKEY, entity);
    }

    /**
     * 回复指定信箱中的邮件
     * 
     * @param box
     * @param index 信件在信箱的索引,为信箱信息的信件列表中每个信件对象的index值
     * @param subject
     * @param content
     * @return
     */
    public static String replyMail(Context context, MailboxType box, int index, String subject,
            String content) {
        ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();

        params.add(new BasicNameValuePair("title", subject));
        params.add(new BasicNameValuePair("content", content));

        // 封装请求参数的实体对象
        UrlEncodedFormEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(params, HttpManager.CHARSET);
        } catch (UnsupportedEncodingException e) {
            Logcat.e(TAG, "replyMail UnsupportedEncodingException");
        }

        return HttpManager.getInstance(context).postHttp(context,
                API_MAIL + box.toString() + "/reply/" + index
                        + BBSManager.FORMAT + "?appkey=" + BBSManager.APPKEY, entity);
    }

    /**
     * 删除指定信件
     * 
     * @param box
     * @param index 信件在信箱的索引,为信箱信息的信件列表中每个信件对象的index值
     * @return
     */
    public static String deleteMail(Context context, MailboxType box, int index) {
        return HttpManager.getInstance(context).postHttp(context,
                API_MAIL + box.toString() + "/delete/"
                        + index + BBSManager.FORMAT + "?appkey=" + BBSManager.APPKEY, null);
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDate() {
        if (-1 != post_time) {
            return TimeUtils.formatTime(post_time);
        }
        return null;
    }

    @Override
    public int getTitleColor() {
        if (!is_read) {
            return AiYouApplication.getInstance().getResources().getColor(R.color.mail_notread);
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

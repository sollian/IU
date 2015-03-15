
package com.aiyou.bbs.bean;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Color;

import com.aiyou.AiYouApplication;
import com.aiyou.R;
import com.aiyou.bbs.bean.helper.AdapterInterface;
import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.utils.JsonHelper;
import com.aiyou.utils.http.HttpManager;
import com.aiyou.utils.logcat.Logcat;
import com.aiyou.utils.time.TimeUtils;

/**
 * 文章元数据
 * 
 * @author sollian
 */
public class Article implements Serializable, AdapterInterface {

    private static final String TAG = Article.class.getSimpleName();
    /**
	 * 
	 */
    private static final long serialVersionUID = 11112L;

    // 文章
    private static final String API_ARTICLE = BBSManager.API_HEAD + "/article/";

    // 文章id
    public int id = -1;
    // 该文章所属主题的id
    public int group_id = -1;
    // 该文章回复文章的id
    public int reply_id = -1;
    // 文章标记 分别是m g ; b u o 8
    public String flag = null;
    // 文章是否置顶
    public boolean is_top = false;
    // 该文章是否是主题帖
    public boolean is_subject = false;
    // 文章是否有附件
    public boolean has_attachment = false;
    // 当前登陆用户是否对文章有管理权限 包括编辑，删除，修改附件
    public boolean is_admin = false;
    // 文章标题
    public String title = null;
    // 文章发表时间，unixtimestamp
    public long post_time = -1;
    // 所属版面名称
    public String board_name = null;
    // 在/board/:name的文章列表和/search/(article|threads)中不存在此属性
    public String content = null;
    // 该文章的前一篇文章id,只存在于/article/:board/:id中
    public int previous_id = -1;
    // 该文章的后一篇文章id,只存在于/article/:board/:id中
    public int next_id = -1;
    // 该文章同主题前一篇文章id,只存在于/article/:board/:id中
    public int threads_previous_id = -1;
    // 该文章同主题后一篇文章id,只存在于/article/:board/:id中
    public int threads_next_id = -1;
    // 该主题回复文章数,只存在于/board/:name，/threads/:board/:id和/search/threads中
    public int reply_count = -1;
    /**
     * 附加
     */
    public User user = null;
    public Attachment attachment = null;

    public Article() {

    }

    public Article(String strJson) {
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
            content = JsonHelper.getString(jsonObject, "content");
            reply_count = JsonHelper.getInt(jsonObject, "reply_count");
            previous_id = JsonHelper.getInt(jsonObject, "previous_id");
            next_id = JsonHelper.getInt(jsonObject, "next_id");
            threads_previous_id = JsonHelper
                    .getInt(jsonObject, "threads_previous_id");
            threads_next_id = JsonHelper.getInt(jsonObject, "threads_next_id");

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

            JSONObject attachmentObj = JsonHelper.getJSONObject(jsonObject,
                    "attachment");
            if (null != attachmentObj) {
                attachment = new Attachment(attachmentObj.toString());
            }
        } catch (JSONException e) {
        }
    }

    public JSONObject getJson() {
        JSONObject jsonObject = new JSONObject();
        JsonHelper.put(jsonObject, "id", id);
        JsonHelper.put(jsonObject, "group_id", group_id);
        JsonHelper.put(jsonObject, "reply_id", reply_id);
        JsonHelper.put(jsonObject, "flag", flag);
        JsonHelper.put(jsonObject, "is_top", is_top);
        JsonHelper.put(jsonObject, "is_subject", is_subject);
        JsonHelper.put(jsonObject, "has_attachment", has_attachment);
        JsonHelper.put(jsonObject, "is_admin", is_admin);
        JsonHelper.put(jsonObject, "title", title);
        JsonHelper.put(jsonObject, "post_time", post_time);
        JsonHelper.put(jsonObject, "board_name", board_name);
        JsonHelper.put(jsonObject, "content", content);
        JsonHelper.put(jsonObject, "previous_id", previous_id);
        JsonHelper.put(jsonObject, "next_id", next_id);
        JsonHelper.put(jsonObject, "threads_previous_id", threads_previous_id);
        JsonHelper.put(jsonObject, "threads_next_id", threads_next_id);
        JsonHelper.put(jsonObject, "reply_count", reply_count);
        JsonHelper.put(jsonObject, "user", user.getJson());
        return jsonObject;
    }

    public static String getArticle(Context context, String board, int id) {
        return HttpManager.getInstance(context).getHttp(context,
                API_ARTICLE + board + "/" + id + BBSManager.FORMAT + "?appkey="
                        + BBSManager.APPKEY);
    }

    /**
     * 发布新文章/主题
     * 
     * @param board
     * @param title 新文章的标题
     * @param content 新文章的内容，可以为空
     * @param reid 新文章回复其他文章的id
     * @return
     */
    public static String sendArticle(Context context, String board, String title,
            String content, String reid) {
        ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();

        params.add(new BasicNameValuePair("title", title));
        params.add(new BasicNameValuePair("content", content));
        if (null != reid) {
            params.add(new BasicNameValuePair("reid", reid));
        }

        // 封装请求参数的实体对象
        UrlEncodedFormEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(params, HttpManager.CHARSET);
        } catch (UnsupportedEncodingException e) {
            Logcat.e(TAG, "sendArticle UnsupportedEncodingException");
        }

        return HttpManager.getInstance(context).postHttp(context, API_ARTICLE + board
                + "/post" + BBSManager.FORMAT + "?appkey=" + BBSManager.APPKEY, entity);
    }

    /**
     * 转载文章
     * 
     * @param board 文章所在版面
     * @param id 文章ID
     * @param target 要转载的版面
     * @return 转载的文章元数据
     */
    public static String crossArticle(Context context, String board, int id, String target) {
        ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();

        params.add(new BasicNameValuePair("target", target));

        // 封装请求参数的实体对象
        UrlEncodedFormEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(params, HttpManager.CHARSET);
        } catch (UnsupportedEncodingException e) {
            Logcat.e(TAG, "crossArticle UnsupportedEncodingException");
        }

        return HttpManager.getInstance(context).postHttp(context, API_ARTICLE + board + "/cross/"
                + id + BBSManager.FORMAT + "?appkey=" + BBSManager.APPKEY, entity);
    }

    /**
     * 转寄文章
     * 
     * @param board 文章所在版面
     * @param id 文章ID
     * @param target 收件人ID
     * @return 转载的文章元数据
     */
    public static String forwardArticle(Context context, String board, int id, String target) {
        ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();

        params.add(new BasicNameValuePair("target", target));

        // 封装请求参数的实体对象
        UrlEncodedFormEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(params, HttpManager.CHARSET);
        } catch (UnsupportedEncodingException e) {
            Logcat.e(TAG, "forwardArticle UnsupportedEncodingException");
        }

        return HttpManager.getInstance(context).postHttp(context, API_ARTICLE + board + "/forward/"
                + id + BBSManager.FORMAT + "?appkey=" + BBSManager.APPKEY, entity);
    }

    /**
     * 更新指定文章/主题
     * 
     * @param board 合法的版面名称
     * @param id 文章或主题id
     * @param title 修改后的文章标题
     * @param content 修改后的文章内容
     * @return 文章元数据
     */
    public static String updateArticle(Context context, String board, int id, String title,
            String content) {
        ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();

        params.add(new BasicNameValuePair("title", title));
        params.add(new BasicNameValuePair("content", content));

        // 封装请求参数的实体对象
        UrlEncodedFormEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(params, HttpManager.CHARSET);
        } catch (UnsupportedEncodingException e) {
            Logcat.e(TAG, "forwardArticle UnsupportedEncodingException");
        }

        return HttpManager.getInstance(context).postHttp(context, API_ARTICLE + board + "/update/"
                + id + BBSManager.FORMAT + "?appkey=" + BBSManager.APPKEY, entity);
    }

    /**
     * 删除指定文章
     * 
     * @param board 合法的版面名称
     * @param id 文章或主题id
     * @return 文章元数据
     */
    public static String deleteArticle(Context context, String board, int id) {
        return HttpManager.getInstance(context).postHttp(context, API_ARTICLE + board + "/delete/"
                + id + BBSManager.FORMAT + "?appkey=" + BBSManager.APPKEY, null);
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
        if (is_top) {
            return Color.RED;
        } else if ("g".equals(flag)) {
            return AiYouApplication.getInstance().getResources().getColor(R.color.marker_g);
        } else if ("m".equals(flag)) {
            return AiYouApplication.getInstance().getResources().getColor(R.color.marker_m);
        } else if ("b".equals(flag)) {
            return AiYouApplication.getInstance().getResources().getColor(R.color.marker_b);
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

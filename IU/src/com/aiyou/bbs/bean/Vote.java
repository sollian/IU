
package com.aiyou.bbs.bean;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.aiyou.AiYouApplication;
import com.aiyou.R;
import com.aiyou.bbs.bean.helper.AdapterInterface;
import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.utils.JsonHelper;
import com.aiyou.utils.http.HttpManager;
import com.aiyou.utils.logcat.Logcat;

/**
 * 投票元数据
 * 
 * @author sollian
 */
public class Vote implements Serializable, AdapterInterface {
    private static final String TAG = Vote.class.getSimpleName();
    /**
     * 
     */
    private static final long serialVersionUID = 11115L;

    // 投票内容
    public static final String API_VOTE = BBSManager.API_HEAD + "/vote/";

    // 投票标识id
    public int vid = -1;
    // 投票标题
    public String title;
    // 投票发起时间戳
    public long start = -1;
    // 投票截止时间戳
    public long end = -1;
    // 投票参与的人数
    public int user_count = -1;
    // 投票总票数(投票类型为单选时与user_count相等)，如果设置投票后可见且还没投票这个值为-1,只存在于/vote/:id中
    public int vote_count = -1;
    // 投票类型，0为单选，1为多选
    public int type = -1;
    // 每个用户能投票数的最大值，只有当type为1时，此属性有效
    public int limit = -1;
    // 投票所关联的投票版面的文章id
    public int aid = -1;
    // 投票是否截止
    public boolean is_end = false;
    // 投票是否被删除
    public boolean is_deleted = false;
    // 投票结果是否投票后可见
    public boolean is_result_voted = false;
    // 投票发起人的用户元数据，如果该用户不存在则为字符串
    public User user;
    // 当前用户的投票结果，如果用户已投票，则含有两个属性time(int)和viid(array)，分别表示投票的时间和所投选项的viid数组；如果用户没投票则为false
    public Voted voted;
    // 投票选项，由投票选项元数据组成的数组，只存在于/vote/:id中
    public Option[] options;

    public Vote(String strJson) {
        if (strJson == null) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(strJson);
            vid = JsonHelper.getInt(jsonObject, "vid");
            title = JsonHelper.getString(jsonObject, "title");
            start = JsonHelper.getLong(jsonObject, "start");
            end = JsonHelper.getLong(jsonObject, "end");
            user_count = JsonHelper.getInt(jsonObject, "user_count");
            vote_count = JsonHelper.getInt(jsonObject, "vote_count");
            type = JsonHelper.getInt(jsonObject, "type");
            limit = JsonHelper.getInt(jsonObject, "limit");
            aid = JsonHelper.getInt(jsonObject, "aid");
            is_end = JsonHelper.getBoolean(jsonObject, "is_end");
            is_deleted = JsonHelper.getBoolean(jsonObject, "is_deleted");
            is_result_voted = JsonHelper.getBoolean(jsonObject, "is_result_voted");

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

            JSONArray optionArray = JsonHelper.getJSONArray(jsonObject, "options");
            if (null != optionArray) {
                int length = optionArray.length();
                options = new Option[length];
                for (int i = 0; i < length; i++) {
                    options[i] = new Option(optionArray.opt(i).toString());
                }
            }

            JSONObject voteObject = JsonHelper.getJSONObject(jsonObject, "voted");
            if (null != voteObject) {
                voted = new Voted(voteObject.toString());
            } else {
                voted = null;
            }
        } catch (Exception e) {
        }
    }

    /**
     * 获取投票信息
     * 
     * @param vid 投票vid
     * @return 投票元数据
     */
    public static String getVote(Context context, int vid) {
        return HttpManager.getInstance(context).getHttp(context,
                API_VOTE + vid + BBSManager.FORMAT + "?appkey="
                        + BBSManager.APPKEY);
    }

    /**
     * 投票操作
     * 
     * @param context
     * @param vote
     * @return
     */
    public static String sendVote(Context context, Vote vote) {
        // 封装请求的参数集合
        ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        if (vote.type == 0) {
            // 单选
            for (int i = 0; i < vote.options.length; i++) {
                if (vote.options[i].isChecked) {
                    params.add(new BasicNameValuePair("vote",
                            vote.options[i].viid + ""));
                }
            }
        } else {
            // 多选
            for (int i = 0; i < vote.options.length; i++) {
                if (vote.options[i].isChecked) {
                    params.add(new BasicNameValuePair("vote[]",
                            vote.options[i].viid + ""));
                }
            }
        }
        // 封装请求参数的实体对象
        UrlEncodedFormEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(
                    params, "utf-8");
        } catch (UnsupportedEncodingException e) {
            Logcat.e(TAG, "sendVote UnsupportedEncodingException");
        }

        return HttpManager.getInstance(context).postHttp(context,
                API_VOTE + vote.vid + BBSManager.FORMAT + "?appkey="
                        + BBSManager.APPKEY, entity);
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDate() {
        if (-1 != end) {
            if (is_end) {
                return "已截止";
            }
        }
        return "";
    }

    @Override
    public int getTitleColor() {
        if (null != voted) {
            return AiYouApplication.getInstance().getResources().getColor(R.color.vote_notvoted);
        }
        return -1;
    }

    @Override
    public int getCount() {
        return user_count;
    }

    @Override
    public User getUser() {
        return user;
    }

    /**
     * 投票选项元数据
     * 
     * @author sollian
     */
    public class Option {
        // 投票选项标识id
        public int viid = -1;
        // 选项内容
        public String label;
        // 该选项为已投票数，如果设置投票后可见且还没投票这个值为-1
        public int num = -1;
        public double num_relative = 0;

        /**
         * 附加——该选项是否被选中
         */
        public boolean isChecked = false;

        public Option(String strJson) {
            if (strJson == null) {
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(strJson);
                viid = JsonHelper.getInt(jsonObject, "viid");
                label = JsonHelper.getString(jsonObject, "label");
                num = JsonHelper.getInt(jsonObject, "num");
            } catch (Exception e) {
            }
        }
    }

    public class Voted implements Serializable {
        /**
    	 * 
    	 */
        private static final long serialVersionUID = 11116L;

        // 所投选项的viid数组
        public int[] viids;
        // 投票的时间
        public long time = -1;

        public Voted(String strJson) {
            if (strJson == null) {
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(strJson);
                time = JsonHelper.getLong(jsonObject, "time");

                JSONArray jsonArray = JsonHelper.getJSONArray(jsonObject, "viid");
                if (null != jsonArray) {
                    int length = jsonArray.length();
                    viids = new int[length];
                    for (int i = 0; i < length; i++) {
                        viids[i] = jsonArray.getInt(i);
                    }
                }
            } catch (Exception e) {
            }
        }
    }
}

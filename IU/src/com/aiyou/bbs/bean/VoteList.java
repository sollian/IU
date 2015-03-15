
package com.aiyou.bbs.bean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.utils.JsonHelper;
import com.aiyou.utils.http.HttpManager;

/**
 * 投票列表
 * 
 * @author sollian
 */
public class VoteList {

    public enum VoteType {
        NEW("new"), // 最新投票
        ME("me"), // 我的投票
        JOIN("join"), // 我参与的投票
        HOT("hot"), // 热门投票
        ALL("all");// 全部投票

        private String mType;

        private VoteType(String type) {
            mType = type;
        }

        @Override
        public String toString() {
            return mType;
        }
    };

    // 所查询的投票列表的投票元数据构成的数组
    public Vote[] votes;
    // 当前投票列表的分页信息
    public Pagination pagination;

    public VoteList(String strJson) {
        if (strJson == null) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(strJson);
            pagination = new Pagination(JsonHelper.getJSONObject(jsonObject,
                    "pagination").toString());

            JSONArray jsonArray = JsonHelper.getJSONArray(jsonObject, "votes");
            if (null != jsonArray) {
                int length = jsonArray.length();
                votes = new Vote[length];
                for (int i = 0; i < length; i++) {
                    votes[i] = new Vote(jsonArray.opt(i).toString());
                }
            }
        } catch (JSONException e) {
        }
    }

    /**
     * 获取投票列表
     * 
     * @param type
     * @return
     */
    public static String getVoteList(Context context, VoteType type) {
        return getVoteList(context, type, 1);
    }

    /**
     * 获取投票列表
     * 
     * @param type
     * @param page
     * @return
     */
    public static String getVoteList(Context context, VoteType type, int page) {
        return HttpManager.getInstance(context).getHttp(context,
                Vote.API_VOTE + "category/" + type.toString()
                        + BBSManager.FORMAT + "?page=" + page + "&appkey=" + BBSManager.APPKEY);
    }
}

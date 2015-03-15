
package com.aiyou.bbs.bean;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.aiyou.utils.JsonHelper;

/**
 * 分页元数据
 * 
 * @author sollian
 */
public class Pagination implements Serializable {
    /**
	 * 
	 */
    private static final long serialVersionUID = 11120L;

    // 总页数
    public int page_all_count = 1;
    // 当前页数
    public int page_current_count = 1;
    // 每页元素个数
    public int item_page_count = 0;
    // 所有元素个数
    public int item_all_count = 0;

    public Pagination(String strJson) {
        try {
            JSONObject jsonObject = new JSONObject(strJson);
            page_all_count = JsonHelper.getInt(jsonObject, "page_all_count");
            page_current_count = JsonHelper.getInt(jsonObject, "page_current_count");
            item_page_count = JsonHelper.getInt(jsonObject, "item_page_count");
            item_all_count = JsonHelper.getInt(jsonObject, "item_all_count");
        } catch (JSONException e) {
        }
    }
}

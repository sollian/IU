
package com.aiyou.iptv.bean;

import java.io.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

import com.aiyou.utils.JsonHelper;

public class Chanel implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -4312807256953771673L;
    public String logo;
    public String name;
    public String url;
    public long frequency = 0;

    public Chanel() {

    }

    public Chanel(String strJson) {
        if (strJson == null) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(strJson);
            logo = JsonHelper.getString(jsonObject, "img");
            name = JsonHelper.getString(jsonObject, "name");
            url = JsonHelper.getString(jsonObject, "url");
        } catch (JSONException e) {
        }
    }
}

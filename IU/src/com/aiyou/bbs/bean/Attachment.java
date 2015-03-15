
package com.aiyou.bbs.bean;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.utils.JsonHelper;
import com.aiyou.utils.FileCache.FileManager;
import com.aiyou.utils.http.HttpManager;
import com.aiyou.utils.logcat.Logcat;

/**
 * 附件元数据
 * 
 * @author sollian
 */
public class Attachment implements Serializable {
    private static final String TAG = Attachment.class.getSimpleName();
    /**
	 * 
	 */
    private static final long serialVersionUID = 11117L;

    // 附件
    private static final String API_ATTACHMENT = BBSManager.API_HEAD
            + "/attachment/";

    // 文件列表
    public File[] files = null;
    // 剩余空间大小
    public String remain_space = null;
    // 剩余附件个数
    public int remain_count = -1;

    public Attachment(String strJson) {
        if (strJson == null) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(strJson);
            remain_space = JsonHelper.getString(jsonObject, "remain_space");
            remain_count = JsonHelper.getInt(jsonObject, "remain_count");
            JSONArray jsonArray = JsonHelper.getJSONArray(jsonObject, "file");
            if (null != jsonArray) {
                int length = jsonArray.length();
                files = new File[length];

                JSONObject fileObj = null;
                for (int i = 0; i < length; i++) {
                    fileObj = (JSONObject) jsonArray.opt(i);
                    files[i] = new File(fileObj.toString());
                }
            }
        } catch (JSONException e) {
        }
    }

    /**
     * 上传附件
     * 
     * @param board 合法的版面名称
     * @param id 文章或主题id
     * @param file 文件
     * @return 附件信息
     */
    public static String sendAttachment(Context context, String board, int id, java.io.File file) {
        MultipartEntity mpEntity = new MultipartEntity(
                HttpMultipartMode.BROWSER_COMPATIBLE, null,
                Charset.forName("gb2312")); // 文件传输,注意编码！！！！！否则文件名会乱码
        ContentBody cbFile = new FileBody(file);
        mpEntity.addPart("file", cbFile);
        return HttpManager.getInstance(context).postHttp(context,
                API_ATTACHMENT + board + "/add/" + id
                        + BBSManager.FORMAT + "?appkey=" + BBSManager.APPKEY, mpEntity);
    }

    /**
     * 获取附件信息
     * 
     * @param board 合法的版面名称
     * @param id 文章或主题id
     * @return 用户空间/文章附件元数据
     */
    public static String getAttachments(Context context, String board, int id) {
        return HttpManager.getInstance(context).getHttp(context, API_ATTACHMENT + board + "/" + id
                + BBSManager.FORMAT + "?appkey=" + BBSManager.APPKEY);
    }

    /**
     * 删除附件
     * 
     * @param board 合法的版面名称
     * @param id 文章或主题id
     * @param attachName 附件名
     * @return
     */
    public static String deleteAttachment(Context context, String board, int id,
            String attachName) {
        ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();

        params.add(new BasicNameValuePair("name", attachName));

        // 封装请求参数的实体对象
        UrlEncodedFormEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(params, "utf-8");
        } catch (UnsupportedEncodingException e) {
            Logcat.e(TAG, "deleteAttachment UnsupportedEncodingException");
        }
        return HttpManager.getInstance(context).postHttp(context, API_ATTACHMENT + board
                + "/delete/" + id + BBSManager.FORMAT + "?appkey=" + BBSManager.APPKEY, entity);
    }

    /**
     * 附件中包含的文件类
     * 
     * @author 守宪
     */
    public class File implements Serializable {
        /**
		 * 
		 */
        private static final long serialVersionUID = 11118L;

        // 文件名
        public String name = null;
        // 文件链接，在用户空间的文件，该值为空
        public String url = null;
        // 文件大小
        public String size = null;
        // 宽度为120px的缩略图，用户空间的文件，该值为空,附件为图片格式(jpg,png,gif)存在
        public String thumbnail_small = null;
        // 宽度为240px的缩略图，用户空间的文件，该值为空,附件为图片格式(jpg,png,gif)存在
        public String thumbnail_middle = null;

        public File(String strJson) {
            if (strJson == null) {
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(strJson);
                name = JsonHelper.getString(jsonObject, "name");
                url = JsonHelper.getString(jsonObject, "url");
                size = JsonHelper.getString(jsonObject, "size");
                thumbnail_small = JsonHelper.getString(jsonObject, "thumbnail_small");
                thumbnail_middle = JsonHelper.getString(jsonObject,
                        "thumbnail_middle");
                if (!FileManager.isImage(name)) {
                    url = url
                            .replace("api.byr.cn/attachment", "bbs.byr.cn/att");
                    thumbnail_small = thumbnail_small.replace(
                            "api.byr.cn/attachment", "bbs.byr.cn/att");
                    thumbnail_small = thumbnail_small.replace(
                            "api.byr.cn/attachment", "bbs.byr.cn/att");
                }
            } catch (JSONException e) {
            }
        }
    }
}

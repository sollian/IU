
package com.aiyou.bbs.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aiyou.AiYouApplication;
import com.aiyou.bbs.bean.Board;
import com.aiyou.bbs.bean.Refer.ReferType;
import com.aiyou.bbs.bean.Article;
import com.aiyou.bbs.bean.Favorite;
import com.aiyou.bbs.bean.Section;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

/**
 * BBS工具类 API文档—— https://github.com/xw2423/nForum/wiki/nForum-API
 *
 * @author sollian
 */
public class BBSManager {
    public static final String REFER_RECEIVER_ACTION = "android.intent.action.MY_RECEIVER";

    public static final String APPKEY = "365c7cb3aeb92163";//论坛申请的appkey
    public static final String API_HEAD = "http://api.byr.cn";
    public static final String BBS_URL = "http://bbs.byr.cn";
    public static final String FORMAT = ".json";

    public static final String GUEST = "guest";

    /**
     * SharedPreferences
     */
    private static final String SPNAME = "bbs";
    private static final String SP_BBS_COLLECT = "bbs_collect";
    // 键
    private static final String KEY_BBS_WEBVIEW_SCALESIZE = "bbs_webview_scalesize";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_PASSWORD = "user_password";
    private static final String KEY_MAIL = "bbs_mail";
    private static final String KEY_ROOT_SECTION = "bbs_root_section";

    /**
     * 用户信息
     */
    private static String BBS_USER_ID = null;
    private static String BBS_USER_PASSWORD = null;
    /**
     * webview的缩放值
     */
    private int mDefaultWvScale = 200;
    private static int BBS_WEBVIEW_SCALESIZE = -1;

    private SharedPreferences mSharedPref;

    private static BBSManager mInstance;

    private String mAppTail;

    @SuppressWarnings("deprecation")
    private BBSManager(Context context) {
        mSharedPref = context.getSharedPreferences(SPNAME, Context.MODE_PRIVATE);
        WebView wv = new WebView(context);
        mDefaultWvScale = (int) (wv.getScale() * 100);
    }

    public static BBSManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (BBSManager.class) {
                if (mInstance == null) {
                    mInstance = new BBSManager(context);
                }
            }
        }
        return mInstance;
    }

    public void setAppTail(String tail) {
        mAppTail = tail;
    }

    public String getAppTail() {
        return mAppTail;
    }

    /**
     * 设置webview的scalesize
     *
     * @param size
     */
    public void setWebViewScaleSize(int size) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putInt(KEY_BBS_WEBVIEW_SCALESIZE, size);
        editor.commit();
        BBS_WEBVIEW_SCALESIZE = size;
    }

    /**
     * 获取webview的scalesize
     *
     * @return
     */
    public int getWebViewScaleSize() {
        if (-1 == BBS_WEBVIEW_SCALESIZE) {
            BBS_WEBVIEW_SCALESIZE = mSharedPref.getInt(KEY_BBS_WEBVIEW_SCALESIZE, mDefaultWvScale);
        }
        return BBS_WEBVIEW_SCALESIZE;
    }

    /**
     * 设置用户ID和密码
     *
     * @param strId
     * @param strPassword
     */
    public void setUserInfo(String strId, String strPassword) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(KEY_USER_ID, strId);
        editor.putString(KEY_USER_PASSWORD, strPassword);
        editor.commit();

        BBS_USER_ID = strId;
        BBS_USER_PASSWORD = strPassword;
    }

    /**
     * 清除用户ID和密码
     */
    public void clearUserInfo() {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(KEY_USER_ID, null);
        editor.putString(KEY_USER_PASSWORD, null);
        editor.commit();

        BBS_USER_ID = null;
        BBS_USER_PASSWORD = null;
    }

    /**
     * 获取用户ID
     *
     * @return
     */
    public String getUserId() {
        if (null == BBS_USER_ID) {
            BBS_USER_ID = mSharedPref.getString(KEY_USER_ID, GUEST);
        }
        return BBS_USER_ID;
    }

    /**
     * 获取用户密码
     *
     * @return
     */
    public String getUserPassword() {
        if (null == BBS_USER_PASSWORD) {
            BBS_USER_PASSWORD = mSharedPref.getString(KEY_USER_PASSWORD, "");
        }
        return BBS_USER_PASSWORD;
    }

    /**
     * 设置版面是否允许附件
     *
     * @param board
     * @param flag
     */
    public void setAllowAttachment(String board, boolean flag) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(board, flag);
        editor.commit();
    }

    /**
     * 查询版面是否允许附件
     *
     * @param board
     * @return
     */
    public boolean isAllowAttachment(String board) {
        return mSharedPref.getBoolean(board, true);
    }

    /**
     * 获取收藏的文章
     *
     * @return 收藏的文章数组
     */
    public static Article[] getArticleCollect() {
        Article[] article;
        SharedPreferences database = AiYouApplication.getInstance().getSharedPreferences(
                SP_BBS_COLLECT, Context.MODE_PRIVATE);
        @SuppressWarnings("unchecked")
        Map<String, String> map = (Map<String, String>) database.getAll();
        int size = map.size();
        article = new Article[size];
        int i = 0;
        for (String json : map.values()) {
            article[i++] = new Article(json);
        }
        return article;
    }

    /**
     * 清空收藏
     */
    public static void clearArticleCollect() {
        SharedPreferences database = AiYouApplication.getInstance().getSharedPreferences(
                SP_BBS_COLLECT, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = database.edit();
        editor.clear();
        editor.commit();
    }

    /**
     * 删除某一条收藏
     *
     */
    public static void deleteArticleCollect(Article article) {
        SharedPreferences database = AiYouApplication.getInstance().getSharedPreferences(
                SP_BBS_COLLECT, Context.MODE_PRIVATE);
        String key = article.board_name + "_" + article.id;
        if (database.contains(key)) {
            SharedPreferences.Editor editor = database.edit();
            editor.remove(key);
            editor.commit();
        }
    }

    /**
     * 将article加入收藏
     *
     * @param article
     */
    public static void putArticleCollect(Article article) {
        SharedPreferences database = AiYouApplication.getInstance().getSharedPreferences(
                SP_BBS_COLLECT, Context.MODE_PRIVATE);
        if (database.contains(article.board_name + "_" + article.id)) {
            return;
        }
        SharedPreferences.Editor editor = database.edit();
        editor.putString(article.board_name + "_" + article.id, article
                .getJson().toString());
        editor.commit();
    }

    /**
     * 设置Refer消息
     *
     * @param type
     * @param count
     */
    public void setBBSNotificationRefer(ReferType type, int count) {
        if (count < 0) {
            count = 0;
        }
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putInt(type.toString(), count);
        editor.commit();
    }

    /**
     * 获取是否有Refer消息
     *
     * @param type
     * @return
     */
    public int getBBSNotificationRefer(ReferType type) {
        return mSharedPref.getInt(type.toString(), 0);
    }

    /**
     * 设置是否有新邮件
     *
     * @param flag
     */
    public void setBBSNotificationMail(boolean flag) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(KEY_MAIL, flag);
        editor.commit();
    }

    /**
     * 获取是否有新邮件
     *
     * @return
     */
    public boolean getBBSNotificationMail() {
        return mSharedPref.getBoolean(KEY_MAIL, false);
    }

    /**
     * 保存论坛根分区数据
     *
     * @param json
     */
    public void saveRootSec(String json) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(KEY_ROOT_SECTION, json);
        editor.commit();
    }

    /**
     * 获取论坛根分区数据
     *
     * @return
     */
    public String getRootSec() {
        return mSharedPref.getString(KEY_ROOT_SECTION, null);
    }

    /**
     * 动态初始化手机论坛各个板块
     */
    public static List<TreeElement> initTreeViewData(Context context,
                                                     List<TreeElement> treeElementList) {
        Section sections = Section.getRootSection(context);
        if (sections != null && sections.sections != null) {
            if (treeElementList == null) {
                treeElementList = new ArrayList<>();
            } else {
                treeElementList.clear();
            }

            TreeElement element;
            for (Section section : sections.sections) {
                element = new TreeElement(section.name, section.description, true);
                treeElementList.add(element);
            }
        }
        return treeElementList;
    }

    /**
     * 向EditText中添加表情图片的方法
     *
     * @param context
     * @param et        目标EditText
     * @param bitmap    要添加的图片
     * @param imageName 表示图片的String
     * @param isSingle  图片是否只能包含一次
     */
    public static void addPic(Context context, EditText et, Bitmap bitmap,
                              String imageName, boolean isSingle) {
        if (isSingle) {
            String tempStr = et.getText().toString();
            if (tempStr.contains(imageName)) {
                Toast.makeText(context, "已包含该文件", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        ImageSpan imageSpan = new ImageSpan(context, bitmap);
        SpannableString spannableString = new SpannableString(imageName);
        spannableString.setSpan(imageSpan, 0, spannableString.length(),
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        Editable e = et.getText();
        int st = et.getSelectionStart();
        int en = et.getSelectionEnd();
        e.replace(st, en, spannableString);
    }

    /**
     * 关键字高亮显示
     *
     * @param target 需要高亮的关键字
     */
    public static SpannableStringBuilder highlight(String source, String target) {
        SpannableStringBuilder spannable = new SpannableStringBuilder(source);
        CharacterStyle span;

        Pattern p = Pattern.compile(target);
        Matcher m = p.matcher(source);
        while (m.find()) {
            span = new ForegroundColorSpan(Color.RED);// 需要重复！
            spannable.setSpan(span, m.start(), m.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannable;
    }

    public static boolean checkFavorite(String boardName) {
        if (Favorite.mFavorite == null || Favorite.mFavorite.boards == null) {
            return false;
        }
        for (Board b : Favorite.mFavorite.boards) {
            if (b.name.equals(boardName)) {
                return true;
            }
        }
        return false;
    }
}

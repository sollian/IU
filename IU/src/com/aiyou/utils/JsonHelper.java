
package com.aiyou.utils;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.aiyou.AiYouApplication;
import com.aiyou.bbs.bean.Article;
import com.aiyou.bbs.bean.Attachment;
import com.aiyou.bbs.bean.Mail;
import com.aiyou.utils.FileCache.FileManager;
import com.aiyou.utils.logcat.Logcat;

/**
 * Json解析辅助类
 * 
 * @author sollian
 */
public class JsonHelper {
    private static final String TAG = JsonHelper.class.getSimpleName();

    public static String getString(JSONObject jsonObject, String key) {
        return jsonObject.optString(key, null);
    }

    public static int getInt(JSONObject jsonObject, String key) {
        return jsonObject.optInt(key, -1);
    }

    public static long getLong(JSONObject jsonObject, String key) {
        return jsonObject.optLong(key, -1);
    }

    public static double getDouble(JSONObject jsonObject, String key) {
        return jsonObject.optDouble(key, -1);
    }

    public static boolean getBoolean(JSONObject jsonObject, String key) {
        return jsonObject.optBoolean(key, false);
    }

    public static JSONObject getJSONObject(JSONObject jsonObject, String key) {
        return jsonObject.optJSONObject(key);
    }

    public static JSONArray getJSONArray(JSONObject jsonObject, String key) {
        return jsonObject.optJSONArray(key);
    }

    public static void put(JSONObject obj, String key, Object value) {
        if (value != null) {
            try {
                obj.put(key, value);
            } catch (JSONException e) {
                Logcat.e(TAG, "put JSONException");
            }
        }
    }

    /**
     * 检查返回的是否是错误信息
     * 
     * @param strJson
     * @return null——非错误信息
     */
    public static String checkError(String strJson) {
        if (TextUtils.isEmpty(strJson)) {
            return null;
        }
        String strResult = null;
        try {
            JSONObject jsonObject = new JSONObject(strJson);
            strResult = jsonObject.getString("msg");
        } catch (JSONException e) {
            strResult = null;
        }

        return strResult;
    }

    /**
     * 将content转换为html内容
     * 
     * @param obj 可选：Article|Mail
     * @param isArticle 是否是Article
     * @return 字符串数组：[0]——文章内容；[1]——回复内容
     */
    @SuppressLint("DefaultLocale")
    public static String[] toHtml(Object obj, boolean isArticle) {
        String strResult = null;
        Attachment attachment = null;
        boolean has_attachment = false;
        if (isArticle) {
            Article article = (Article) obj;
            strResult = article.content;
            attachment = article.attachment;
            has_attachment = article.has_attachment;
        } else {
            Mail mail = (Mail) obj;
            strResult = mail.content;
            attachment = mail.attachment;
            has_attachment = mail.has_attachment;
        }
        Pattern p = null;
        Matcher m = null;
        String temp = null;
        String strReplace = null;
        MatchResult mr = null;
        // 去除多余的尾巴
        while (strResult.endsWith("-") || strResult.endsWith("\n")) {
            strResult = strResult.substring(0, strResult.length() - 1);
        }
        // 替换<>
        strResult = strResult.replace("<", "&lt;");
        strResult = strResult.replace(">", "&gt;");
        /**
         * 处理回复
         */
        String strReply = "";
        p = Pattern.compile("【[^】]*?在[\\s\\S]*?的大作中提到:[^】]*?】");
        m = p.matcher(strResult);
        String strTemp = null;
        while (m.find()) {
            strTemp = m.group();
            strReply = m.group();
            break;
        }
        if (null != strTemp) {
            int nIndex = strResult.indexOf(strTemp);
            nIndex += strTemp.length();
            String str1 = strResult.substring(0, nIndex);
            String strNewTemp = "<font color=\"#919600\">" + strTemp
                    + "</font>";
            str1 = str1.replace(strTemp, strNewTemp);

            String str2 = strResult.substring(nIndex);

            p = Pattern.compile("[\\s\n]?: ([\\s\\S]*\n: )*[^\n]*");
            m = p.matcher(str2);
            while (m.find()) {
                temp = m.group().replaceAll("\\[[^(em)]*?^\\]\\]", "");
                strReplace = "<font color=\"#009600\">" + temp + "</font>";
                str2 = str2.replace(m.group(), strReplace);
                strReply += m.group();
            }
            strResult = str1 + str2;
        }
        // 链接
        strResult = strResult.replaceAll("=http://", "\\[sollian\\]");
        strResult = strResult.replaceAll("=https://", "\\[sollian1\\]");
        strResult = strResult.replaceAll(
                "(http[s]?://[0-9a-zA-Z\\-\\+\\.\\?&%_/=#!~:]*)",
                "<a href=\"$1\">$1</a>");
        strResult = strResult.replaceAll("\\[sollian\\]", "=http://");
        strResult = strResult.replaceAll("\\[sollian1\\]", "=https://");

        p = Pattern
                .compile("\\[(?:URL|url)=([^\\]]*?)\\]([\\s\\S]*?)\\[/(?:URL|url)\\]");
        m = p.matcher(strResult);
        String url = null;
        String text = null;
        while (m.find()) {
            mr = m.toMatchResult();
            url = mr.group(1);
            text = mr.group(2);
            strReplace = "<a href=\"" + url + "\">" + text + "</a>";
            strResult = strResult.replace(m.group(), strReplace);
        }
        // marquee动画
        p = Pattern.compile("\\[fly\\]([\\s\\S]*?)\\[/fly\\]");
        m = p.matcher(strResult);
        while (m.find()) {
            mr = m.toMatchResult();
            text = mr.group(1);
            strReplace = "<marquee width=\"100%\" behavior=\"alternate\" scrollamount=\"3\">"
                    + text + "</marquee>";
            strResult = strResult.replace(m.group(), strReplace);
        }

        p = Pattern.compile("\\[move\\]([\\s\\S]*?)\\[/move\\]");
        m = p.matcher(strResult);
        while (m.find()) {
            mr = m.toMatchResult();
            text = mr.group(1);
            strReplace = "<marquee scrollamount=\"3\">" + text + "</marquee>";
            strResult = strResult.replace(m.group(), strReplace);
        }
        // 粗体
        strResult = strResult.replaceAll("\\[[bB]\\]", "<b>");
        strResult = strResult.replaceAll("\\[/[bB]\\]", "</b>");
        // 斜体
        strResult = strResult.replaceAll("\\[[iI]\\]", "<i>");
        strResult = strResult.replaceAll("\\[/[iI]\\]", "</i>");
        // 下划线
        strResult = strResult.replaceAll("\\[[uU]\\]", "<u>");
        strResult = strResult.replaceAll("\\[/[uU]\\]", "</u>");
        // 字体
        p = Pattern.compile("\\[face=([^\\]]*?)\\]([\\s\\S]*?)\\[/face\\]");
        m = p.matcher(strResult);
        String face = null;
        while (m.find()) {
            mr = m.toMatchResult();
            face = mr.group(1);
            text = mr.group(2);
            strReplace = "<font face=\"" + face + "\">" + text + "</font>";
            strResult = strResult.replace(m.group(), strReplace);
        }
        // 字体颜色
        p = Pattern.compile("\\[color=([^\\]]*?)\\]([\\s\\S]*?)\\[/color\\]");
        m = p.matcher(strResult);
        String color = null;
        while (m.find()) {
            mr = m.toMatchResult();
            color = mr.group(1);
            text = mr.group(2);
            strReplace = "<font color=\"" + color + "\">" + text + "</font>";
            strResult = strResult.replace(m.group(), strReplace);
        }
        // 字体大小
        p = Pattern.compile("\\[size=([^\\]]*?)\\]([\\s\\S]*?)\\[/size\\]");
        m = p.matcher(strResult);
        String size = null;
        while (m.find()) {
            mr = m.toMatchResult();
            size = mr.group(1);
            text = mr.group(2);
            strReplace = "<font size=\"" + size + "\">" + text + "</font>";
            strResult = strResult.replace(m.group(), strReplace);
        }
        // 代码块
        p = Pattern.compile("\\[code=([^\\]]*?)\\]([\\s\\S]*?)\\[/code\\]");
        m = p.matcher(strResult);
        while (m.find()) {
            mr = m.toMatchResult();
            text = mr.group(2);
            strReplace = "<pre>" + text + "</pre>";
            strResult = strResult.replace(m.group(), strReplace);
        }
        // 图片
        p = Pattern.compile("\\[(?:IMG|img)=([^\\]]*?)\\]\\[/(?:IMG|img)\\]");
        m = p.matcher(strResult);
        while (m.find()) {
            mr = m.toMatchResult();
            url = mr.group(1);
            strReplace = "<image=" + url + ">";
            strResult = strResult.replace(m.group(), strReplace);
        }
        // swf
        p = Pattern.compile("\\[(?:SWF|swf)=([^\\]]*?)\\]\\[/(?:SWF|swf)\\]");
        m = p.matcher(strResult);
        while (m.find()) {
            mr = m.toMatchResult();
            url = mr.group(1);
            strReplace = "<a href=\"" + url + "\">" + "观看视频：" + url + "</a>";
            strResult = strResult.replace(m.group(), strReplace);
        }
        // radio
        p = Pattern
                .compile("\\[(?:RADIO|radio)=([^\\]]*?)\\].*?\\[/(?:RADIO|radio)\\]");
        m = p.matcher(strResult);
        while (m.find()) {
            mr = m.toMatchResult();
            url = mr.group(1);
            strReplace = "<a href=\"" + url + "\">" + "radio地址：" + url + "</a>";
            strResult = strResult.replace(m.group(), strReplace);
        }
        // 音频
        p = Pattern
                .compile("\\[(?:MP|mp)3=([^\\]]*?) auto=0\\]\\[/(?:MP|mp)3\\]");
        m = p.matcher(strResult);
        while (m.find()) {
            mr = m.toMatchResult();
            url = mr.group(1);
            strReplace = "<audio controls=\"controls\" src=\"" + url
                    + "\" STYLE=\"opacity:0.6;\">" + "<a href=\"" + url
                    + "\">点击查看：" + url + "</a>" + "</audio>";
            strResult = strResult.replace(m.group(), strReplace);
        }
        // 表情
        p = Pattern.compile("\\[(em[a|b|c]?\\d+)\\]");
        m = p.matcher(strResult);
        String name = null;
        while (m.find()) {
            mr = m.toMatchResult();
            name = mr.group(1);
            strReplace = "<img src=\"file:///android_asset/face/" + name
                    + ".gif\" " + "alt=\"" + name
                    + "\" style=\"display:inline;border-style:none\"/>";
            strResult = strResult.replace(m.group(), strReplace);
        }
        /**
         * 附件
         */
        String fileName = null;
        String strToReplace = null;
        if (has_attachment) {
            int length = attachment.files.length;
            for (int i = 0; i < length; i++) {
                strToReplace = "[upload=" + (i + 1) + "][/upload]";
                fileName = attachment.files[i].name.toLowerCase();
                strReplace = processAttachment(attachment, fileName, i);
                if (strResult.contains(strToReplace)) {
                    strToReplace = "\\[upload=" + (i + 1) + "\\]\\[/upload\\]";
                    strResult = strResult
                            .replaceFirst(strToReplace, strReplace);
                } else {
                    strResult += "\n" + strReplace;
                }
            }
        }
        /**
         * 处理ansi_escape_code 学习网址：
         * http://www.chinaunix.net/old_jh/23/266419.html
         * http://www.bluesock.org/~willg/dev/ansi.html
         * http://en.wikipedia.org/wiki/ANSI_escape_code
         */
        if (strResult.contains("\u001b[")) {
            strResult = strResult.replaceAll("\\u001b", "[ub]");
            // 去掉关闭所有属性
            // strResult = strResult.replaceAll("\\[ub\\]\\[0m", "");
            // 去掉恢复光标位置
            strResult = strResult.replaceAll("\\[ub\\]\\[s", "");
            // 去掉恢复光标位置
            strResult = strResult.replaceAll("\\[ub\\]\\[\\d{0,2}I", "");
            // 去掉光标光标上下左右移动
            strResult = strResult.replaceAll("\\[ub\\]\\[\\d+[ABCD]", "");
            // 去掉清屏
            strResult = strResult.replaceAll("\\[ub\\]\\[2J", "");
            // 去掉清除从光标到行尾的内容
            strResult = strResult.replaceAll("\\[ub\\]\\[K", "");
            // 去掉隐藏/显示光标
            strResult = strResult.replaceAll("\\[ub\\]\\[\\?25[lh]", "");
            // 去掉设置光标位置
            strResult = strResult.replaceAll("\\[ub\\]\\[\\d+;\\d+H", "");

            p = Pattern.compile("\\[ub\\]\\[(?:\\d+;)*(\\d+)[mM]([^\\[]*)");
            m = p.matcher(strResult);
            String strColor = null;
            int nFg = 0;
            while (m.find()) {
                mr = m.toMatchResult();
                nFg = Integer.parseInt(mr.group(1));
                text = mr.group(2);
                strColor = getColor(nFg);
                if (null != strColor) {
                    strReplace = "<font color=\"" + strColor + "\">" + text
                            + "</font>";
                    strResult = strResult.replace(m.group(), strReplace);
                }
            }
            // 去掉恢复光标位置
            strResult = strResult.replaceAll("\\[ub\\]\\[u", "");

            p = Pattern.compile("\\[ub\\]\\[(\\d+;)*\\d*m");
            m = p.matcher(strResult);
            while (m.find()) {
                strResult = strResult.replace(m.group(), "");
            }
        }
        strResult = strResult.replaceAll("\\[ub\\]", "");

        p = Pattern
                .compile("\\[(?:GLOW|glow)[\\s\\S]*?\\]([\\s\\S]*?)\\[/(?:GLOW|glow)\\]");
        m = p.matcher(strResult);
        while (m.find()) {
            mr = m.toMatchResult();
            strReplace = mr.group(1);
            strResult = strResult.replace(m.group(), strReplace);
        }

        String array[] = new String[2];
        array[0] = strResult;
        array[1] = strReply;
        return array;
    }

    /**
     * 获取颜色
     * 
     * @param ng
     * @return
     */
    private static String getColor(int ng) {
        String strColor = null;
        switch (ng) {
            case 30:
                strColor = "#000000";
                break;
            case 31:
                strColor = "#e80000";
                break;
            case 32:
                strColor = "#009600";
                break;
            case 33:
                strColor = "#919600";
                break;
            case 34:
                strColor = "#0000ff";
                break;
            case 35:
                strColor = "#ff00ff";
                break;
            case 36:
                strColor = "#00ffff";
                break;
            case 37:
                strColor = "#888888";
                break;
            case 130:
                strColor = "#cccccc";
                break;
            case 131:
                strColor = "#ffe0e0";
                break;
            case 132:
                strColor = "#90ee90";
                break;
            case 133:
                strColor = "#ffff00";
                break;
            case 134:
                strColor = "#add8e6";
                break;
            case 135:
                strColor = "#ffe0ff";
                break;
            case 136:
                strColor = "#e0ffff";
                break;
            case 137:
                strColor = "#ffffff";
                break;
            case 500:
                strColor = "#919600";
                break;
            default:
                strColor = "#ffff00";
                break;
        }
        return strColor;
    }

    /**
     * 处理附件
     * 
     * @param fileName
     * @param index
     * @return
     */
    private static String processAttachment(Attachment attachment,
            String fileName, int index) {
        if (index < 0 || index >= attachment.files.length) {
            return "";
        }
        String strReplace = "";
        if (FileManager.isImage(fileName)) {
            if (SwitchManager.getInstance(AiYouApplication.getInstance()).isLargeImageEnabled()) {
                // 显示大缩略图
                strReplace = "<image="
                        + attachment.files[index].thumbnail_middle + ">";
            } else {
                // 显示小缩略图
                strReplace = "<image="
                        + attachment.files[index].thumbnail_small + ">";
            }
        } else if (FileManager.isMp3(fileName)) {
            strReplace = "<audio controls=\"controls\" src=\""
                    + attachment.files[index].url
                    + "\" STYLE=\"opacity:0.6;\">" + "<a href=\""
                    + attachment.files[index].url + "\">点击查看："
                    + attachment.files[index].url + "</a>" + "</audio>";
        } else {
            strReplace = "<a href=\"" + attachment.files[index].url + "\">"
                    + "附件（" + attachment.files[index].size + "）："
                    + attachment.files[index].name + "</a>\n";
        }
        return strReplace;
    }
}

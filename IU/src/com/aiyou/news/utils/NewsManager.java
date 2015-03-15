
package com.aiyou.news.utils;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aiyou.utils.AiYouManager;

public class NewsManager {

    // 信息门户地址
    public static final String URL_INFO_HEAD = "http://portal.bupt.edu.cn";
    // 信息门户通告
    public static final String URL_INFO_INFORM = "http://portal.bupt.edu.cn/sites/main/column_by_upper.jsp?ColumnID=38&page=";
    // 信息门户新闻
    public static final String URL_INFO_NEWS = "http://portal.bupt.edu.cn/sites/main/column_by_upper.jsp?ColumnID=37&page=";
    // 北邮要闻地址
    public static final String URL_HEADLINE_HEAD = "http://www.bupt.edu.cn";
    // 北邮要闻
    public static final String URL_HEADLINE = "http://www.bupt.edu.cn/list/list.php?p=81_15_";
    // 北邮要闻缩放比例
    public static final int mHeadlineScaleSize = 175;
    // 信息门户缩放比例
    public static final int mNewsScaleSize = 150;

    /**
     * 匹配网页内容—信息息门户
     * 
     * @param htmlSource 获取的html源码
     * @return 封装好的新闻标题列表
     */
    public static News getNewsTitle(String htmlSource) {
        ArrayList<News> result = new ArrayList<News>();
        News news = null;

        Pattern p = Pattern
                .compile("href=\"(.*?)\" target=\"_blank\" title=\"(.*?)\"");
        Matcher m = p.matcher(htmlSource);
        while (m.find()) {
            MatchResult mr = m.toMatchResult();
            news = new News();
            news.title = mr.group(2);
            String url = mr.group(1);
            if (!url.contains(URL_INFO_HEAD)) {
                news.url = URL_INFO_HEAD + url;
            } else {
                news.url = url;
            }
            result.add(news);
        }
        int length = result.size();
        int index = 0;
        p = Pattern.compile("<dd>(\\d{4}\\-\\d{2}\\-\\d{2})</dd>");
        m = p.matcher(htmlSource);
        while (m.find()) {
            MatchResult mr = m.toMatchResult();
            news = result.get(index);
            news.date = mr.group(1);
            index++;
            if (index >= length - 1) {
                break;
            }
        }
        index = 0;
        p = Pattern
                .compile("<dt style=\"width:110px;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;\">(.*?)</dt>");
        m = p.matcher(htmlSource);
        while (m.find()) {
            MatchResult mr = m.toMatchResult();
            news = result.get(index);
            String from = mr.group(1);
            if (AiYouManager.getTxtWithoutNTSRElement(from, "") != null) {
                news.from = from;
            }
            index++;
            if (index > length - 1) {
                break;
            }
        }
        news = new News();
        news.list = result;
        return news;
    }

    /**
     * 获取信息门户新闻、通告内容
     * 
     * @param htmlSource
     * @return
     */
    public static String getNewsContent(String htmlSource) {
        String result = htmlSource;

        Pattern p = Pattern.compile("data_ue_src=\".*?\"");
        Matcher m = p.matcher(result);
        while (m.find()) {
            result = result.replace(m.group(), "");
        }

        p = Pattern
                .compile("<span objparam=\"fieldname:Content\"  tag=\"_ddfield\"  objid=\"6044\" >([\\s\\S]*)</span>");
        m = p.matcher(result);
        while (m.find()) {
            MatchResult mr = m.toMatchResult();
            result = mr.group(1);
        }
        // 去掉前面的空白字符
        p = Pattern.compile("^[\\s]|[\t]|[\r]|[\n]|[?]");
        m = p.matcher(result);
        result = m.replaceAll("");
        // 修正图片、连接地址
        result = completeHerf(result, URL_INFO_HEAD);
        result = completeImgSrc(result, URL_INFO_HEAD);

        return result;
    }

    /**
     * 匹配网页内容—北邮要闻
     * 
     * @param htmlSource 获取的html源码
     * @return 封装好的新闻标题列表
     */
    public static News getHeadlineTitle(String htmlSource) {
        ArrayList<News> result = new ArrayList<News>();
        News news = null;
        // 获取新闻主体
        String[] arr1 = htmlSource.split("<ul class=\"ovhi\">");
        if (arr1.length >= 2) {
            htmlSource = arr1[1];
        } else {
            return news;
        }
        String[] arr2 = htmlSource.split("</ul>");
        htmlSource = arr2[0];
        // 获取连接和标题
        Pattern p = Pattern
                .compile("href=\"(.*?)\" title=\"(.*?)\"><[^>]*?>(.*?)</font>");
        Matcher m = p.matcher(htmlSource);
        while (m.find()) {
            MatchResult mr = m.toMatchResult();
            news = new News();
            String url = mr.group(1);
            if (!url.contains(URL_HEADLINE_HEAD)) {
                news.url = URL_HEADLINE_HEAD + url;
            } else {
                news.url = url;
            }
            // strTitle = getStringFromSign(mr.group(2));
            news.title = mr.group(2);
            // String[] arr = strTitle.split("&nbsp;");
            news.date = mr.group(3);
            result.add(news);
        }
        news = new News();
        news.list = result;
        return news;
    }

    /**
     * 获取北邮要闻新闻内容
     * 
     * @param htmlSource
     * @return
     */
    public static String getHeadlineContent(String htmlSource) {
        String result = htmlSource;

        Pattern p = Pattern.compile("data_ue_src=\".*?\"");
        Matcher m = p.matcher(result);
        while (m.find()) {
            result = result.replace(m.group(), "");
        }

        p = Pattern.compile("<div class=\"content detail\">([\\s\\S]*?)</div>");
        m = p.matcher(result);
        while (m.find()) {
            MatchResult mr = m.toMatchResult();
            result = mr.group(1);
        }
        // 去掉前面的空白字符
        p = Pattern.compile("^[\\s]|[\t]|[\r]|[\n]|[?]");
        m = p.matcher(result);
        result = m.replaceAll("");
        // 修正图片、连接地址
        result = completeHerf(result, URL_HEADLINE_HEAD);
        result = completeImgSrc(result, URL_HEADLINE_HEAD);

        return result;
    }

    /**
     * 将路径中的中文名进行编码，以获取正确的路径
     * 
     * @param path
     * @return
     */
    @SuppressWarnings("deprecation")
    public static String imgPathEncoder(String path) {
        String convertPath = "";
        String arr[] = path.split("/");
        int length = arr.length;
        for (int i = 0; i < length - 1; i++) {
            convertPath += arr[i] + "/";
        }
        convertPath += URLEncoder.encode(arr[length - 1]);
        return convertPath;
    }

    /**
     * 将图片的地址补全
     * 
     * @param html 图片的相对地址
     * @param urlPath 要补充的前缀
     * @return 返回绝对地址
     */
    private static String completeImgSrc(String html, String urlPath) {
        String str = html;
        String strarr[] = str.split("src=\"");
        str = strarr[0];
        for (int i = 1; i < strarr.length; i++) {// 将图片的路径补充完全
            if ("http".equals(strarr[i].substring(0, 4))) {
                str += "src=\"" + strarr[i];
            } else {
                str += "src=\"" + urlPath + strarr[i];
            }
        }
        return str;
    }

    /**
     * 将链接的地址补全
     * 
     * @param html 相对地址
     * @param urlPath 要添加的前缀
     * @return 绝对地址
     */
    private static String completeHerf(String html, String urlPath) {
        String str = html;
        String strarr[] = str.split("href=\"");
        str = strarr[0];
        for (int i = 1; i < strarr.length; i++) {// 将链接的路径补充完全
            if ("http".equals(strarr[i].substring(0, 4))) {
                str += "href=\"" + strarr[i];
            } else {
                str += "href=\"" + urlPath + strarr[i];
            }
        }
        return str;
    }

}

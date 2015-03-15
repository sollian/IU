
package external.foldablelist.item;

import com.aiyou.bbs.bean.Article;
import com.aiyou.utils.JsonHelper;
import com.aiyou.utils.time.TimeUtils;

import android.content.Context;

/**
 * @author sollian
 */
public class Painting {
    private String strImgUrl = null;
    private Article article;
    private int color;

    public Painting(Context context, Article article) {
        this.article = article;
        processContent(JsonHelper.toHtml(article, true));
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    /**
     * 返回内容
     * 
     * @return
     */
    public Article getArticle() {
        return article;
    }

    /**
     * 返回标题
     * 
     * @return
     */
    public String getTitle() {
        return article.title;
    }

    /**
     * 返回作者
     * 
     * @return
     */
    public String getAuthor() {
        return article.user.id;
    }

    /**
     * 返回日期
     * 
     * @return
     */
    public String getDate() {
        return TimeUtils.getLocalTime(article.post_time);
    }

    /**
     * 返回头像地址
     * 
     * @return
     */
    public String getFaceUrl() {
        if (null != article.user) {
            // 头像
            if (null != article.user.face_url) {
                return article.user.face_url;
            }
        }
        return null;
    }

    /**
     * 返回图片地址
     * 
     * @return
     */
    public String getImageUrl() {
        return strImgUrl;
    }

    /**
     * 获取第一张图片地址
     * 
     * @param html
     */
    private void processContent(String[] html) {
        String[] array = html[0].split("<image");
        int length = array.length;
        for (int i = 0; i < length; i++) {
            String str = array[i].trim();
            if (str.startsWith("=")) {
                int index = str.indexOf(">");
                if (index > 0) {
                    String strImg = str.substring(1, index);
                    if (null == strImgUrl) {
                        strImgUrl = strImg;
                    }
                }
            }
        }
    }
}

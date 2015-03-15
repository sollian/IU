
package com.aiyou.utils.time;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TimeUtils {
    /**
     * 对时间戳进行自定义格式化
     * 
     * @param timeStamp 时间戳
     * @return
     */
    public static String formatTime(final long timeStamp) {
        String strTime = null;
        final long timemillis = timeStamp * 1000;
        final long curTime = System.currentTimeMillis();
        final long tempTime = curTime - timemillis;
        if (tempTime < 1000 * 60) {
            // 小于1min
            strTime = "刚刚";
        } else if (tempTime < 1000 * 60 * 60) {
            // 小于1h
            strTime = tempTime / (1000 * 60) + "分钟前";
        } else if (tempTime < 1000 * 60 * 60 * 24) {
            // 小于1天
            long lHour = tempTime / (1000 * 60 * 60);
            long lMin = (tempTime % (1000 * 60 * 60)) / (1000 * 60);
            strTime = lHour + "小时" + lMin + "分前";
        } else if (tempTime < 1000l * 60 * 60 * 24 * 30) {// 此处注意要将int转换为long
            // 小于1个月
            strTime = tempTime / (1000l * 60 * 60 * 24) + "天前";
        } else if (tempTime < 1000l * 60 * 60 * 24 * 30 * 12) {// 此处注意要将int转换为long
            // 小于1年
            strTime = tempTime / (1000l * 60 * 60 * 24 * 30) + "个月前";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",
                    Locale.CHINA);
            strTime = sdf.format(timemillis);
        }
        return strTime;
    }

    /**
     * 获取时间
     * 
     * @param timeStamp
     * @return
     */
    public static String getLocalTime(long timeStamp) {
        timeStamp *= 1000;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.CHINA);
        return sdf.format(timeStamp);
    }
}

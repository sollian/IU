
package com.aiyou.electricity;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class ConsumeInfo {
    /**
     * 抄表时间
     */
    public String time;
    /**
     * 剩余电量
     */
    public String remain;

    public static int parseHtml(List<ConsumeInfo> list, Document doc) {
        if (doc == null) {
            return 0;
        }
        if (list == null) {
            list = new ArrayList<>();
        }
        Elements table = doc.select("table#GridView1").select("tr");
        int size = table.size();
        if (size < 2) {
            return 0;
        }
        Element tr;
        Elements td2;
        ConsumeInfo info;
        for (int i = 1; i < size - 1; i++) {
            tr = table.get(i);
            td2 = tr.children();
            if (td2.size() != 3) {
                continue;
            }
            info = new ConsumeInfo();
            info.time = td2.get(1).text();
            info.remain = td2.get(2).text();
            list.add(info);
        }
        return table.select("a").size() + 1;
    }

}

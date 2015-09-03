
package com.aiyou.electricity;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class BuyEleInfo {
    /**
     * 交费时间
     */
    public String time;
    /**
     * 购买电量
     */
    public String buy;
    /**
     * 购买金额
     */
    public String money;
    /**
     * 收费类型
     */
    public String type;

    public static int parseHtml(List<BuyEleInfo> list, Document doc) {
        if (doc == null) {
            return 0;
        }
        if (list == null) {
            list = new ArrayList<>();
        }
        Elements table = doc.select("table#GridView2").select("tr");
        int size = table.size();
        if (size < 2) {
            return 0;
        }
        Element tr;
        Elements td2;
        BuyEleInfo info;
        for (int i = 1; i < size - 1; i++) {
            tr = table.get(i);
            td2 = tr.children();
            if (td2.size() != 7) {
                continue;
            }
            info = new BuyEleInfo();
            info.time = td2.get(1).text();
            info.buy = td2.get(2).text();
            info.money = td2.get(3).text();
            info.type = td2.get(4).text();
            list.add(info);
        }
        return table.select("a").size() + 1;
    }
}

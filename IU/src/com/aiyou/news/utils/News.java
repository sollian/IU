
package com.aiyou.news.utils;

import java.io.Serializable;
import java.util.ArrayList;

public class News implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 0x1211;
    public static final String IS_FILE = "is_file";

    public enum NewsType {
        inform, news, headline;
    }

    public String title = "";
    public String url;
    public String date = "";
    public String from = "";
    public String content;

    public ArrayList<News> list;
}

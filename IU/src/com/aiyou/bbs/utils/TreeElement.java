
package com.aiyou.bbs.utils;

import java.util.ArrayList;

/**
 * BBS分区列表用到的bean
 * 
 * @author sollian
 */
public class TreeElement {
    public String mName;
    public String mDesc;
    public boolean mIsSection = false;
    public boolean mHasChild = false;
    public boolean mIsExpanded = false;
    public int mLevel = 0;
    public ArrayList<TreeElement> mChildList = new ArrayList<TreeElement>();

    /**
     * 添加子分区
     * 
     * @param c
     */
    public void addChild(TreeElement c) {
        mChildList.add(c);
        mHasChild = true;
        c.mLevel = this.mLevel + 1;
    }

    public TreeElement(String name, String description, boolean isSection) {
        mName = name;
        mDesc = description;
        mLevel = 0;
        mHasChild = false;
        mIsSection = isSection;
    }
}

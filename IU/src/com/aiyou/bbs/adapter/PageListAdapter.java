
package com.aiyou.bbs.adapter;

import java.util.List;

import com.aiyou.R;
import com.aiyou.utils.SwitchManager;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * BBSContentArticleActivity的pagedrawer的adapter
 * 
 * @author sollian
 */
public class PageListAdapter extends BaseAdapter {
    // 变量声明
    private LayoutInflater mInflater;
    private List<String> mList;
    private SwitchManager mSwitchMgr;

    // 构造函数
    public PageListAdapter(Context context, List<String> list) {
        mInflater = LayoutInflater.from(context);
        mList = list;
        mSwitchMgr = SwitchManager.getInstance(context);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_page, null);
            holder = new ViewHolder();
            holder.tv = (TextView) convertView.findViewById(R.id.page_litem_tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (mSwitchMgr.isNightModeEnabled()) {
            // 夜间模式
            holder.tv.setTextColor(Color.GRAY);
        } else {
            // 日间模式
            holder.tv.setTextColor(Color.BLACK);
        }
        holder.tv.setText("" + (position + 1));
        if (mList.get(position).equals("false")) {
            holder.tv.setTextSize(18);
            holder.tv.setBackgroundColor(Color.parseColor("#00000000"));
        } else {
            holder.tv.setTextSize(25);
            if (mSwitchMgr.isNightModeEnabled()) {
                holder.tv.setBackgroundColor(Color.parseColor("#aa00aaaa"));
            } else {
                holder.tv.setBackgroundColor(Color.parseColor("#aa000088"));
            }
        }
        return convertView;
    }

    private class ViewHolder {
        public TextView tv;
    }
}


package com.aiyou.news.adapter;

import java.util.List;

import com.aiyou.R;
import com.aiyou.news.utils.News;
import com.aiyou.utils.AiYouManager;
import com.aiyou.utils.SwitchManager;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 信息门户、北邮要闻的adapter类
 * 
 * @author sollian
 */
public class NewsListAdapter extends BaseAdapter {
    // 变量声明
    private LayoutInflater mInflater;
    private List<News> mList;
    private Context mContext;

    // 构造函数
    public NewsListAdapter(Context context,
            List<News> list) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mList = list;
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
            convertView = mInflater.inflate(R.layout.list_item_news, null);
            holder = new ViewHolder();
            holder.fl = (FrameLayout) convertView
                    .findViewById(R.id.news_litem_fl);
            holder.ll = (LinearLayout) convertView
                    .findViewById(R.id.news_litem_ll_custom);
            holder.tv_title = (TextView) convertView
                    .findViewById(R.id.news_litem_tv_title);
            holder.tv_from = (TextView) convertView
                    .findViewById(R.id.news_litem_tv_from);
            holder.tv_date = (TextView) convertView
                    .findViewById(R.id.news_litem_tv_date);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (position == 0) {
            holder.fl.setPadding(0, AiYouManager.getInstance(mContext).dip2px(50), 0, 0);
        } else {
            holder.fl.setPadding(0, 0, 0, 0);
        }
        if (SwitchManager.getInstance(mContext).isNightModeEnabled()) {
            holder.ll.setBackgroundColor(Color.parseColor("#222222"));
            holder.tv_title.setTextColor(Color.GRAY);
            holder.tv_from.setTextColor(Color.GRAY);
            holder.tv_date.setTextColor(Color.GRAY);
        } else {
            holder.ll.setBackgroundColor(Color.WHITE);
            holder.tv_title.setTextColor(Color.BLACK);
            holder.tv_from.setTextColor(Color.BLACK);
            holder.tv_date.setTextColor(Color.BLACK);
        }

        News news = mList.get(position);

        holder.tv_title.setText(Html.fromHtml(news.title));
        holder.tv_from.setText(news.from);
        holder.tv_date.setText(news.date);
        return convertView;
    }

    private class ViewHolder {
        public FrameLayout fl;
        public LinearLayout ll;
        public TextView tv_title;
        public TextView tv_from;
        public TextView tv_date;
    }
}

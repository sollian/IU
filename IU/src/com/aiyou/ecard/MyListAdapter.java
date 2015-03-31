
package com.aiyou.ecard;

import java.util.List;

import com.aiyou.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

class MyListAdapter extends BaseAdapter {
    private List<ConsumeInfo> mList;
    private LayoutInflater mInflater;

    public MyListAdapter(Context context, List<ConsumeInfo> list) {
        mList = list;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public ConsumeInfo getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.ecard_list_item, null);
            holder = new ViewHolder();
            holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
            holder.tv_description = (TextView) convertView.findViewById(R.id.tv_description);
            holder.tv_money_deal = (TextView) convertView.findViewById(R.id.tv_money_deal);
            holder.tv_money_remain = (TextView) convertView.findViewById(R.id.tv_money_remain);
            holder.tv_station = (TextView) convertView.findViewById(R.id.tv_station);
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            holder.ll = (LinearLayout) convertView.findViewById(R.id.ll);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (position % 2 == 1) {
            holder.ll.setBackgroundColor(0xffcccccc);
        } else {
            holder.ll.setBackgroundColor(0xffffffff);
        }
        ConsumeInfo info = mList.get(position);
        holder.tv_time.setText(info.time);
        holder.tv_description.setText(info.description);
        holder.tv_money_deal.setText(info.money_deal);
        holder.tv_money_remain.setText(info.money_remain);
        holder.tv_station.setText(info.station);
        holder.tv_name.setText(info.name);
        return convertView;
    }

    class ViewHolder {
        public TextView tv_time;
        public TextView tv_description;
        public TextView tv_money_deal;
        public TextView tv_money_remain;
        public TextView tv_station;
        public TextView tv_name;
        public LinearLayout ll;
    }
}

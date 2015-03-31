
package com.aiyou.electricity;

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

class ListAdapter1 extends BaseAdapter {
    private List<ConsumeInfo> mList;
    private LayoutInflater mInflater;

    public ListAdapter1(Context context, List<ConsumeInfo> list) {
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

    @SuppressLint("InflateParams") @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.electricity_vp_list_item1, null);
            holder = new ViewHolder();
            holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
            holder.tv_remain = (TextView) convertView.findViewById(R.id.tv_remain);
            holder.ll = (LinearLayout)convertView.findViewById(R.id.ll);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if(position % 2 == 1) {
            holder.ll.setBackgroundColor(0xffcccccc);
        } else {
            holder.ll.setBackgroundColor(0xffffffff);
        }
        ConsumeInfo info = mList.get(position);
        holder.tv_time.setText(info.time);
        holder.tv_remain.setText(info.remain);
        return convertView;
    }

    class ViewHolder {
        public TextView tv_time;
        public TextView tv_remain;
        public LinearLayout ll;
    }
}

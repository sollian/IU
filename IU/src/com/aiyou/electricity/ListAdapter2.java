
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

class ListAdapter2 extends BaseAdapter {
    private List<BuyEleInfo> mList;
    private LayoutInflater mInflater;

    public ListAdapter2(Context context, List<BuyEleInfo> list) {
        mList = list;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public BuyEleInfo getItem(int position) {
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
            convertView = mInflater.inflate(R.layout.electricity_vp_list_item2, null);
            holder = new ViewHolder();
            holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
            holder.tv_electricity = (TextView) convertView.findViewById(R.id.tv_electricity);
            holder.tv_money = (TextView) convertView.findViewById(R.id.tv_money);
            holder.tv_type = (TextView) convertView.findViewById(R.id.tv_type);
            holder.ll = (LinearLayout) convertView.findViewById(R.id.ll);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (position % 2 == 0) {
            holder.ll.setBackgroundColor(0xffcccccc);
        } else {
            holder.ll.setBackgroundColor(0xffffffff);
        }
        BuyEleInfo info = mList.get(position);
        holder.tv_time.setText(info.time);
        holder.tv_electricity.setText(info.buy);
        holder.tv_money.setText(info.money);
        holder.tv_type.setText(info.type);
        return convertView;
    }

    class ViewHolder {
        public TextView tv_time;
        public TextView tv_electricity;
        public TextView tv_money;
        public TextView tv_type;
        public LinearLayout ll;
    }
}

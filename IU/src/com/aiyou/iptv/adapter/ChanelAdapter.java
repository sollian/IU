package com.aiyou.iptv.adapter;

import java.util.List;

import com.aiyou.R;
import com.aiyou.iptv.bean.Chanel;
import com.aiyou.utils.SwitchManager;

import external.smartimageview.SmartImageView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChanelAdapter extends BaseAdapter {
    private List<Chanel> mList;
    private LayoutInflater mInflater;
    private Context mContext;

    public ChanelAdapter(Context context, List<Chanel> list) {
        mContext = context;
        mList = list;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Chanel getItem(int position) {
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
            convertView = mInflater.inflate(R.layout.item_chanel, null);
            holder = new ViewHolder();
            holder.ll = (LinearLayout) convertView.findViewById(R.id.ll);
            holder.siv_logo = (SmartImageView) convertView
                    .findViewById(R.id.siv_logo);
            holder.tv_title = (TextView) convertView
                    .findViewById(R.id.tv_title);
            holder.tv_frequency = (TextView) convertView
                    .findViewById(R.id.tv_frequency);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Chanel chanel = mList.get(position);
        if (!TextUtils.isEmpty(chanel.logo)) {
            holder.siv_logo.setImageUrl(chanel.logo, R.drawable.default_logo,
                    R.drawable.default_logo);
        } else {
            holder.siv_logo.setImageResource(R.drawable.default_logo);
        }
        if (!TextUtils.isEmpty(chanel.name)) {
            holder.tv_title.setText(chanel.name);
        } else {
            holder.tv_title.setText("");
        }
        holder.tv_frequency.setText("观看" + chanel.frequency + "次");

        if (SwitchManager.getInstance(mContext).isNightModeEnabled()) {
            holder.ll.setBackgroundColor(0xff222222);
            holder.tv_title.setTextColor(0xff888888);
        } else {
            holder.ll.setBackgroundColor(0xffffffff);
            holder.tv_title.setTextColor(0xff000000);
        }
        return convertView;
    }

    class ViewHolder {
        public LinearLayout ll;
        public SmartImageView siv_logo;
        public TextView tv_title;
        public TextView tv_frequency;
    }

}


package com.aiyou.map.adapter;

import java.util.List;

import com.aiyou.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * 地图显示不同地点的spinner的adapter
 * 
 * @author sollian
 */
public class MySpinnerAdapter extends BaseAdapter {

    private List<String> mList;
    private LayoutInflater mInflater;
    private String mColor;

    public MySpinnerAdapter(Context context, List<String> list,
            String color) {
        this.mList = list;
        this.mInflater = LayoutInflater.from(context);
        this.mColor = color;
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
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_sp, null);
        }
        TextView tv = (TextView) convertView
                .findViewById(R.id.sp_litem_tv_department);
        tv.setText(mList.get(position));
        convertView.setBackgroundColor(Color.parseColor(mColor));
        return convertView;
    }

}


package com.aiyou.bbs.adapter;

import java.util.List;

import com.aiyou.R;
import com.aiyou.bbs.bean.Board;
import com.aiyou.view.DarkImageView;
import com.aiyou.view.ScrollTextView;

import external.OtherView.Win8ProgressBar;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class FavoriteAdapter extends BaseAdapter {
    private Context mContext;
    private List<Board> mList;
    private LayoutInflater mInflater;
    private DeleteFavoriteListener mListener;

    public FavoriteAdapter(Context context, List<Board> list, DeleteFavoriteListener listener) {
        mContext = context;
        mList = list;
        mListener = listener;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Board getItem(int position) {
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
            convertView = mInflater.inflate(R.layout.list_item_favorite, null);
            holder = new ViewHolder();
            holder.stv = (ScrollTextView) convertView.findViewById(R.id.stv);
            holder.div = (DarkImageView) convertView.findViewById(R.id.div);
            holder.wpb = (Win8ProgressBar) convertView.findViewById(R.id.progress_bar);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Board board = mList.get(position);
        holder.stv.setText(board.description);
        holder.div.setTag(board);
        holder.div.setVisibility(View.VISIBLE);
        if (holder.wpb.isStart()) {
            holder.wpb.stop();
        }
        holder.wpb.setVisibility(View.GONE);
        final Win8ProgressBar pb = holder.wpb;
        holder.div.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Board board = (Board) v.getTag();
                if (mListener != null) {
                    mListener.onDelete(board);
                    v.setVisibility(View.GONE);
                    pb.setVisibility(View.VISIBLE);
                    pb.start();
                }
            }
        });
        return convertView;
    }

    class ViewHolder {
        public ScrollTextView stv;
        public DarkImageView div;
        public Win8ProgressBar wpb;
    }

    public interface DeleteFavoriteListener {
        public void onDelete(Board board);
    }
}

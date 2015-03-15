
package com.aiyou.bbs.adapter;

import java.util.List;

import com.aiyou.R;
import com.aiyou.bbs.bean.Board;
import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.bbs.utils.TreeElement;
import com.aiyou.utils.AiYouManager;
import com.aiyou.utils.SwitchManager;
import com.aiyou.view.DarkImageView;

import external.OtherView.Win8ProgressBar;
import external.shimmer.ShimmerTextView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * BBS分区列表的adapter
 * 
 * @author solli_000
 */
public class TreeViewAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<TreeElement> mList;
    private AiYouManager mIUMgr;
    private SwitchManager mSwitchMgr;
    private SelectFavoriteListener mListener;

    public TreeViewAdapter(Context context, List<TreeElement> list,
            SelectFavoriteListener listener) {
        mInflater = LayoutInflater.from(context);
        mList = list;
        mListener = listener;
        mIUMgr = AiYouManager.getInstance(context);
        mSwitchMgr = SwitchManager.getInstance(context);
    }

    public int getCount() {
        return mList.size();
    }

    public Object getItem(int position) {
        return mList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("NewApi")
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.list_item_treeview, null);
            holder.iv1 = (ImageView) convertView
                    .findViewById(R.id.list_item_treeview_iv_1);
            holder.iv2 = (ImageView) convertView
                    .findViewById(R.id.list_item_treeview_iv_2);
            holder.iv3 = (ImageView) convertView
                    .findViewById(R.id.list_item_treeview_iv_3);
            holder.iv4 = (ImageView) convertView
                    .findViewById(R.id.list_item_treeview_iv_4);
            holder.iv5 = (ImageView) convertView
                    .findViewById(R.id.list_item_treeview_iv_5);
            holder.shtv = (ShimmerTextView) convertView
                    .findViewById(R.id.list_item_treeview_shtv);
            holder.div = (DarkImageView) convertView.findViewById(R.id.div);
            holder.wpb = (Win8ProgressBar)convertView.findViewById(R.id.list_item_treeview_progress_bar);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (mSwitchMgr.isNightModeEnabled()) {
            // 夜间模式
            holder.iv1.setImageResource(R.drawable.directory_divider_night);
            holder.iv3.setImageResource(R.drawable.directory_divider_night);
            holder.iv4.setImageResource(R.drawable.directory_divider_night);
            holder.iv5.setImageResource(R.drawable.directory_divider_night);
        } else {
            // 日间模式
            holder.iv1.setImageResource(R.drawable.directory_divider_day);
            holder.iv3.setImageResource(R.drawable.directory_divider_day);
            holder.iv4.setImageResource(R.drawable.directory_divider_day);
            holder.iv5.setImageResource(R.drawable.directory_divider_day);
        }

        final TreeElement obj = mList.get(position);

        holder.shtv.setText(obj.mDesc);
        if (obj.mIsSection) {
            if (mSwitchMgr.isNightModeEnabled()) {
                holder.shtv.setTextColor(Color.parseColor("#00aaaa"));
            } else {
                holder.shtv.setTextColor(Color.BLUE);
            }
            TextPaint tp = holder.shtv.getPaint();
            tp.setFakeBoldText(true);
        } else {
            if (mSwitchMgr.isNightModeEnabled()) {
                holder.shtv.setTextColor(Color.GRAY);
            } else {
                holder.shtv.setTextColor(Color.BLACK);
            }
            TextPaint tp = holder.shtv.getPaint();
            tp.setFakeBoldText(false);
        }

        int margin = mIUMgr.dip2px(20);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                mIUMgr.dip2px(2), mIUMgr.dip2px(50));
        params.leftMargin = margin * obj.mLevel;
        holder.iv1.setLayoutParams(params);

        if (obj.mLevel == 0) {
            holder.iv3.setVisibility(View.GONE);
            holder.iv4.setVisibility(View.GONE);
            holder.iv5.setVisibility(View.GONE);
        } else if (obj.mLevel == 1) {
            holder.iv3.setVisibility(View.VISIBLE);
            holder.iv4.setVisibility(View.GONE);
            holder.iv5.setVisibility(View.GONE);
        } else if (obj.mLevel == 2) {
            holder.iv3.setVisibility(View.VISIBLE);
            holder.iv4.setVisibility(View.VISIBLE);
            holder.iv5.setVisibility(View.GONE);
        } else {
            holder.iv3.setVisibility(View.VISIBLE);
            holder.iv4.setVisibility(View.VISIBLE);
            holder.iv5.setVisibility(View.VISIBLE);
        }

        if (!obj.mIsSection) {
            if (mSwitchMgr.isNightModeEnabled()) {
                holder.iv2.setImageResource(R.drawable.divider_white_h_night);
            } else {
                holder.iv2.setImageResource(R.drawable.divider_white_h_day);
            }
            if (Build.VERSION.SDK_INT >= 11) {
                holder.iv2.setRotation(0);
            }
        } else {
            if (mSwitchMgr.isNightModeEnabled()) {
                // 夜间模式
                if (Build.VERSION.SDK_INT >= 11) {
                    holder.iv2.setImageResource(R.drawable.back_night);
                } else {
                    holder.iv2.setImageResource(R.drawable.divider_white_h_night);
                }
            } else {
                // 日间模式
                if (Build.VERSION.SDK_INT >= 11) {
                    holder.iv2.setImageResource(R.drawable.back_day);
                } else {
                    holder.iv2.setImageResource(R.drawable.divider_white_h_day);
                }
            }
            if (Build.VERSION.SDK_INT >= 11) {
                if (obj.mIsExpanded) {
                    holder.iv2.setRotation(-90);
                } else {
                    holder.iv2.setRotation(180);
                }
            }
        }
        if(holder.wpb.isStart()) {
            holder.wpb.stop();
        }
        holder.wpb.setVisibility(View.GONE);
        if (obj.mIsSection) {
            holder.div.setVisibility(View.GONE);
        } else {
            holder.div.setVisibility(View.VISIBLE);
            if (BBSManager.checkFavorite(obj.mName)) {
                holder.div.setImageResource(R.drawable.collect);
            } else {
                holder.div.setImageResource(R.drawable.collect1);
            }
            Board board = new Board();
            board.name = obj.mName;
            board.description = obj.mDesc;
            holder.div.setTag(board);
            final Win8ProgressBar pb = holder.wpb;
            holder.div.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Board board = (Board) v.getTag();
                    mListener.onSelectFavorite(board);
                    v.setVisibility(View.GONE);
                    pb.setVisibility(View.VISIBLE);
                    pb.start();
                }
            });
        }

        return convertView;
    }

    class ViewHolder {
        public ImageView iv1;
        public ImageView iv2;
        public ImageView iv3;
        public ImageView iv4;
        public ImageView iv5;
        public ShimmerTextView shtv;
        public DarkImageView div;
        public Win8ProgressBar wpb;
    }

    public interface SelectFavoriteListener {
        public void onSelectFavorite(Board board);
    }
}

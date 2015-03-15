
package com.aiyou.bbs.adapter;

import java.util.List;

import com.aiyou.R;
import com.aiyou.bbs.BBSUserInfoActivity;
import com.aiyou.bbs.bean.User;
import com.aiyou.bbs.bean.helper.AdapterInterface;
import com.aiyou.bbs.utils.BBSListHelper;
import com.aiyou.bbs.utils.BBSListHelper.BeanType;
import com.aiyou.utils.ActivityFunc;
import com.aiyou.utils.AiYouManager;
import com.aiyou.utils.SwitchManager;
import com.aiyou.view.DarkImageView;

import external.ArcMenu.RayMenu;
import external.OtherView.CircleImageView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class BBSListAdapter extends BaseAdapter {
    private Context mContext;
    private List<AdapterInterface> mList;
    private LayoutInflater mInflater;
    private OnRayMenuClickListener mListener;
    private BBSListHelper mMember;
    private AiYouManager mIUMgr;
    private SwitchManager mSwitchMgr;

    public BBSListAdapter(Context context, List<AdapterInterface> list,
            OnRayMenuClickListener listener) {
        mContext = context;
        mList = list;
        mInflater = LayoutInflater.from(context);
        mListener = listener;
        mMember = BBSListHelper.getInstance();
        getTag();
        mIUMgr = AiYouManager.getInstance(mContext);
        mSwitchMgr = SwitchManager.getInstance(mContext);
    }

    private BeanType getTag() {
        return mMember.beanType;
    }

    @Override
    public void notifyDataSetChanged() {
        getTag();
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public AdapterInterface getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("NewApi")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
            if (holder.tag != getTag()
                    && (holder.tag == BeanType.VOTELIST || getTag() == BeanType.VOTELIST)) {
                convertView = null;
            }
        }
        if (convertView == null) {
            holder = new ViewHolder();
            holder.tag = getTag();
            if (holder.tag == BeanType.VOTELIST) {
                convertView = mInflater.inflate(
                        R.layout.list_item_bbs_list_vote, null);
                holder.fl = (FrameLayout) convertView
                        .findViewById(R.id.list_item_bbslist_vote_fl);
                holder.ll = (LinearLayout) convertView
                        .findViewById(R.id.list_item_bbslist_vote_ll);
                holder.civ_face = (CircleImageView) convertView
                        .findViewById(R.id.list_item_bbslist_vote_civ_face);
                holder.tv_title = (TextView) convertView
                        .findViewById(R.id.list_item_bbslist_vote_tv_title);
                holder.tv_author = (TextView) convertView
                        .findViewById(R.id.list_item_bbslist_vote_tv_author);
                holder.tv_date = (TextView) convertView
                        .findViewById(R.id.list_item_bbslist_vote_tv_date);
                holder.tv_count = (TextView) convertView
                        .findViewById(R.id.list_item_bbslist_vote_tv_count);
            } else {
                convertView = mInflater.inflate(
                        R.layout.list_item_bbs_list_board, null);
                holder.fl = (FrameLayout) convertView
                        .findViewById(R.id.list_item_bbslist_board_fl);
                holder.ll = (LinearLayout) convertView
                        .findViewById(R.id.list_item_bbslist_board_ll);
                holder.civ_face = (CircleImageView) convertView
                        .findViewById(R.id.list_item_bbslist_board_civ_face);
                holder.tv_title = (TextView) convertView
                        .findViewById(R.id.list_item_bbslist_board_tv_title);
                holder.tv_author = (TextView) convertView
                        .findViewById(R.id.list_item_bbslist_board_tv_author);
                holder.tv_date = (TextView) convertView
                        .findViewById(R.id.list_item_bbslist_board_tv_date);
                holder.rm = (RayMenu) convertView
                        .findViewById(R.id.list_item_bbslist_board_rm);
            }
            convertView.setTag(holder);
        }
        if (position == 0) {
            // 为第一个item设置50dp的PaddingTop
            holder.fl.setPadding(0, mIUMgr.dip2px(50), 0, 0);
        } else {
            holder.fl.setPadding(0, 0, 0, 0);
        }
        if (mSwitchMgr.isNightModeEnabled()) {
            holder.ll.setBackgroundResource(R.drawable.background_list_night);
            holder.tv_title.setTextColor(mContext.getResources().getColor(
                    R.color.font_night));
            holder.tv_author.setTextColor(mContext.getResources().getColor(
                    R.color.night_author));
        } else {
            holder.ll.setBackgroundResource(R.drawable.background_list_day);
            holder.civ_face.setBorderColor(Color.WHITE);
            holder.tv_title.setTextColor(mContext.getResources().getColor(
                    R.color.font_black_day));
            holder.tv_author.setTextColor(Color.BLUE);
        }

        final AdapterInterface adapterInterface = mList.get(position);

        final User user = adapterInterface.getUser();
        /**
         * 设置用户信息
         */
        if (mSwitchMgr.isFaceEnabled()) {
            // 头像
            if (user != null && null != user.face_url) {
                holder.civ_face
                        .setImageUrl(user.face_url,
                                R.drawable.iu_default_gray,
                                R.drawable.iu_default_green);
            } else {
                holder.civ_face.setImageResource(R.drawable.iu_default_green);
            }
            if (null != user && user.id != null) {
                holder.civ_face.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if ("原帖已删除".equals(user.id)) {
                            Toast.makeText(mContext, "用户不存在", Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }
                        ActivityFunc.startActivity((Activity) mContext,
                                BBSUserInfoActivity.class,
                                user, false);
                    }
                });
            } else {
                holder.civ_face.setOnClickListener(null);
            }
        } else {
            holder.civ_face.setVisibility(View.GONE);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, mIUMgr.dip2px(100));
            params.leftMargin = 0;
            holder.ll.setLayoutParams(params);
            holder.ll.setPadding(mIUMgr.dip2px(5), 0, mIUMgr.dip2px(5), 0);
        }

        // 用户ID
        if (user != null && null != user.id) {
            holder.tv_author.setText(adapterInterface.getUser().id);
        } else {
            holder.tv_author.setText("获取用户失败");
        }
        /**
         * 设置标题
         */
        final String title = adapterInterface.getTitle();
        if (null != title) {
            holder.tv_title.setText(title);
        } else {
            holder.tv_title.setText("获取标题失败");
        }
        final int color = adapterInterface.getTitleColor();
        if (-1 != color) {
            holder.tv_title.setTextColor(color);
        }
        /**
         * 设置发布时间
         */
        final String date = adapterInterface.getDate();
        if (null != date) {
            holder.tv_date.setText(date);
        } else {
            holder.tv_date.setText("获取时间失败");
        }

        if (getTag() == BeanType.VOTELIST) {
            /**
             * 设置投票人数
             */
            final int count = adapterInterface.getCount();
            if (-1 != count) {
                holder.tv_count.setText(count + "");
            } else {
                holder.tv_count.setText("");
            }
        } else {
            /**
             * 设置RayMenu
             */
            // 重置
            holder.rm.reset();
            int rmItem;
            if (getTag() == BeanType.BOARD
                    || getTag() == BeanType.WIDGET) {
                rmItem = R.drawable.main_collect;
            } else {
                rmItem = R.drawable.main_clear;
            }
            String strTag;
            DarkImageView item = new DarkImageView(mContext);
            strTag = Integer.toString(position);
            item.setTag(strTag);
            if (Build.VERSION.SDK_INT >= 11) {
                item.setRotationY(180);
            }
            item.setImageResource(rmItem);
            holder.rm.addItem(item, new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onRayMenuClick(
                            Integer.parseInt(v.getTag().toString()));
                }
            });
        }

        return convertView;
    }

    private class ViewHolder {
        public FrameLayout fl;
        public LinearLayout ll;
        public CircleImageView civ_face;
        public TextView tv_title;
        public TextView tv_author;
        public TextView tv_date;

        public TextView tv_count;
        public RayMenu rm;
        public BeanType tag;
    }

    public interface OnRayMenuClickListener {
        public void onRayMenuClick(int position);
    }
}

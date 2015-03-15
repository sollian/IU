
package com.aiyou.bbs;

import java.util.ArrayList;
import java.util.List;

import com.aiyou.BaseActivity;
import com.aiyou.R;
import com.aiyou.bbs.adapter.TreeViewAdapter;
import com.aiyou.bbs.adapter.TreeViewAdapter.SelectFavoriteListener;
import com.aiyou.bbs.bean.Board;
import com.aiyou.bbs.bean.Favorite;
import com.aiyou.bbs.bean.Section;
import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.bbs.utils.TreeElement;
import com.aiyou.utils.ActivityFunc;
import com.aiyou.utils.JsonHelper;
import com.aiyou.utils.NetWorkManager;
import com.aiyou.utils.SwitchManager;
import com.aiyou.utils.thread.ThreadUtils;

import external.PullToRefresh.PullToRefreshBase;
import external.PullToRefresh.PullToRefreshListView;
import external.PullToRefresh.PullToRefreshBase.Mode;
import external.PullToRefresh.PullToRefreshBase.OnRefreshListener2;
import external.shimmer.Shimmer;
import external.shimmer.ShimmerTextView;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 分区列表
 * 
 * @author sollian
 */
public class BBSSectionActivity extends BaseActivity implements
        OnItemClickListener, OnRefreshListener2<ListView>, SelectFavoriteListener {
    public static List<TreeElement> mTreeListElements = new ArrayList<TreeElement>();

    public static final String KEY_NAME = "name";
    public static final String KEY_DESC = "desc";

    private static final int MSG_GET_SECTION = 0;
    private static final int MSG_FAVORITE = 1;
    private static final int MSG_ERROR = -1;

    private static final String KEY_DATA = "data";
    protected static final String KEY_INFO = "info";

    private TreeViewAdapter mAdapter;
    private int mSectionPostion = 0;
    /**
     * 用户id扫光特效
     */
    private Shimmer mShimmer;
    /**
     * 控件
     */
    private PullToRefreshListView mPTRListView;
    private ListView mListView;

    private Handler mHandler = new Handler(new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            if (MSG_GET_SECTION == msg.what) {
                Bundle data = msg.getData();
                Section section = (Section) data.getSerializable(KEY_DATA);
                data.clear();
                updateTreeElements(section);
            } else if (msg.what == MSG_FAVORITE) {
                Bundle data = msg.getData();
                String strJson = data.getString(KEY_DATA);
                String strInfo = data.getString(KEY_INFO);
                data.clear();
                Favorite.mFavorite = new Favorite(strJson);
                mAdapter.notifyDataSetChanged();
                if (!TextUtils.isEmpty(strInfo)) {
                    Toast.makeText(getBaseContext(), strInfo, Toast.LENGTH_SHORT)
                            .show();
                }
            } else if (MSG_ERROR == msg.what) {
                mAdapter.notifyDataSetChanged();
                Bundle data = msg.getData();
                String strError = data.getString(KEY_DATA);
                data.clear();
                if (TextUtils.isEmpty(strError)) {
                    strError = NetWorkManager.MSG_NONET;
                }
                Toast.makeText(getBaseContext(), strError, Toast.LENGTH_SHORT)
                        .show();
                if (Build.VERSION.SDK_INT > 11) {
                    /**
                     * 停止扫光
                     */
                    if (null != mShimmer && mShimmer.isAnimating()) {
                        mShimmer.cancel();
                    }
                }
                mListView.setEnabled(true);
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SwitchManager.getInstance(getBaseContext()).isNightModeEnabled()) {
            // 夜间模式
            this.setTheme(R.style.ThemeNight);
        } else {
            // 日间模式
            this.setTheme(R.style.ThemeDay);
        }
        setContentView(R.layout.activity_bbs_section);
        init();
    }

    @SuppressWarnings("deprecation")
    private void init() {
        /**
         * 设置背景
         */
        LinearLayout ll = (LinearLayout) findViewById(R.id.activity_bbssection_ll_section);
        Bitmap bmp = (Bitmap) (getIntent().getParcelableExtra(ActivityFunc.KEY_BACKGROUND));
        if (null != bmp) {
            Drawable drawable = new BitmapDrawable(bmp);
            ll.setBackgroundDrawable(drawable);
        } else {
            if (SwitchManager.getInstance(getBaseContext()).isNightModeEnabled()) {
                ll.setBackgroundColor(getResources().getColor(
                        R.color.bbs_background_night));
            } else {
                ll.setBackgroundColor(getResources().getColor(
                        R.color.bbs_background_day));
            }
        }
        /**
         * 列表
         */
        mPTRListView = (PullToRefreshListView) findViewById(R.id.activity_bbssection_lv_section);
        mPTRListView.setOnRefreshListener(this);
        mPTRListView.setShowIndicator(false);
        mListView = mPTRListView.getRefreshableView();
        // 隐藏footer、header
        hideHeaderFooter();
        /**
         * list适配器
         */
        if (mTreeListElements.isEmpty()) {
            BBSManager.initTreeViewData(getBaseContext(), mTreeListElements);
        }
        mAdapter = new TreeViewAdapter(this, mTreeListElements, this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        if (!mTreeListElements.get(position - 1).mIsSection) {
            /**
             * 选择一个版面浏览后禁用lv，防止逻辑混乱
             */
            mListView.setEnabled(false);
            // 获取版面name，返回给BBSListActivity
            TreeElement element = (TreeElement) parent
                    .getItemAtPosition(position);
            /**
             * 设置返回数据
             */
            // 使用Intent返回数据
            Intent intent = new Intent();
            // 把返回数据存入Intent
            intent.putExtra(KEY_NAME, element.mName);
            intent.putExtra(KEY_DESC, element.mDesc);
            // 设置返回数据
            setResult(RESULT_OK, intent);
            selfFinish(null);
            return;
        }
        // 修正position
        position = position - 1;
        if (mTreeListElements.get(position).mIsExpanded) {
            mTreeListElements.get(position).mIsExpanded = false;
            TreeElement element = mTreeListElements.get(position);
            ArrayList<TreeElement> temp = new ArrayList<TreeElement>();
            int len = mTreeListElements.size();
            for (int i = position + 1; i < len; i++) {
                if (element.mLevel >= mTreeListElements.get(i).mLevel) {
                    break;
                }
                temp.add(mTreeListElements.get(i));
            }
            mTreeListElements.removeAll(temp);
            mAdapter.notifyDataSetChanged();
        } else {
            TreeElement tempElement = mTreeListElements.get(position);
            tempElement.mIsExpanded = true;
            mSectionPostion = position;
            if (tempElement.mHasChild) {
                updateTreeElements(null);
            } else {
                /**
                 * 禁用listview
                 */
                mListView.setEnabled(false);
                if (Build.VERSION.SDK_INT > 11) {
                    ShimmerTextView shtv = (ShimmerTextView) view
                            .findViewById(R.id.list_item_treeview_shtv);
                    /**
                     * 开启扫光
                     */
                    if (null != mShimmer && mShimmer.isAnimating()) {
                        mShimmer.cancel();
                    }
                    mShimmer = new Shimmer();
                    mShimmer.setDuration(800);
                    mShimmer.start(shtv);
                }
                /**
                 * 开启获取section线程
                 */
                threadSection(tempElement.mName);
            }
        }
    }

    private void threadSection(final String section) {
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            if (null != mHandler) {
                mHandler.sendEmptyMessage(MSG_ERROR);
            }
            return;
        }
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                String strJson = Section.getSection(BBSSectionActivity.this, section);
                if (TextUtils.isEmpty(strJson)) {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(MSG_ERROR);
                    }
                    return;
                }
                String strError = JsonHelper.checkError(strJson);
                if (null != strError) {
                    // 是 错误信息
                    if (mHandler != null) {
                        Message msg = mHandler.obtainMessage(MSG_ERROR);
                        Bundle data = msg.getData();
                        data.putString(KEY_DATA, strError);
                        mHandler.sendMessage(msg);
                    }
                    return;
                }
                // 不是 错误信息
                Section section = new Section(strJson);
                if (section != null && null != mHandler) {
                    Message msg = mHandler.obtainMessage(MSG_GET_SECTION);
                    Bundle data = msg.getData();
                    data.putSerializable(KEY_DATA, section);
                    mHandler.sendMessage(msg);
                }
            }
        });
    }

    /**
     * 更新treeListElements,调用之前应该先更新nSectionPostion
     * 
     * @param position
     */
    private void updateTreeElements(Section section) {
        TreeElement tempElement = mTreeListElements.get(mSectionPostion);
        // 添加新数据
        if (null != section) {
            // 更新description
            if (null != section.description) {
                tempElement.mDesc = section.description;
            }
            if (null != section.sub_sections) {
                for (int i = 0; i < section.sub_sections.length; i++) {
                    TreeElement ele = new TreeElement(section.sub_sections[i],
                            section.sub_sections[i], true);
                    tempElement.addChild(ele);
                }
            }
            if (null != section.boards) {
                for (int i = 0; i < section.boards.length; i++) {
                    TreeElement ele = new TreeElement(section.boards[i].name,
                            section.boards[i].description, false);
                    tempElement.addChild(ele);
                }
            }
        }
        if (tempElement.mLevel == 0) {
            // 折叠所有项
            boolean flag = true;
            ArrayList<TreeElement> tempList = new ArrayList<TreeElement>();
            for (TreeElement element : mTreeListElements) {
                if (element.mName.equals(tempElement.mName)) {
                    flag = false;
                    continue;
                }
                if (element.mLevel == 0) {
                    element.mIsExpanded = false;
                } else {
                    tempList.add(element);
                    if (flag) {
                        mSectionPostion--;
                    }
                }
            }
            mTreeListElements.removeAll(tempList);
            tempList.clear();
            tempList = null;
        }
        // 显示展开项
        int position = mSectionPostion;
        for (TreeElement element : tempElement.mChildList) {
            element.mLevel = tempElement.mLevel + 1;
            element.mIsExpanded = false;
            mTreeListElements.add(++position, element);
        }
        mAdapter.notifyDataSetChanged();
        mListView.setSelection(mSectionPostion);
        /**
         * 停止扫光
         */
        if (null != mShimmer && mShimmer.isAnimating()) {
            mShimmer.cancel();
        }
        /**
         * 设置listview可用
         */
        mListView.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        if (Build.VERSION.SDK_INT > 11) {
            if (null != mShimmer && mShimmer.isAnimating()) {
                mShimmer.cancel();
            }
        }
        mShimmer = null;
        System.gc();
    }

    public void selfFinish(View view) {
        scrollToFinishActivity();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 按下键盘上返回按钮
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            selfFinish(null);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        completeRefresh();
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        completeRefresh();
    }

    private void completeRefresh() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mPTRListView.onRefreshComplete();
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void hideHeaderFooter() {
        mPTRListView.setPullLabel("", Mode.BOTH);
        mPTRListView.setRefreshingLabel("", Mode.BOTH);
        mPTRListView.setReleaseLabel("", Mode.BOTH);
        mPTRListView.setLoadingDrawable(null, Mode.BOTH);
    }

    @Override
    public void onSelectFavorite(Board board) {
        boolean flag = BBSManager.checkFavorite(board.name);
        if (flag) {
            // 删除收藏
            threadDeleteFavorite(board);
        } else {
            // 添加收藏
            threadAddFavorite(board);
        }
    }

    private void threadDeleteFavorite(final Board board) {
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            return;
        }
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                String strJson = Favorite.deleteFavorite(BBSSectionActivity.this, 0, board.name);
                if (TextUtils.isEmpty(strJson)) {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(MSG_ERROR);
                    }
                    return;
                }
                String strError = JsonHelper.checkError(strJson);
                if (strError != null) {
                    if (mHandler != null) {
                        Message msg = mHandler.obtainMessage(MSG_ERROR);
                        Bundle data = msg.getData();
                        data.putString(KEY_DATA, strError);
                        mHandler.sendMessage(msg);
                    }
                    return;
                }
                if (mHandler != null) {
                    Message msg = mHandler.obtainMessage(MSG_FAVORITE);
                    Bundle data = msg.getData();
                    data.putString(KEY_DATA, strJson);
                    mHandler.sendMessage(msg);
                }
            }
        });
    }

    private void threadAddFavorite(final Board board) {
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            return;
        }
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                String strJson = Favorite.addFavorite(BBSSectionActivity.this, 0, board.name);
                if (TextUtils.isEmpty(strJson)) {
                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(MSG_ERROR);
                    }
                    return;
                }
                String strError = JsonHelper.checkError(strJson);
                if (strError != null) {
                    if (mHandler != null) {
                        Message msg = mHandler.obtainMessage(MSG_ERROR);
                        Bundle data = msg.getData();
                        data.putString(KEY_DATA, strError);
                        mHandler.sendMessage(msg);
                    }
                    return;
                }
                if (mHandler != null) {
                    Message msg = mHandler.obtainMessage(MSG_FAVORITE);
                    Bundle data = msg.getData();
                    data.putString(KEY_DATA, strJson);
                    data.putString(KEY_INFO, "已收藏 " + board.description);
                    mHandler.sendMessage(msg);
                }
            }
        });
    }
}

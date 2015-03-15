
package com.aiyou.bbs;

import java.util.Random;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.aiyou.BaseActivity;
import com.aiyou.R;
import com.aiyou.bbs.bean.Article;
import com.aiyou.bbs.bean.Vote;
import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.utils.ActivityFunc;
import com.aiyou.utils.AiYouManager;
import com.aiyou.utils.JsonHelper;
import com.aiyou.utils.NetWorkManager;
import com.aiyou.utils.SwitchManager;
import com.aiyou.utils.thread.ThreadUtils;
import com.aiyou.utils.time.TimeUtils;
import com.aiyou.view.ScrollTextView;

import external.OtherView.CircleImageView;
import external.OtherView.Win8ProgressBar;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 投票内容
 * 
 * @author sollian
 */
public class BBSVoteActivity extends BaseActivity {
    public static final String KEY_VOTE = "vote";

    private static final int MSG_VOTE = 0;
    private static final int MSG_VOTE_DESCRIPTION = 1;
    private static final int MSG_ERROR = -1;
    private static final String KEY_DATA = "data";

    private SwitchManager mSwitchMgr;
    private AiYouManager mIUMgr;
    /**
     * 存放服务器返回的json数据
     */
    private String mDescription = null;
    /**
     * 投票元数据
     */
    private Vote mVote;
    /**
     * 控件
     */
    private ScrollTextView mTitleSTV;
    private CircleImageView mFaceCIV;
    private TextView mAuthorTV, mDateTV, mVotedTV;
    private TextView mChoiceTV;
    private TextView mDescTV;
    private LinearLayout mContentLLayout;
    // 进度条
    private FrameLayout mProgressFLayout;
    private Win8ProgressBar mProgressBar;

    private Handler mHandler = new Handler(new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            if (MSG_VOTE == msg.what) {
                if (mVote != null) {
                    showContent();
                }
            } else if (MSG_VOTE_DESCRIPTION == msg.what) {
                if (TextUtils.isEmpty(mDescription)) {
                    mDescTV.setVisibility(View.GONE);
                } else {
                    mDescTV.setText(mDescription);
                    mDescTV.setVisibility(View.VISIBLE);
                }
            } else if (MSG_ERROR == msg.what) {
                Bundle data = msg.getData();
                String strError = data.getString(KEY_DATA);
                data.clear();
                if (TextUtils.isEmpty(strError)) {
                    strError = NetWorkManager.MSG_NONET;
                }
                // 连接服务器失败
                Toast.makeText(getBaseContext(), strError, Toast.LENGTH_SHORT)
                        .show();
            }
            showProgress(false);
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSwitchMgr = SwitchManager.getInstance(getBaseContext());
        if (mSwitchMgr.isNightModeEnabled()) {
            // 夜间模式
            this.setTheme(R.style.ThemeNight);
        } else {
            // 日间模式
            this.setTheme(R.style.ThemeDay);
        }
        setContentView(R.layout.activity_bbs_vote);

        init();

        Intent intent = getIntent();
        mVote = (Vote) intent.getSerializableExtra(KEY_VOTE);

        mTitleSTV.setText(mVote.title);

        startThread(false);
        threadGetDescription();
    }

    /**
     * 开启线程的方法
     * 
     * @param flagSend true——投票；false——获取vote内容
     */
    private void startThread(boolean flagSend) {
        showProgress(true);
        if (flagSend) {
            // 进行投票
            voteThread();
        } else {
            // 获取投票内容
            threadGetVote();
        }
    }

    /**
     * 显示投票内容
     */
    private void showContent() {
        mContentLLayout.removeAllViews();
        /**
         * 设置头像
         */
        if (mSwitchMgr.isFaceEnabled()) {
            if (null != mVote.user) {
                // 头像
                if (null != mVote.user.face_url) {
                    mFaceCIV.setImageUrl(mVote.user.face_url);
                } else {
                    mFaceCIV.setImageResource(R.drawable.iu_default_green);
                }
                mFaceCIV.setTag(mVote.user);
            }
        } else {
            mFaceCIV.setVisibility(View.GONE);
        }
        /**
         * 设置用户ID
         */
        if (mVote.user != null) {
            mAuthorTV.setText(mVote.user.id);
        }
        if (SwitchManager.getInstance(getBaseContext()).isNightModeEnabled()) {
            mAuthorTV.setTextColor(Color.parseColor("#00aaaa"));
        } else {
            mAuthorTV.setTextColor(Color.BLUE);
        }
        /**
         * 时间
         */
        if (mVote.is_end) {
            mDateTV.setText("已截止");
            mDateTV.setTextColor(Color.RED);
        } else {
            mDateTV.setText(TimeUtils.getLocalTime(mVote.end) + "截止");
            mDateTV.setTextColor(Color.GRAY);
        }
        /**
         * 是否已投过了
         */
        if (mVote.voted == null) {
            mVotedTV.setText("未投");
            mVotedTV.setTextColor(Color.GRAY);
        } else {
            mVotedTV.setText("已投");
            mVotedTV.setTextColor(Color.parseColor("#008800"));
        }
        /**
         * 多选单选
         */
        String str = "";
        if (mVote.is_result_voted) {
            str += "投票查看结果  ";
        }
        int nItem = 1;
        if (mVote.type == 1) {
            // 多选
            if (mVote.limit != 0) {
                nItem = mVote.limit;
                str += "最多可选" + mVote.limit + "项";
            } else {
                nItem = mVote.options.length;
                str += "最多可选" + mVote.options.length + "项";
            }
        }
        if (!"".equals(str)) {
            SpannableStringBuilder span = BBSManager.highlight(str, nItem + "");
            mChoiceTV.setText(span);
            mChoiceTV.setVisibility(View.VISIBLE);
        } else {
            mChoiceTV.setVisibility(View.GONE);
        }

        /**
         * 投票选项
         */
        processContent();
        mContentLLayout.invalidate();
    }

    @SuppressLint("InflateParams") private void processContent() {
        int length = mVote.options.length;

        LayoutInflater inflater = LayoutInflater.from(this);
        Random random = new Random();
        Animation anim;
        TextView tv_label, tv_pb, tv_progress;
        LinearLayout ll;
        for (int position = 0; position < length; position++) {
            View convertView = inflater.inflate(R.layout.vote_item, null);
            tv_label = (TextView) convertView
                    .findViewById(R.id.vote_item_tv_label);
            tv_pb = (TextView) convertView
                    .findViewById(R.id.vote_item_tv_pb);
            tv_progress = (TextView) convertView
                    .findViewById(R.id.vote_item_tv_progress);
            ll = (LinearLayout) convertView
                    .findViewById(R.id.vote_item_ll);
            ll.setId(position);
            mContentLLayout.addView(convertView);
            if (mSwitchMgr.isNightModeEnabled()) {
                tv_label.setTextColor(Color.GRAY);
                tv_progress.setTextColor(Color.GRAY);
            } else {
                tv_label.setTextColor(Color.BLACK);
                tv_progress.setTextColor(Color.BLACK);
            }
            if (!mVote.is_end && mVote.voted == null) {
                int limit = mVote.limit;
                if (limit == 0) {
                    limit = mVote.options.length;
                }
                ll.setOnClickListener(new lLayoutClickListener(mVote.type, limit));
            }
            if (mVote.voted != null) {
                for (int i = 0; i < mVote.voted.viids.length; i++) {
                    if (mVote.voted.viids[i] == mVote.options[position].viid) {
                        ll.setBackgroundColor(Color.parseColor("#88000088"));
                    }
                }
            }

            tv_label.setText(mVote.options[position].label);

            if (mVote.vote_count != -1) {
                /**
                 * 计算相对投票数
                 */
                int max = 0;
                for (int i = 0; i < mVote.options.length; i++) {
                    if (mVote.options[i].num > max) {
                        max = mVote.options[i].num;
                    }
                }
                if (max > 0) {
                    for (int i = 0; i < mVote.options.length; i++) {
                        mVote.options[i].num_relative = (double) mVote.options[i].num
                                / max;
                    }
                }
                /**
                 * 计算tv_pb的宽度
                 */
                int width = (int) ((AiYouManager.getScreenWidth() - mIUMgr.dip2px(100)) * mVote.options[position].num_relative);
                LayoutParams lp = tv_pb.getLayoutParams();
                lp.width = width;
                tv_pb.setLayoutParams(lp);

                anim = AnimationUtils.loadAnimation(this,
                        R.anim.vote_pb);
                tv_pb.startAnimation(anim);
                /**
                 * 产生随机颜色
                 */
                int r = random.nextInt(256);
                int g = random.nextInt(256);
                int b = random.nextInt(256);
                int pb_color = Color.rgb(r, g, b);
                tv_pb.setBackgroundColor(pb_color);
                /**
                 * 计算百分比
                 */
                if(mVote.options[position].num <= 0 || mVote.vote_count <= 0) {
                    tv_progress.setText("0%");
                } else {
                tv_progress.setText(String.format("%.2f", ((double) 100
                        * mVote.options[position].num / mVote.vote_count))
                        + "%");
                }
                anim = AnimationUtils.loadAnimation(this, R.anim.vote_tv);
                tv_progress.startAnimation(anim);
            }
        }
    }

    public void onClick(View view) {
        int nId = view.getId();
        if (R.id.activity_bbsmail_civ_face == nId) {
            // 点击头像，查看用户信息
            ActivityFunc.startActivity(BBSVoteActivity.this,
                    BBSUserInfoActivity.class, mVote.user, false);
        }
    }

    /**
     * 点击投票按钮
     * 
     * @param view
     */
    public void onVote(View view) {
        if (mVote.voted == null && !mVote.is_end) {
            boolean flag = false;
            for (int i = 0; i < mVote.options.length; i++) {
                if (mVote.options[i].isChecked) {
                    flag = true;
                }
            }
            if (flag) {
                startThread(true);
            } else {
                Toast.makeText(getBaseContext(), "至少选择一项", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    /**
     * 投票线程 由 {@link #startThread(boolean)} 启动
     */
    private void voteThread() {
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            if (null != mHandler) {
                mHandler.sendEmptyMessage(MSG_ERROR);
            }
            return;
        }
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                String strJson = Vote.sendVote(BBSVoteActivity.this, mVote);
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
                threadGetVote();
            }
        });
    }

    /**
     * 获取投票线程 由 {@link #startThread(boolean)} 启动
     */
    private void threadGetVote() {
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            if (null != mHandler) {
                mHandler.sendEmptyMessage(MSG_ERROR);
            }
            return;
        }
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                String strJson = Vote.getVote(BBSVoteActivity.this, mVote.vid);
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
                // 将json数据解析为元数据
                try {
                    JSONObject obj = new JSONObject(strJson);
                    obj = JsonHelper.getJSONObject(obj, "vote");
                    mVote = new Vote(obj.toString());
                    if (null != mHandler) {
                        mHandler.sendEmptyMessage(MSG_VOTE);
                    }
                } catch (JSONException e) {
                    if (mHandler != null) {
                        Message msg = mHandler.obtainMessage(MSG_ERROR);
                        Bundle data = msg.getData();
                        data.putString(KEY_DATA, "数据解析错误");
                        mHandler.sendMessage(msg);
                    }
                }
            }
        });
    }

    /**
     * 获取投票描述的线程
     */
    private void threadGetDescription() {
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            if (null != mHandler) {
                mHandler.sendEmptyMessage(MSG_ERROR);
            }
            return;
        }
        if (null != mDescription) {
            return;
        }
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                String strJson = Article.getArticle(BBSVoteActivity.this, "nVote", mVote.aid);
                if (TextUtils.isEmpty(strJson)) {
                    if (mHandler != null) {
                        Message msg = mHandler.obtainMessage(MSG_ERROR);
                        Bundle data = msg.getData();
                        data.putString(KEY_DATA, "获取描述失败");
                        mHandler.sendMessage(msg);
                    }
                    return;
                }
                // 检查返回的是否是错误信息
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
                Article article = new Article(strJson);
                String content = article.content;
                if (content == null) {
                    if (mHandler != null) {
                        Message msg = mHandler.obtainMessage(MSG_ERROR);
                        Bundle data = msg.getData();
                        data.putString(KEY_DATA, "获取描述失败");
                        mHandler.sendMessage(msg);
                    }
                    return;
                }
                Pattern p = Pattern.compile("\n描述:([^\n]*)");
                Matcher m = p.matcher(content);
                String str = null;
                while (m.find()) {
                    MatchResult mr = m.toMatchResult();
                    str = mr.group(1);
                    break;
                }
                if (TextUtils.isEmpty(str)) {
                    mDescription = null;
                } else {
                    mDescription = str;
                }
                if (null != mHandler) {
                    mHandler.sendEmptyMessage(MSG_VOTE_DESCRIPTION);
                }
            }
        });
    }

    /**
     * 设置cpb_progress的状态和是否显示
     * 
     * @param flag
     */
    private void showProgress(boolean flag) {
        if (flag) {
            mProgressFLayout.setVisibility(View.VISIBLE);
            mProgressBar.start();
        } else {
            mProgressFLayout.setVisibility(View.GONE);
            mProgressBar.stop();
        }
    }

    /**
     * 左上角返回按钮
     */
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
    protected void onDestroy() {
        super.onDestroy();
        mContentLLayout.removeAllViews();
        mVote = null;
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        System.gc();
    }

    private void init() {
        mIUMgr = AiYouManager.getInstance(getBaseContext());
        /**
         * 背景图片
         */
        ImageView iv_background = (ImageView) findViewById(R.id.activity_bbsvote_iv_background);

        LinearLayout ll_container = (LinearLayout) findViewById(R.id.activity_bbsvote_ll_container);
        // 是否是简约模式
        if (mSwitchMgr.isSimpleModeEnabled()) {
            iv_background.setVisibility(View.GONE);
            if (mSwitchMgr.isNightModeEnabled()) {
                ll_container.setBackgroundColor(getResources().getColor(
                        R.color.bbs_background_night));
            } else {
                ll_container.setBackgroundColor(getResources().getColor(
                        R.color.bbs_background_day));
            }
        }

        mTitleSTV = (ScrollTextView) findViewById(R.id.activity_bbsvote_stv_title);
        mFaceCIV = (CircleImageView) findViewById(R.id.activity_bbsmail_civ_face);
        mAuthorTV = (TextView) findViewById(R.id.activity_bbsvote_tv_author);
        mDateTV = (TextView) findViewById(R.id.activity_bbsvote_tv_date);
        mVotedTV = (TextView) findViewById(R.id.activity_bbsvote_tv_voted);
        mChoiceTV = (TextView) findViewById(R.id.activity_bbsvote_tv_choice);
        mDescTV = (TextView) findViewById(R.id.activity_bbsvote_tv_description);
        mContentLLayout = (LinearLayout) findViewById(R.id.activity_bbsvote_ll_content);
        /**
         * 进度条
         */
        mProgressFLayout = (FrameLayout) findViewById(R.id.fl_progress);
        mProgressBar = (Win8ProgressBar) findViewById(R.id.progress_bar);

        if (mSwitchMgr.isNightModeEnabled()) {
            mDescTV.setTextColor(Color.GRAY);
        } else {
            mDescTV.setTextColor(Color.BLACK);
        }
    }

    class lLayoutClickListener implements OnClickListener {
        private boolean isSingleChoice;
        private int multiChoiceLimit;

        public lLayoutClickListener(int type, final int multiChoiceLimit) {
            if (type == 0) {
                this.isSingleChoice = true;
            } else {
                this.isSingleChoice = false;
            }

            this.multiChoiceLimit = multiChoiceLimit;
        }

        @Override
        public void onClick(View view) {
            int position = view.getId();
            if (isSingleChoice) {
                for (int i = 0; i < mVote.options.length; i++) {
                    mVote.options[i].isChecked = false;
                    ((LinearLayout) mContentLLayout.getChildAt(i))
                            .setBackgroundColor(Color.TRANSPARENT);
                }
                mVote.options[position].isChecked = true;
                view.setBackgroundColor(Color.parseColor("#88000088"));
            } else {
                if (mVote.options[position].isChecked) {
                    mVote.options[position].isChecked = false;
                    view.setBackgroundColor(Color.parseColor("#00000000"));
                    return;
                }
                int count = 0;
                for (int i = 0; i < mVote.options.length; i++) {
                    if (mVote.options[i].isChecked) {
                        count++;
                    }
                }
                if (count < multiChoiceLimit) {
                    mVote.options[position].isChecked = true;
                    view.setBackgroundColor(Color.parseColor("#88000088"));
                } else {
                    Toast.makeText(getBaseContext(), "已达到多选上限",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}

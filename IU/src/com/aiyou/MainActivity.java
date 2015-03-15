
package com.aiyou;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aiyou.bbs.BBSContentActivity;
import com.aiyou.bbs.BBSListActivity;
import com.aiyou.bbs.PullRefer.BBSService;
import com.aiyou.bbs.bean.Article;
import com.aiyou.bbs.bean.Board;
import com.aiyou.bbs.bean.Favorite;
import com.aiyou.bbs.bean.Section;
import com.aiyou.bbs.bean.Refer.ReferType;
import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.map.MapActivity;
import com.aiyou.map.data.MapHelper;
import com.aiyou.news.NewsListActivity;
import com.aiyou.utils.ActivityFunc;
import com.aiyou.utils.AiYouManager;
import com.aiyou.utils.JsonHelper;
import com.aiyou.utils.NetWorkManager;
import com.aiyou.utils.NetWorkManager.NetStatsChangeListener;
import com.aiyou.utils.SwitchManager;
import com.aiyou.utils.http.HttpManager;
import com.aiyou.utils.logcat.Logcat;
import com.aiyou.utils.thread.ThreadUtils;
import com.aiyou.view.IUWidget;
import com.umeng.analytics.MobclickAgent;

import external.OtherView.ActivitySplitAnimationUtil;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String KEY_TITLE = "title";
    private static final String KEY_CONTENT = "content";
    // 最多查询的每日祝福的页数
    private static final int MAX_PAGE = 5;
    // 北邮愿望树版面名称
    private static final String BOARD_BLESS = "Blessing";

    private static final int MSG_BLESS = 0;

    private NetWorkManager mNetWorkManager;

    // 后台服务
    private static final int INTERVAL = 1000 * 60 * 10;// 10分钟检查一次
    private AlarmManager mAlarmMgr;
    private PendingIntent mPendingIntent;

    private boolean mIsGettingSec = false;

    private boolean mStartImmediatly = false;

    /**
     * 爱邮心声
     */
    private String mIUTitle, mIUContent, mIUUrl;

    /**
     * UI组件
     */
    // 背景
    private ImageView mBgImgView;
    // 每日祝福
    private IUWidget mBlessWidget;
    // 爱邮心声
    private IUWidget mIUWidget;
    // 爱邮心声详细
    private FrameLayout mIUDetailFLayout;
    private TextView mIUDetailTV;
    // 闪屏
    private FrameLayout mSplashView;

    private Handler mHandler = new Handler(new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            if (msg.what == MSG_BLESS) {
                Bundle data = msg.getData();
                mBlessWidget.setTitle(data.getString(KEY_TITLE));
                mBlessWidget.setContent(data.getString(KEY_CONTENT));
                startAlphaAnimation(mBlessWidget);
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNetWorkManager = NetWorkManager.getInstance(this);

        init();

        showSplash();
    }

    @SuppressWarnings("deprecation")
    public void onClick(View view) {
        int nId = view.getId();
        if (R.id.iu_detail == nId
                || R.id.tv_iu_detail == nId) {
            mIUDetailFLayout.setVisibility(View.GONE);
        }
        if (R.id.iu_bless == nId) {
            // 打开每日祝福
            Article article = (Article) view.getTag();
            Intent intent = new Intent(this, BBSContentActivity.class);
            intent.putExtra(BBSContentActivity.KEY_ARTICLE, article);
            ActivityFunc.startActivity(this, intent);
        } else if (R.id.iu_iu == nId) {
            // 打开爱邮心声
            if (!"0".equals(mIUTitle) && !"0".equals(mIUContent)) {
                if ("0".equals(mIUUrl)) {
                    if (SwitchManager.getInstance(getBaseContext()).isSimpleModeEnabled()) {
                        if (SwitchManager.getInstance(getBaseContext()).isNightModeEnabled()) {
                            mIUDetailFLayout.setBackgroundColor(getResources()
                                    .getColor(R.color.bbs_background_night));
                        } else {
                            mIUDetailFLayout.setBackgroundColor(getResources()
                                    .getColor(R.color.bbs_background_day));
                        }
                    } else {
                        mIUDetailFLayout.setBackgroundDrawable(AiYouManager
                                .getBlurBg(this));
                    }
                    mIUDetailFLayout.setVisibility(View.VISIBLE);
                    mIUDetailTV.setText(mIUContent);
                } else {
                    String arr[] = mIUUrl.split("<url>");
                    if (3 != arr.length) {
                        return;
                    } else {
                        Intent intent = null;
                        // 链接到bbs
                        Article article = new Article();
                        article.title = arr[0];
                        article.board_name = arr[1];
                        article.group_id = Integer.parseInt(arr[2]);
                        intent = new Intent(this,
                                BBSContentActivity.class);
                        intent.putExtra(BBSContentActivity.KEY_ARTICLE, article);
                        ActivityFunc.startActivity(this, intent);
                    }
                }
            }
        }
    }

    public void onLabelClick(View view) {
        if (!checkSectionData() || Favorite.mFavorite == null) {
            initFavorite();
            Toast.makeText(getBaseContext(), "正在初始化论坛数据", Toast.LENGTH_SHORT).show();
            return;
        }
        int nId = view.getId();
        double dPercent = 0;
        Intent intent = null;
        if (nId == R.id.news) {
            // 新闻公告
            intent = new Intent(this, NewsListActivity.class);
            dPercent = 0.125;
        } else if (nId == R.id.bbs) {
            // 论坛
            intent = new Intent(this, BBSListActivity.class);
            dPercent = 0.375;
        } else if (nId == R.id.map) {
            // 地图
            if (MapHelper.getMapDatas() == null) {
                Toast.makeText(getBaseContext(), "地图数据尚未完成初始化", Toast.LENGTH_SHORT).show();
                MapHelper.initMapDatas(getApplicationContext());
                return;
            }

            intent = new Intent(this, MapActivity.class);
            dPercent = 0.625;
        } else if (nId == R.id.set) {
            // 设置
            intent = null;
            ActivityFunc.startActivity(this, SetActivity.class, null,
                    true);
            return;
        }
        // 如果这个activity已经启动了，就不产生新的activity，而只是把这个activity实例加到栈顶来就可以了。
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        if (SwitchManager.getInstance(this).isSimpleModeEnabled()
                || Build.VERSION.SDK_INT < 14) {
            ActivityFunc.startActivity(this, intent);
        } else {
            ActivitySplitAnimationUtil.startActivity(this, intent,
                    dPercent);
        }
    }

    private boolean checkSectionData() {
        if (Section.getRootSection(getBaseContext()) != null) {
            return true;
        }
        if (!mIsGettingSec) {
            initSections();
        }
        return false;
    }

    private void initSections() {
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            return;
        }
        mIsGettingSec = true;
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                Section.getRootSection(MainActivity.this);
                mIsGettingSec = false;
            }
        });
    }
    
    private void initFavorite() {
        if(Favorite.mFavorite != null) {
            return;
        }
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            return;
        }
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                    String strJson = Favorite.getFavorite(AiYouApplication.getInstance(), 0);
                    if(!TextUtils.isEmpty(strJson) && JsonHelper.checkError(strJson) == null) {
                        Favorite.mFavorite = new Favorite(strJson);
                    }
                }
        });
    }

    @Override
    public void onBackPressed() {
        if (View.VISIBLE == mIUDetailFLayout.getVisibility()) {
            // 关于页面
            mIUDetailFLayout.setVisibility(View.GONE);
            return;
        }
        scrollToFinishActivity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 程序退出时需调用的方法
        mNetWorkManager.recycle();
        HttpManager.getInstance(this).release();
        ThreadUtils.shutDown();

        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        if (null != mAlarmMgr && mPendingIntent != null) {
            mAlarmMgr.cancel(mPendingIntent);
        }
        mAlarmMgr = null;
        mPendingIntent = null;
        ActivitySplitAnimationUtil.destroy();
    }

    private void showSplash() {
        // 显示闪屏页面
        mSplashView.setVisibility(View.VISIBLE);
        if (!NetWorkManager.getInstance(getBaseContext()).isNetAvailable()) {
            Toast.makeText(getBaseContext(), "    网络不可用    \n联网后加载页面",
                    Toast.LENGTH_LONG).show();
            NetStatsChangeListener listener = new NetStatsChangeListener() {
                @Override
                public void onNetStatsChange() {
                    if (mNetWorkManager.isNetAvailable()) {
                        getUMParams();
                        getBlessList(1);
                        initFavorite();
                        hideSplash();
                        mNetWorkManager.unregisterNetStatsChangeListener(this);
                    }
                }
            };
            mNetWorkManager.registerNetStatsChangeListener(listener);
        } else {
            getUMParams();
            getBlessList(1);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    hideSplash();
                }
            }, 2000);
        }
    }

    private void getUMParams() {
        // 友盟在线参数
//        MobclickAgent.updateOnlineConfig(this);
        mIUTitle = MobclickAgent.getConfigParams(AiYouApplication.getInstance(), "main_iu_title");
        mIUContent = MobclickAgent.getConfigParams(AiYouApplication.getInstance(), "main_iu_content");
        mIUUrl = MobclickAgent.getConfigParams(AiYouApplication.getInstance(), "main_iu_url");
    }

    private void getBlessList(final int page) {
        if (page > MAX_PAGE) {
            // 最多查询5页
            return;
        }
        ThreadUtils.execute(new Runnable() {
            public void run() {
                String strJson = Board.getBoard(MainActivity.this, BOARD_BLESS, page);
                if (TextUtils.isEmpty(strJson)) {
                    return;
                }
                String strError = JsonHelper.checkError(strJson);
                if (strError != null) {
                    Logcat.e(TAG, strError);
                    return;
                }
                // 将json数据解析为User元数据
                Board board = new Board(strJson);
                if (board.articles == null) {
                    Logcat.e(TAG, "board.articles为空");
                    return;
                }
                Article article = null;
                for (int i = 0; i < board.articles.length; i++) {
                    if (board.articles[i].is_top
                            || board.articles[i].user.id.equals("deliver")
                            || board.articles[i].user.id.equals("原帖已删除")) {
                        continue;
                    }
                    article = board.articles[i];
                    break;
                }
                if (null == article) {
                    // 没有用户发的帖子，查看下一页
                    getBlessList(page + 1);
                } else {
                    // 找到了用户的帖子,获取帖子的内容
                    getBlessContent(article.id);
                }
            }
        });
    }

    private void getBlessContent(final int id) {
        ThreadUtils.execute(new Runnable() {
            public void run() {
                String strJson = Article.getArticle(MainActivity.this, BOARD_BLESS, id);
                if (TextUtils.isEmpty(strJson)) {
                    return;
                }
                // 祝福内容
                String strError = JsonHelper.checkError(strJson);
                if (strError != null) {
                    Logcat.e(TAG, strError);
                    return;
                }
                // 将json数据解析为User元数据
                Article article = new Article(strJson);
                String title = article.title;
                String content = article.content;
                if (null != content) {
                    Pattern p = Pattern.compile("(\\[[\\s\\S]*?\\])");
                    Matcher m = p.matcher(content);
                    while (m.find()) {
                        content = content.replace(m.group(), "");
                    }
                    // 去除多余的尾巴
                    while (content.endsWith("-") || content.endsWith("\n")) {
                        content = content
                                .substring(0, content.length() - 1);
                    }
                }
                mBlessWidget.setTag(article);
                if (null != mHandler) {
                    Message msg = mHandler.obtainMessage(MSG_BLESS);
                    Bundle data = msg.getData();
                    data.putString(KEY_TITLE, title);
                    data.putString(KEY_CONTENT, content);
                    mHandler.sendMessage(msg);
                }
            }
        });
    }

    private void hideSplash() {
        SwitchManager switchMgr = SwitchManager.getInstance(getBaseContext());
        if (switchMgr.isFirstRun()) {
            Intent intent = new Intent(this, HelpActivity.class);
            ActivityFunc.startActivity(this, intent);
            switchMgr.disableFirstRun();
        }
        startSpalshAnim();

        // tag动画
        startTagAnimation();
        // 背景饱和度动画
        startBackgroundAnimation();

        if (!TextUtils.isEmpty(mIUTitle) && !TextUtils.isEmpty(mIUContent)
                && !"0".equals(mIUTitle) && !"0".equals(mIUContent)) {
            mIUWidget.setTitle(mIUTitle);
            mIUWidget.setContent(mIUContent);
            startAlphaAnimation(mIUWidget);
        }
    }

    /**
     * 透明度动画
     * 
     * @param view
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startAlphaAnimation(final View view) {
        if (Build.VERSION.SDK_INT < 11) {
            view.setVisibility(View.VISIBLE);
            return;
        }
        int delay = 2500;
        if (mStartImmediatly) {
            delay = 0;
        }
        final int duration = 500;
        final ObjectAnimator oa = ObjectAnimator.ofFloat(view, "alpha", 0, 1f);
        oa.setDuration(duration);
        oa.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                oa.start();
            }
        }, delay);
    }

    /**
     * 背景饱和度动画
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startBackgroundAnimation() {
        if (Build.VERSION.SDK_INT < 11) {
            return;
        }
        final int delay = 1000;
        final int duration = 1000;
        final ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        mBgImgView.setColorFilter(filter);
        final ValueAnimator va = ValueAnimator.ofFloat(0, 1f);
        va.setDuration(duration);
        va.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                matrix.setSaturation(value);
                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(
                        matrix);
                mBgImgView.setColorFilter(filter);
            }
        });
        va.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mStartImmediatly = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                va.start();
            }
        }, delay);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startTagAnimation() {
        if (Build.VERSION.SDK_INT < 11) {
            return;
        }
        final int delay = 500;
        final int interval = 200;
        final int duration = 1000;
        final LinearLayout[] ll = new LinearLayout[4];
        ll[0] = (LinearLayout) findViewById(R.id.news);
        ll[1] = (LinearLayout) findViewById(R.id.bbs);
        ll[2] = (LinearLayout) findViewById(R.id.map);
        ll[3] = (LinearLayout) findViewById(R.id.set);
        int tempwidth = ll[0].getWidth();
        if (tempwidth <= 0) {
            tempwidth = 200;
        }
        final int width = tempwidth;
        final ValueAnimator va[] = new ValueAnimator[4];
        for (int i = 0; i < va.length; i++) {
            va[i] = ValueAnimator.ofFloat(0, 1f);
            va[i].setDuration(duration);
            final int j = i;
            va[i].addUpdateListener(new AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (Float) animation.getAnimatedValue();
                    ll[j].setTranslationX((-1f + value) * width);
                    ll[j].setRotationY((1 - value) * 270);
                }
            });
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    va[j].start();
                }
            }, delay + interval * i);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startSpalshAnim() {
        if (Build.VERSION.SDK_INT < 11) {
            mSplashView.setVisibility(View.GONE);
            return;
        }
        final int duration = 1000;
        final ObjectAnimator oa = ObjectAnimator.ofFloat(mSplashView,
                "alphascale", 1f, 0);
        oa.setDuration(duration);
        oa.setInterpolator(new DecelerateInterpolator());
        oa.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                mSplashView.setAlpha(value);
                mSplashView.setScaleX((3 - value) / 2);
                mSplashView.setScaleY((3 - value) / 2);
            }
        });
        oa.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mSplashView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        oa.start();
    }

    private void init() {
        BBSManager bbsManager = BBSManager.getInstance(getBaseContext());
        bbsManager.setBBSNotificationMail(false);
        bbsManager.setBBSNotificationRefer(ReferType.REPLY, 0);
        bbsManager.setBBSNotificationRefer(ReferType.AT, 0);

        mBgImgView = (ImageView) findViewById(R.id.bg);
        mBlessWidget = (IUWidget) findViewById(R.id.iu_bless);
        mIUWidget = (IUWidget) findViewById(R.id.iu_iu);
        mIUDetailFLayout = (FrameLayout) findViewById(R.id.iu_detail);
        mIUDetailTV = (TextView) findViewById(R.id.tv_iu_detail);
        mSplashView = (FrameLayout) findViewById(R.id.splash);

        /**
         * 开启服务
         */
        mAlarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
        mPendingIntent = PendingIntent.getService(this, 0, new Intent(this,
                BBSService.class), Intent.FLAG_ACTIVITY_NEW_TASK);
        long now = System.currentTimeMillis();
        mAlarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, now, INTERVAL, mPendingIntent);
    }
}

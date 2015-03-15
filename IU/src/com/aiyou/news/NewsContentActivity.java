
package com.aiyou.news;

import java.util.ArrayList;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.aiyou.BaseActivity;
import com.aiyou.R;
import com.aiyou.news.utils.News;
import com.aiyou.news.utils.News.NewsType;
import com.aiyou.news.utils.NewsManager;
import com.aiyou.utils.ActivityFunc;
import com.aiyou.utils.AiYouManager;
import com.aiyou.utils.SwitchManager;
import com.aiyou.utils.share.ShareTask;
import com.aiyou.view.GetScrollDistanceScrollView.OnScrollListener;
import com.aiyou.view.ScrollTextView;
import com.aiyou.viewLargeImage.ViewLargeImageActivity;

import external.OtherView.BounceScrollView;
import external.OtherView.Win8ProgressBar;
import external.SmartImageView.SmartImageView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

/**
 * 显示信息门户、北邮要闻、论坛RSS内容的Activity
 * 
 * @author sollian
 */
@SuppressWarnings("deprecation")
@SuppressLint("DefaultLocale")
public class NewsContentActivity extends BaseActivity implements
        OnScrollListener {
    /**
     * 新闻类型标志位
     */
    private NewsType mMode;

    private News mNews;

    private ArrayList<String> mImgUrlList = new ArrayList<String>();

    private int mImgId = 0;

    /**
     * 控件
     */
    // popmenu
    private FrameLayout mMenuFLayout;
    // 标题
    private LinearLayout mTitleLLayout;
    private ScrollTextView mTitleSTV;
    // 滚动布局
    private BounceScrollView mBounceSV;
    // 内容
    private LinearLayout mContentLLayout;
    // 进度条
    private FrameLayout mProgressFLayout;
    private Win8ProgressBar mProgressBar;

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
        setContentView(R.layout.activity_news_content);
        init();
        Intent intent = getIntent();
        mMode = (NewsType) intent.getSerializableExtra(NewsListActivity.KEY_MODE);
        mNews = (News) intent.getSerializableExtra(NewsListActivity.KEY_DATA);
        if (mNews == null) {
            selfFinish(null);
        }
        // 设置标题
        mTitleSTV.setText(mNews.title);
        processContent(mNews.content);
    }

    public void onShare(View view) {
        // 分享
        ShareTask task = new ShareTask(NewsContentActivity.this, mNews.title, mNews.url,
                new ShareTask.ShareListener() {
                    @Override
                    public void onShareStart() {
                        showProgress(true);
                    }

                    @Override
                    public void onShareFinish(Boolean success) {
                        showProgress(false);
                    }
                });
        task.execute();
        mMenuFLayout.setVisibility(View.GONE);
    }

    /**
     * 网址复制
     * 
     * @param view
     */
    public void onCopy(View view) {
        // 将网址复制到剪贴板
        ClipboardManager copy = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        copy.setText(mNews.url);
        Toast.makeText(getBaseContext(), "复制成功", Toast.LENGTH_SHORT).show();
        mMenuFLayout.setVisibility(View.GONE);
    }

    public void onClick(View view) {
        if (view == mMenuFLayout) {
            mMenuFLayout.setVisibility(View.GONE);
        }
    }

    private void processContent(String data) {
        // 清空图片地址list
        mImgUrlList.clear();
        // 分割出图片
        String array1[] = data.split("<(img|IMG)");
        String wvContent = array1[0];

        String imgSrc = "";

        int index;
        String strImg;
        Pattern p;
        Matcher m;
        MatchResult mr;
        String imgName;
        for (int i = 1; i < array1.length; i++) {
            index = array1[i].indexOf(">");
            strImg = array1[i].substring(0, index + 1);
            p = Pattern.compile("src=\"(.*?)\"");
            m = p.matcher(strImg);
            while (m.find()) {
                mr = m.toMatchResult();
                imgSrc = mr.group(1);
            }
            String array2[] = imgSrc.split("/");
            imgName = array2[array2.length - 1];
            if (imgName.contains(".gif") || imgName.contains(".GIF")
                    || imgName.equalsIgnoreCase("icon_default.png")) {
                wvContent += "<img" + array1[i];
            } else {
                // 显示WebView
                processWebView(wvContent);
                // 更新wvContent
                wvContent = array1[i].substring(index + 1);
                // 显示照片
                processImage(imgSrc);
            }
        }
        // 显示WebView
        processWebView(wvContent);
        // 更新视图
        mContentLLayout.invalidate();
    }

    /**
     * 处理WebView显示内容的方法
     * 
     * @param wvContent 要处理得内容
     */
    private void processWebView(String wvContent) {
        String tableArr[];
        // 分割出表格
        if (wvContent.contains("<TABLE") || wvContent.contains("<table")) {
            tableArr = wvContent.split("</?(table|TABLE)>?");
        } else {
            tableArr = new String[1];
            tableArr[0] = wvContent;
        }

        Document doc;
        String outerHtml;
        String text;
        for (int j = 0; j < tableArr.length; j++) {
            wvContent = tableArr[j];
            if (j % 2 == 1) {
                wvContent = "<table" + wvContent + "</table>";
            }
            doc = Jsoup.parse(wvContent);
            outerHtml = doc.outerHtml();
            text = doc.text();
            text = AiYouManager.getTxtWithoutNTSRElement(text, "");// 去掉非显示字符
            if (text != "") {
                WebView wv = new WebView(NewsContentActivity.this);
                // 取消硬件加速，否则会闪屏——奇怪的是该设置导致文字显示不出来，不知为甚
                // wv.setLayerType(WebView.LAYER_TYPE_SOFTWARE, paint);
                // 设置背景色
                if (Build.VERSION.SDK_INT >= 16) {
                    wv.setBackgroundColor(0x01000000);
                } else {
                    if (SwitchManager.getInstance(getBaseContext()).isNightModeEnabled()) {
                        wv.setBackgroundColor(Color.parseColor("#111111"));
                    } else {
                        wv.setBackgroundColor(Color.parseColor("#ffffff"));
                    }
                }
                wv.setHorizontalScrollBarEnabled(false);// 水平滚动条不显示
                // 控制页面缩放比例
                if (mMode == NewsType.headline) {
                    // 北邮要闻
                    wv.setInitialScale(NewsManager.mHeadlineScaleSize);
                } else {
                    // 信息门户
                    wv.setInitialScale(NewsManager.mNewsScaleSize);
                }
                outerHtml = "<body style=\"text-align:justify;text-justify:distribute-all-lines;\">"
                        + outerHtml + "</body>";
                int fontSize = AiYouManager.getInstance(getBaseContext()).sp2px(8);
                if (SwitchManager.getInstance(getBaseContext()).isNightModeEnabled()) {
                    outerHtml = "<style type=\"text/css\">body{ font-size: "
                            + fontSize
                            + "px; color:#888888}a:link{color:#00aaaa}</style>"
                            + outerHtml;
                } else {
                    outerHtml = "<style type=\"text/css\">body{ font-size: "
                            + fontSize + "px; color:#000000}</style>"
                            + outerHtml;
                }
                wv.loadDataWithBaseURL(null, outerHtml, "text/html", "utf-8",
                        null);
                mContentLLayout.addView(wv);
            }
        }
    }

    /**
     * 显示图片的方法
     * 
     * @param imgSrc 图片路径
     */
    private void processImage(String imgSrc) {
        SmartImageView siv = new SmartImageView(NewsContentActivity.this);
        siv.setScaleType(ScaleType.CENTER_INSIDE);

        LayoutParams param = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        siv.setLayoutParams(param);
        siv.setMaxWidth(AiYouManager.getInstance(getBaseContext()).dip2px(200));
        siv.setMaxHeight(AiYouManager.getInstance(getBaseContext()).dip2px(300));

        String url = NewsManager.imgPathEncoder(imgSrc);
        siv.setImageUrl(url, R.drawable.iu_default_gray,
                R.drawable.iu_default_green);
        siv.setBackgroundColor(Color.WHITE);

        siv.setAdjustViewBounds(true);

        siv.setTag(mImgId++ + "+" + url);
        // 将图片地址添加到listImgSrc中
        mImgUrlList.add(url);
        // 单击图片查看大图
        siv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NewsContentActivity.this,
                        ViewLargeImageActivity.class);
                intent.putExtra(ViewLargeImageActivity.KEY_URL, (String) view.getTag());
                intent.putExtra(ViewLargeImageActivity.KEY_URL_LIST, mImgUrlList);
                intent.putExtra(ViewLargeImageActivity.KEY_NEWS, true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                ActivityFunc.startActivity(NewsContentActivity.this, intent);
            }
        });
        mContentLLayout.addView(siv);
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

    public void selfFinish(View view) {
        scrollToFinishActivity();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 按下键盘上返回按钮
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            selfFinish(null);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            showPopMenu(null);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mImgUrlList.clear();
        mImgUrlList = null;
        mNews = null;
        System.gc();
    }

    @Override
    public void onScroll(int y, int oldY) {
        if (y < AiYouManager.getInstance(getBaseContext()).dip2px(70)) {
            mTitleLLayout.clearAnimation();
            mTitleLLayout.setTag(null);
            if (View.VISIBLE != mTitleLLayout.getVisibility()) {
                showTitle(true);
            }
        } else {
            if (mTitleLLayout.getTag() == null) {
                if (y > oldY && View.VISIBLE == mTitleLLayout.getVisibility()) {
                    showTitle(false);
                } else if (y < oldY && View.VISIBLE != mTitleLLayout.getVisibility()) {
                    showTitle(true);
                }
            }
        }
    }

    /**
     * 是否显示标题栏
     * 
     * @param flag
     */
    private void showTitle(final boolean flag) {
        Animation anim = null;
        mTitleLLayout.setTag("anim");
        if (flag) {
            anim = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_top);
            anim.setDuration(1000);
            mTitleLLayout.setVisibility(View.VISIBLE);
            mTitleLLayout.startAnimation(anim);
        } else {
            anim = AnimationUtils.loadAnimation(this, R.anim.slide_out_to_top);
            anim.setDuration(1000);
            mTitleLLayout.startAnimation(anim);
        }
        anim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                if (!flag) {
                    mTitleLLayout.setVisibility(View.GONE);
                }
                mTitleLLayout.setTag(null);
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationStart(Animation arg0) {
            }
        });
    }

    public void showPopMenu(View view) {
        if (View.VISIBLE == mMenuFLayout.getVisibility()) {
            mMenuFLayout.setVisibility(View.GONE);
        } else {
            mMenuFLayout.setVisibility(View.VISIBLE);
        }
    }

    private void init() {
        mTitleLLayout = (LinearLayout) findViewById(R.id.content_ll_custom_head);
        mTitleSTV = (ScrollTextView) findViewById(R.id.activity_newscontent_stv_title);

        mBounceSV = (BounceScrollView) findViewById(R.id.activity_newscontent_sv);
        mBounceSV.setOnScrollListener(this);
        mContentLLayout = (LinearLayout) findViewById(R.id.activity_newscontent_ll_content);
        /**
         * popmenu
         */
        mMenuFLayout = (FrameLayout) findViewById(R.id.activity_newscontent_fl_menu);
        /**
         * 进度条
         */
        mProgressFLayout = (FrameLayout) findViewById(R.id.fl_progress);
        mProgressBar = (Win8ProgressBar) findViewById(R.id.progress_bar);
    }
}

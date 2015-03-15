
package com.aiyou.appwidget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.aiyou.R;
import com.aiyou.bbs.bean.Article;
import com.aiyou.bbs.bean.Widget;
import com.aiyou.utils.JsonHelper;
import com.aiyou.utils.NetWorkManager;
import com.aiyou.utils.logcat.Logcat;
import com.aiyou.utils.thread.ThreadUtils;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.Toast;

/**
 * 十大列表的adapter
 * 
 * @author sollian
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetViewsFactory implements
        RemoteViewsService.RemoteViewsFactory {
    private static final String TAG = WidgetViewsFactory.class.getSimpleName();
    private static final String KEY_MSG = "msg";

    private ArrayList<Article> mArticleList = new ArrayList<Article>();
    private Context mContext;
    private Widget mWidget;
    @SuppressWarnings("unused")
    private int mAppWidgetId;

    private Handler mHandler = new Handler(new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            String str = msg.getData().getString(KEY_MSG);
            Toast.makeText(mContext, str, Toast.LENGTH_SHORT).show();
            return true;
        }
    });

    public WidgetViewsFactory(Context context, Intent intent,
            ArrayList<Article> listArticle) {
        this.mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        this.mArticleList = listArticle;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        return mArticleList.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews row = new RemoteViews(mContext.getPackageName(),
                R.layout.widget_row);

        row.setTextViewText(R.id.widget_row_tv_title,
                mArticleList.get(position).title);
        if (position % 2 == 0) {
            row.setImageViewResource(R.id.widget_row_iv_bg,
                    R.drawable.background_widget_list_white);
        } else {
            row.setImageViewResource(R.id.widget_row_iv_bg,
                    R.drawable.background_widget_list_blue);
        }
        Intent i = new Intent();
        Bundle extras = new Bundle();

        extras.putSerializable(WidgetProvider.EXTRA_ARTICLE,
                mArticleList.get(position));
        i.putExtras(extras);
        row.setOnClickFillInIntent(R.id.widget_row_tv_title, i);

        return row;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onDataSetChanged() {
        Future<?> future = getWidget();
        if (future != null) {
            try {
                boolean result = (Boolean) future.get();
                if (!result) {
                    return;
                }
            } catch (InterruptedException e) {
                Logcat.e(TAG, "onDataSetChanged() InterruptedException");
            } catch (ExecutionException e) {
                Logcat.e(TAG, "onDataSetChanged() ExecutionException:" + e.getMessage());
            }
        } else {
            return;
        }
        mArticleList.clear();
        mArticleList.addAll(Arrays.asList(mWidget.articles));
        sendMessage("更新成功");
    }

    /**
     * 发送消息
     * 
     * @param str
     */
    private void sendMessage(String str) {
        Message msg = mHandler.obtainMessage();
        Bundle data = msg.getData();
        data.putString(KEY_MSG, str);
        msg.setData(data);
        mHandler.sendMessage(msg);
    }

    /**
     * 获取十大的线程
     */
    private Future<?> getWidget() {
        if (!NetWorkManager.getInstance(mContext).isNetAvailable()) {
            sendMessage(NetWorkManager.MSG_NONET);
            return null;
        }
        return ThreadUtils.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                String strJson = Widget.getTopten(mContext);
                if (strJson == null) {
                    sendMessage(NetWorkManager.MSG_NONET);
                    return false;
                }
                String str = JsonHelper.checkError(strJson);
                if (null == str) {
                    // 非错误信息
                    mWidget = new Widget(strJson);
                    return true;
                } else {
                    sendMessage(str);
                    return false;
                }
            }
        });
    }
}

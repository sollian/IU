
package com.aiyou.appwidget;

import com.aiyou.R;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * @author sollian
 */
public class WidgetProvider extends AppWidgetProvider {
    public static String EXTRA_ARTICLE = "widget_article";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        for (int i = 0; i < appWidgetIds.length; i++) {
            updateWidget(context, appWidgetIds[i]);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasCategory(Intent.CATEGORY_ALTERNATIVE)) {
            // “手动更新”广播
            Uri data = intent.getData();
            int buttonId = Integer.parseInt(data.getSchemeSpecificPart());
            Toast.makeText(context, "开始更新", Toast.LENGTH_SHORT).show();
            AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            mgr.notifyAppWidgetViewDataChanged(buttonId, R.id.widget_lv);
        }
        super.onReceive(context, intent);
    }

    @SuppressWarnings("deprecation")
    private void updateWidget(Context context, int id) {
        Intent serviceIntent = new Intent(context, WidgetService.class);

        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
        serviceIntent.setData(Uri.parse(serviceIntent
                .toUri(Intent.URI_INTENT_SCHEME)));

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.widget);

        remoteViews.setRemoteAdapter(id, R.id.widget_lv, serviceIntent);

        Intent clickIntent = new Intent(context, WidgetClickActivity.class);
        PendingIntent clickPI = PendingIntent.getActivity(context, 0,
                clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setPendingIntentTemplate(R.id.widget_lv, clickPI);
        // 设置点击按钮对应的PendingIntent：即点击按钮时，发送广播。
        remoteViews.setOnClickPendingIntent(R.id.widget_bt,
                getPendingIntent(context, id));

        AppWidgetManager.getInstance(context).updateAppWidget(id, remoteViews);
    }

    private PendingIntent getPendingIntent(Context context, int id) {
        Intent intent = new Intent();
        intent.setClass(context, WidgetProvider.class);
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        intent.setData(Uri.parse("custom:" + id));
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        return pi;
    }
}

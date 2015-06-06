
package com.aiyou.appwidget;

import java.util.ArrayList;
import com.aiyou.bbs.bean.Article;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViewsService;

/**
 * @author sollian
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetService extends RemoteViewsService {
    private ArrayList<Article> mArticleList = new ArrayList<Article>();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return (new WidgetViewsFactory(this.getBaseContext(), intent,
                mArticleList));
    }
}

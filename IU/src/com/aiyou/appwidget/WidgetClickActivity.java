
package com.aiyou.appwidget;

import com.aiyou.bbs.BBSContentActivity;
import com.aiyou.bbs.bean.Article;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * 响应列表点击事件的activity
 * 
 * @author sollian
 */
public class WidgetClickActivity extends Activity {
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        Article article = (Article) (getIntent()
                .getSerializableExtra(WidgetProvider.EXTRA_ARTICLE));
        Intent intent = new Intent(this, BBSContentActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(BBSContentActivity.KEY_ARTICLE, article);
        startActivity(intent);
        finish();
    }
}

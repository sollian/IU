
package com.aiyou.bbs;

import android.content.Intent;
import android.os.Bundle;

import com.aiyou.BaseActivity;
import com.aiyou.bbs.bean.Article;
import com.aiyou.utils.ActivityFunc;

/**
 * 该Activity为连接友盟推送与BBSContentActivity的桥梁
 *
 * @author 守宪
 */
public class BBSPushLinkActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Article article = new Article();
        article.title = intent.getStringExtra("title");
        article.board_name = intent.getStringExtra("board");
        article.group_id = Integer.parseInt(intent.getStringExtra("group"));
        intent = new Intent(this, BBSContentActivity.class);
        intent.putExtra(BBSContentActivity.KEY_ARTICLE, article);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ActivityFunc.startActivity(BBSPushLinkActivity.this, intent);
        finish();
    }
}

package com.aiyou.viewLargeImage;

import android.os.Bundle;
import android.text.TextUtils;

import com.aiyou.BaseActivity;
import com.aiyou.R;
import com.aiyou.utils.filecache.FileManager;

import external.largeimage.LongImageView;

/**
 * Created by sollian on 2015/10/28.
 */
public class ViewLongImageActivity extends BaseActivity {
    public static final String KEY_URL = "url";

    private LongImageView vLarge;
    private byte[] inputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_long_image);

        if (!checkData()) {
            finish();
            return;
        }

        init();
    }

    private boolean checkData() {
        String url = getIntent().getStringExtra(KEY_URL);
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        FileManager fileManager = new FileManager(FileManager.DIR_LARGEIMG);
        inputStream = fileManager.getImage(url);
        if (inputStream == null) {
            return false;
        }
        return true;
    }

    private void init() {
        vLarge = (LongImageView) findViewById(R.id.long_img);
        vLarge.setImage(inputStream);
    }
}

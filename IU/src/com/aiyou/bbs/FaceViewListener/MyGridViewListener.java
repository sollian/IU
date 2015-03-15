
package com.aiyou.bbs.FaceViewListener;

import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.utils.FileCache.FileManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 表情GridView的监听器
 * 
 * @author sollian
 */
public class MyGridViewListener implements OnItemClickListener {
    private int mType;// 表情种类
    private String mImageId;
    private String mImageName;
    private EditText mEditText;
    private Context mContext;

    public MyGridViewListener(Context context, int type, EditText et) {
        mContext = context;
        mType = type;
        mEditText = et;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        switch (mType) {
            case 0:
                mImageId = "em" + (position + 1);
                mImageName = "[em" + (position + 1) + "]";
                break;
            case 1:
                mImageId = "emc" + position;
                mImageName = "[emc" + position + "]";
                break;
            case 2:
                mImageId = "emb" + position;
                mImageName = "[emb" + position + "]";
                break;
            case 3:
                mImageId = "ema" + position;
                mImageName = "[ema" + position + "]";
                break;
        }
        try {
            Bitmap bitmap = FileManager.getImageFromAssetsFile(mImageId + ".gif",
                    mContext);
            BBSManager.addPic(mContext, mEditText, bitmap, mImageName, false);
        } catch (NullPointerException e) {
        }
    }
}

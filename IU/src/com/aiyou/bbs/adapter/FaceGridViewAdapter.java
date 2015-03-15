
package com.aiyou.bbs.adapter;

import com.aiyou.utils.AiYouManager;
import com.aiyou.utils.FileCache.FileManager;
import com.aiyou.view.DarkImageView;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView.ScaleType;

/**
 * 装载表情的gridview的adapter
 * 
 * @author sollian
 */
public class FaceGridViewAdapter extends BaseAdapter {
    private Context mContext;
    private int mFaceType;// 0-em; 1-emc; 2-emb; 3-ema
    private AiYouManager mIUMgr;

    public FaceGridViewAdapter(Context context, int faceType) {
        mContext = context;
        mFaceType = faceType;
        mIUMgr = AiYouManager.getInstance(mContext);
    }

    @Override
    public int getCount() {
        int length = 0;
        switch (mFaceType) {
            case 0:
                length = em.length;
                break;
            case 1:
                length = emc.length;
                break;
            case 2:
                length = emb.length;
                break;
            case 3:
                length = ema.length;
                break;
        }
        return length;
    }

    @Override
    public Object getItem(int position) {
        switch (mFaceType) {
            case 0:
                return em[position];
            case 1:
                return emc[position];
            case 2:
                return emb[position];
            case 3:
                return ema[position];
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new DarkImageView(mContext);
            convertView.setLayoutParams(new GridView.LayoutParams(mIUMgr
                    .dip2px(40), mIUMgr.dip2px(40)));
            ((DarkImageView) convertView).setScaleType(ScaleType.FIT_XY);
        }
        String id = null;
        switch (mFaceType) {
            case 0:
                id = em[position];
                break;
            case 1:
                id = emc[position];
                break;
            case 2:
                id = emb[position];
                break;
            case 3:
                id = ema[position];
                break;
        }
        Bitmap bmp = FileManager.getImageFromAssetsFile(id, mContext);
        ((DarkImageView) convertView).setImageBitmap(bmp);
        return convertView;
    }

    /**
     * 显示的图片数组
     */
    public static final String[] em = {
            "em1.gif", "em2.gif", "em3.gif", "em4.gif",
            "em5.gif", "em6.gif", "em7.gif", "em8.gif", "em9.gif", "em10.gif",
            "em11.gif", "em12.gif", "em13.gif", "em14.gif", "em15.gif",
            "em16.gif", "em17.gif", "em18.gif", "em19.gif", "em20.gif",
            "em21.gif", "em22.gif", "em23.gif", "em24.gif", "em25.gif",
            "em26.gif", "em27.gif", "em28.gif", "em29.gif", "em30.gif",
            "em31.gif", "em32.gif", "em33.gif", "em34.gif", "em35.gif",
            "em36.gif", "em37.gif", "em38.gif", "em39.gif", "em40.gif",
            "em41.gif", "em42.gif", "em43.gif", "em44.gif", "em45.gif",
            "em46.gif", "em47.gif", "em48.gif", "em49.gif", "em50.gif",
            "em51.gif", "em52.gif", "em53.gif", "em54.gif", "em55.gif",
            "em56.gif", "em57.gif", "em58.gif", "em59.gif", "em60.gif",
            "em61.gif", "em62.gif", "em63.gif", "em64.gif", "em65.gif",
            "em66.gif", "em67.gif", "em68.gif", "em69.gif", "em70.gif",
            "em71.gif", "em72.gif", "em73.gif",
    };
    public static final String[] ema = {
            "ema0.gif", "ema1.gif", "ema2.gif", "ema3.gif",
            "ema4.gif", "ema5.gif", "ema6.gif", "ema7.gif", "ema8.gif",
            "ema9.gif", "ema10.gif", "ema11.gif", "ema12.gif", "ema13.gif",
            "ema14.gif", "ema15.gif", "ema16.gif", "ema17.gif", "ema18.gif",
            "ema19.gif", "ema20.gif", "ema21.gif", "ema22.gif", "ema23.gif",
            "ema24.gif", "ema25.gif", "ema26.gif", "ema27.gif", "ema28.gif",
            "ema29.gif", "ema30.gif", "ema31.gif", "ema32.gif", "ema33.gif",
            "ema34.gif", "ema35.gif", "ema36.gif", "ema37.gif", "ema38.gif",
            "ema39.gif", "ema40.gif", "ema41.gif",
    };
    public static final String[] emb = {
            "emb0.gif", "emb1.gif", "emb2.gif", "emb3.gif",
            "emb4.gif", "emb5.gif", "emb6.gif", "emb7.gif", "emb8.gif",
            "emb9.gif", "emb10.gif", "emb11.gif", "emb12.gif", "emb13.gif",
            "emb14.gif", "emb15.gif", "emb16.gif", "emb17.gif", "emb18.gif",
            "emb19.gif", "emb20.gif", "emb21.gif", "emb22.gif", "emb23.gif",
            "emb24.gif",
    };
    public static final String[] emc = {
            "emc0.gif", "emc1.gif", "emc2.gif", "emc3.gif",
            "emc4.gif", "emc5.gif", "emc6.gif", "emc7.gif", "emc8.gif",
            "emc9.gif", "emc10.gif", "emc11.gif", "emc12.gif", "emc13.gif",
            "emc14.gif", "emc15.gif", "emc16.gif", "emc17.gif", "emc18.gif",
            "emc19.gif", "emc20.gif", "emc21.gif", "emc22.gif", "emc23.gif",
            "emc24.gif", "emc25.gif", "emc26.gif", "emc27.gif", "emc28.gif",
            "emc29.gif", "emc30.gif", "emc31.gif", "emc32.gif", "emc33.gif",
            "emc34.gif", "emc35.gif", "emc36.gif", "emc37.gif", "emc38.gif",
            "emc39.gif", "emc40.gif", "emc41.gif", "emc42.gif", "emc43.gif",
            "emc44.gif", "emc45.gif", "emc46.gif", "emc47.gif", "emc48.gif",
            "emc49.gif", "emc50.gif", "emc51.gif", "emc52.gif", "emc53.gif",
            "emc54.gif", "emc55.gif", "emc56.gif", "emc57.gif", "emc58.gif",
    };
}

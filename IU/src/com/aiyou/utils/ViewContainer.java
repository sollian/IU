package com.aiyou.utils;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.ViewGroup;
import external.smartimageview.SmartImageView;

public class ViewContainer {
    @SuppressLint("UseSparseArrays")
    private Map<Long, SmartImageView> mSIVMap = new HashMap<Long, SmartImageView>();

    private Context mContext;

    public ViewContainer(Context context) {
        mContext = context;
    }

    public SmartImageView getSIV(long wrapTime) {
        SmartImageView siv = null;
        for (Long l : mSIVMap.keySet()) {
            if (l < wrapTime) {
                siv = mSIVMap.get(l);
                if(siv.getParent() != null) {
                    ((ViewGroup)siv.getParent()).removeView(siv);
                }
                mSIVMap.remove(l);
                l = System.currentTimeMillis();
                mSIVMap.put(l, siv);
                break;
            }
        }
        if (siv == null) {
            siv = new SmartImageView(mContext);
            mSIVMap.put(System.currentTimeMillis(), siv);
        }
        return siv;
    }

}

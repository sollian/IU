
package com.aiyou.view;

import com.aiyou.R;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 用于展示每日祝福、爱邮心声的布局
 * 
 * @author sollian
 */
public class IUWidget extends LinearLayout {
    private TextView mTagTV, mTitleTV, mContentTV;

    public IUWidget(Context context) {
        super(context);
        init();
    }

    public IUWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        initAttrs(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public IUWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
        initAttrs(context, attrs);
    }

    public void setTag(String tag) {
        mTagTV.setText(tag);
    }

    public void setTitle(String title) {
        mTitleTV.setText(title);
    }

    public void setContent(String content) {
        mContentTV.setText(content);
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.iuwidget, this);
        mTagTV = (TextView) view.findViewById(R.id.tag);
        mTitleTV = (TextView) view.findViewById(R.id.title);
        mContentTV = (TextView) view.findViewById(R.id.content);
    }

    @SuppressWarnings("deprecation")
    private void initAttrs(Context context, AttributeSet attrs) {

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IUWidget, 0, 0);
        int tagTextColor = a.getColor(R.styleable.IUWidget_tagTextColor, Color.BLACK);
        int titleTextColor = a.getColor(R.styleable.IUWidget_titleTextColor, Color.BLACK);
        int tontentTextColor = a.getColor(R.styleable.IUWidget_contentTextColor, Color.BLACK);

        Drawable tagBg = a.getDrawable(R.styleable.IUWidget_tagBackground);
        Drawable titleBg = a.getDrawable(R.styleable.IUWidget_titleBackground);
        Drawable contentBg = a.getDrawable(R.styleable.IUWidget_contentBackground);

        String tag = a.getString(R.styleable.IUWidget_tagText);

        a.recycle();

        mTagTV.setTextColor(tagTextColor);
        mTitleTV.setTextColor(titleTextColor);
        mContentTV.setTextColor(tontentTextColor);

        mTagTV.setBackgroundDrawable(tagBg);
        mTitleTV.setBackgroundDrawable(titleBg);
        mContentTV.setBackgroundDrawable(contentBg);

        mTagTV.setText(tag);
    }
}

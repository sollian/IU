
package com.aiyou.view;

import com.aiyou.R;

import external.SwitchButton.SwitchButton;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SwitchPreferences extends LinearLayout implements OnCheckedChangeListener {

    public interface OnPrefChangeListener {
        public void onPrefChanged(SwitchPreferences pref, boolean isChecked);
    }

    private TextView mTitleTV;
    private SwitchButton mSwitchBtn;
    private OnPrefChangeListener mListener;

    public SwitchPreferences(Context context) {
        super(context);
        init();
    }

    public SwitchPreferences(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        initAttrs(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SwitchPreferences(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
        initAttrs(context, attrs);
    }

    public void setChecked(boolean checked) {
        mSwitchBtn.setChecked(checked);
    }

    public void setOnPrefChangeListener(OnPrefChangeListener listener) {
        mListener = listener;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (mListener != null) {
            mListener.onPrefChanged(this, isChecked);
        }
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.switch_preferences, this);
        mTitleTV = (TextView) view.findViewById(R.id.title);
        mSwitchBtn = (SwitchButton) view.findViewById(R.id.switch_btn);
        mSwitchBtn.setOnCheckedChangeListener(this);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwitchPref, 0, 0);
        String title = a.getString(R.styleable.SwitchPref_prefTitle);
        a.recycle();
        mTitleTV.setText(title);
    }

}

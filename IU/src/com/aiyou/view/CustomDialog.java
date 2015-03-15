
package com.aiyou.view;

import com.aiyou.AiYouApplication;
import com.aiyou.R;
import com.aiyou.utils.SwitchManager;

import external.OtherView.Win8ProgressBar;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CustomDialog extends Dialog {

    private TextView mTitleTextView;
    private TextView mMessageTextView;
    private EditText mEditText;

    private LinearLayout mButtonLinearLayout;
    private Button mOKBtn, mCancelBtn;
    private Win8ProgressBar mProgressBar;

    public CustomDialog(Context context) {
        this(context, R.style.CustomeDialogThreme);
    }

    public CustomDialog(Context context, int theme) {
        super(context, theme);
        setContentView(R.layout.custom_dialog);
        init();
    }

    private void init() {
        mTitleTextView = (TextView) findViewById(R.id.custom_dialog_tv_title);
        mMessageTextView = (TextView) findViewById(R.id.custom_dialog_tv_message);
        mEditText = (EditText) findViewById(R.id.custom_dialog_et);

        mButtonLinearLayout = (LinearLayout) findViewById(R.id.custom_dialog_ll_bt);
        mOKBtn = (Button) findViewById(R.id.ok_btn);
        mCancelBtn = (Button) findViewById(R.id.cancel_btn);
        mProgressBar = (Win8ProgressBar) findViewById(R.id.progress_bar);

        LinearLayout ll = (LinearLayout) findViewById(R.id.custom_dialog_ll);

        if (SwitchManager.getInstance(AiYouApplication.getInstance()).isNightModeEnabled()) {
            FrameLayout fl_root = (FrameLayout) findViewById(R.id.custom_dialog_fl_root);
            fl_root.setBackgroundColor(Color.parseColor("#222222"));
            ll.setBackgroundResource(R.drawable.background_rect_night);
            mTitleTextView.setTextColor(getContext().getResources().getColor(
                    R.color.font_night));
            mMessageTextView.setTextColor(getContext().getResources().getColor(
                    R.color.font_night));
            mEditText.setBackgroundResource(R.drawable.background_et_night);
            mEditText.setTextColor(getContext().getResources().getColor(
                    R.color.font_night));
        }

        setCanceledOnTouchOutside(true);
        setCancelable(true);
    }

    public CustomDialog setTitle(String title) {
        mTitleTextView.setText(title);
        return this;
    }

    public CustomDialog setMessage(String message) {
        mMessageTextView.setText(message);
        return this;
    }

    public CustomDialog setOKButton(View.OnClickListener listener) {
        mButtonLinearLayout.setVisibility(View.VISIBLE);
        mOKBtn.setVisibility(View.VISIBLE);
        if (listener == null) {
            listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            };
        }
        mOKBtn.setOnClickListener(listener);
        return this;
    }

    public CustomDialog setOKGetEditTextContentListener(final GetEditTextContentListener listener) {
        mButtonLinearLayout.setVisibility(View.VISIBLE);
        mOKBtn.setVisibility(View.VISIBLE);
        mEditText.setVisibility(View.VISIBLE);
        mOKBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(v, mEditText.getText().toString());
            }
        });
        return this;
    }

    public CustomDialog setCancelButton(View.OnClickListener listener) {
        mButtonLinearLayout.setVisibility(View.VISIBLE);
        mCancelBtn.setVisibility(View.VISIBLE);
        if (listener == null) {
            listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            };
        }
        mCancelBtn.setOnClickListener(listener);
        return this;
    }

    public void startProgress() {
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        mOKBtn.setVisibility(View.GONE);
        mCancelBtn.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.start();
    }

    public void stopProgress() {
        mOKBtn.setVisibility(View.VISIBLE);
        mCancelBtn.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mProgressBar.stop();
    }

    public interface GetEditTextContentListener {
        public void onClick(View view, String content);
    }
}

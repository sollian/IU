
package com.aiyou.view;

import com.aiyou.AiYouApplication;
import com.aiyou.utils.SwitchManager;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.widget.ImageView;

public class DarkImageView extends ImageView {

    public DarkImageView(Context context) {
        super(context);
        init();
    }

    public DarkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DarkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if (SwitchManager.getInstance(AiYouApplication.getInstance()).isNightModeEnabled()) {
            setColorFilter(Color.GRAY, Mode.MULTIPLY);
        } else {
            clearColorFilter();
        }
    }

}

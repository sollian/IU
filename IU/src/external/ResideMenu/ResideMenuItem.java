
package external.ResideMenu;

import com.aiyou.R;
import com.aiyou.utils.SwitchManager;

import external.OtherView.CircleImageView;
import external.shimmer.Shimmer;
import external.shimmer.ShimmerTextView;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * User: special Date: 13-12-10 Time: 下午11:05 Mail: specialcyci@gmail.com
 */
public class ResideMenuItem extends LinearLayout {

    /** menu item icon */
    private ImageView iv_icon;
    /** menu item title */
    private TextView tv_title;

    private LinearLayout ll_info;
    private CircleImageView civ_face;
    private ShimmerTextView shtv_id;
    private TextView tv_name;

    // 用户id扫光特效
    private Shimmer shimmer = null;

    public ResideMenuItem(Context context) {
        super(context);
        initViews(context);
        ll_info.setVisibility(View.VISIBLE);
        iv_icon.setVisibility(View.GONE);
        tv_title.setVisibility(View.GONE);
    }

    public ResideMenuItem(Context context, int icon, String title) {
        super(context);
        initViews(context);
        iv_icon.setImageResource(icon);
        tv_title.setText(title);
    }

    public ResideMenuItem(Context context, int height, String strColor,
            String flag) {
        super(context);
        initViews(context);
        iv_icon.setVisibility(View.GONE);
        if (null != strColor) {
            tv_title.setBackgroundColor(Color.parseColor(strColor));
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, height);
        tv_title.setLayoutParams(params);
    }

    private void initViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.residemenu_item, this);
        iv_icon = (ImageView) findViewById(R.id.iv_icon);

        tv_title = (TextView) findViewById(R.id.tv_title);

        ll_info = (LinearLayout) findViewById(R.id.ll_info);
        civ_face = (CircleImageView) findViewById(R.id.civ_face);
        shtv_id = (ShimmerTextView) findViewById(R.id.shtv_id);
        tv_name = (TextView) findViewById(R.id.tv_name);

        civ_face.setImageResource(R.drawable.iu_default_green);
        shtv_id.setText("——");
        tv_name.setText("——");
        if (SwitchManager.getInstance(getContext()).isNightModeEnabled()) {
            tv_title.setTextColor(getResources().getColor(R.color.font_night));
        } else {
            tv_title.setTextColor(getResources().getColor(
                    R.color.font_white_day));
        }
    }

    /**
     * set the icon color;
     * 
     * @param icon
     */
    public void setIcon(int icon) {
        iv_icon.setImageResource(icon);
    }

    /**
     * set the title with resource ;
     * 
     * @param title
     */
    public void setTitle(int title) {
        tv_title.setText(title);
    }

    /**
     * set the title with string;
     * 
     * @param title
     */
    public void setTitle(String title) {
        tv_title.setText(title);
    }

    /**
     * 设置用户信息
     * 
     * @param strFaceUrl
     * @param strId
     * @param strName
     */
    public void setUserInfo(String strFaceUrl, String strId, String strName) {
        if (null != strFaceUrl) {
            civ_face.setImageURL(strFaceUrl);
        }
        if (null != strId) {
            shtv_id.setText(strId);
            startShimmer();
        }
        if (null != strName) {
            tv_name.setText(strName);
        }
    }

    public void startShimmer() {
        if (Build.VERSION.SDK_INT < 11) {
            return;
        }
        if (null != shimmer && shimmer.isAnimating()) {
            shimmer.cancel();
        }
        shimmer = new Shimmer();
        shimmer.setRepeatCount(2);
        shimmer.setDuration(800);
        shimmer.start(shtv_id);
    }
}

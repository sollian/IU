
package com.aiyou.map;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

import com.aiyou.BaseActivity;
import com.aiyou.AiYouApplication;
import com.aiyou.R;
import com.aiyou.map.adapter.MySpinnerAdapter;
import com.baidu.lbsapi.panoramaview.*;
import com.baidu.lbsapi.BMapManager;

/**
 * 查看全景图
 * 
 * @author sollian
 */
public class PanoramaActivity extends BaseActivity {
    private PanoramaView mPanoView;
    private Spinner mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 先初始化BMapManager
        AiYouApplication app = AiYouApplication.getInstance();
        if (app.mBMapManager == null) {
            app.mBMapManager = new BMapManager(app);
            app.mBMapManager.init(new AiYouApplication.MyGeneralListener());
        }
        setContentView(R.layout.activity_panorama);

        mSpinner = (Spinner) findViewById(R.id.activity_panorama_sp);
        mPanoView = (PanoramaView) findViewById(R.id.activity_panorama_pv);
        mPanoView.setShowTopoLink(true);
        mPanoView.setZoomGestureEnabled(false);
        mPanoView.setRotateGestureEnabled(true);

        ArrayList<String> list = new ArrayList<String>();
        list.add("西门");
        list.add("东门");
        list.add("3号教学楼");
        list.add("图书馆");
        list.add("学生公寓");
        list.add("体育馆");
        list.add("新食堂");

        MySpinnerAdapter adapter = new MySpinnerAdapter(this, list, "#880088");
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v,
                    int position, long id) {
                switch (position) {
                    case 0:
                        mPanoView.setPanorama(116.361672, 39.966862);
                        mPanoView.setPanoramaHeading(90);
                        break;
                    case 1:
                        mPanoView.setPanorama(116.367592, 39.968293);
                        mPanoView.setPanoramaHeading(-90);
                        break;
                    case 2:
                        mPanoView.setPanorama(116.363019, 39.966475);
                        mPanoView.setPanoramaHeading(0);
                        break;
                    case 3:
                        mPanoView.setPanorama(116.363945, 39.9685);
                        mPanoView.setPanoramaHeading(90);
                        break;
                    case 4:
                        mPanoView.setPanorama(116.362858, 39.968943);
                        mPanoView.setPanoramaHeading(-90);
                        break;
                    case 5:
                        mPanoView.setPanorama(116.36584, 39.96774);
                        mPanoView.setPanoramaHeading(45);
                        break;
                    case 6:
                        mPanoView.setPanorama(116.363729, 39.96962);
                        mPanoView.setPanoramaHeading(-90);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    public void selfFinish(View view) {
        scrollToFinishActivity();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 按下键盘上返回按钮
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            selfFinish(null);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPanoView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPanoView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPanoView.destroy();
    }
}

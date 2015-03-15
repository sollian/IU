
package com.aiyou.map;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

import com.aiyou.BaseActivity;
import com.aiyou.R;
import com.aiyou.map.adapter.MySpinnerAdapter;
import com.aiyou.map.data.MapData;
import com.aiyou.map.data.MapData.DataType;
import com.aiyou.map.data.MapHelper;
import com.aiyou.utils.AiYouManager;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.GroundOverlayOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;

import external.OtherView.ActivitySplitAnimationUtil;

public class MapActivity extends BaseActivity implements
        OnGetRoutePlanResultListener {

    private static final int MSG_WALK = 0;
    private static final int MSG_DRIVE = 1;
    private static final int MSG_TRANSIT = 2;

    private InfoWindow mInfoWindow;

    private BaiduMap mBaiduMap;
    private String mSearchType = "全部";

    private Set<Marker> mMarkerSet = new HashSet<Marker>();

    private Set<BitmapDescriptor> mBmpDescSet = new HashSet<BitmapDescriptor>();
    private BitmapDescriptor mBdGround = BitmapDescriptorFactory
            .fromResource(R.drawable.map_ground_overlay);

    /**
     * 定位相关
     */
    private LocationClient mLocClient;
    public MyLocationListenner mListener = new MyLocationListenner();
    // 是否首次定位
    boolean mIsFirstLoc = true;
    private LocationMode mCurrentMode;

    private int mNodeIndex = -2;// 节点索引,供浏览节点时使用
    private RouteLine<?> mRoute;
    private RoutePlanSearch mSearch; // 搜索模块，也可去掉地图模块独立使用
    private LatLng mCurPosition;
    private boolean mFlagUpdateCurPos = false;
    private Marker mMarkerDst;
    /**
     * 控件
     */
    private Spinner mSpinner;
    private TextView mClearTV;
    // MapView 是地图主控件
    private MapView mMapView;
    // 路线规划
    private TextView mBtnPre;// 上一个节点
    private TextView mBtnNext;// 下一个节点
    private TextView mClearBtn;
    private LinearLayout mLinearLayout;
    private TextView mDriveBtn, mWalkBtn, mTransitBtn;

    private Handler mHandler = new Handler(new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            mBaiduMap.hideInfoWindow();
            // 重置浏览节点的路线数据
            mRoute = null;
            resetOverlay(null);
            mClearBtn.setVisibility(View.VISIBLE);
            mLinearLayout.setVisibility(View.VISIBLE);
            // 设置起终点信息，对于tranist search 来说，城市名无意义
            PlanNode stNode = PlanNode.withLocation(mCurPosition);
            PlanNode enNode = PlanNode.withLocation(mMarkerDst.getPosition());
            if (msg.what == MSG_WALK) {
                mSearch.walkingSearch((new WalkingRoutePlanOption()).from(
                        stNode).to(enNode));
            } else if (msg.what == MSG_DRIVE) {
                mSearch.drivingSearch((new DrivingRoutePlanOption()).from(
                        stNode).to(enNode));
            } else if (msg.what == MSG_TRANSIT) {
                mSearch.transitSearch((new TransitRoutePlanOption())
                        .from(stNode).city("北京").to(enNode));
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        int delay = 0;
        if (ActivitySplitAnimationUtil.canPlay()
                && Build.VERSION.SDK_INT >= 14) {
            delay = 1000;
            // 中心打开动画
            ActivitySplitAnimationUtil.prepareAnimation(this);
            ActivitySplitAnimationUtil.animate(this, delay);
        }

        mMapView = (MapView) findViewById(R.id.bmapView);

        mHandler.postDelayed(new Runnable() {
            public void run() {
                initBaiduMap();
                init();
            }
        }, delay);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        // MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
        mMapView.onDestroy();
        mMapView = null;

        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;

        mBdGround.recycle();
        if (!mBmpDescSet.isEmpty()) {
            for (BitmapDescriptor descriptor : mBmpDescSet) {
                descriptor.recycle();
            }
        }
        mBmpDescSet.clear();
        ActivitySplitAnimationUtil.cancel();

        System.gc();
    }

    @Override
    protected void onPause() {
        // MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // MapView的生命周期与Activity同步，当activity恢复时需调用MapView.onResume()
        mMapView.onResume();
    }

    public void selfFinish(View view) {
        if (Build.VERSION.SDK_INT >= 14) {
            ActivitySplitAnimationUtil.finish(this);
        } else {
            scrollToFinishActivity();
        }
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

    private void initBaiduMap() {
        mBaiduMap = mMapView.getMap();
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(17.5f);
        mBaiduMap.setMapStatus(msu);
        mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
            public boolean onMarkerClick(final Marker marker) {
                mFlagUpdateCurPos = true;
                mMarkerDst = marker;
                mLinearLayout.setVisibility(View.VISIBLE);
                TextView text = new TextView(getBaseContext());
                text.setTextColor(Color.BLACK);
                text.setGravity(Gravity.CENTER);
                text.setTextSize(AiYouManager.getInstance(getBaseContext()).sp2px(9));
                text.setBackgroundResource(R.drawable.map_popup);
                final LatLng ll = marker.getPosition();
                Point p = mBaiduMap.getProjection().toScreenLocation(ll);
                p.y -= 47;
                LatLng llInfo = mBaiduMap.getProjection().fromScreenLocation(p);
                OnInfoWindowClickListener listener = new OnInfoWindowClickListener() {
                    public void onInfoWindowClick() {
                        mBaiduMap.hideInfoWindow();
                        mLinearLayout.setVisibility(View.VISIBLE);
                    }
                };
                text.setText(marker.getTitle());
                mInfoWindow = new InfoWindow(BitmapDescriptorFactory
                        .fromView(text), llInfo, 0, listener);
                mBaiduMap.showInfoWindow(mInfoWindow);
                return true;
            }
        });
    }

    private void init() {
        initSpinner();
        initSearch();
        initOverlay();

        mClearTV = (TextView) findViewById(R.id.activity_map_tv_clear);

        final TextView requestLocButton = (TextView) findViewById(R.id.activity_map_tv);
        mCurrentMode = LocationMode.NORMAL;
        requestLocButton.setText("普通");
        OnClickListener btnClickListener = new OnClickListener() {
            public void onClick(View v) {
                // 设置定位模式
                switch (mCurrentMode) {
                    case NORMAL:
                        ((TextView) v).setText("跟随");
                        mCurrentMode = LocationMode.FOLLOWING;
                        mBaiduMap
                                .setMyLocationConfigeration(new MyLocationConfiguration(
                                        mCurrentMode, true, null));
                        break;
                    case COMPASS:
                        ((TextView) v).setText("普通");
                        mCurrentMode = LocationMode.NORMAL;
                        mBaiduMap
                                .setMyLocationConfigeration(new MyLocationConfiguration(
                                        mCurrentMode, true, null));
                        break;
                    case FOLLOWING:
                        ((TextView) v).setText("罗盘");
                        mCurrentMode = LocationMode.COMPASS;
                        mBaiduMap
                                .setMyLocationConfigeration(new MyLocationConfiguration(
                                        mCurrentMode, true, null));
                        break;
                }
            }
        };
        requestLocButton.setOnClickListener(btnClickListener);
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(mListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    private void initOverlay() {
        MapData[] datas = MapHelper.getMapDatas();
        Marker marker = null;
        Bundle bundle = null;
        mMarkerSet.clear();
        BitmapDescriptor descriptor = null;
        for (MapData data : datas) {
            descriptor = BitmapDescriptorFactory.fromResource(data.getDescId());
            mBmpDescSet.add(descriptor);
            marker = (Marker) (mBaiduMap.addOverlay(new MarkerOptions()
                    .position(new LatLng(data.getLat(), data.getLng()))
                    .icon(descriptor)
                    .zIndex(9)));
            marker.setTitle(data.getName());
            bundle = new Bundle();
            bundle.putString("type", data.getType());
            marker.setExtraInfo(bundle);
            mMarkerSet.add(marker);
        }

        // add ground overlay
        LatLng southwest = new LatLng(39.96398, 116.361625);
        LatLng northeast = new LatLng(39.97084, 116.367706);
        LatLngBounds bounds = new LatLngBounds.Builder().include(northeast)
                .include(southwest).build();

        OverlayOptions ooGround = new GroundOverlayOptions()
                .positionFromBounds(bounds).image(mBdGround).transparency(0.3f).zIndex(1);
        mBaiduMap.addOverlay(ooGround);

        MapStatusUpdate u = MapStatusUpdateFactory
                .newLatLng(bounds.getCenter());
        mBaiduMap.setMapStatus(u);
    }

    private void updateOverlay() {
        String type = null;
        for (Marker marker : mMarkerSet) {
            type = marker.getExtraInfo().getString("type");
            if (mSearchType.equals("全部") || mSearchType.equals(type)) {
                marker.setVisible(true);
            } else {
                marker.setVisible(false);
            }
        }
    }

    private void initSearch() {
        mBtnPre = (TextView) findViewById(R.id.activity_map_tv_pre);
        mBtnNext = (TextView) findViewById(R.id.activity_map_tv_next);
        mClearBtn = (TextView) findViewById(R.id.activity_map_tv_clear);
        mDriveBtn = (TextView) findViewById(R.id.activity_map_tv_drive);
        mWalkBtn = (TextView) findViewById(R.id.activity_map_tv_walk);
        mTransitBtn = (TextView) findViewById(R.id.activity_map_tv_transit);
        mLinearLayout = (LinearLayout) findViewById(R.id.activity_map_ll);
        mLinearLayout.setVisibility(View.GONE);
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);
        mClearBtn.setVisibility(View.INVISIBLE);
        // 初始化搜索模块，注册事件监听
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);
    }

    private void initSpinner() {
        mSpinner = (Spinner) findViewById(R.id.activity_map_sp);

        List<String> list = new ArrayList<String>();
        list.add("全部");
        DataType[] dataTypes = DataType.values();
        String strType = null;
        for (DataType type : dataTypes) {
            strType = type.getType();
            if (!list.contains(strType)) {
                list.add(strType);
            }
        }

        MySpinnerAdapter adapter = new MySpinnerAdapter(this, list, "#880088");
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v,
                    int position, long id) {
                mBaiduMap.hideInfoWindow();
                mSearchType = parent.getItemAtPosition(position).toString();
                if (mClearTV.getVisibility() == View.VISIBLE) {
                    clearOverlay(null);
                }
                resetOverlay(null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    /**
     * 重新添加Overlay
     * 
     * @param view
     */
    public void resetOverlay(View view) {
        updateOverlay();
        initSearch();
    }

    /**
     * 清除所有Overlay
     * 
     * @param view
     */
    public void clearOverlay(View view) {
        mWalkBtn.setText("步行搜索");
        mTransitBtn.setText("公交搜索");
        mDriveBtn.setText("驾车搜索");
        mBaiduMap.clear();
        initOverlay();
        initSearch();
    }

    /**
     * 发起路线规划搜索示例
     * 
     * @param v
     */
    public void SearchButtonProcess(View v) {
        int id = v.getId();
        ((TextView) v).setText("计算中...");
        if (id == R.id.activity_map_tv_drive) {
            mHandler.sendEmptyMessage(MSG_DRIVE);
        } else if (id == R.id.activity_map_tv_walk) {
            mHandler.sendEmptyMessage(MSG_WALK);
        } else if (id == R.id.activity_map_tv_transit) {
            mHandler.sendEmptyMessage(MSG_TRANSIT);
        }
    }

    /**
     * 节点浏览示例
     * 
     * @param v
     */
    public void nodeClick(View v) {
        if (mNodeIndex < -1 || mRoute == null || mRoute.getAllStep() == null
                || mNodeIndex >= mRoute.getAllStep().size()) {
            return;
        }
        // 设置节点索引
        if (v.getId() == R.id.activity_map_tv_next
                && mNodeIndex < mRoute.getAllStep().size() - 1) {
            mNodeIndex++;
        } else if (v.getId() == R.id.activity_map_tv_pre && mNodeIndex > 1) {
            mNodeIndex--;
        } else {
            return;
        }

        // 获取节结果信息
        LatLng nodeLocation = null;
        String nodeTitle = null;
        Object step = mRoute.getAllStep().get(mNodeIndex);
        if (step instanceof DrivingRouteLine.DrivingStep) {
            nodeLocation = ((DrivingRouteLine.DrivingStep) step).getEntrace()
                    .getLocation();
            nodeTitle = ((DrivingRouteLine.DrivingStep) step).getInstructions();
        } else if (step instanceof WalkingRouteLine.WalkingStep) {
            nodeLocation = ((WalkingRouteLine.WalkingStep) step).getEntrace()
                    .getLocation();
            nodeTitle = ((WalkingRouteLine.WalkingStep) step).getInstructions();
        } else if (step instanceof TransitRouteLine.TransitStep) {
            nodeLocation = ((TransitRouteLine.TransitStep) step).getEntrace()
                    .getLocation();
            nodeTitle = ((TransitRouteLine.TransitStep) step).getInstructions();
        }

        if (nodeLocation == null || nodeTitle == null) {
            return;
        }
        // 移动节点至中心
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(nodeLocation));
        // show popup
        View viewCache = getLayoutInflater()
                .inflate(R.layout.custom_text_view, null);
        TextView popupText = (TextView) viewCache.findViewById(R.id.textcache);
        popupText.setBackgroundResource(R.drawable.map_popup);
        popupText.setText(nodeTitle);
        mBaiduMap.showInfoWindow(new InfoWindow(BitmapDescriptorFactory
                .fromView(popupText), nodeLocation, 0, null));

    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {
        mWalkBtn.setText("步行搜索");
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(getBaseContext(), "抱歉，未找到结果", Toast.LENGTH_SHORT)
                    .show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            result.getSuggestAddrInfo();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            showToast(result.getRouteLines().get(0).getDistance());
            mNodeIndex = -1;
            mBtnPre.setVisibility(View.VISIBLE);
            mBtnNext.setVisibility(View.VISIBLE);
            mRoute = result.getRouteLines().get(0);
            WalkingRouteOverlay overlay = new WalkingRouteOverlay(mBaiduMap);
            // mBaiduMap.setOnMarkerClickListener(overlay);
            // routeOverlay = overlay;
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }

    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult result) {
        mTransitBtn.setText("公交搜索");
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(getBaseContext(), "抱歉，未找到结果", Toast.LENGTH_SHORT)
                    .show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            result.getSuggestAddrInfo();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            showToast(result.getRouteLines().get(0).getDistance());
            mNodeIndex = -1;
            mBtnPre.setVisibility(View.VISIBLE);
            mBtnNext.setVisibility(View.VISIBLE);
            mRoute = result.getRouteLines().get(0);
            TransitRouteOverlay overlay = new TransitRouteOverlay(mBaiduMap);
            // mBaiduMap.setOnMarkerClickListener(overlay);
            // routeOverlay = overlay;
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {
        mDriveBtn.setText("驾车搜索");
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(getBaseContext(), "抱歉，未找到结果", Toast.LENGTH_SHORT)
                    .show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            result.getSuggestAddrInfo();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            showToast(result.getRouteLines().get(0).getDistance());
            mNodeIndex = -1;
            mBtnPre.setVisibility(View.VISIBLE);
            mBtnNext.setVisibility(View.VISIBLE);
            mRoute = result.getRouteLines().get(0);
            DrivingRouteOverlay overlay = new DrivingRouteOverlay(mBaiduMap);
            // routeOverlay = overlay;
            // mBaiduMap.setOnMarkerClickListener(overlay);
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
    }

    private void showToast(long meter) {
        if (meter >= 1000) {
            float fmeter = meter / 1000.0f;
            Toast.makeText(getBaseContext(),
                    String.format("%.2f", fmeter) + "千米", Toast.LENGTH_LONG)
                    .show();
        } else {
            Toast.makeText(getBaseContext(), meter + "米", Toast.LENGTH_LONG)
                    .show();
        }
    }

    /**
     * 查看全景图
     * 
     * @param view
     */
    public void panoramaView(View view) {
        Intent intent = new Intent(MapActivity.this, PanoramaActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, 0);
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null)
                return;
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (mIsFirstLoc) {
                mIsFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(u);
            }
            if (mFlagUpdateCurPos) {
                mFlagUpdateCurPos = false;
                mCurPosition = new LatLng(location.getLatitude(),
                        location.getLongitude());
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

}

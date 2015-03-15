
package com.aiyou.map.data;

import com.aiyou.R;

public class MapData {
    public enum DataType {
        DORM(R.drawable.map_dorm, 0, "宿舍"), // 宿舍
        TEACHING_BUILDING(R.drawable.map_teaching_building, 1, "教学楼"), // 教学楼
        LIBRARY(R.drawable.map_library, 2, "图书馆"), // 图书馆
        OFFICE_BUILDING(R.drawable.map_office_building, 3, "办公楼"), // 办公楼
        CANTEEN(R.drawable.map_canteen, 4, "食堂"), // 食堂
        CAFE(R.drawable.map_cafe, 5, "休闲购物"), // 咖啡厅
        MARKET(R.drawable.map_market, 6, "休闲购物"), // 超市
        FRUIT(R.drawable.map_fruit, 7, "休闲购物"), // 水果店
        GYM(R.drawable.map_gym, 8, "体育健身"), // 体育馆
        SWIMMING_POOL(R.drawable.map_swimming_pool, 9, "体育健身"), // 游泳馆
        BASKETBALL_COURT(R.drawable.map_basketball_court, 10, "体育健身"), // 篮球场
        PLAYGROUND(R.drawable.map_playground, 11, "体育健身"), // 操场
        BANK(R.drawable.map_bank, 12, "公共设施"), // 银行
        POST_OFFICE(R.drawable.map_post_office, 13, "公共设施"), // 邮局
        HOTEL(R.drawable.map_hotel, 14, "公共设施"), // 宾馆
        HOSPITAL(R.drawable.map_hospital, 15, "公共设施"), // 医院
        CAMERA(R.drawable.map_camera, 16, "照相打印"), // 照相馆
        COPY(R.drawable.map_copy, 17, "照相打印"), // 复印店
        POLICE(R.drawable.map_police, 18, "其他"), // 保卫处
        AUDITORIUM(R.drawable.map_music, 19, "其他"), // 礼堂
        ACTIVITY_CENTER(R.drawable.map_activity_center, 20, "其他"), // 活动中心
        CAREER_CENTER(R.drawable.map_career_center, 21, "其他"), // 就业中心
        SHOWER(R.drawable.map_shower, 22, "公共设施"); // 浴室

        private int mDescId;
        private String mType;

        private DataType(int resId, int id, String type) {
            mDescId = resId;
            mType = type;
        }

        public int getDescId() {
            return mDescId;
        }

        public String getType() {
            return mType;
        }
    };

    private double mLongitude = -1;
    private double mLatitude = -1;
    private String mName;
    private DataType mDataType;

    public void setLng(double lng) {
        mLongitude = lng;
    }

    public double getLng() {
        return mLongitude;
    }

    public void setLat(double lat) {
        mLatitude = lat;
    }

    public double getLat() {
        return mLatitude;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public void setType(int type) {
        DataType[] dataTypes = DataType.values();
        mDataType = dataTypes[type];
    }

    public String getType() {
        return mDataType.getType();
    }

    public int getDescId() {
        return mDataType.getDescId();
    }
}

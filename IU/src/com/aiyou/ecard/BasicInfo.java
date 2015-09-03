
package com.aiyou.ecard;

class BasicInfo {
    /**
     * 学（工）号
     */
    public String id;
    /**
     * 姓名
     */
    public String name;
    /**
     * 头像地址
     */
    public String face_url;
    /**
     * 性别
     */
    public String sex;
    /**
     * 民族
     */
    public String nation;
    /**
     * 主钱包余额
     */
    public String money_main;
    /**
     * 补助余额
     */
    public String money_extra;
    /**
     * 专用钱包余额
     */
    public String money_spec;
    /**
     * 身份
     */
    public String role;
    /**
     * 卡状态
     */
    public String status;
    /**
     * 部门
     */
    public String department;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("卡号=");
        builder.append(id);
        builder.append("\n姓名=");
        builder.append(name);
        builder.append("\n头像=");
        builder.append(face_url);
        builder.append("\n性别=");
        builder.append(sex);
        builder.append("\n民族=");
        builder.append(nation);
        builder.append("\n主钱包余额=");
        builder.append(money_main);
        builder.append("\n补助余额=");
        builder.append(money_extra);
        builder.append("\n专用钱包余额=");
        builder.append(money_spec);
        builder.append("\n身份=");
        builder.append(role);
        builder.append("\n卡状态=");
        builder.append(status);
        builder.append("\n部门=");
        builder.append(department);
        return builder.toString();
    }
}

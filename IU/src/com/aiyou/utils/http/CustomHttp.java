
package com.aiyou.utils.http;

import org.apache.http.client.methods.HttpUriRequest;

import android.content.Context;

public class CustomHttp {
    private HttpUriRequest mHttp;
    private String mTag;

    public CustomHttp(Context context, HttpUriRequest http) {
        mTag = context.getClass().getSimpleName();
        mHttp = http;
    }

    public HttpUriRequest getHttp() {
        return mHttp;
    }

    public String getTag() {
        return mTag;
    }
}

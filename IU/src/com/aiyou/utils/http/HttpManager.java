
package com.aiyou.utils.http;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NoHttpResponseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.aiyou.bbs.utils.BBSManager;
import com.aiyou.utils.logcat.Logcat;

public class HttpManager {
    private static final String TAG = HttpManager.class.getSimpleName();

    public static final String CHARSET = HTTP.UTF_8;

    private static final int POOL_TIMEOUT = 10 * 1000;// 设置连接池超时
    private static final int CONNECT_TIMEOUT = 10 * 1000;
    private static final int READ_TIMEOUT = 10 * 1000;

    private static HttpManager mInstance;
    private ClientConnectionManager mConnMgr = null;
    private HttpParams mParams = null;
    private HttpRequestRetryHandler mRequestRetryHandler = null;
    private ResponseHandler<byte[]> mResponseHandler;

    private Set<CustomHttp> mConnSet = new HashSet<CustomHttp>();

    private AtomicBoolean mFlag = new AtomicBoolean(true);

    private HttpManager(Context context) {
        mFlag.set(true);
    }

    public static HttpManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (HttpManager.class) {
                if (mInstance == null) {
                    mInstance = new HttpManager(context);
                }
            }
        }
        return mInstance;
    }

    public String getHttp(Context context, String netUrl) {
        byte[] data = getHttpByte(context, netUrl);
        if (data == null) {
            return null;
        }
        return new String(data);
    }

    public byte[] getHttpByte(Context context, String netUrl) {
        if (TextUtils.isEmpty(netUrl)) {
            return null;
        }
        synchronized (mFlag) {
            if (!mFlag.get()) {
                try {
                    mFlag.wait();
                } catch (InterruptedException e) {
                    Log.e(TAG, "getHttpByte InterruptedException:" + e.getMessage());
                }
            }
        }
        byte[] result = null;

        HttpGet hg = new HttpGet(netUrl);
        hg.setHeader("Referer", BBSManager.BBS_URL);

        CustomHttp http = new CustomHttp(context, hg);
        mConnSet.add(http);

        try {
            result = getHttpClient(context).execute(hg, getResponseHandler());
        } catch (ClientProtocolException e) {
            Logcat.e(TAG, "getHttpByte ClientProtocolException:" + e.getMessage());
        } catch (IOException e) {
            Logcat.e(TAG, "getHttpByte IOException:" + e.getMessage());
        } catch (Exception e) {
            Logcat.e(TAG, "getHttpByte Exception:" + e.getMessage());
        } finally {
            mConnSet.remove(http);
        }
        return result;
    }

    public String postHttp(Context context, String netUrl, HttpEntity entity) {
        if (TextUtils.isEmpty(netUrl)) {
            return null;
        }
        if (!mFlag.get()) {
            try {
                wait();
            } catch (InterruptedException e) {
                Log.e(TAG, "postHttp InterruptedException:" + e.getMessage());
            }
        }
        String result = null;

        HttpPost hp = new HttpPost(netUrl);
        hp.setHeader("Referer", BBSManager.BBS_URL);

        CustomHttp http = new CustomHttp(context, hp);
        mConnSet.add(http);

        // 把参数设置到httpPost实体
        if (null != entity) {
            hp.setEntity(entity);
        }
        DefaultHttpClient client = getHttpClient(context);
        client.setHttpRequestRetryHandler(getHttpRequestRetryHandler());
        try {
            byte[] data = client.execute(hp, getResponseHandler());
            if (data == null) {
                result = null;
            } else {
                result = new String(data);
            }
        } catch (ClientProtocolException e) {
            Logcat.e(TAG, "postHttp ClientProtocolException:" + e.getMessage());
        } catch (IOException e) {
            Logcat.e(TAG, "postHttp IOException:" + e.getMessage());
        } catch (Exception e) {
            Logcat.e(TAG, "getHttpByte Exception:" + e.getMessage());
        } finally {
            mConnSet.remove(http);
        }

        return result;
    }

    /**
     * 程序退出时调用，调用此函数无需再调用disconnect、disconnectAll函数
     */
    public void release() {
        if (mConnMgr != null) {
            mConnMgr.shutdown();
        }
        mConnMgr = null;
    }

    /**
     * 中断某一个Context开启的所有网络连接
     * 
     * @param context
     */
    public synchronized void disconnect(Context context) {
        if (mConnSet.isEmpty()) {
            return;
        }
        mFlag.set(false);
        String tag = context.getClass().getSimpleName();
        Set<CustomHttp> temp = new HashSet<CustomHttp>();
        synchronized (mConnSet) {
            for (CustomHttp http : mConnSet) {
                if (tag.equals(http.getTag())) {
                    http.getHttp().abort();
                    temp.add(http);
                }
            }
            if (!temp.isEmpty()) {
                mConnSet.removeAll(temp);
            }
        }
        mFlag.set(true);
        synchronized (mFlag) {
            mFlag.notifyAll();
        }
    }

    /**
     * 中断所有网络连接
     */
    public synchronized void disconnectAll() {
        mFlag.set(false);
        if (mConnSet.isEmpty()) {
            return;
        }
        synchronized (mConnSet) {
            for (CustomHttp http : mConnSet) {
                http.getHttp().abort();
            }
            mConnSet.clear();
        }
        mFlag.set(true);
        synchronized (mFlag) {
            mFlag.notifyAll();
        }
    }

    // /**
    // * 将InputStream中的内容读取为比特数组
    // *
    // * @param is
    // * @return
    // */
    // public static byte[] parseStream(InputStream is) {
    // if (is == null) {
    // return null;
    // }
    // byte[] result = new byte[1024];
    //
    // int length = -1;
    // ByteArrayOutputStream byteArrayOutputStream = new
    // ByteArrayOutputStream();
    // try {
    // while ((length = is.read(result)) != -1) {
    // byteArrayOutputStream.write(result, 0, length);
    // }
    // result = byteArrayOutputStream.toByteArray();
    // } catch (IOException e) {
    // Logcat.e(TAG, "parseStream IOException:" + e.getMessage());
    // result = null;
    // } finally {
    // FileManager.close(byteArrayOutputStream);
    // }
    // return result;
    // }

    private HttpRequestRetryHandler getHttpRequestRetryHandler() {
        if (mRequestRetryHandler != null) {
            return mRequestRetryHandler;
        }
        mRequestRetryHandler = new HttpRequestRetryHandler() {
            // 自定义的恢复策略
            public synchronized boolean retryRequest(IOException exception, int executionCount,
                    HttpContext context) {
                // 设置恢复策略，在发生异常时候将自动重试3次
                if (executionCount > 3) {
                    // 超过最大次数则不需要重试
                    return false;
                }
                if (exception instanceof NoHttpResponseException) {
                    // 服务停掉则不重新连接
                    return false;
                }
                if (exception instanceof SSLHandshakeException) {
                    // SSL异常不需要重试
                    return false;
                }
                HttpRequest request = (HttpRequest) context
                        .getAttribute(ExecutionContext.HTTP_REQUEST);
                boolean idempotent = (request instanceof
                        HttpEntityEnclosingRequest);
                if (!idempotent) {
                    // 请求内容相同则重试
                    return true;
                }
                return false;
            }
        };
        return mRequestRetryHandler;
    }

    private ResponseHandler<byte[]> getResponseHandler() {
        if (mResponseHandler != null) {
            return mResponseHandler;
        }
        mResponseHandler = new ResponseHandler<byte[]>() {
            public byte[] handleResponse(HttpResponse response)
                    throws ClientProtocolException, IOException {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    return EntityUtils.toByteArray(entity);
                } else {
                    return null;
                }
            }
        };
        return mResponseHandler;
    }

    /**
     * 获取HttpClient对象
     * 
     * @param context
     * @return
     */
    private DefaultHttpClient getHttpClient(Context context) {
        HttpParams httpParams = getHttpParams();
        // 使用线程安全的连接管理来创建HttpClient
        if (mConnMgr == null) {
            SchemeRegistry schReg = new SchemeRegistry();
            schReg.register(new Scheme("http", PlainSocketFactory
                    .getSocketFactory(), 80));
            schReg.register(new Scheme("https", SSLSocketFactory
                    .getSocketFactory(), 443));
            mConnMgr = new ThreadSafeClientConnManager(httpParams, schReg);
        }
        DefaultHttpClient client = new DefaultHttpClient(mConnMgr, httpParams);

        // 身份验证
        BBSManager bbsMgr = BBSManager.getInstance(context);
        UsernamePasswordCredentials upc = new UsernamePasswordCredentials(
                bbsMgr.getUserId(), bbsMgr.getUserPassword()); // 这一句使用用户名密码建立了一个数据
        AuthScope as = new AuthScope(null, -1);

        BasicCredentialsProvider bcp = new BasicCredentialsProvider();
        bcp.setCredentials(as, upc);
        client.setCredentialsProvider(bcp);
        return client;
    }

    private HttpParams getHttpParams() {
        if (mParams != null) {
            return mParams;
        }
        // 设置一些基本参数
        mParams = new BasicHttpParams();
        HttpProtocolParams.setVersion(mParams, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(mParams, CHARSET);
        HttpProtocolParams.setUseExpectContinue(mParams, true);
        // HttpProtocolParams
        // .setUserAgent(
        // httpParams,
        // "Mozilla/5.0(Linux;U;Android 2.3.3;en-us;Nexus One Build.FRG83) "
        // +
        // "AppleWebKit/553.1(KHTML,like Gecko) Version/4.0 Mobile Safari/533.1");
        // // 代理的设置
        // HttpHost proxy = new HttpHost("10.60.8.20", 8080);
        // httpParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

        /* 从连接池中取连接的超时时间 */
        ConnManagerParams.setTimeout(mParams, POOL_TIMEOUT); // 设置连接池超时0秒钟
        HttpConnectionParams.setConnectionTimeout(mParams,
                CONNECT_TIMEOUT);
        HttpConnectionParams.setSoTimeout(mParams, READ_TIMEOUT); // 设置等待数据超时时间0秒钟
        return mParams;
    }
}

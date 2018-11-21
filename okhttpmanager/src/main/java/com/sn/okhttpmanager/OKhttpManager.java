package com.sn.okhttpmanager;

import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * date:2016/11/20
 * author:易宸锋(dell)
 * function:对OKhttp的简单封装工具类,实现两个功能,从服务端下载数据;从客户端提交数据.但是没有实现文件的上传下载
 * 更好的OKhttp封装工具类,可以使用这个:https://github.com/angcyo/OkHttpUtils/blob/master/README.md
 * function:对okhttp的封装
 * 节约了内存,使所有的网络请求都用一个OkHttpClient对象和Handler对象
 * 简化了okhttp请求网络的冗余步骤
 * 解决了okhttp,网络成功,代码在子线程的问题,把请求网络成功后逻辑代码放到主线程执行
 * 封装用到了哪些知识点?
 * 1.handler , 单例模式 , 接口 ,okhttp
 * <p>
 * 双重锁:http://www.blogjava.net/kenzhh/archive/2013/03/15/357824.html
 */
public class OKhttpManager {
    //////////////////////////////////////////定义成员变量/////////////////////////
    private OkHttpClient mClient;

    private static OKhttpManager sManager;//防止多个线程同时访问.比synchronized轻

    private static Handler mHandler;

    ///////////////////////////////////////使用构造方法,完成初始化////////////////////////
    private OKhttpManager() {
        mClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)//设置超时时间
                .readTimeout(10, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(10, TimeUnit.SECONDS).build();//设置写入超时时间
        mHandler = new Handler();
    }

    ///////////////////////////////////////使用单例模式,通过获取的方式拿到对象/////////////
    public static OKhttpManager getInstance() {
        if (sManager == null) {
            sManager = new OKhttpManager();
        }
        return sManager;
    }

    //////////////////////////////////////////////定义接口//////////////////////////////////////
    interface Func1 {
        void onResponse(String result);
    }

    interface Func2 {
        void onResponse(byte[] result);
    }

    interface Func3 {
        void onResponse(JSONObject jsonObject);
    }

    //////////////////////////////////////////////////使编写代码运行在主线程////////////////////
    //处理请求网络成功的方法,返回的结果是json字符串
    private static void onSuccessJsonStringMethod(final String jsonValue, final Func1 callBack) {
        //这里我用的是mHandler.post(new Runnable()){}方法更新UI,你们用message的试试.
        mHandler.post(new Runnable() {//代码是在UI线程执行
            @Override
            public void run() {
                if (callBack != null) {
                    try {
                        callBack.onResponse(jsonValue);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 返回响应的结果是json对象
     *
     * @param jsonValue
     * @param callBack
     */
    private void onSuccessJsonObjectMethod(final String jsonValue, final Func3 callBack) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callBack != null) {
                    try {
                        callBack.onResponse(new JSONObject(jsonValue));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 请求返回的是byte[] 数组
     *
     * @param data
     * @param callBack
     */
    private void onSuccessByteMethod(final byte[] data, final Func2 callBack) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callBack != null) {
                    callBack.onResponse(data);
                }
            }
        });
    }
///////////////////////////下面就是可以调用的方法了///////////////////////////////////////////////////

    /**
     * 请求指定的url返回的结果是json字符串
     *
     * @param url
     * @param callBack
     */
    public void asyncJsonStringByURL(String url, final Func1 callBack) {
        final Request request = new Request.Builder().url(url).build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {

            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response != null && response.isSuccessful()) {
                    onSuccessJsonStringMethod(response.body().string(), callBack);
                }
            }
        });
    }

    /**
     * 请求返回的是json对象
     *
     * @param url
     * @param callBack
     */
    public void asyncJsonObjectByURL(String url, final Func3 callBack) {
        final Request request = new Request.Builder().url(url).build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response != null && response.isSuccessful()) {
                    onSuccessJsonObjectMethod(response.body().string(), callBack);
                }
            }
        });
    }

    /**
     * 请求返回的是byte字节数组
     */
    public void asyncGetByteByURL(String url, final Func2 callBack) {
        final Request request = new Request.Builder().url(url).build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response != null && response.isSuccessful()) {
                    onSuccessByteMethod(response.body().bytes(), callBack);
                }
            }
        });
    }

    /**
     * 表单提交
     *
     * @param url
     * @param params
     * @param callBack
     */
    public void sendComplexForm(String url, Map<String, String> params, final Func1 callBack) {
        FormBody.Builder form_builder = new FormBody.Builder();//表单对象，包含以input开始的对象，以html表单为主
        //键值非空
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                form_builder.add(entry.getKey(), entry.getValue());
            }
        }
        RequestBody request_body = form_builder.build();
        Request request = new Request.Builder().url(url).post(request_body).build();//采用post方式提交
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response != null && response.isSuccessful()) {
                    onSuccessJsonStringMethod(response.body().string(), callBack);
                }
            }
        });
    }

}

package com.sn.okhttp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * okhttp底层网络请求用的是Socket,长连接
 */
public class MainActivity extends AppCompatActivity {
    private String Path = "http://publicobject.com/helloworld.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * 更加点击事件使用okhttp拦截器
     */
    public void interceptor(View view) {
        new Thread() {
            public void run() {
                try {
                    //建立OkHttpClient对象时,传入其拦截器对象
                    //注意:这里走了一次网络请求,因为网络重定向
                    OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new LoggingInterceptor()).build();

                    Request request = new Request.Builder().url(Path).build();

                    Response response = client.newCall(request).execute();

                    String string = response.body().string();

                    Log.d("ycf", string);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 更加点击事件使用okhttp拦截器,网络拦截器
     */
    public void interceptorNetWork(View view) {
        new Thread() {
            public void run() {
                try {
                    //建立OkHttpClient对象时,传入其拦截器对象,注意这里就变了一个方法addNetworkInterceptor
                    //注意:这里走了两次网络请求,因为网络重定向,使用日志拦截器
//                    OkHttpClient client = new OkHttpClient.Builder().addNetworkInterceptor(new LoggingInterceptor()).build();
                    //缓存拦截器,注意:有两个,我们用自己的拦截器,导包不要倒错了
                    OkHttpClient client = new OkHttpClient.Builder().addNetworkInterceptor(new CacheInterecepter()).build();
                    Request request = new Request.Builder().url(Path).build();
                    Response response = client.newCall(request).execute();
                    String string = response.body().string();
                    Log.d("ycf", string);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}

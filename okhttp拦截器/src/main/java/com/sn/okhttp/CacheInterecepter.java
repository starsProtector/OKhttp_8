package com.sn.okhttp;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * 自定义的缓存拦截器:如果服务器没有给文件在响应头中定义缓存标签,那么我们在拦截器中手动的给响应头加上标签
 * 1.自定义一个类,实现Interceptor
 * 2.在intercept方法中写自己的逻辑
 */
public class CacheInterecepter implements Interceptor {
    @Override//参数:连
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        return response.newBuilder()
                //设置缓存标签,及60秒的时长
                .header("Cache-Control", "max-age=60")
                .build();
    }
}

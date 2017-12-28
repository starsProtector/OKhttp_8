package com.sn.okhttp;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 日志拦截器类,请求来了,先在这里进行处理,可以得到发请求到得到请求消耗多久的时间
 * 作用:可以排查网络请求速度慢的根本原因
 *  1.有可能是我们在请求网络时,客户端写了一堆业务逻辑
 *  2.有可能是服务器端,写的有问题
 *  3.有可能就是网速不给力
 */
class LoggingInterceptor implements Interceptor {
  @Override public Response intercept(Interceptor.Chain chain) throws IOException {
    //拿到Request对象
    Request request = chain.request();

    long t1 = System.nanoTime();
    System.out.println(" request  = " + String.format("Sending request %s on %s%n%s",
            request.url(), chain.connection(), request.headers()));

    //拿到Response对象
    Response response = chain.proceed(request);

    long t2 = System.nanoTime();
    //得出请求网络,到得到结果,中间消耗了多长时间
    System.out.println("response  " + String.format("Received response for %s in %.1fms%n%s",
            response.request().url(), (t2 - t1) / 1e6d, response.headers()));
    return response;
  }
}
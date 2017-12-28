package com.sn.httpsdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;


import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

//注意:使用日志拦截器,要看看模拟有没有SD卡设备,用4.1.1
/**
android客户端以https访问服务器的流程:添加网络权限,依赖okhttp开源框架
     第一种方式:信任所有https主机的访问
         1.OkHttpClient初始化时,配置创建一个证书对象,校验名称,信任所有的主机,也就是信任所有https的请求
         对应所要创建的类,是固定模式,直接拷贝使用即可
         2.okhttp的正常使用

    第二种方式:信任当前证书的https主机的访问
         1.android客户端从服务器下载https证书,放到项目的assets文件下
         2.同okhttp的固定代码,进行https各种配置,比如X.509等,固定的模式,直接拷贝
         3.设置证书,从assets文件下,按照证书的名称读取设置给okhttp
         所以一般我们会把2,3两部封装成一个方法,暴露一个String参数,复制输入证书的名称,然后直接调用即可
         4.接下来就是初级call对象,异步请求,和okhttp的正常使用一样

 两种方式要想看效果,需要添加okhttp的日志拦截器方可

 提示:
     1.信任所有的服务器地址,所有的https网址,我们去请求数据时,都会返回的有数据.
     大部分公司没有去弄证书,偷懒所以就信任所有的服务器地址,这样合法性保证不了,但是数据加密没有问题.
     2.信任自己的服务器,加载自己的证书时,只有我们自己的服务器才会返回有数据,其他https网址就压根不会连接
     这样做最稳妥,下载自己的证书.校验本地证书.

 服务器通过命令行的模式生成一个证书(用java的工具类生成),然后服务器配置这个证书,支持https,端口号443.
 注意:使用日志拦截器,要看看模拟有没有SD卡设备,用4.1.1
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //信任所有https主机的访问
//        loadData();
        //信任当前证书的https主机的访问
        cardData();
    }

    /**
     * 信任所有https的请求：第一种实现，tls／ssl安全协议
     */
    private void loadData() {
        OkHttpClient httpClient =
                new OkHttpClient.Builder()
                        .addInterceptor(new LogInterceptor())//添加okhttp日志拦截器
                        //不加以下两行代码,https请求不到自签名的服务器
                        .sslSocketFactory(createSSLSocketFactory())//创建一个证书对象
                        .hostnameVerifier(new TrustAllHostnameVerifier())//校验名称,这个对象就是信任所有的主机,也就是信任所有https的请求
                        .connectTimeout(10, TimeUnit.SECONDS)//连接超时时间
                        .readTimeout(10, TimeUnit.SECONDS)//读取超时时间
                        .writeTimeout(10, TimeUnit.SECONDS)//写入超时时间
                        .retryOnConnectionFailure(false)//连接不上是否重连,false不重连
                        .build();

        //提交表单,输入的账户名和密码按照服务器定义的格式
        FormBody formbody = new FormBody.Builder().add("mobile", "18612991023").add("password", "111111").build();
        Request request = new Request.Builder().url("https://www.zhaoapi.cn/user/login").post(formbody).build();
//        Request request = new Request.Builder().url("http://www.12306.cn/mormhweb/").post(formbody).build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String s = response.body().toString();
                System.out.println(s);
            }
        });
    }

    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {}
        return ssfFactory;
    }

    private static class TrustAllCerts implements X509TrustManager {
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {}

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    //信任所有的服务器,返回true
    private static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    /**
     * 带证书验证
     */
    private void cardData() {
        //提交表单信息
        FormBody formbody = new FormBody.Builder().add("mobile", "18612991023").add("password", "111111").build();
        //请求对象初始化的设置,请求Url,请求方式,表单信息
        Request request = new Request.Builder().url("https://www.zhaoapi.cn/user/login").post(formbody).build();
        //setCard(),用时直接拷贝,app带证书验证
        setCard("zhaoapi_server.cer").newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

    /**
     * app带证书验证的方法,使用是修改一下zhaoapi_server.cer即可,其他都是固定的模式,直接拷贝
     */
    public OkHttpClient setCard(String zhenshu) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        try {
            //https固定模式,X.509是固定的模式
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            //关联证书的对象
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            String certificateAlias = Integer.toString(0);
            //核心逻辑,信任什么证书,从Assets读取拷贝进去的证书
            keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(getAssets().open(zhenshu)));
            SSLContext sslContext = SSLContext.getInstance("TLS");
            //信任关联器
            final TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            //初始化证书对象
            trustManagerFactory.init(keyStore);
            sslContext.init
                    (
                            null,
                            trustManagerFactory.getTrustManagers(),
                            new SecureRandom()
                    );
            builder.sslSocketFactory(sslContext.getSocketFactory());
            builder.addInterceptor(new LogInterceptor());
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.build();
    }

}

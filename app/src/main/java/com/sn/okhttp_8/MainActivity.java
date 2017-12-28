package com.sn.okhttp_8;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

//大部分代码和源码查看器一致.也就是网络请求那一块,使用的是OKHttp
//作业:使用OKhttp设置缓存
public class MainActivity extends AppCompatActivity {
    //    private String Path="http://192.168.253.1:8080/baidu.html";
//    private String Path = "http://publicobject.com/helloworld.txt";
    private String Path = "https://api-quality.jiemian.com/goodsCat/getBrandList";

    private TextView mText_tv;
    private static final int SUCCESS = 993;
    private static final int FALL = 814;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //加载网络成功,进行UI的更新
                case SUCCESS:
                    String text = (String) msg.obj;
                    mText_tv.setText(text);
                    break;
                //当加载网络失败,执行的逻辑代码
                case FALL:
                    Toast.makeText(MainActivity.this, "不好意思,李红展太帅了,造成网络异常", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //对控件进行初始化
        initView();
    }

    private void initView() {
        mText_tv = (TextView) findViewById(R.id.text_tv);
    }

    public void okhttp_ok(View viwe) {
        new Thread() {
            public void run() {
                OkHttpClient client = new OkHttpClient.Builder().build();
                Request request = new Request.Builder().url(Path).build();
                Response response = null;
                try {
                    response = client.newCall(request).execute();
                    //重定向
                    System.out.println("Response 1 response:          " + response);
                    System.out.println("Response 1 cache response:    " + response.cacheResponse());
                    System.out.println("Response 1 network response:  " + response.networkResponse());
                    String string = response.body().string();
                    //通过handler对象,把数据传入到主线程,进行UI更新
                    Message obtain = Message.obtain();
                    obtain.obj = string;
                    obtain.what = SUCCESS;
                    handler.sendMessage(obtain);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 通过点击事件,异步网络请求,拿到String数据
     *
     * @param view
     */
    public void okhttp_text(View view) {

//        Toast.makeText(MainActivity.this, "哈哈哈哈", Toast.LENGTH_SHORT).show();
        //创建oKHttpClient的对象
        OkHttpClient okHttpClient = new OkHttpClient();
        //创建Request.Builder.
        Request.Builder builder = new Request.Builder();
        //使用 Request.Builder 对象,调用Url方法,传入网络路径
        Request.Builder url = builder.url(Path);
        //使用  Request.Builder对象,调用build方法构建request对象.
        Request request = url.build();
        //创建一个Call对象,参数就request对象.
        Call call = okHttpClient.newCall(request);
        //使用call对象,调用enqueue方法,请求加入调度(异步加载)
        call.enqueue(new Callback() {
            @Override//当请求失败时,调用此方法
            public void onFailure(Call call, IOException e) {
                handler.sendEmptyMessage(FALL);
            }

            @Override//当你们请求成功的时候,调用此方法.
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                //通过handler对象,把数据传入到主线程,进行UI更新
                Message obtain = Message.obtain();
                obtain.obj = string;
                obtain.what = SUCCESS;
                handler.sendMessage(obtain);
            }
        });
    }

    /**
     * B.通过点击事件,异步网络请求,拿到String数据,并进行本地缓存,
     * @param view
     */
    public void okhttp_cache(View view) {
        //B.缓存大小
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        //B.创建Cache对象,文件存放私有目录   参数:1.私有目录文件对象   2.设置缓存大小
        Cache cache = new Cache(getCacheDir(), cacheSize);

        //B.创建oKHttpClient的对象,进行本地缓存(请求头,响应头,内容)
        OkHttpClient okHttpClient = new OkHttpClient.Builder().cache(cache).build();

        Request.Builder builder = new Request.Builder();
        Request.Builder url = builder.url(Path);
        Request request = url.build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override//当请求失败时,调用此方法
            public void onFailure(Call call, IOException e) {
                handler.sendEmptyMessage(FALL);
            }

            @Override//当你们请求成功的时候,调用此方法.
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                //通过handler对象,把数据传入到主线程,进行UI更新
                Message obtain = Message.obtain();
                obtain.obj = string;
                obtain.what = SUCCESS;
                handler.sendMessage(obtain);
            }
        });
    }
}

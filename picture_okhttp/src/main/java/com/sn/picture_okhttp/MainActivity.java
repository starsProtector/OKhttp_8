package com.sn.picture_okhttp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private String Path="https://10.url.cn/eth/ajNVdqHZLLAxibwnrOxXSzIxA76ichutwMCcOpA45xjiapneMZsib7eY4wUxF6XDmL2FmZEVYsf86iaw/";
    private static final int SUCCESS = 993;
    private static final int FALL = 814;
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //加载网络成功,进行UI的更新,处理得到的图片资源
                case SUCCESS:
                    //通过message,拿到字节数组
                    byte[] Picture = (byte[]) msg.obj;
                    //使用BitmapFactory工厂,把字节数组转换为bitmap
                    Bitmap bitmap = BitmapFactory.decodeByteArray(Picture, 0, Picture.length);
                    //通过ImageView,设置图片
                    mImageView_okhttp.setImageBitmap(bitmap);
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
    private ImageView mImageView_okhttp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化控件
        initView();
    }

    private void initView() {
        mImageView_okhttp = (ImageView) findViewById(R.id.imageView_okhttp);
    }

    /**
     * 根据点击事件获取络上的图片资源,使用的是OKhttp框架
     * @param view
     */
    public void Picture_okhttp_bt(View view){
        //1. 创建OKhttpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        //2.建立Request对象,设置参数,请求方式如果是get,就不用设置,默认使用的就是get
        Request request = new Request.Builder()
                .url(Path)//设置请求网址
                .build();//建立request对象
        //3.创建一个Call对象,参数是request对象,发送请求
        Call call = okHttpClient.newCall(request);
        //4.异步请求,请求加入调度
        call.enqueue(new Callback() {
            @Override//请求失败回调
            public void onFailure(Call call, IOException e) {
                handler.sendEmptyMessage(FALL);
            }

            @Override//请求成功回调
            public void onResponse(Call call, Response response) throws IOException {
                //得到从网上获取资源,转换成我们想要的类型
                byte[] Picture_bt = response.body().bytes();
                //通过handler更新UI
                Message message = handler.obtainMessage();
                message.obj=Picture_bt;
                message.what=SUCCESS;
                handler.sendMessage(message);

            }
        });
    }

    //当按钮点击时,执行使用OKhttp上传图片到服务器(http://blog.csdn.net/tangxl2008008/article/details/51777355)
    //注意:有时候上传图片失败,是服务器规定还要上传一个Key,如果开发中关于网络这一块出现问题,就多和面试官沟通沟通
    public void uploading(View view) {
        //图片上传接口地址
        String url="http://123.206.14.104:8080/FileUploadDemo/FileUploadServlet";
        //创建上传文件对象
        File file = new File(Environment.getExternalStorageDirectory(), "big.jpg");


        //创建RequestBody封装参数
        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        //创建MultipartBody,给RequestBody进行设置
        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "big.jpg", fileBody)
                .build();
        //创建Request
        Request request = new Request.Builder()
                .url(url)
                .post(multipartBody)
                .build();

        //创建okhttp对象
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10,TimeUnit.SECONDS)
                .writeTimeout(10,TimeUnit.SECONDS)
                .build();

        //上传完图片,得到服务器反馈数据
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ycf", "uploadMultiFile() e=" + e);
            }


            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i("ycf", "uploadMultiFile() response=" + response.body().string());
            }
        });


    }

}

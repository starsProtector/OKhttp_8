package com.sn.okhttpmanager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import java.util.HashMap;

/**
 * 我使用okhttp,我要吐槽的地方:
 * 1.okhttpClinet,你项目中有上百个类,你请求网络时,都要去创建一个okhttpClinet对象,那么就极大浪费内存
 * 2.okhttpclient,步骤非常的繁琐,代码冗余
 * 3.okhttp异步回调在子线程,你必然有一个操作,handler,把子线程得到数据放到主线程更新UI,创建handler对象
 */
public class MainActivity extends AppCompatActivity {
    //使用封装后的OKhttp,所定义的成员变量
    private OKhttpManager mOKhttpManager= OKhttpManager.getInstance();

    private String json_path = "http://publicobject.com/helloworld.txt";
    private String Login_path = "http://169.254.53.96:8080/web/LoginServlet";

    private String Picture_path="https://10.url.cn/eth/ajNVdqHZLLAxibwnrOxXSzIxA76ichutwMCcOpA45xjiapneMZsib7eY4wUxF6XDmL2FmZEVYsf86iaw/";
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = (ImageView) findViewById(R.id.imageView);
    }

    /**
     * 通过点击事件执行okhttp里封装的根据网址,获取字符串的逻辑操作.
     * @param view
     */
    public void okhttp_json(View view){
        mOKhttpManager.asyncJsonStringByURL(json_path, new OKhttpManager.Func1() {
            @Override
            public void onResponse(String result) {
                System.out.println(result);
            }
        });
    }

    //像服务器提交账号及密码
    public void okhttp_table(View view){
        HashMap<String,String> map = new HashMap<String, String>();
        map.put("qq","10000");
        map.put("pwd","abcde");
        mOKhttpManager.sendComplexForm(Login_path, map, new OKhttpManager.Func1() {
            @Override
            public void onResponse(String result) {
                System.out.println("登录状态: "+result);
            }
        });
    }

    //下载图片
    public void okhttp_picture(View view){

        mOKhttpManager.asyncGetByteByURL(Picture_path, new OKhttpManager.Func2() {
            @Override
            public void onResponse(byte[] result) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(result, 0, result.length);
                mImageView.setImageBitmap(bitmap);
            }
        });

    }



}

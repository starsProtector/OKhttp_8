package com.sn.post;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 有时访问,出现401是什么意思?怎么解决
 */
public class MainActivity extends AppCompatActivity {

    private EditText mEt_qq;
    private EditText mEt_pwd;
    private TextView mTv_status;

    String path = "http://169.254.53.96:8080/web/LoginServlet";

    private static final int SUCCESS = 665;
    private static final int FALL = 894;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    String text= (String) msg.obj;
                    mTv_status.setText(text);
                    break;
                case FALL:
                    Toast.makeText(MainActivity.this, "没有网", Toast.LENGTH_SHORT).show();
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
        //对控件进行初始化操作
        initVIew();
    }

    private void initVIew() {
        mEt_qq = (EditText) findViewById(R.id.et_qq);
        mEt_pwd = (EditText) findViewById(R.id.et_pwd);
        mTv_status = (TextView) findViewById(R.id.tv_status);
    }

    /**
     * 使用Post进行表单(键值对)上传,完成登录
     * @param view
     */
    public void login(View view){

        //得到用户输入的信息,进行非空判断
        String qq = mEt_qq.getText().toString().trim();
        String pwd =mEt_pwd.getText().toString().trim();
        if(TextUtils.isEmpty(qq) || TextUtils.isEmpty(pwd) ){
            Toast.makeText(MainActivity.this, "不能输入为空", Toast.LENGTH_SHORT).show();
            return;
        }

        //1.0 创建okhttpClinet
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10,TimeUnit.SECONDS)
                .writeTimeout(10,TimeUnit.SECONDS)
                .build();

        FormBody formBody= new FormBody.Builder()
                .add("qq", qq).add("pwd", pwd)
                .build();

        Request request= new Request.Builder()
                .post(formBody)
                .url(path)
                .build();

        Call call = okHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.sendEmptyMessage(FALL);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Message msg = Message.obtain();
                msg.obj=string;
                msg.what=SUCCESS;
                handler.sendMessage(msg);
            }
        });
    }
}

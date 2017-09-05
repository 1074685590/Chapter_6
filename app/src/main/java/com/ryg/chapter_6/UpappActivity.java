package com.ryg.chapter_6;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpappActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "UpappActivity";
    private OkHttpClient mOkHttpClient;
    private ImageView mUpapp_iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upapp);
        findViewById(R.id.btn_up).setOnClickListener(this);
        mUpapp_iv = (ImageView) findViewById(R.id.upapp_iv);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_up:
                upApp();
                break;
        }

    }

    private void upApp() {
//        downAsynFile();
        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra("apkUrl", "http://acj3.pc6.com/pc6_soure/2017-8/com.kitchen_b2c_23.apk");
        startService(intent);
    }

    private void getAsynHttp() {
        mOkHttpClient=new OkHttpClient();
        Request.Builder requestBuilder = new Request.Builder().url("http://www.baidu.com");
        //可以省略，默认是GET请求
        requestBuilder.method("GET",null);
        Request request = requestBuilder.build();
        Call mcall= mOkHttpClient.newCall(request);
        mcall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (null != response.cacheResponse()) {
                    String str = response.cacheResponse().toString();
                    Log.i("wangshu", "cache---" + str);
                } else {
                    response.body().string();
                    String str = response.networkResponse().toString();
                    Log.i("wangshu", "network---" + str);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "请求成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    private void downAsynFile() {
        mOkHttpClient = new OkHttpClient();
//        String url = "http://img.my.csdn.net/uploads/201603/26/1458988468_5804.jpg";
        String url = "http://acj3.pc6.com/pc6_soure/2017-8/com.kitchen_b2c_23.apk";
        Request request = new Request.Builder().url(url).build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) {
                final InputStream inputStream = response.body().byteStream();
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(new File("/sdcard/wangshu.jpg"));
                    byte[] buffer = new byte[2048];
                    int len = 0;
                    while ((len = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                    }
                    fileOutputStream.flush();
                } catch (IOException e) {
                    Log.d(TAG, "onResponse: IOException");
                    e.printStackTrace();
                }
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mUpapp_iv.setImageBitmap(BitmapFactory.decodeFile("/sdcard/wangshu.jpg"));
//                    }
//                });
                Log.d(TAG, "onResponse: 文件下载成功");
                Log.d(TAG, "onResponse: 所在线程线程"+Thread.currentThread().getName());
            }
        });
//        mOkHttpClient.newCall(new FileCallBack());
    }
}

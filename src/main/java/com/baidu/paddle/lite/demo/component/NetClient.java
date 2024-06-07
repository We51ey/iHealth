package com.baidu.paddle.lite.demo.component;

import android.os.Looper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetClient {
    private static NetClient netClient;
    private NetClient(){
        client = initOkHttpClient();
    }
    public final OkHttpClient client;

    /**
     * 初始化OkHttpClient
     * @return
     */
    private OkHttpClient initOkHttpClient(){
        //初始化的时候可以自定义一些参数
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(5000, TimeUnit.MILLISECONDS)//设置读取超时为10秒
                .connectTimeout(5000, TimeUnit.MILLISECONDS)//设置链接超时为10秒
                .build();
        return okHttpClient;
    }

    /**
     * 异步的Get请求
     * @return
     */
    public static NetClient getNetClient(){
        if(netClient == null){
            netClient = new NetClient();
        }
        return netClient;
    }

    /**
     * 同步的Get请求
     * @param url
     * @return Response
     */
    public void callNet(String url, final MyCallBack mCallback){
        Request request = new Request.Builder().url(url).build();
        Call call = getNetClient().initOkHttpClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Looper.prepare();
                //请求网络失败，调用自己的接口，通过传回的-1可以知道错误类型
                mCallback.onFailure(-1);
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //请求网络成功说明服务器有响应，但返回的是什么我们无法确定，可以根据响应码判断
                if (response.code() == 200) {
                    Looper.prepare();
                    //如果是200说明正常，调用MyCallBack的成功方法，传入数据
                    mCallback.onResponse(response.body().string());
                    Looper.loop();
                }else{
                    Looper.prepare();
                    //如果不是200说明异常，调用MyCallBack的失败方法将响应码传入
                    mCallback.onFailure(response.code());
                    Looper.loop();
                }
            }
        });
    }


}

package com.example.lcweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by admin on 2017/9/4.
 */

public class HttpUtil {

    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request
                .Builder()
                .url(address)
                .build();
        //自动开启子线程的耗时作业
        client.newCall(request).enqueue(callback);
    }
}

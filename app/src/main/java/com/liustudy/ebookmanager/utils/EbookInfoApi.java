package com.liustudy.ebookmanager.utils;

import android.util.Log;

import com.liustudy.ebookmanager.beans.Constant;
import com.liustudy.ebookmanager.beans.EbookInfo;
import com.liustudy.ebookmanager.interfaces.Api;

import java.io.IOException;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EbookInfoApi implements Api<EbookInfo> {
    @Override
    public EbookInfo download() {

        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url(Constant.url)
                .get()
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("onFailure",e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
            }
        });

        return null;
    }
}

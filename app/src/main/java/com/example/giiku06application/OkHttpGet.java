package com.example.giiku06application;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpGet extends AsyncTask<String,String,String> {

    private static final String API_HOST = "navitime-route-totalnavi.p.rapidapi.com";
    private static final String API_KEY = "ee6978f011mshb71e6fca9594701p1c2bcbjsn3aa55c5144d5";
    private String currentTime;
    private String latitude;
    private String longitude;
    private String goalPoint;
    // コールバックインターフェースの定義
    public interface OnDataReceivedListener {
        void onDataReceived(String resData);
    }

    private OnDataReceivedListener listener;

    public OkHttpGet(String currentTime, String latitude, String longitude, String goalPoint, OnDataReceivedListener listener){
        this.currentTime = currentTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.goalPoint = goalPoint;
        this.listener = listener;
    }


    @Override
    protected String doInBackground(String... strings) {
        String apiUrl =
                "https://navitime-route-totalnavi.p.rapidapi.com/route_transit?start=" + latitude + "%2C" + longitude +
                        "&goal=" + goalPoint +
                        "&start_time=" + currentTime +
                        "&datum=wgs84&term=1440&limit=3&coord_unit=degree";

        Log.d("DEBUG", "url : " + apiUrl);
        // レスポンスデータ
        String resData = "";
        // OkHttpのインスタンス生成
        OkHttpClient client = new OkHttpClient();
        // リクエストをビルド
        Request request = new Request.Builder()
                .url(apiUrl)
                .get()
                .addHeader("X-RapidAPI-Key", API_KEY)
                .addHeader("X-RapidAPI-Host", API_HOST)
                .build();

        Log.d("DEBUG", "time : " + currentTime);
        Log.d("DEBUG", "lat : " + latitude);
        Log.d("DEBUG", "lon : " + longitude);
        try {
            // リクエスト実行
            Response response = client.newCall(request).execute();
            // レスポンスのbodyからデータ取得
            resData = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resData;
    }

    @Override
    protected void onPostExecute(String resData) {
        // 結果をコールバックする
        if (listener != null) {
            listener.onDataReceived(resData);
        }
    }
}

package com.example.giiku06application;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetGoalPoint extends AsyncTask<String, String, String> {
    private static final String API_HOST = "navitime-transport.p.rapidapi.com";
    private static final String API_KEY = "ee6978f011mshb71e6fca9594701p1c2bcbjsn3aa55c5144d5";
    private String keyWord;

    // コールバックインターフェースの定義
    public interface OnGoalPointReceivedListener {
        void onGoalPointReceived(String resGoalPoint);
    }

    private OnGoalPointReceivedListener listener;

    public GetGoalPoint(String keyWord, GetGoalPoint.OnGoalPointReceivedListener listener){
        this.keyWord = keyWord;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... strings) {
        String apiUrl =
                "https://navitime-transport.p.rapidapi.com/transport_node?word=" + keyWord +
                        "&coord_unit=degree&offset=0&datum=wgs84&limit=1";

        Log.d("DEBUG", "searchUrl : " + apiUrl);
        // レスポンスデータ
        String resGoalPoint = "";
        // OkHttpのインスタンス生成
        OkHttpClient client = new OkHttpClient();
        // リクエストをビルド
        Request request = new Request.Builder()
                .url(apiUrl)
                .get()
                .addHeader("X-RapidAPI-Key", API_KEY)
                .addHeader("X-RapidAPI-Host", API_HOST)
                .build();

        try {
            // リクエスト実行
            Response response = client.newCall(request).execute();
            // レスポンスのbodyからデータ取得
            resGoalPoint = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resGoalPoint;
    }

    @Override
    protected void onPostExecute(String resGoalPoint) {
        // 結果をコールバックする
        if (listener != null) {
            listener.onGoalPointReceived(resGoalPoint);
        }
    }
}
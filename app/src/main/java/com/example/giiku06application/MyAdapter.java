package com.example.giiku06application;

import static android.widget.RemoteViewsService.*;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class MyAdapter implements RemoteViewsService.RemoteViewsFactory {
    private final Context context;
    private final String[] ride_time;
    private final String[] drop_off_time;
    private final String[] route_text;
    private final String[] last_station_text;

    public MyAdapter(Context context, String[] ride_time, String[] drop_off_time, String[] route_text, String[] last_station_text) {
        this.context = context;
        this.ride_time = ride_time;
        this.drop_off_time = drop_off_time;
        this.route_text = route_text;
        this.last_station_text = last_station_text;
    }

    @Override
    public void onCreate() {
        // 初期化の処理を行う場合に実装

    }

    @Override
    public void onDataSetChanged() {
        // データソースの変更があった場合に実装
//        notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        // 終了処理を行う場合に実装
    }

    @Override
    public int getCount() {
        return last_station_text.length;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_item);

        // データソースから必要な情報を取得し、ビューに設定
        remoteViews.setTextViewText(R.id.ride_time,ride_time[position]);
        remoteViews.setTextViewText(R.id.drop_off_time,drop_off_time[position]);
        remoteViews.setTextViewText(R.id.route_text,route_text[position]);
        remoteViews.setTextViewText(R.id.last_station_text,last_station_text[position]);

        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        // ローディング中のビューをカスタマイズする場合に実装
        return null;
    }

    @Override
    public int getViewTypeCount() {
        // 表示するビューのタイプの数を返す場合に実装
        return 1;
    }

    @Override
    public long getItemId(int position) {
        // ビューのIDを返す場合に実装
        return position;
    }

    @Override
    public boolean hasStableIds() {
        // アイテムが安定したIDを持つかどうかを返す場合に実装
        return true;
    }
}
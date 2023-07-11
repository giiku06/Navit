package com.example.giiku06application;

import static android.widget.RemoteViewsService.*;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class MyAdapter implements RemoteViewsService.RemoteViewsFactory {
    private final Context context;
    private final String[] dataSource;

    public MyAdapter(Context context, String[] dataSource) {
        this.context = context;
        this.dataSource = dataSource;
        Log.d("TAG", "MyAdapter: "+dataSource);
    }

    @Override
    public void onCreate() {
        // 初期化の処理を行う場合に実装

    }

    @Override
    public void onDataSetChanged() {
        // データソースの変更があった場合に実装
    }

    @Override
    public void onDestroy() {
        // 終了処理を行う場合に実装
    }

    @Override
    public int getCount() {
        return dataSource.length;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_item);

        // データソースから必要な情報を取得し、ビューに設定
        remoteViews.setTextViewText(R.id.last_station_text,dataSource[position]);

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
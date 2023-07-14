package com.example.giiku06application;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.TextView;

import java.util.ArrayList;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Implementation of App Widget functionality.
 */
public class MainAppWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        //listに渡すデータの作成
        String[] ride_time = {"11 : 00", "12 : 00", "13 : 00"};
        String[] drop_off_time = {"11 : 30", "12 : 30", "13 : 30"};
        String[] route_text = {"東山線", "鶴舞線", "桜通線"};
        String[] last_station_text = {"高畑行", "上小田井行", "太閤通行"};
        //データの格納
        Bundle bundle = new Bundle();
        bundle.putStringArray("ride_time", ride_time);
        bundle.putStringArray("drop_off_time", drop_off_time);
        bundle.putStringArray("route_text", route_text);
        bundle.putStringArray("last_station_text", last_station_text);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main_app_widget);
        Intent intent = new Intent(context, MyWidgetService.class);
        intent.putExtras(bundle);
        views.setRemoteAdapter(R.id.widget_list_view, intent);
        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

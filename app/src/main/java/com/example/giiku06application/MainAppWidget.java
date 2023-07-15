package com.example.giiku06application;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.TextView;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Implementation of App Widget functionality.
 */
public class MainAppWidget extends AppWidgetProvider implements OkHttpGet.OnDataReceivedListener{

    //listに渡すデータの作成
    String[] ride_time = {"11 : 00", "12 : 00", "13 : 00"};
    String[] drop_off_time = {"11 : 30", "12 : 30", "13 : 30"};
    String[] route_text = {"東山線", "鶴舞線", "桜通線"};
    String[] last_station_text = {"高畑行", "上小田井行", "太閤通行"};
    public static final String ACTION_MANUAL_UPDATE = "com.example.giiku06application.MANUAL_UPDATE";
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        int sec = calendar.get(Calendar.SECOND);
        @SuppressLint("DefaultLocale") String currentTime = String.format("%04d-%02d-%02dT%02d:%02d:%02d", year, month, day, hour, min, sec);
        SharedPreferences sharedPreferences = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        String latitude = sharedPreferences.getString("latitude", "35.170222");
        String longitude = sharedPreferences.getString("longitude", "136.883082");
        // OkHttpGetのインスタンスを使用して処理を行う
        OkHttpGet okHttpGet = new OkHttpGet(currentTime, latitude, longitude,  "35.170222,136.883082", this);
        okHttpGet.execute();
        Log.d("onUpdate", "onUpdate: test");
        //データの格納
        Bundle bundle = new Bundle();
        bundle.putStringArray("ride_time", ride_time);
        bundle.putStringArray("drop_off_time", drop_off_time);
        bundle.putStringArray("route_text", route_text);
        bundle.putStringArray("last_station_text", last_station_text);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main_app_widget);
        Intent intent = new Intent(context, MyWidgetService.class);
        Intent manualUpdateIntent = new Intent(context, MainAppWidget.class);
        manualUpdateIntent.setAction(ACTION_MANUAL_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, manualUpdateIntent, PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.button_manual_update, pendingIntent);
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
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction().equals(ACTION_MANUAL_UPDATE)) {
            // 手動更新アクションを受信した場合、onUpdateを呼び出す
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisAppWidget = new ComponentName(context.getPackageName(), MainAppWidget.class.getName());
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
            onUpdate(context, appWidgetManager, appWidgetIds);
            Log.d("ACTION_MANUAL_UPDATE", "onReceive: ACTION_MANUAL_UPDATE");
        }
    }

    @Override
    public void onDataReceived(String resData) {
        // 受け取ったJSONデータを解析するなどの処理を行う
        try {
            JSONObject rootJSON = new JSONObject(resData);
            JSONArray itemsArray = rootJSON.getJSONArray("items");

//            出発時刻の配列
            String[] fromTimes = new String[itemsArray.length()];
//            到着時刻の配列
            String[] toTimes = new String[itemsArray.length()];

//            路線名の配列
            String[] lineNames = new String[itemsArray.length()];
//            目的地から近い最寄り駅の配列
            String[] goalStationNames = new String[itemsArray.length()];

//            items(経路の候補)[0~2]まで回す
            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject item = itemsArray.getJSONObject(i);
                JSONObject summary = item.getJSONObject("summary");
                JSONObject move = summary.getJSONObject("move");

//                日時の取得
                String fromTime = move.getString("from_time");
                String toTime = move.getString("to_time");

//                取得日時からhh:mmのみ切り出しフォーマット→配列に格納
                String formattedFromTime = fromTime.substring(11, 16);
                fromTimes[i] = formattedFromTime;
                String formattedToTime = toTime.substring(11, 16);
                toTimes[i] = formattedToTime;

//                sections部
                JSONArray sectionsArray = item.getJSONArray("sections");

//                路線名の抽出処理
                String lineName = "";
                for (int j = 0; j < sectionsArray.length(); j++) {
                    JSONObject section = sectionsArray.getJSONObject(j);

//                    section配列からtype:moveで、move:walkでないものを抽出
                    if (section.getString("type").equals("move") &&
                            !section.getString("move").equals("walk")){
                        lineName = section.getString("line_name");
                        break; //ひとつ目が抽出出来たらループを抜ける
                    }
                }
                lineNames[i] = lineName;

//                目的地の最寄り駅の抽出処理
                String goalStationName = "";
                for (int j = sectionsArray.length() - 1; j >= 0; j--) {
                    JSONObject section = sectionsArray.getJSONObject(j);
//                    section配列の後ろからtype:pointで、node_idが含まれているものを検索
                    if (section.getString("type").equals("point") && section.has("node_id")) {
                        goalStationName = section.getString("name");
                        break; // 抽出できたらループを抜ける
                    }
                }
                goalStationNames[i] = goalStationName;

            }
            String fromTimesString = TextUtils.join(",", fromTimes);
            String toTimesString = TextUtils.join(",", toTimes);
            String lineNamesString = TextUtils.join(",", lineNames);
            String goalStationNamesString = TextUtils.join(",", goalStationNames);

            Log.d("DEBUG", "from: " + fromTimesString);
            Log.d("DEBUG", "to: " + toTimesString);
            Log.d("DEBUG", "line: " + lineNamesString);
            Log.d("DEBUG", "goal: " + goalStationNamesString);

            //listに渡すデータの作成
            ride_time = fromTimesString.split(",");
            drop_off_time = toTimesString.split(",");
            route_text = lineNamesString.split(",");
            last_station_text = goalStationNamesString.split(",");
            Log.d("ride_time", "onDataReceived: "+ride_time[0]);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

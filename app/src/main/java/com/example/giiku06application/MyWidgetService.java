package com.example.giiku06application;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MyWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new NavitWidgetFactory();
    }

    private class NavitWidgetFactory implements RemoteViewsFactory {

        private static final String TAG = "NavitViewFactory";

        private JSONArray jsons = new JSONArray();

        //初期データの作成
        String[] ride_time = {"11 : 00", "12 : 00", "13 : 00"};
        String[] drop_off_time = {"11 : 30", "12 : 30", "13 : 30"};
        String[] route_text = {"東山線", "鶴舞線", "桜通線"};
        String[] last_station_text = {"高畑行", "上小田井行", "太閤通行"};
        int year;
        int month;
        int day;
        int hour;
        int min;
        Intent ClickIntent;

        @Override
        public void onCreate() {
            Log.d(TAG, "onCreate: ");
        }

        @Override
        public void onDataSetChanged() {
            fetchData();
        }

        private void fetchData() {
            Log.d(TAG, "fetchData: start fetch");
            try {
                Context context = getApplicationContext();
                Calendar calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH) + 1;
                day = calendar.get(Calendar.DATE);
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                min = calendar.get(Calendar.MINUTE);
                int sec = calendar.get(Calendar.SECOND);
                @SuppressLint("DefaultLocale") String currentTime = String.format("%04d-%02d-%02dT%02d:%02d:%02d", year, month, day, hour, min, sec);
        //        位置情報の受け取り
                SharedPreferences sharedPreferences = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
                String latitude = sharedPreferences.getString("latitude", "35.170222");
                String longitude = sharedPreferences.getString("longitude", "136.883082");
        //        目的地の最寄り駅のID受け取り
                SharedPreferences sharedGoalPoint = context.getSharedPreferences("goal_point_pref", Context.MODE_PRIVATE);
                String goalPoint = sharedGoalPoint.getString("goalPoint", "00000094");
                String apiUrl =
                        "https://navitime-route-totalnavi.p.rapidapi.com/route_transit?start=" + latitude + "%2C" + longitude +
                                "&goal=" + goalPoint +
                                "&start_time=" + currentTime +
                                "&datum=wgs84&term=1440&limit=3&coord_unit=degree";
                OkHttpClient httpClient = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(apiUrl)
                        .get()
                        .addHeader("X-RapidAPI-Key", API_KEY)
                        .addHeader("X-RapidAPI-Host", API_HOST)
                        .build();
                try (Response response = httpClient.newCall(request).execute()) {
                    String responseStr = Objects.requireNonNull(response.body()).string();
                    JSONObject rootJSON = new JSONObject(responseStr);

//                    JSON生データの格納
                    jsons = rootJSON.getJSONArray("items");
                    moldData();
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                Log.d(TAG, "fetchData: error");
            }
        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            return jsons.length();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if(jsons.length() <= 0) {
                return null;
            }
            RemoteViews rv = null;
            if(jsons != null) {
                rv = new RemoteViews(getPackageName(), R.layout.widget_item);

                // データセット
                rv.setTextViewText(R.id.ride_time,ride_time[position]);
                rv.setTextViewText(R.id.drop_off_time,drop_off_time[position]);
                rv.setTextViewText(R.id.route_text,route_text[position]);
                rv.setTextViewText(R.id.last_station_text,last_station_text[position]);

                rv.setOnClickFillInIntent(R.id.view_item, ClickIntent);
            }

            return rv;
        }

        private void moldData() {
            try {
                JSONArray itemsArray = jsons;
                // 出発時刻の配列
                String[] fromTimes = new String[itemsArray.length()];
                // 到着時刻の配列
                String[] toTimes = new String[itemsArray.length()];

                // 路線名の配列
                String[] lineNames = new String[itemsArray.length()];
                // 目的地から近い最寄り駅の配列
                String[] goalStationNames = new String[itemsArray.length()];
                // 通過駅情報を格納した配列
//                JSONArray[] roadMapArrays = new JSONArray[itemsArray.length()];
                JSONArray roadMapArrays = new JSONArray();


                //            items(経路の候補)[0~2]まで回す
                for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject item = itemsArray.getJSONObject(i);

                    // itemsの中のSummary→Summaryの中のmoveを抽出
                    JSONObject summary = item.getJSONObject("summary");
                    JSONObject move = summary.getJSONObject("move");

                    //sections部（スタートからゴールまでの全ての通過点を抽出）
                    JSONArray sectionsArray = item.getJSONArray("sections");

                    // 到着時間をmove>to_timeから取得
                    String toTime = move.getString("to_time");
                    //　取得日時からhh:mmのみ切り出しフォーマット→配列に格納
                    String formattedToTime = toTime.substring(11, 16);
                    toTimes[i] = formattedToTime;

                    // 路線名の抽出処理
                    String lineName = "";
//                    sections部の中の各sectionを順番に閲覧
                    for (int j = 0; j < sectionsArray.length(); j++) {
                        JSONObject section = sectionsArray.getJSONObject(j);

                        // section配列からtype:moveで、move:walkでないものを抽出
                        if (section.getString("type").equals("move") &&
                                !section.getString("move").equals("walk")) {
                            lineName = section.getString("line_name");

                            JSONObject rideOnSection = sectionsArray.getJSONObject(j);
                            // 電車乗車時間であるsections>乗車駅＋1>from_timeから取得に変更する
                            String fromTime = rideOnSection.getString("from_time");
                            //　取得日時からhh:mmのみ切り出しフォーマット→配列に格納
                            String formattedFromTime = fromTime.substring(11, 16);
                            fromTimes[i] = formattedFromTime;

                            break; //ひとつ目が抽出出来たらループを抜ける
                        }
                    }
                    lineNames[i] = lineName.substring(0, Math.min(lineName.length(), 8));;

                    // 目的地の最寄り駅の抽出処理
                    String goalStationName = "";
                    for (int j = sectionsArray.length() - 1; j >= 0; j--) {
                        JSONObject section = sectionsArray.getJSONObject(j);
                        // section配列の後ろからtype:pointで、node_idが含まれているものを検索
                        if (section.getString("type").equals("point") && section.has("node_id")) {
                            goalStationName = section.getString("name");
                            break; // 抽出できたらループを抜ける
                        }
                    }
                    goalStationNames[i] = goalStationName.substring(0, Math.min(goalStationName.length(), 5));

                    JSONArray roadMapArray = new JSONArray();
                    // SectionsArrayの1からLength-1までを切り取る（0と最後は不要なデータのため）
                    for (int k = 1; k < sectionsArray.length(); k++) {
                        JSONObject sectionObject = sectionsArray.getJSONObject(k);
                        roadMapArray.put(sectionObject);
                    }
                    Log.d("debug", "moldData: "+roadMapArray);
                    roadMapArrays.put(i,roadMapArray);

                }
                String fromTimesString = TextUtils.join(",", fromTimes);
                String toTimesString = TextUtils.join(",", toTimes);
                String lineNamesString = TextUtils.join(",", lineNames);
                String goalStationNamesString = TextUtils.join(",", goalStationNames);
//                String roadMapArraysString = TextUtils.join(",", roadMapArrays);

                Log.d("moldData", "from: " + fromTimesString);
                Log.d("moldData", "to: " + toTimesString);
                Log.d("moldData", "line: " + lineNamesString);
                Log.d("moldData", "goal: " + goalStationNamesString);

                Log.d("DEBUG", "roadMaps" + roadMapArrays.toString());

                SharedPreferences sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("roadMaps", roadMapArrays.toString());
                editor.apply();


                //listに渡すデータの作成
                ride_time = fromTimesString.split(",");
                drop_off_time = toTimesString.split(",");
                route_text = lineNamesString.split(",");
                last_station_text = goalStationNamesString.split(",");

                String station = roadMapArrays.getJSONArray(0).getJSONObject(1).getString("name");
                Log.d(TAG, "station: "+station);
                ClickIntent = new Intent();
                ClickIntent.setData(
                    Uri.parse("https://www.navitime.co.jp/transfer/searchlist?" +
                            "orvStationName=" + station +
                            "&dnvStationName=" + last_station_text[0] +
                            "&year=" + year +
                            "&month=" + month +
                            "&day=" + day +
                            "&hour=" + hour +
                            "&minute=" + min +

                            "&basis=1&freePass=0&sort=4&wspeed=100&airplane=1&sprexprs=1&utrexprs=1&othexprs=1&mtrplbus=1&intercitybus=1&ferry=1&wspeed=100&airplane=1&sprexprs=1&utrexprs=1&othexprs=1&mtrplbus=1&intercitybus=1&ferry=1"
                    )
                );

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            Log.v(TAG, "[getItemId]: " + position);

            return position;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}

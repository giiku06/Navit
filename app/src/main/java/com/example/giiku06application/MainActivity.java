package com.example.giiku06application;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.AsyncTask;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.util.Log;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements LocationListener, GetGoalPoint.OnGoalPointReceivedListener {

    private LocationManager mLocationManager;
    private String bestProvider;
    private EditText inputGoalPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedGoalPoint = getSharedPreferences("goal_point_pref", Context.MODE_PRIVATE);
        String goalPointName = sharedGoalPoint.getString("goalPointName","-1");
        if (!goalPointName.equals("-1")){
            TextView textView = findViewById(R.id.setting_goal_point);
            textView.setText(goalPointName);
        }
        initLocationManager();
        inputGoalPoint = (EditText) findViewById(R.id.input_goal_point);
    }

//    目的地付近の最寄り駅を検索
    public void SearchPoint(View MyButton) {
        String keyWord = inputGoalPoint.getText().toString();

        if (!keyWord.isEmpty()){
//            最寄り駅の検索をリクエスト
            GetGoalPoint getGoalPoint = new GetGoalPoint(keyWord, this);
            getGoalPoint.execute();
        }
    }
    @Override
    public void onGoalPointReceived(String resGoalPoint) {
        // 受け取ったJSONデータを解析するなどの処理を行う
        try {
            JSONObject rootJSON = new JSONObject(resGoalPoint);
            JSONArray itemsArray = rootJSON.getJSONArray("items");
            if (itemsArray.length() > 0) {
                JSONObject item = itemsArray.getJSONObject(0);
                Log.d("TAG", "onGoalPointReceived: "+item.toString());
                String goal_id = item.getString("id");
                TextView errorMsg = findViewById(R.id.error_msg);
                errorMsg.setVisibility(View.INVISIBLE);

//            MainAppWidgetに値の受け渡し
                SharedPreferences sharedGoalPoint = getSharedPreferences("goal_point_pref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedGoalPoint.edit();
                editor.putString("goalPoint", goal_id);
                String goalPointName = item.getString("name");
                editor.putString("goalPointName", goalPointName);
                TextView textView = findViewById(R.id.setting_goal_point);
                textView.setText(goalPointName);
                editor.apply();

                Log.d("DEBUG", "goal_id :" + goal_id);
            }else {
                // itemsが空の場合の処理
                TextView errorMsg = findViewById(R.id.error_msg);
                errorMsg.setText("目的地周辺に駅が見つかりません");
                errorMsg.setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        locationStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationStop();
    }

    private void initLocationManager() {
//        インスタンス生成
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//        詳細設定
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setSpeedRequired(false);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
        bestProvider = mLocationManager.getBestProvider(criteria, true);
    }

    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            // パーミッションの許可を取得する
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
        }
    }
    private void locationStart() {
        checkPermission();
        mLocationManager.requestLocationUpdates(bestProvider, 60000, 3, this);
    }

    private void locationStop() {
        mLocationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
//        位置情報の変更が入った時に行う処理

        // 現在時刻を取得して表示する
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        int sec = calendar.get(Calendar.SECOND);

        @SuppressLint("DefaultLocale") String currentTime = String.format("%04d-%02d-%02dT%02d:%02d:%02d", year, month, day, hour, min, sec);

        String latitude = String.valueOf(location.getLatitude());
        String longitude = String.valueOf(location.getLongitude());
        SharedPreferences sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("latitude", latitude);
        editor.putString("longitude", longitude); // データを保存する
        editor.apply();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("DEBUG", "called onProviderDisabled");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("DEBUG", "called onProviderEnabled");
    }

    // OkHttpGetからのデータ受け取り処理
//    @Override
//    public void onDataReceived(String resData) {
//        // 受け取ったJSONデータを解析するなどの処理を行う
//        try {
//            JSONObject rootJSON = new JSONObject(resData);
//            JSONArray itemsArray = rootJSON.getJSONArray("items");
//
////            出発時刻の配列
//            String[] fromTimes = new String[itemsArray.length()];
////            到着時刻の配列
//            String[] toTimes = new String[itemsArray.length()];
//
////            路線名の配列
//            String[] lineNames = new String[itemsArray.length()];
////            目的地から近い最寄り駅の配列
//            String[] goalStationNames = new String[itemsArray.length()];
//
////            items(経路の候補)[0~2]まで回す
//            for (int i = 0; i < itemsArray.length(); i++) {
//                JSONObject item = itemsArray.getJSONObject(i);
//                JSONObject summary = item.getJSONObject("summary");
//                JSONObject move = summary.getJSONObject("move");
//
////                日時の取得
//                String fromTime = move.getString("from_time");
//                String toTime = move.getString("to_time");
//
////                取得日時からhh:mmのみ切り出しフォーマット→配列に格納
//                String formattedFromTime = fromTime.substring(11, 16);
//                fromTimes[i] = formattedFromTime;
//                String formattedToTime = toTime.substring(11, 16);
//                toTimes[i] = formattedToTime;
//
////                sections部
//                JSONArray sectionsArray = item.getJSONArray("sections");
//
////                路線名の抽出処理
//                String lineName = "";
//                for (int j = 0; j < sectionsArray.length(); j++) {
//                    JSONObject section = sectionsArray.getJSONObject(j);
//
////                    section配列からtype:moveで、move:walkでないものを抽出
//                    if (section.getString("type").equals("move") &&
//                        !section.getString("move").equals("walk")){
//                        lineName = section.getString("line_name");
//                        break; //ひとつ目が抽出出来たらループを抜ける
//                    }
//                }
//                lineNames[i] = lineName;
//
////                目的地の最寄り駅の抽出処理
//                String goalStationName = "";
//                for (int j = sectionsArray.length() - 1; j >= 0; j--) {
//                    JSONObject section = sectionsArray.getJSONObject(j);
////                    section配列の後ろからtype:pointで、node_idが含まれているものを検索
//                    if (section.getString("type").equals("point") && section.has("node_id")) {
//                        goalStationName = section.getString("name");
//                        break; // 抽出できたらループを抜ける
//                    }
//                }
//                goalStationNames[i] = goalStationName;
//
//            }
//            String fromTimesString = TextUtils.join(",", fromTimes);
//            String toTimesString = TextUtils.join(",", toTimes);
//            String lineNamesString = TextUtils.join(",", lineNames);
//            String goalStationNamesString = TextUtils.join(",", goalStationNames);
//
//            Log.d("DEBUG", "from: " + fromTimesString);
//            Log.d("DEBUG", "to: " + toTimesString);
//            Log.d("DEBUG", "line: " + lineNamesString);
//            Log.d("DEBUG", "goal: " + goalStationNamesString);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
}
package com.example.giiku06application;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String API_HOST = "https://navitime-transport.p.rapidapi.com/transport_node/around?coord=35.166816%2C136.952957&limit=2&term=60&datum=wgs84&coord_unit=degree&walk_speed=5";
    private static final String API_KEY = "ee6978f011mshb71e6fca9594701p1c2bcbjsn3aa55c5144d5";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://navitime-transport.p.rapidapi.com/transport_node/id?id=00005077&datum=wgs84&coord_unit=degree")
                .get()
                .addHeader("X-RapidAPI-Key", "ee6978f011mshb71e6fca9594701p1c2bcbjsn3aa55c5144d5")
                .addHeader("X-RapidAPI-Host", "navitime-transport.p.rapidapi.com")
                .build();

        try {
            Response response = client.newCall(request).execute();
            Log.d("res", "onCreate: "+response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
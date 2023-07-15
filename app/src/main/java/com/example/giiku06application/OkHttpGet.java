package com.example.giiku06application;

import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpGet {
    private static final String API_HOST = "navitime-transport.p.rapidapi.com";
    private static final String API_KEY = "ee6978f011mshb71e6fca9594701p1c2bcbjsn3aa55c5144d5";
    private String currentTime;
    private String latitude;
    private String longitude;

    private String goalDummy;

    public OkHttpGet(String currentTime, String latitude, String longitude, String goalDummy){
        this.currentTime = currentTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.goalDummy = goalDummy;
    }
    String apiUrl =
            "https://navitime-route-totalnavi.p.rapidapi.com/route_transit?start=" + latitude + "%2C" + longitude +
                    "&goal=" + goalDummy +
                    "&start_time=" + currentTime +
                    "&datum=wgs84&term=1440&limit=3&coord_unit=degree";
    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder()
            .url(apiUrl)
            .get()
            .addHeader("X-RapidAPI-Key", API_KEY)
            .addHeader("X-RapidAPI-Host", API_HOST)
            .build();
//
//    Response response = client.newCall(request).execute();
//        Log.d("res", "onCreate: " + response);
}

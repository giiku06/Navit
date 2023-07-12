package com.example.giiku06application;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViewsService;

public class MyWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Bundle bundle = intent.getExtras();
        String[] ride_time = bundle.getStringArray("ride_time");
        String[] drop_off_time = bundle.getStringArray("drop_off_time");
        String[] route_text = bundle.getStringArray("route_text");
        String[] last_station_text = bundle.getStringArray("last_station_text");
        return new MyAdapter(this.getApplicationContext(), ride_time, drop_off_time, route_text,last_station_text);
    }
}
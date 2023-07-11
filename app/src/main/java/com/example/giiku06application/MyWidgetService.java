package com.example.giiku06application;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViewsService;

public class MyWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Bundle bundle = intent.getExtras();
        String[] dataSource = bundle.getStringArray("data_source");
        return new MyAdapter(this.getApplicationContext(), dataSource);
    }
}
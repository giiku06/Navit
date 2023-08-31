package com.example.giiku06application;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class MainAppWidget extends AppWidgetProvider{

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for(int appWidgetId : appWidgetIds) {
            Intent remoteViewsFactoryIntent = new Intent(context, MyWidgetService.class);
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.main_app_widget);
            rv.setRemoteAdapter(R.id.listview, remoteViewsFactoryIntent);

            appWidgetManager.updateAppWidget(appWidgetId, rv);
        }

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
    }
}

package com.example.giiku06application;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class MainAppWidget extends AppWidgetProvider{

    private static final String CLICK_WIDGET = "com.example.giiku06application.CLICK_WIDGET";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for(int appWidgetId : appWidgetIds) {
            Intent remoteViewsFactoryIntent = new Intent(context, MyWidgetService.class);
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.main_app_widget);
            rv.setRemoteAdapter(R.id.listview, remoteViewsFactoryIntent);

//            setOnItemSelectedPendingIntent(context,rv);

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
        if(CLICK_WIDGET.equals(intent.getAction())) {

            int clickedPosition = intent.getIntExtra("position", -1);
            Log.d("TAG", "onReceive: "+clickedPosition);
        }
    }
    private void setOnItemSelectedPendingIntent(Context ctx, RemoteViews rv) {
        Intent itemClickIntent = new Intent(ctx, MainAppWidget.class);
        itemClickIntent.setAction(CLICK_WIDGET);

        PendingIntent itemClickPendingIntent = PendingIntent.getBroadcast(
                ctx,
                0,
                itemClickIntent,
                PendingIntent.FLAG_MUTABLE
        );

        rv.setPendingIntentTemplate(R.id.listview, itemClickPendingIntent);
    }
}

package com.example.giiku06application;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

public class MainAppWidget extends AppWidgetProvider{

    private static final String CLICK_WIDGET = "com.example.giiku06application.CLICK_WIDGET";
    private static final String UPDATE_WIDGET = "com.example.giiku06application.UPDATE_WIDGET";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for(int appWidgetId : appWidgetIds) {
            Intent remoteViewsFactoryIntent = new Intent(context, MyWidgetService.class);
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.main_app_widget);
            rv.setRemoteAdapter(R.id.listview, remoteViewsFactoryIntent);

            setOnItemSelectedPendingIntent(context, rv);
            setBackgroundSelectedPendingIntent(context, rv);
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
        Log.d("onReceive", "onReceive: ");
        if(CLICK_WIDGET.equals(intent.getAction())) {
            Uri uri = intent.getData();

            if(uri != null) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(browserIntent);
            }
        }else if(UPDATE_WIDGET.equals(intent.getAction())){
            Log.d("UpdateWidget", "onReceive: Update");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, MainAppWidget.class));

            for (int appWidgetId : appWidgetIds) {
                if(appWidgetId != 0) {
                    AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(appWidgetId, R.id.listview);
                }
            }
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

    private void setBackgroundSelectedPendingIntent(Context ctx, RemoteViews rv) {
        Intent ClickBackground = new Intent(ctx, MainAppWidget.class);
        ClickBackground.setAction(UPDATE_WIDGET);

        PendingIntent backgroundClickPendingIntent = PendingIntent.getBroadcast(
                ctx,
                0,
                ClickBackground,
                PendingIntent.FLAG_MUTABLE
        );

        rv.setOnClickPendingIntent(R.id.background, backgroundClickPendingIntent);
    }
}

package com.my_widget.myelsewidget;

/**
 * Created by Sergey.Kudryashov on 2/26/2015.
 */
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.app.PendingIntent;

public class MyProvider extends AppWidgetProvider {

    SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM HH:mm");

    final String LOG_TAG = "States";
    final String MYWIDGET_UPDATE = "org.imix.MYWIDGET_UPDATE";
    private static final String SYNC_CLICKED    = "automaticWidgetSyncButtonClick";
    private ComponentName thisWidget;
    private RemoteViews views;
    Timer myTimer = new Timer();
    public final static String BROADCAST_ACTION = "ru.startandroid.develop.p0961servicebackbroadcast";
    RemoteViews rv;
    BroadcastReceiver br;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

        Log.v(LOG_TAG, "onEnabled");


    }



    private void startTimer(final Context context, final AppWidgetManager manager, final int[] appWidgetIds) {
        Log.v(LOG_TAG, "Timer start");

        myTimer.schedule(new TimerTask() {

            int counter = 0;

            @Override
            public void run() {
                Log.v(LOG_TAG, "Timer: " + counter);
                counter++;

                SharedPreferences sp = context.getSharedPreferences(
                        ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE);

                for (int i : appWidgetIds) {
                    updateWidget(context, manager, sp, i);
                }
            }
        }, 0, 17 * 60 * 1000);
    }

    private void stopTimer() {
        myTimer.cancel();
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        Log.d("onUpdate", "yo");
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        SharedPreferences sp = context.getSharedPreferences(
                ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE);


        for (int i : appWidgetIds) {
            Log.v(LOG_TAG, "appWidgetIds: " + i);
            updateWidget(context, appWidgetManager, sp, i);
        }
        startTimer(context, appWidgetManager, appWidgetIds);



        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                int status = intent.getIntExtra("list_ready", 0);
                Log.d(LOG_TAG, "receive intent");

                // Ловим сообщения о старте задач
                if (status == 1) {
                    rv.setInt(R.id.updateProgressBar, "setVisibility", View.GONE);
                }

            }
        };
        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
        // регистрируем (включаем) BroadcastReceiver
        context.registerReceiver(br, intFilt);
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    void updateWidget(Context context, AppWidgetManager appWidgetManager,
                      SharedPreferences sp,
                      int appWidgetId) {

        Log.d("MyProvider widget id", String.valueOf(appWidgetId));

        rv = new RemoteViews(context.getPackageName(),
                R.layout.widget);


        rv.setInt(R.id.updateProgressBar, "setVisibility", View.VISIBLE);


        try{
            String CurrencyZone = (sp.getString(ConfigActivity.CURRENCY_ZONE, null)!=null) ? sp.getString(ConfigActivity.CURRENCY_ZONE, null): "";
            Log.d("updateWidgt", CurrencyZone);



            setUpdateTV(rv, context, appWidgetId);


            setList(rv, context, appWidgetId, CurrencyZone);

            appWidgetManager.updateAppWidget(appWidgetId, rv);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId,
                    R.id.lvList);
        }catch (Exception e){
            Log.e("update widget error", e.toString());
        }




    }

    void setUpdateTV(RemoteViews rv, Context context, int appWidgetId) {



        rv.setTextViewText(R.id.tvUpdate,
                sdf.format(new Date(System.currentTimeMillis())));
        Intent updIntent = new Intent(context, MyProvider.class);
        updIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                new int[]{appWidgetId});
        PendingIntent updPIntent = PendingIntent.getBroadcast(context,
                appWidgetId, updIntent, 0);
        rv.setOnClickPendingIntent(R.id.BtnUpdate, updPIntent);
    }

    void setList(RemoteViews rv, Context context, int appWidgetId, String CurrencyZone) {

        //

        Intent adapter = new Intent(context, GetDataService.class);
        Log.d("CurencyZone", CurrencyZone);
        adapter.putExtra("ZONE", CurrencyZone);
        rv.setRemoteAdapter(R.id.lvList, adapter);




    }






    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        stopTimer();
        Log.d(LOG_TAG, "onDeleted " + Arrays.toString(appWidgetIds));

        // Удаляем Preferences
        Editor editor = context.getSharedPreferences(
                ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE).edit();
        for (int widgetID : appWidgetIds) {
            editor.remove(ConfigActivity.CURRENCY_ZONE + widgetID);
        }
        editor.commit();
    }


}

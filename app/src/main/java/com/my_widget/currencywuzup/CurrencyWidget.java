package com.my_widget.currencywuzup;

/**
 * Created by Sergey.Kudryashov on 2/26/2015.
 */
import java.sql.Date;
import java.text.SimpleDateFormat;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.app.PendingIntent;

import org.json.JSONArray;

public class CurrencyWidget extends AppWidgetProvider {

    SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM HH:mm");

    final String LOG_TAG = "WIDGET";
    GetRates getRates;
    RemoteViews rv;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        Log.d(LOG_TAG, "receive intent");

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

    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    void updateWidget(Context context, AppWidgetManager appWidgetManager,
                      SharedPreferences sp,
                      int appWidgetId) {

        Log.d(LOG_TAG, "CurrencyWidget widget id: " + String.valueOf(appWidgetId));

        rv = new RemoteViews(context.getPackageName(),
                R.layout.widget);

        getRates = new GetRates();

        rv.setInt(R.id.updateProgressBar, "setVisibility", View.VISIBLE);
        rv.setInt(R.id.BtnUpdate, "setVisibility", View.GONE);


        try{
            String CurrencyZone = (sp.getString(ConfigActivity.CURRENCY_ZONE, null)!=null) ? sp.getString(ConfigActivity.CURRENCY_ZONE, null): "";
            Log.d(LOG_TAG, "updateWidgt" + CurrencyZone);

            JSONArray pairs = getRates.get(CurrencyZone);

            Log.d(LOG_TAG, "Get rates: " + pairs.toString());

            rv.setInt(R.id.updateProgressBar, "setVisibility", View.GONE);
            rv.setInt(R.id.BtnUpdate, "setVisibility", View.VISIBLE);


            if(pairs != null){
                setUpdateTV(rv, context, appWidgetId);

                setList(rv, context, appWidgetId, pairs.toString());

                appWidgetManager.updateAppWidget(appWidgetId, rv);
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId,
                        R.id.lvList);
            }

        }catch (Exception e){
            Log.e("update widget error", e.toString());
        }

    }

    void setUpdateTV(RemoteViews rv, Context context, int appWidgetId) {



        rv.setTextViewText(R.id.tvUpdate,
                sdf.format(new Date(System.currentTimeMillis())));
        Intent updIntent = new Intent(context, CurrencyWidget.class);
        updIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                new int[]{appWidgetId});
        PendingIntent updPIntent = PendingIntent.getBroadcast(context,
                appWidgetId, updIntent, 0);
        rv.setOnClickPendingIntent(R.id.BtnUpdate, updPIntent);
    }

    void setList(RemoteViews rv, Context context, int appWidgetId, String pairs) {

        Intent adapter = new Intent(context, GetDataService.class);

        adapter.putExtra("PAIRS", pairs);
        rv.setRemoteAdapter(R.id.lvList, adapter);

    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        // Удаляем Preferences
        Editor editor = context.getSharedPreferences(
                ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE).edit();
        for (int widgetID : appWidgetIds) {
            editor.remove(ConfigActivity.CURRENCY_ZONE + widgetID);
        }
        editor.commit();
    }
}
package com.my_widget.myelsewidget;

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
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.app.PendingIntent;
/*
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;*/

import org.json.JSONArray;

public class CurrencyWidget extends AppWidgetProvider {

    SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM HH:mm");

    final String LOG_TAG = "WIDGET";
    final String FOREX_AFFILIATE_LINK = "http://forextime.com/register/open-account?partner_id=4803303";
    GetRates getRates;
    RemoteViews rv;

    public static final String ACTION_GOTO_APP = "ACTION_GOTO_APP";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        Log.d(LOG_TAG, "onReceive() " + intent.getAction());

        if (ACTION_GOTO_APP.equals(intent.getAction()))
        {
           openApp(context);
        }

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        Log.d("onUpdate", "yo");
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        SharedPreferences sp = context.getSharedPreferences(
                ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE);

        Log.d(LOG_TAG, "SharedPreferences: " + sp.toString());

        for (int i : appWidgetIds) {
            Log.v(LOG_TAG, "appWidgetIds: " + i);
            updateWidget(context, appWidgetManager, sp, i);
            startBrowsing(context, appWidgetManager, i);
        }



    }

    public void openApp(Context context) {
        Intent intent = new Intent(context, OptionsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    public void updateWidget(Context context, AppWidgetManager appWidgetManager,
                             SharedPreferences sp,
                             int appWidgetId) {

        Log.d(LOG_TAG, "CurrencyWidget widget id: " + String.valueOf(appWidgetId));

        rv = new RemoteViews(context.getPackageName(),
                R.layout.widget);

        getRates = new GetRates();

        rv.setInt(R.id.updateProgressBar, "setVisibility", View.VISIBLE);
        rv.setInt(R.id.BtnUpdate, "setVisibility", View.GONE);


        try{
            String CurrencyZone = sp.getString(ConfigActivity.CURRENCY_ZONE, null);
            Boolean bitcoin = sp.getBoolean(ConfigActivity.ADD_BITCOIN, false);
            Boolean gold = sp.getBoolean(ConfigActivity.ADD_GOLD, false);
            String customPairs = (sp.getString(ConfigActivity.CUSTOM_PAIRS, null)!=null) ? sp.getString(ConfigActivity.CUSTOM_PAIRS, null): "";
            Log.d(LOG_TAG, "updateWidgt Zone before rem: " + CurrencyZone);

            if (CurrencyZone == null) return;

            /*Editor editor = sp.edit();
            // Need to remove for next config it will be empty and this method wont start before config is set
            editor.putString(ConfigActivity.CURRENCY_ZONE, null);
            editor.commit();*/

            Log.d(LOG_TAG, "Current_zone after rem: " + sp.getString(ConfigActivity.CURRENCY_ZONE, null));

            JSONArray pairs = getRates.get(CurrencyZone, bitcoin, gold, customPairs);

            Log.d(LOG_TAG, "Get rates: " + pairs.toString());

            rv.setInt(R.id.updateProgressBar, "setVisibility", View.GONE);
            rv.setInt(R.id.BtnUpdate, "setVisibility", View.VISIBLE);


            if(pairs != null){
                setUpdateTV(rv, context, appWidgetId);
                setGotoApp(rv, context, appWidgetId);

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

        Log.d(LOG_TAG, "setUpdatetv");

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

    private void startBrowsing(Context ctx,
                               AppWidgetManager appWidgetManager, int widgetID) {
        RemoteViews widgetView = new RemoteViews(ctx.getPackageName(), R.layout.widget);
        Uri uri = Uri.parse(FOREX_AFFILIATE_LINK);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        PendingIntent pIntent = PendingIntent.getActivity(ctx, widgetID, intent, 0);
        // viewID - our clickable view ID
        widgetView.setOnClickPendingIntent(R.id.StartTradeForexBtn, pIntent);
        //rv.setOnClickPendingIntent(R.id.StartTradeForexBtn, pIntent);

        appWidgetManager.updateAppWidget(widgetID, widgetView);


    }

    void setGotoApp(RemoteViews rv, Context context, int appWidgetId) {

        Log.d(LOG_TAG, "setGotoAppBtn");

        //rv.setTextViewText(R.id.tvUpdate,
                //sdf.format(new Date(System.currentTimeMillis())));
        Intent updIntent = new Intent(context, CurrencyWidget.class);
        updIntent.setAction(ACTION_GOTO_APP);
        //updIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                //new int[]{appWidgetId});
        PendingIntent updPIntent = PendingIntent.getBroadcast(context,
                appWidgetId, updIntent, 0);
        rv.setOnClickPendingIntent(R.id.goToSettingsBtn, updPIntent);
    }

    void setList(RemoteViews rv, Context context, int appWidgetId, String pairs) {


        Intent adapter = new Intent(context, GetDataService.class);

        Log.d(LOG_TAG, "setList: " + pairs);
        adapter.putExtra("PAIRS", pairs);
        adapter.setData(Uri.parse(adapter.toUri(Intent.URI_INTENT_SCHEME)));
        rv.setRemoteAdapter(R.id.lvList, adapter);
        rv.setEmptyView(R.id.lvList, android.R.id.empty);

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
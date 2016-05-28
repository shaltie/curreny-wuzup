package com.my_widget.myelsewidget;

/**
 * Created by Sergey.Kudryashov on 2/26/2015.
 */

        import java.sql.Date;
        import java.text.SimpleDateFormat;
        import java.util.ArrayList;
        import java.util.Random;

        import android.app.Notification;
        import android.app.NotificationManager;
        import android.app.PendingIntent;
        import android.appwidget.AppWidgetManager;
        import android.content.Context;
        import android.content.Intent;
        import android.util.Log;
        import android.widget.RemoteViews;
        import android.widget.RemoteViewsService.RemoteViewsFactory;

        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;

public class MyFactory implements RemoteViewsFactory {

    ArrayList<String> pair;
    ArrayList<String> ask;
    ArrayList<String> bid;
    ArrayList<String> arrow;
    Context context;
    int widgetID;
    JSONArray pairsArray;
    GetRates getRates;
    String currecyZone;
    final String LIST_READY = "list_ready";
    public final static String BROADCAST_ACTION = "ru.startandroid.develop.p0961servicebackbroadcast";


    NotificationManager notificationManager;
    Notification myNotification;
    private static final int MY_NOTIFICATION_ID=1;



    MyFactory(Context ctx, Intent intent) {
        context = ctx;
        widgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        Log.d("CURRENCY ZONE", intent.getExtras().toString());
        currecyZone = intent.getStringExtra("ZONE").toString();


        //String pairs = intent.getStringExtra("PAIRS");
        /*Log.d("FACTORY", pairs);
        try {
            pairsArray = new JSONArray(pairs);
            Log.d("Factory pairsArray", pairsArray.toString());
        } catch (JSONException e) {
            Log.e("Factory array fucked up", pairsArray.toString());
        }*/
    }

    @Override
    public void onCreate() {

        //JSONArray pairs = getRates.send();
        pair = new ArrayList<String>();
        ask = new ArrayList<String>();
        bid = new ArrayList<String>();
        arrow = new ArrayList<String>();
    }

    @Override
    public int getCount() {
        return pair.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {

        Log.d("LOADINGGGGGG!!!!","Yo");
        return null;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Log.d("Factory getvw set_chngd", ask.get(position) + " / " + bid.get(position));
        RemoteViews rView = new RemoteViews(context.getPackageName(),
                R.layout.item);
        rView.setTextViewText(R.id.pair, pair.get(position));
        rView.setTextViewText(R.id.askbid, ask.get(position) + " / " + bid.get(position));

        int min = 0;
        int max = 1;

        Random r = new Random();
        int randomiser = r.nextInt(max - min + 1) + min;
        if(randomiser == 1){
            rView.setTextViewText(R.id.arrow, "▲");
            rView.setTextColor(R.id.arrow, context.getResources().getColor(R.color.green));
        }else{
            rView.setTextViewText(R.id.arrow, "▼");
            rView.setTextColor(R.id.arrow, context.getResources().getColor(R.color.red));
        }

        return rView;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onDataSetChanged() {

        getRates = new GetRates();

        context.startService(new Intent(context, PushNotices.class));

        /*myNotification = new Notification.Builder(context)
                .setContentTitle("Exercise of Notification!")
                .setContentText("http://android-er.blogspot.com/")
                .setTicker("Notification!")
                .setWhen(System.currentTimeMillis())
                //.setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher)
                .build();*/

        //notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        //notificationManager.notify(MY_NOTIFICATION_ID, myNotification);



        try{
            Log.d("Factory Getting Pairs", "asd");
            JSONArray pairs = getRates.send(currecyZone);
            Log.d("Pairs", pairs.toString());
            ArrayList<String> pairsList = new ArrayList<String>();
            if (pairs != null) {
                for (int i=0;i<pairs.length();i++){
                    Log.d("For pairs"+i, "Yo");
                    Log.d("For pairs"+i, pairs.get(i).toString());
                    pairsList.add(pairs.get(i).toString());
                }
            }

            pair.clear();
            //data.add(sdf.format(new Date(System.currentTimeMillis())));
            //data.add(String.valueOf(hashCode()));
            //data.add(String.valueOf(widgetID));
            //pairsArray

            pairsArray = new JSONArray(pairsList.toString());
            Log.d("Pair Array to json",pairsArray.toString());
            pair = new ArrayList<String>();
            ask = new ArrayList<String>();
            bid = new ArrayList<String>();
            for (int i = 0; i < pairsArray.length(); i++) {
                try{
                    String p = pairsArray.getJSONObject(i).getString("symbol");
                    String a = pairsArray.getJSONObject(i).getString("ask");
                    String b = pairsArray.getJSONObject(i).getString("bid");

                    Log.d("pairs", a + " " + b);


                    pair.add(p);
                    ask.add(a);
                    bid.add(b);

                }catch (JSONException e){
                    Log.e("List setup error", e.toString());
                }

            }
            Log.d("LIST READY", "YYYYYYY");
            Intent intent = new Intent(BROADCAST_ACTION);
            intent.putExtra(LIST_READY, 1);

            context.sendBroadcast(intent);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e("Requesting fails", e.toString());
        }


    }

    @Override
    public void onDestroy() {

    }

}
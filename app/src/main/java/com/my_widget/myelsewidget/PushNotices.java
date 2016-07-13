package com.my_widget.myelsewidget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import android.app.AlertDialog;
import android.app.IntentService;
import android.app.Notification;
        import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

public class PushNotices extends IntentService {
    NotificationManager nm;

    final String LOG_TAG = "Push_Notices";

    ArrayList renderedWatchersList;

    SharedPreferences sPref;

    GetRates getRates;

    public PushNotices() {
        super("myname");
    }


    @Override
    public void onCreate() {

        Log.d(LOG_TAG, "Create");
        super.onCreate();
        getRates = new GetRates();
        //nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(LOG_TAG, "On start");
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Random r = new Random();
        int randomInt = r.nextInt(100 - 1 + 1) + 1;
        noticeCheckLooper(randomInt);
    }

    void noticeCheckLooper(int randomInt){

        Log.d(LOG_TAG, "init checker:" + randomInt);
        checkRates();

        try {
            TimeUnit.SECONDS.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        noticeCheckLooper(randomInt);

    }

    void checkRates(){

        sPref = getSharedPreferences("PRICE_WATCHERS", MODE_PRIVATE);

        Map<String,?> allPrefs = sPref.getAll();

        if(allPrefs.size() > 0){

            Log.d(LOG_TAG, "allPref size >0: " + allPrefs.size());

            String pairs = "";

            Map<String, Map<String, String>> watchers = new HashMap<>();

            renderedWatchersList = new ArrayList<String>();

            for(Map.Entry<String,?> entry : allPrefs.entrySet()){
                String val = entry.getValue().toString();
                String key = entry.getKey().toString();
                renderedWatchersList.add(key);
                String[] arr = val.split("--");

                String cond;
                String pair;
                String price;
                if(arr.length==3){

                    pair = arr[0];
                    cond = arr[1];
                    price = arr[2];
                    Map<String, String> condition = new HashMap<>();
                    condition.put(cond, price);

                    if(watchers.get(pair) == null){
                        watchers.put(pair, condition);
                    }else{
                        watchers.get(pair).put(cond, price);
                    }

                    pairs += (pair + "-");
                }

            }

            if(pairs == "") return;

            try{
                JSONArray pairsArray = getRates.get("custom", false, false, pairs);
                if(pairsArray != null){

                    for (int i=0;i<pairsArray.length();i++){
                        try{

                            String p = pairsArray.getJSONObject(i).getString("symbol");
                            String a = pairsArray.getJSONObject(i).getString("ask");
                            Log.d(LOG_TAG, "notice checked: " + p + " " + a);
                            Map <String, String>  result = calc(p, a, watchers.get(p));
                            if(result != null){
                                Log.d(LOG_TAG, "Notice reached");
                                sendNotif(p, result.get("condition"), result.get("price"), i);
                                removeWatcher(i);
                            }

                        }catch (Exception e){
                            Log.d(LOG_TAG, e.toString());
                        }
                    }

                }

            }catch (Exception e){
                Log.d(LOG_TAG, e.toString());
            }

        }else{
            //stopService(new Intent(this, PushNotices.class));
        }



    }

    public void removeWatcher(int w){
        Log.d(LOG_TAG, "removingWatcehr: " + w);
        SharedPreferences.Editor ed = sPref.edit();
        ed.remove(renderedWatchersList.get(w).toString());
        ed.apply();
    }

    Map<String, String> calc(String p, String a, Map<String, String> watcher){

        Log.d(LOG_TAG, "calc func: " + p + " " + a);

        if(watcher != null){

            for(HashMap.Entry<String, String> entry : watcher.entrySet()) {
                String key = entry.getKey().toString();
                String value = entry.getValue();
                Map<String, String> res = new HashMap<>();
                if( ((key.equals("<=")) && (Float.parseFloat(a) <= Float.parseFloat(value))) ||
                        ((key.equals(">=")) && (Float.parseFloat(a) >= Float.parseFloat(value)))
                        ){
                    Log.d(LOG_TAG, "calc condition ok");
                    res.put("condition", key);
                    res.put("price", value);
                    return res;
                }
            }

        }
        return null;

    }

    void sendNotif(String pair, String condition, String price, int nId) {
        Log.d(LOG_TAG, "Send notice");

        String text = "";
        String currency = pair.replaceAll(".*?(.?.?.?)?$", "$1");;

        text += pair + " ";
        text += (condition.equals("<=")) ? getResources().getString(R.string.equal_or_less) : getResources().getString(R.string.equal_or_more);
        text += " " + price + currency;

        try {

            Notification.Builder mBuilder =
                    new Notification.Builder(this)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle(getResources().getString(R.string.push_notification_title))
                            .setContentText(text);
            Intent resultIntent = new Intent(this, OptionsActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(OptionsActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            mBuilder.setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_LIGHTS|Notification.DEFAULT_VIBRATE);

            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT < 16) {
                mNotificationManager.notify(nId, mBuilder.getNotification());
            } else {
                mNotificationManager.notify(nId, mBuilder.build());
            }

        } catch (Exception e) {
            Log.e("Notice error", e.toString());
        }

    }

    public IBinder onBind(Intent arg0) {
        return null;
    }
}

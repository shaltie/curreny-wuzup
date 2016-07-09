package com.my_widget.myelsewidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by shaltie on 09/07/16.
 */
public class MyBroadReceiv extends BroadcastReceiver {

    final String LOG_TAG = "myLogs";

    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive " + intent.getAction());
        context.startService(new Intent(context, PushNotices.class));
    }
}
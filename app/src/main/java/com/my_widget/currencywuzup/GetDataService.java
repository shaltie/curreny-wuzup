package com.my_widget.currencywuzup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViewsService;


public class GetDataService extends RemoteViewsService {

    SharedPreferences sPref;
    String result;
    SharedPreferences.Editor ed;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        Log.d("RemoteViewsFactory!", intent.toString());

        return new MyFactory(getApplicationContext(), intent);
    }

}

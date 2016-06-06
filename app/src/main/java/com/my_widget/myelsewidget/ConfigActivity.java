package com.my_widget.myelsewidget;

/**
 * Created by shaltie on 22/08/15.
 */

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;

public class ConfigActivity extends Activity {

    int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    Intent resultValue;

    final String LOG_TAG = "myLogs";

    public final static String WIDGET_PREF = "widget_pref";
    //public final static String WIDGET_TEXT = "widget_text_";
    public final static String CURRENCY_ZONE = "currency_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate config");

        // извлекаем ID конфигурируемого виджета
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        Log.d(LOG_TAG, "onCreate config 1");

        if (extras != null) {
            widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        Log.d(LOG_TAG, "onCreate config 2");

        // и проверяем его корректность
        if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        Log.d(LOG_TAG, "onCreate config 3");


        // формируем intent ответа
        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);

        Log.d(LOG_TAG, "onCreate config 4");

        // отрицательный ответ
        setResult(RESULT_CANCELED, resultValue);

        setContentView(R.layout.config);
    }


    public void onClick(View v) {
        int selZone1 = ((RadioGroup) findViewById(R.id.rgCurrencyZone))
                .getCheckedRadioButtonId();
        String selZone = "";

        switch (selZone1) {
            case R.id.euroZone:
                selZone = "eur";
                break;
            case R.id.rubZone:
                selZone = "rub";
                break;
            case R.id.usdZone:
                selZone = "usd";
                break;
        }

        //EditText etText = (EditText) findViewById(R.id.etText);

        // Записываем значения с экрана в Preferences
        SharedPreferences sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);
        Editor editor = sp.edit();
        //editor.putString(WIDGET_TEXT + widgetID, etText.getText().toString());
        Log.d("Config widget id", String.valueOf(widgetID));
        editor.putString(CURRENCY_ZONE, selZone);
        editor.commit();

        // положительный ответ
        setResult(RESULT_OK, resultValue);

        Log.d(LOG_TAG, "finish config " + widgetID);
        finish();
    }
}
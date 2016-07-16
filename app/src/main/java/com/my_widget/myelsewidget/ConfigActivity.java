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
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.Arrays;
import java.util.List;

public class ConfigActivity extends Activity {
    //public static GoogleAnalytics analytics;

    int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    Intent resultValue;

    final String LOG_TAG = "Config Log";

    LinearLayout checkboxLayout;
    Switch showBitcoinRateSwitch;
    Switch showGoldRateSwitch;


    public final static String WIDGET_PREF = "widget_pref";
    //public final static String WIDGET_TEXT = "widget_text_";
    public final static String CURRENCY_ZONE = "currency_";
    public final static String ADD_BITCOIN = "add_bitcoin";
    public final static String ADD_GOLD = "add_gold";
    public final static String CUSTOM_PAIRS = "custom_pairs";

    private final int CKB_ID_PREFIX = 100500;

    private Tracker mTracker;

    List<String> configCustomCkbs;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate config");

        // извлекаем ID конфигурируемого виджета
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null) {
            widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }


        // и проверяем его корректность
        if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }



        // формируем intent ответа
        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);


        // отрицательный ответ
        setResult(RESULT_CANCELED, resultValue);

        setContentView(R.layout.config);

        try {
            AnalyticsApplication application = (AnalyticsApplication) getApplication();
            mTracker = application.getDefaultTracker();
        }catch (Exception e){
            Log.d(LOG_TAG, e.toString());
        }


        checkboxLayout = (LinearLayout) findViewById(R.id.customZoneBlock);
        showBitcoinRateSwitch = (Switch) findViewById(R.id.showBitcoinRateSwitch);
        showGoldRateSwitch = (Switch) findViewById(R.id.showGoldRateSwitch);

        configCustomCkbs = Arrays.asList(getResources().getStringArray(R.array.config_custom_checkboxes));

        createCheckboxList();

    }

    private void createCheckboxList(){

        for(int i = 0; i < configCustomCkbs.size(); i++) {
            CheckBox cb = new CheckBox(this);
            cb.setText(String.valueOf(configCustomCkbs.get(i)));
            cb.setId(CKB_ID_PREFIX + i);
            cb.setTag(String.valueOf(configCustomCkbs.get(i)));
            checkboxLayout.addView(cb);
        }
        //configCustomCkbs.recycle();
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.customZoneSelect:
                if (checked) {
                    checkboxLayout.setVisibility(View.VISIBLE);
                    showBitcoinRateSwitch.setVisibility(View.GONE);
                    showGoldRateSwitch.setVisibility(View.GONE);
                }
                break;
            default:
                // Ninjas rule
                if (checked) {
                    checkboxLayout.setVisibility(View.GONE);
                    showBitcoinRateSwitch.setVisibility(View.VISIBLE);
                    showGoldRateSwitch.setVisibility(View.VISIBLE);
                }
                    break;
        }
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
            case R.id.arabicZone:
                selZone = "arabic";
                break;
            case R.id.customZoneSelect:
                selZone = "custom";
                break;
        }




        boolean showBitcoin = showBitcoinRateSwitch.isChecked();
        boolean showGold = showGoldRateSwitch.isChecked();



        //EditText etText = (EditText) findViewById(R.id.etText);

        // Записываем значения с экрана в Preferences
        SharedPreferences sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);
        Editor editor = sp.edit();
        //editor.putString(WIDGET_TEXT + widgetID, etText.getText().toString());
        Log.d("Config widget id", String.valueOf(widgetID));
        editor.putString(CURRENCY_ZONE, selZone);
        editor.putBoolean(ADD_BITCOIN, showBitcoin);
        editor.putBoolean(ADD_GOLD, showGold);

        try{
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction("Zone " + selZone + ", btc " + ((showBitcoin)?"y":"n"))
                    .build());
        }catch(Exception e){
            Log.d(LOG_TAG, e.toString());
        }



        if(selZone.equals("custom")){



            Log.d(LOG_TAG, "selzone = custom");
            String customPairs = "";
            for(int i = 0; i < checkboxLayout.getChildCount(); i++){
                View ckb = checkboxLayout.getChildAt(i);
                CheckBox cb = (CheckBox)ckb;
                if(cb.isChecked()){
                    Object tag = cb.getTag();
                    customPairs += (tag.toString() + '-');
                }

            }
            Log.d(LOG_TAG + " Pairs", customPairs);
            editor.putString(CUSTOM_PAIRS, customPairs);

            try {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Custome pairs: " + customPairs)
                        .build());
            }catch(Exception e){
                Log.d(LOG_TAG, e.toString());
            }

        }
        editor.commit();

        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        // положительный ответ
        setResult(RESULT_OK, resultValue);

        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, this, CurrencyWidget.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] {widgetID});
        sendBroadcast(intent);


        Log.d(LOG_TAG, "finish config " + widgetID);
        finish();
    }
}
package com.my_widget.myelsewidget;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OptionsActivity extends Activity implements View.OnClickListener {

    List<String> pairsList;
    String[] conditionsList = {"<=", ">="};

    final String LOG_TAG = "Options Activity";

    public List<String> renderedWatchersList;

    SharedPreferences sPref;

    Button btnAdd;
    Spinner pairsSpinner;
    Spinner conditionsSpinner;
    EditText enterPairTargetPrice;
    ListView watchersList;
    LinearLayout watchersBlock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        btnAdd = (Button) findViewById(R.id.addPriceReachWatcherBtn);
        btnAdd.setOnClickListener(this);
        pairsSpinner = (Spinner) findViewById(R.id.choosePairSpinner);
        enterPairTargetPrice = (EditText) findViewById(R.id.enterPairTargetPrice);
        conditionsSpinner = (Spinner) findViewById(R.id.chooseConditionSpinner);
        watchersList = (ListView) findViewById(R.id.watchersList);
        watchersBlock = (LinearLayout) findViewById(R.id.optionsActivityWatchersBlock);

        sPref = getSharedPreferences("PRICE_WATCHERS", MODE_PRIVATE);

        pairsList = Arrays.asList(getResources().getStringArray(R.array.config_custom_checkboxes));

        // адаптер списка пар
        ArrayAdapter<String> pairsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, pairsList);
        pairsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pairsSpinner.setAdapter(pairsAdapter);

        // адаптер списка условий
        ArrayAdapter<String> conditionsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, conditionsList);
        conditionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        conditionsSpinner.setAdapter(conditionsAdapter);
        // выделяем элемент
        conditionsSpinner.setSelection(0);

        loadWatchersList();


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addPriceReachWatcherBtn:
                addWatcher();
                break;
            default:
                break;
        }
    }



    private void addWatcher(){

        String ps = pairsSpinner.getSelectedItem().toString();
        String cs = conditionsSpinner.getSelectedItem().toString();
        String pr = enterPairTargetPrice.getText().toString();

        try {

            SharedPreferences.Editor ed = sPref.edit();
            String opts = ps+"--"+cs+"--"+pr;

            ed.putString("PriceWatcher_" + ps + "_" + cs, opts);
            ed.commit();

            //Toast.makeText(getBaseContext(), "Adding: " + opts.toString(), Toast.LENGTH_SHORT).show();

            loadWatchersList();

        }catch(Exception e){
            Log.d(LOG_TAG, e.toString());
        }




    }

    public void removeWatcher(int watcherId){

        try {
            SharedPreferences.Editor ed = sPref.edit();
            ed.remove(renderedWatchersList.get(watcherId));
            ed.commit();
            loadWatchersList();

        }catch(Exception e){
            Log.d(LOG_TAG, e.toString());
        }

    }

    private void loadWatchersList(){

        final OptionsActivity self = this;

        ArrayAdapter <String> adapter;


        ArrayList<String> activeWatchers = new ArrayList<String>();

        renderedWatchersList = new ArrayList<String>();

        Map<String,?> allPrefs = sPref.getAll();



        if(allPrefs.size() > 0){

            Log.d(LOG_TAG, "allPref size >0: " + allPrefs.size());
            watchersBlock.setVisibility(View.VISIBLE);

            for(Map.Entry<String,?> entry : allPrefs.entrySet()){
                String val = entry.getValue().toString();
                String key = entry.getKey().toString();
                String[] arr = val.split("--");
                String condition = "";
                String currency = "";
                if(arr.length<3){
                    arr = new String[] {getResources().getString(R.string.data_incorrect), "", ""};
                }else{
                    // Get last 3 characters from currency pair
                    currency = arr[0].replaceAll(".*?(.?.?.?)?$", "$1");;
                    condition = (arr[1].equals("<=")) ? getResources().getString(R.string.equal_or_less) : getResources().getString(R.string.equal_or_more);
                }
                String out = arr[0] + " " + condition + " " + arr[2] + " " + currency;

                // add to array for render
                activeWatchers.add(out);
                // dublicate with other array, but put keys instead of text
                renderedWatchersList.add(key);
                //String value = entry.getValue().toString();
            }

            adapter = new ArrayAdapter <String> (this, android.R.layout.simple_list_item_1, activeWatchers);
            adapter.notifyDataSetChanged();
            watchersList.setAdapter(adapter);

            watchersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {

                    new AlertDialog.Builder(self)
                            .setMessage(getResources().getString(R.string.watcher_delete_confirm_text))
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {

                                    removeWatcher(position);

                                }
                            })
                            .setNegativeButton(android.R.string.no, null).show();

                }
            });

            startService(new Intent(this, PushNotices.class));

        }else{
            watchersBlock.setVisibility(View.GONE);
            stopService(new Intent(this, PushNotices.class));
        }




    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

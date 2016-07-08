package com.my_widget.myelsewidget;

/**
 * Created by Sergey.Kudryashov on 2/26/2015.
 */


        import java.util.ArrayList;
        import java.util.Random;

        import android.appwidget.AppWidgetManager;
        import android.content.Context;
        import android.content.Intent;
        import android.util.Log;
        import android.widget.RemoteViews;
        import android.widget.RemoteViewsService.RemoteViewsFactory;

        import org.json.JSONArray;
        import org.json.JSONException;

public class MyFactory implements RemoteViewsFactory {

    ArrayList<String> pair;
    ArrayList<String> ask;
    ArrayList<String> bid;
    ArrayList<String> arrow;
    Context context;
    int widgetID;
    JSONArray pairsArray;
    RemoteViews rView;
    Intent widgetIntent;

    final String LOG_CAT = "FactoryAdapter";

    MyFactory(Context ctx, Intent intent) {
        context = ctx;
        widgetIntent = intent;
        widgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        Log.d(LOG_CAT, "MyFactory start");

    }

    @Override
    public void onCreate() {

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

        return null;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Log.d(LOG_CAT, "Factory getvw set_chngd: "+ ask.get(position) + " / " + bid.get(position));

        rView = new RemoteViews(context.getPackageName(),
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



        String jsonArray = widgetIntent.getStringExtra("PAIRS");
        Log.d(LOG_CAT, "onDataSetChanged: " + jsonArray);
        try {
            pairsArray = new JSONArray(jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try{

            ArrayList<String> pairsList = new ArrayList<String>();
            if (pairsArray != null) {
                for (int i=0;i<pairsArray.length();i++){
                    Log.d("For pairs"+i, "Yo");
                    Log.d("For pairs"+i, pairsArray.get(i).toString());
                    pairsList.add(pairsArray.get(i).toString());
                }
            }

            pair.clear();

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

        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e("Requesting fails", e.toString());
        }

    }

    @Override
    public void onDestroy() {

    }

}
package com.my_widget.myelsewidget;

import android.os.StrictMode;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Sergey.Kudryashov on 2/26/2015.
 */
public class GetRates {

    final String CAT_LOG = "GET RATES";

    public GetRates(){

        Log.v(CAT_LOG, "GetRates starts! YO YO YO YO");

    }

    public JSONArray get(String CurrencyZone) throws JSONException {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        HttpClient httpclient = new DefaultHttpClient();
        String Url = "http://skudr.ru/currency/?t="+CurrencyZone;
        HttpGet httpget = new HttpGet(Url);
        JSONArray res = null;

        Log.d(CAT_LOG, "Requesting - Ran get: " + Url);
        try{
            httpget.setHeader("Accept", "application/json");
            httpget.setHeader("Content-type", "application/json");
            //HttpResponse httpResponse = httpclient.execute(httpget);
            //Log.d("Requesting",httpResponse.toString());
            HttpResponse response = httpclient.execute(httpget);

            // for JSON:
            if(response != null)
            {
                InputStream is = response.getEntity().getContent();



                BufferedReader reader = new BufferedReader(new InputStreamReader(is));


                String json = reader.readLine();
                JSONTokener tokener = new JSONTokener(json);
                res = new JSONArray(tokener);
                //is.close();
            }

        }catch (ClientProtocolException e) {
            Log.d("Requesting client prtcl",e.toString());
            // TODO Auto-generated catch block
        } catch (IOException e) {
            Log.d("Requesting IO",e.toString());
            // TODO Auto-generated catch block
        }
        return res;
    }

}

package com.example.nightscoutlite;

import android.app.IntentService;

import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.util.Log;
import android.widget.Toast;


import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static java.lang.Boolean.TRUE;

public class UpdateService extends IntentService {

    private static final String TAG = "UpdateService";

    static String url;
    static ArrayList<String> final_values;
    static String trend;

    public static void setUrl(String url) {
        UpdateService.url = url;
    }

    public static void setFinalValues(ArrayList<String> final_values) {
        UpdateService.final_values = final_values;
    }

    public static ArrayList<String> getFinalValues() {
        return final_values;
    }

    public static String getTrend() {
        return trend;
    }

    public static void setTrend(String trend) {
        UpdateService.trend = trend;
    }

    public UpdateService() {
        super("UpdateService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action.equals("com.example.nightscoutlite.MainActivity$1") == TRUE) {
                url = intent.getStringExtra("url");
                UpdateService.setUrl(url);
                this.getNSValues();
                intent.putStringArrayListExtra("values", UpdateService.getFinalValues());
                intent.putExtra("url", UpdateService.url);
                intent.putExtra("trend", UpdateService.getTrend());
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }
        }
    }

    private static String getUrl(String base) {
        return "https://" + base + "/api/v1/";
    }

    public void getNSValues() {

        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected()) {

            Log.i(TAG, "Service is running");

            final Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(getUrl(UpdateService.url))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            RetroEntriesInterface service = retrofit.create(RetroEntriesInterface.class);
            Call<List<NightscoutData>> retroCall = service.loadData();

            retroCall.enqueue(new Callback<List<NightscoutData>>() {

                @Override
                public void onResponse(Call<List<NightscoutData>> call, Response<List<NightscoutData>> response) {

                    Log.i(TAG, "Retrofit response was successful");

                    response.body().sort(new DateSorter());
                    ArrayList<String> values = new ArrayList<>();

                    int j = 0;
                    for (NightscoutData entry : response.body()) {
                        values.add(j + "/" + entry.getSgv());
                        j++;
                    }

                    UpdateService.setTrend(response.body().get(9).getDirection());
                    UpdateService.setFinalValues(values);

                    int lastValue = response.body().get(9).getSgv();
                    final SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("NS", MODE_PRIVATE);
                    int def_low_limit = sharedPref.getInt("low_limit", 0);
                    int def_high_limit = sharedPref.getInt("high_limit", 0);

                    if (lastValue <= def_low_limit) {
                        Log.i(TAG,"Blood glucose value is too low: " + lastValue);
                        Toast.makeText(getApplicationContext(), "Blood glucose value is too low: " + lastValue, Toast.LENGTH_SHORT).show();
                    }
                    if (lastValue >= def_high_limit) {
                        Log.i(TAG,"Blood glucose value is too high: " + lastValue);
                        Toast.makeText(getApplicationContext(), "Blood glucose value is too high: " + lastValue, Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<List<NightscoutData>> call, Throwable t) {
                    Log.i(TAG, "Something went wrong. Please try again later!");
                    ArrayList<String> values = new ArrayList<>();
                    for (int j = 0; j < 10; j++)
                        values.add(j+"/0");
                    UpdateService.setTrend("No data");
                    UpdateService.setFinalValues(values);
                    Toast.makeText(getApplicationContext(), "Something went wrong. Please try again later!", Toast.LENGTH_SHORT).show();
                }

            });

        } else {
            Log.i(TAG, "No internet connection");
            ArrayList<String> values = new ArrayList<>();
            for (int j = 0; j < 10; j++)
                values.add(j+"/0");
            UpdateService.setTrend("No data");
            UpdateService.setFinalValues(values);
        }
    }
}

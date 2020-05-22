package com.example.nightscoutlite;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Boolean.TRUE;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Nightscout Lite";

    private BroadcastReceiver mNetworkReceiver;
    private final String example_url = "https://test.herokuapp.com/api/v1/";
    Intent i;
    String defaultUrl;

    public String getDefaultUrl() {
        return defaultUrl;
    }

    public void setDefaultUrl(String url) {
        this.defaultUrl = url;
    }

    public Intent getI() {
        return i;
    }

    public void setI(Intent i) {
        this.i = i;
    }

    BroadcastReceiver myReceiver = new BroadcastReceiver() {

        private static final String TAG = "MyServiceReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {

            final TextView displayTrend = findViewById(R.id.textView);
            final LineChart chart = findViewById(R.id.chart);

            final SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("NS", MODE_PRIVATE);
            int def_low_limit = sharedPref.getInt("low_limit", 0);
            int def_high_limit = sharedPref.getInt("high_limit", 0);

            chart.getAxisLeft().setAxisMinimum(0);
            chart.getAxisRight().setAxisMinimum(0);
            chart.getAxisLeft().setAxisMaximum(400);
            chart.getAxisRight().setAxisMaximum(400);

            XAxis xAxis = chart.getXAxis();
            xAxis.setEnabled(false);

            LimitLine upper = new LimitLine(def_high_limit, "HIGH");
            upper.setLineWidth(4f);
            upper.enableDashedLine(10f,10f, 10f);
            upper.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
            upper.setTextSize(15f);

            LimitLine lower = new LimitLine(def_low_limit, "LOW");
            lower.setLineWidth(4f);
            lower.enableDashedLine(10f,10f, 0f);
            lower.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_BOTTOM);
            lower.setTextSize(15f);

            YAxis left = chart.getAxisLeft();
            left.removeAllLimitLines();
            left.addLimitLine(upper);
            left.addLimitLine(lower);

            chart.getDescription().setText("NS data");

            if (intent != null) {
                final String action = intent.getAction();
                if (action.equals(myReceiver.getClass().getName()) == TRUE) {
                    ArrayList<String> values = intent.getStringArrayListExtra("values");
                    String result = intent.getStringExtra("url");
                    String trend = intent.getStringExtra("trend");

                    if (values != null && values.size() > 0) {
                        Log.i(TAG, values.toString());

                        ArrayList<Entry> valuesToDraw = new ArrayList<>();

                        for (String elem : values) {
                            String[] value = elem.split("/");
                            valuesToDraw.add(new Entry(Float.valueOf(value[0]), Float.valueOf(value[1])));
                        }

                        displayTrend.setText("Trend: " + trend);
                        Log.i(TAG, "Trend: " + trend);
                        this.draw(valuesToDraw, MainActivity.this.getDefaultUrl(), chart);
                    }
                    else {
                        Log.i(TAG, "Service could not retrieve data");
                    }
                }
            }
        }

        private void draw(ArrayList<Entry> values, String description, LineChart chart) {
            LineDataSet set1;
            if (chart.getData() != null && chart.getData().getDataSetCount() > 0) {
                Log.i(TAG, "I just update chart");
                set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);
                set1.setValues(values);
            } else {
                Log.i(TAG, "I try to draw chart");
                set1 = new LineDataSet(values, description);
                set1.setDrawIcons(false);
                set1.setDrawValues(true);
                set1.enableDashedLine(10f, 1f, 0f);
                set1.enableDashedHighlightLine(10f, 1f, 0f);
                set1.setColor(Color.DKGRAY);
                set1.setCircleColor(Color.DKGRAY);
                set1.setLineWidth(1f);
                set1.setCircleRadius(3f);
                set1.setDrawCircleHole(false);
                set1.setValueTextSize(9f);
                set1.setDrawFilled(true);
                set1.setFormLineWidth(1f);
                set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
                set1.setFormSize(15.f);
                if (Utils.getSDKInt() >= 18) {
                    Drawable drawable = ContextCompat.getDrawable(MainActivity.this, R.drawable.fade_blue);
                    set1.setFillDrawable(drawable);
                } else {
                    set1.setFillColor(Color.DKGRAY);
                }
                ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                dataSets.add(set1);
                LineData data = new LineData(dataSets);
                chart.setData(data);
            }
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
            chart.invalidate();
        }
    };

    IntentFilter intentFilter = new IntentFilter(myReceiver.getClass().getName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "Now I am created");

        final EditText url = findViewById(R.id.editText);
        final EditText low_limit = findViewById(R.id.editText2);
        final EditText high_limit = findViewById(R.id.editText3);
        final Button setLimits = findViewById(R.id.button4);
        final Button set_default = findViewById(R.id.button6);
        final Button showProfile = findViewById(R.id.button2);

        mNetworkReceiver = new NetworkChangeReceiver();
        registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        final SharedPreferences sharedPref = this.getSharedPreferences("NS", MODE_PRIVATE);
        int def_low_limit = sharedPref.getInt("low_limit", 0);
        if (def_low_limit == 0) {
            SharedPreferences.Editor editor = getSharedPreferences("NS", MODE_PRIVATE).edit();
            editor.putInt("low_limit", 70);
            editor.commit();
            low_limit.setHint("Low limit: " + 70);
        } else {
            low_limit.setHint("Low limit: " + def_low_limit);
        }
        int def_high_limit = sharedPref.getInt("high_limit", 0);
        if (def_high_limit == 0) {
            SharedPreferences.Editor editor = getSharedPreferences("NS",MODE_PRIVATE).edit();
            editor.putInt("high_limit", 140);
            editor.commit();
            high_limit.setHint("High limit: " + 140);
        } else {
            high_limit.setHint("High limit: " + def_high_limit);
        }

        showProfile.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.putExtra("url", MainActivity.this.getDefaultUrl());
                startActivity(intent);
            }
        });

        set_default.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String value = url.getText().toString();

                if (value.length() == 0)
                    Toast.makeText(MainActivity.this, "Please insert an url!", Toast.LENGTH_SHORT).show();
                else {
                    SharedPreferences.Editor editor = getSharedPreferences("NS", MODE_PRIVATE).edit();
                    editor.putString("default_url", value);
                    editor.commit();
                    MainActivity.this.setDefaultUrl(value);
                    if (MainActivity.this.getI() != null)
                        MainActivity.this.getI().putExtra("url", MainActivity.this.getDefaultUrl());
                    url.setText("");

                }
                Log.i(TAG, "Url is: " + MainActivity.getUrl(value));
            }
        });

        setLimits.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String value_low = low_limit.getText().toString();
                String value_high = high_limit.getText().toString();

                if (value_low.length() != 0 && value_high.length() != 0) {

                    int low = Integer.valueOf(value_low);
                    int high = Integer.valueOf(value_high);
                    if (high <= low) {
                        Toast.makeText(MainActivity.this, "Low limit value cannot be bigger than high limit", Toast.LENGTH_SHORT).show();
                        low_limit.setText("");
                        high_limit.setText("");
                    }
                    else {
                        SharedPreferences.Editor editor = getSharedPreferences("NS", MODE_PRIVATE).edit();
                        editor.putInt("low_limit", low);
                        editor.putInt("high_limit", high);
                        editor.commit();
                        Log.i(TAG, "New limits set. Low: " + low + ", high: " + high);
                        low_limit.setHint("Low limit: " + low);
                        high_limit.setHint("High limit: " + high);
                        low_limit.setText("");
                        high_limit.setText("");
                    }
                } else {
                    if (value_high.length() == 0 && value_low.length() == 0) { }
                    else {
                        if (value_high.length() == 0) {
                            int low = Integer.valueOf(value_low);
                            int def_high = sharedPref.getInt("high_limit", 0);
                            if (low >= def_high) {
                                Toast.makeText(MainActivity.this, "Low limit value cannot be bigger than high limit", Toast.LENGTH_SHORT).show();
                                low_limit.setText("");
                            }
                            else {
                                SharedPreferences.Editor editor = getSharedPreferences("NS", MODE_PRIVATE).edit();
                                editor.putInt("low_limit", low);
                                editor.commit();
                                Log.i(TAG, "New low limit set: " + low);
                                low_limit.setHint("Low limit: " + low);
                                low_limit.setText("");
                            }
                        } else {
                            int high = Integer.valueOf(value_high);
                            int def_low = sharedPref.getInt("low_limit", 0);
                            if (def_low >= high) {
                                Toast.makeText(MainActivity.this, "Low limit value cannot be bigger than high limit", Toast.LENGTH_SHORT).show();
                                high_limit.setText("");
                            }
                            else {
                                SharedPreferences.Editor editor = getSharedPreferences("NS", MODE_PRIVATE).edit();
                                editor.putInt("high_limit", high);
                                editor.commit();
                                Log.i(TAG, "New high limit set: " + high);
                                high_limit.setHint("High limit: " + high);
                                high_limit.setText("");
                            }
                        }
                    }

                }

            }
        });

    }

    private static String getUrl(String base) {
        return "https://" + base + "/api/v1/";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mNetworkReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "Now I resume");
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.i(TAG, "Now I am on post resume");

        final SharedPreferences sharedPref = this.getSharedPreferences("NS", MODE_PRIVATE);
        String tmp = sharedPref.getString("default_url", null);
        if (tmp == null)
            Toast.makeText(MainActivity.this, "No default url set. Please load or set one and restart app!", Toast.LENGTH_SHORT).show();
        else {

            MainActivity.this.setDefaultUrl(tmp);
            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            Intent tmpI = new Intent(MainActivity.this, UpdateService.class);
            MainActivity.this.setI(tmpI);
            LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(myReceiver, intentFilter);
            MainActivity.this.getI().setAction(myReceiver.getClass().getName());
            MainActivity.this.getI().putExtra("url", MainActivity.this.getDefaultUrl());

            Timer timer = new Timer();
            TimerTask task = new TimerTask() {

                @Override
                public void run () {
                    startService(i);
                }
            };

            timer.schedule (task, 0l, 1000*1*30);  // 1000*10*60 every 10 minutes

            if (netInfo != null && netInfo.isConnected()) { }
            else {
                Toast.makeText(MainActivity.this, "No internet connection. Cannot display data", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Log.i(TAG, "Now I am on post create");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "Now I start");

        String action = "com.NS_RECEIVE";
        registerReceiver(myReceiver, new IntentFilter(action));
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "Now I stop");
        unregisterReceiver(myReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "Now I am paused");
        super.onPause();
    }

}

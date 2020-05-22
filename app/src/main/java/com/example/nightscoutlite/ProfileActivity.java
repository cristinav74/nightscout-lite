package com.example.nightscoutlite;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.widget.TextView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "Profile";

    Button showGraph;
    TextView profile;
    TextView dia;
    TextView isf;
    TextView icr;
    TextView units;
    TextView basals;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        showGraph = findViewById(R.id.button3);
        profile = findViewById(R.id.textView2);
        dia = findViewById(R.id.textView3);
        isf = findViewById(R.id.textView4);
        icr = findViewById(R.id.textView5);
        units = findViewById(R.id.textView6);
        basals = findViewById(R.id.textView7);

        Intent intent = getIntent();
        url = intent.getStringExtra("url");

        showGraph.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private static String getUrl(String base) {
        return "https://" + base + "/api/v1/";
    }

    @Override
    protected void onStart() {
        super.onStart();

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getUrl(url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetroProfileInterface service = retrofit.create(RetroProfileInterface.class);

        Call<List<NightscoutProfile>> retroCall = service.loadData();

        retroCall.enqueue(new Callback<List<NightscoutProfile>>() {
            @Override
            public void onResponse(Call<List<NightscoutProfile>> call, Response<List<NightscoutProfile>> response) {

                Log.i(TAG, "Retrofit response was successful");

                NightscoutProfile nsProfile = response.body().get(0);

                profile.setText("Profile: "+ nsProfile.getDefaultProfile() + " (" + url + ")");
                units.setText("Units: " + nsProfile.getUnits());
                Log.i(TAG, nsProfile.toString());
                isf.setText("ISF: " + nsProfile.getISF());
                dia.setText("DIA: " + nsProfile.getDIA());
                icr.setText("ICR: " + nsProfile.getICR());
                basals.setText("Basal rates (Hour -> Units):\n\n");

                for (ProfileRawData elem : nsProfile.getBasals()) {
                    basals.append("     " + elem.getTime() + "    ->     " + elem.getValue() + "\n");
                }
            }

            @Override
            public void onFailure(Call<List<NightscoutProfile>> call, Throwable t) {
                Log.i(TAG, "Something went wrong. Please try again later!");
            }
        });
    }
}

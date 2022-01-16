package com.power.powerBattery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import android.text.Html;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class speed_test extends AppCompatActivity {
    TextView now, max_now, min_now, result;
    RelativeLayout speed_test_layout;
    BatteryManager batteryManager;
    final int[] min = {Integer.MAX_VALUE};
    final int[] max = {Integer.MIN_VALUE};
    final int[] avg = {0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_test);
        now = findViewById(R.id.now);
        max_now = findViewById(R.id.max_now);
        min_now = findViewById(R.id.min_now);
        result = findViewById(R.id.result);
        speed_test_layout = findViewById(R.id.speed_test_layout);
        batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
        getApplicationContext().registerReceiver(broadcastReceiver, new IntentFilter("BatteryLevelReceiver"));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        MobileAds.initialize(getApplicationContext(), initializationStatus -> {
        });

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            Bundle b = intent.getExtras();
            assert b != null;
            long rate = b.getLong("rate");
            int level = b.getInt("level");
            String voltage_str = b.getString("voltage");
            String tech = b.getString("tech");
            boolean charging = b.getBoolean("charging");
            int health_color = b.getInt("health_color");
            String health_str = "<b>Health</b>" + "<br>" + b.getString("health") + "</br>";
            int current_now = (batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) / 1000) * -1;
            now.setText(Html.fromHtml("<b>Current Now :</b><br></br>" + current_now + "mA"));
            if (current_now < min[0]) {
                min_now.setText(Html.fromHtml("<b>Min. speed :</b><br></br>" + current_now + "mA"));
                min[0] = current_now;
            }
            if (current_now > max[0]) {
                max_now.setText(Html.fromHtml("<b>Max. speed :</b><br></br>" + current_now + "mA"));
                max[0] = current_now;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String status;
                if (batteryManager.isCharging()) {
                    if (current_now > 900) {
                        status = "<b>Charging fast</b>";
                        speed_test_layout.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.current_flow_good)));
                    } else if (current_now > 400) {
                        status = "<b>Charging moderately</b>";
                        speed_test_layout.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.current_flow_moderate)));
                    } else {
                        status = "<b>Charging slowly</b>";
                        speed_test_layout.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.current_flow_low)));
                    }

                } else {
                    if (current_now < -900) {
                        status = "<b>Draining heavily</b>";
                        speed_test_layout.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.current_flow_low)));
                    } else if (current_now < -400) {
                        status = "<b>Draining moderately</b>";
                        speed_test_layout.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.current_flow_moderate)));
                    } else {
                        status = "<b>Draining slowly</b>";
                        speed_test_layout.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.current_flow_good)));
                    }

                }
                result.setText(Html.fromHtml(status));

            }
        }
    };
}
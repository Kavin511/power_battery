package com.power.powerBattery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.instream.InstreamAd;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

public class speed_test extends AppCompatActivity {
    TextView now, max_now, min_now, result;
    RelativeLayout speed_test_layout;
    BatteryManager batteryManager;
    final int[] min = {Integer.MAX_VALUE};
    final int[] max = {Integer.MIN_VALUE};
    final int[] avg = {0};

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mInterstitialAd.show();
        finish();
    }

    private InterstitialAd mInterstitialAd;

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
        ConstraintLayout testParent = findViewById(R.id.testParent);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        Button startTest = findViewById(R.id.loadAd);
        if (mInterstitialAd.isLoaded()) {
            speed_test_layout.setVisibility(View.VISIBLE);
            mInterstitialAd.show();
        }
        startTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speed_test_layout.setVisibility(View.VISIBLE);
                mInterstitialAd.show();
                startTest.setVisibility(View.INVISIBLE);
                testParent.setVisibility(View.VISIBLE);
                Snackbar.make(startTest, "Loading ad! Speed test will start soon", Snackbar.LENGTH_SHORT).show();
            }

        });
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
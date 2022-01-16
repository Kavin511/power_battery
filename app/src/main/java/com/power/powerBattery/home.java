package com.power.powerBattery;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.power.powerBattery.dp.BatteryHistoryAdapter;
import com.power.powerBattery.dp.BatteryHistoryViewModel;

import me.itangqi.waveloadingview.WaveLoadingView;

public class home extends Fragment {
    SwitchCompat low_battery_notification, low_battery_alarm, high_battery_alarm,high_battery_notification;
    WaveLoadingView waveLoadingView;
    TextView temperature,current,health,remaining_time;
    TextView plugged,voltage,total_capacity,technology,available_charge;
    BatteryManager batteryManager;
    double batteryCapacity = 0;
    AdView mAdView;
    BatteryHistoryViewModel batteryHistoryViewModel;
    int k=0;
    @Nullable
    BatteryHistoryAdapter adapter;
    public home() {

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @SuppressLint("PrivateApi")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        requireContext().registerReceiver(broadcastReceiver,new IntentFilter("BatteryLevelReceiver"));
            View v = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ? inflater.inflate(R.layout.fragment_home, container, false) : inflater.inflate(R.layout.home_below_oreo, container, false);
            adapter = new BatteryHistoryAdapter(getContext());
            batteryManager = (BatteryManager) this.requireActivity().getSystemService(Context.BATTERY_SERVICE);
            batteryHistoryViewModel = new ViewModelProvider(this).get(BatteryHistoryViewModel.class);
            high_battery_notification = v.findViewById(R.id.battery_full_charge_notification);
            temperature = v.findViewById(R.id.temperature);
            current = v.findViewById(R.id.current);
            health = v.findViewById(R.id.health);
            remaining_time = v.findViewById(R.id.remaining_time);
            waveLoadingView = v.findViewById(R.id.waveLoadingView);
            plugged = v.findViewById(R.id.plugged);
            voltage = v.findViewById(R.id.voltage);
            total_capacity = v.findViewById(R.id.total_capacity);
            technology = v.findViewById(R.id.technology);
            available_charge = v.findViewById(R.id.available_capacity);

            final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";
            try {
                Object mPowerProfile = Class.forName(POWER_PROFILE_CLASS)
                        .getConstructor(Context.class)
                        .newInstance(getContext());
                batteryCapacity = (double) Class
                        .forName(POWER_PROFILE_CLASS)
                        .getMethod("getBatteryCapacity", new Class<?>[]{})
                        .invoke(mPowerProfile);

            } catch (Exception e) {
                e.printStackTrace();
            }
            String total_charge = "" + (int) batteryCapacity;
            total_charge += "<em> mAh</em>";
            total_capacity.setText(Html.fromHtml(total_charge));
        MobileAds.initialize(getContext(), initializationStatus -> {
        });

//       mAdView = v.findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);
            return v;

    }
    final BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            Bundle b = intent.getExtras();
            assert b != null;
            long rate = b.getLong("rate");
            int level = b.getInt("level");
            String voltage_str = b.getString("voltage");
            String tech = b.getString("tech");
            boolean charging = b.getBoolean("charging");
//            String status = b.getString("status");
            int health_color = b.getInt("health_color");
            String health_str = "<b>Health</b>" + "<br>" + b.getString("health") + "</br>";
            health.setText(Html.fromHtml(health_str));
            health.setTextColor(health_color);
            setTextViewDrawableColor(health,health_color);
            waveLoadingView.setProgressValue(level);
            String sourceString = "<b>" + "Current Now" + "</b><br>" + rate + " mA";
            current.setText(Html.fromHtml(sourceString));
            if (rate < 0) {
                setTextViewDrawableColor(current,Color.parseColor("#ff6347"));
            } else {
                setTextViewDrawableColor(current,Color.parseColor("#388538"));
            }
            if (charging) {
                waveLoadingView.startAnimation();
            } else {
                waveLoadingView.pauseAnimation();
            }
            waveLoadingView.setCenterTitle("" + level + "%");
            plugged.setText(b.getString("plugged_source"));
            voltage.setText(String.format("%s V", voltage_str));
            technology.setText(Build.DEVICE);
            technology.setText(tech);
            int available_c = (int) (level * (int) batteryCapacity / 100);
            String charge_text = "<b>Battery<br></br></b>" + available_c + " <em> mAh</em>";
            available_charge.setText(Html.fromHtml(charge_text));
            String remaining_time_val=b.getString("remaining_time","<b>24h left</b>");
            remaining_time.setText(Html.fromHtml(remaining_time_val));

            float temperature_value = (b.getFloat("temperature") / 10);
            String temperature_text="<b>Temperature</b><br></br>" + temperature_value + "°C";
            temperature.setText(Html.fromHtml(temperature_text));
            if (temperature_value<30)
            {
                setTextViewDrawableColor(temperature,Color.parseColor("#75aec7"));
            }
            else if(temperature_value>30&&temperature_value<42) {
                setTextViewDrawableColor(temperature,Color.parseColor("#F89451"));
            }
            else
            {
                setTextViewDrawableColor(temperature,Color.parseColor("#D84315"));
            }
            k=1;
        }
    };
    private void setTextViewDrawableColor(@NonNull TextView textView, int color) {
        for(Drawable drawable:textView.getCompoundDrawables())
        {
            if (drawable!=null)
            {
                drawable.setTint(color);
            }
        }
        textView.setTextColor(color);
    }
}

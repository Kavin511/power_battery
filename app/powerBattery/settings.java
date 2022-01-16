package com.power.powerBattery;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.switchmaterial.SwitchMaterial;

import static android.content.Context.MODE_PRIVATE;
import static com.power.powerBattery.App.current_speed_state;

import static com.power.powerBattery.App.high_battery_notification_state;
import static com.power.powerBattery.App.high_temperature_notification_state;
import static com.power.powerBattery.App.high_voltage_notification_state;
import static com.power.powerBattery.App.low_battery_notification_state;
import static com.power.powerBattery.App.permanent_notification_state;

public class settings extends Fragment {
    public settings() {
        // Required empty public constructor
    }
    @NonNull
    public static settings newInstance() {
        settings fragment = new settings();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    SwitchMaterial full_charge_notification,current_speed,permanent_notification,high_voltage_notification,high_temperature_notification,battery_low_notification;
    SharedPreferences sharedPrefs;
    SharedPreferences.Editor editor;
    TextView oreo_text;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_settings, container, false);
        permanent_notification=v.findViewById(R.id.permanent_notification);
        battery_low_notification=v.findViewById(R.id.battery_low_charge_notification);
        full_charge_notification=v.findViewById(R.id.battery_full_charge_notification);
        high_temperature_notification=v.findViewById(R.id.battery_high_temperature_notification);
        high_voltage_notification=v.findViewById(R.id.battery_high_voltage_notification);
        current_speed=v.findViewById(R.id.current_speed);
        oreo_text=v.findViewById(R.id.oreo_text);
//        BatteryLevelReceiver batteryLevelReceiver=new BatteryLevelReceiver();
        editor= requireActivity().getSharedPreferences("com.power.powerBattery",MODE_PRIVATE).edit();
        sharedPrefs = requireActivity().getSharedPreferences("com.power.powerBattery", MODE_PRIVATE);
       full_charge_notification.setChecked(sharedPrefs.getBoolean(high_battery_notification_state,false));
        permanent_notification.setChecked(sharedPrefs.getBoolean(permanent_notification_state,false));
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        if (!permanent_notification.isChecked()) {
            current_speed.setEnabled(false);
            high_voltage_notification.setEnabled(false);
            battery_low_notification.setEnabled(false);
            high_temperature_notification.setEnabled(false);
            full_charge_notification.setEnabled(false);
        }
        else
        {
            high_voltage_notification.setEnabled(true);
            battery_low_notification.setEnabled(true);
            high_temperature_notification.setEnabled(true);
            full_charge_notification.setEnabled(true);
        }
        }

        current_speed.setChecked(sharedPrefs.getBoolean(current_speed_state,false));
        high_voltage_notification.setChecked(sharedPrefs.getBoolean(high_voltage_notification_state,false));
        battery_low_notification.setChecked(sharedPrefs.getBoolean(low_battery_notification_state,false));
        high_temperature_notification.setChecked(sharedPrefs.getBoolean(high_temperature_notification_state,false));
        if (sharedPrefs.getBoolean(permanent_notification_state,false))
        {
            current_speed.setChecked(sharedPrefs.getBoolean(current_speed_state,false));
            Intent serviceIntent=new Intent(getContext(),Battery_service.class);
            ContextCompat.startForegroundService(requireContext(),serviceIntent);
        }
        else
            current_speed.setEnabled(false);
        switchChanges(current_speed,current_speed_state);
        switchChanges(full_charge_notification,high_battery_notification_state);

        switchChanges(battery_low_notification,low_battery_notification_state);
        switchChanges(high_voltage_notification,high_voltage_notification_state);

        switchChanges(high_temperature_notification,high_temperature_notification_state);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            switchChanges(current_speed,current_speed_state);
            oreo_text.setVisibility(View.VISIBLE);
        }
        else
        {
            current_speed.setVisibility(View.GONE);
            oreo_text.setVisibility(View.GONE);
        }
        permanent_notification.setOnClickListener(v1 -> {
            permanent_notification.setChecked(!sharedPrefs.getBoolean(permanent_notification_state,false));
            getPermanent_notification();
            editor.putBoolean(permanent_notification_state,!sharedPrefs.getBoolean(permanent_notification_state,false));
            editor.apply();
        });
        MobileAds.initialize(getContext(), initializationStatus -> {
        });

//       AdView mAdView = v.findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);
        return v;
    }
    public void getPermanent_notification() {
        if (!permanent_notification.isChecked())
        {
            current_speed.setChecked(false);
            current_speed.setEnabled(false);
            full_charge_notification.setChecked(false);
            full_charge_notification.setEnabled(false);
            battery_low_notification.setEnabled(false);
            battery_low_notification.setChecked(false);
            high_voltage_notification.setChecked(false);
            high_voltage_notification.setEnabled(false);
            high_temperature_notification.setChecked(false);
            high_temperature_notification.setEnabled(false);
            switchChanges(current_speed,current_speed_state);
            switchChanges(full_charge_notification,high_battery_notification_state);
            switchChanges(battery_low_notification,low_battery_notification_state);
            switchChanges(high_voltage_notification,high_voltage_notification_state);
            switchChanges(high_temperature_notification,high_temperature_notification_state);
            Intent serviceIntent=new Intent(getContext(),Battery_service.class);
                    requireContext().stopService(serviceIntent);
        }
        else
        {
            current_speed.setChecked(false);
            current_speed.setEnabled(true);
            switchChanges(current_speed,current_speed_state);
            full_charge_notification.setEnabled(true);
            switchChanges(full_charge_notification,high_battery_notification_state);
            battery_low_notification.setEnabled(true);
            switchChanges(battery_low_notification,low_battery_notification_state);
            high_voltage_notification.setEnabled(true);
            switchChanges(high_voltage_notification,high_voltage_notification_state);
            high_temperature_notification.setEnabled(true);
            switchChanges(high_temperature_notification,high_temperature_notification_state);
            full_charge_notification.setChecked(false);
            battery_low_notification.setChecked(false);
            high_voltage_notification.setChecked(false);
            high_temperature_notification.setChecked(false);
            Intent serviceIntent=new Intent(getContext(),Battery_service.class);
            ContextCompat.startForegroundService(requireContext(),serviceIntent);
        }
    }
    void switchChanges(@NonNull SwitchMaterial switch_name, String keyValue)
    {
        switch_name.setOnCheckedChangeListener((compoundButton, b) -> editorCommit(keyValue,switch_name));
    }
    private void editorCommit(String keyValue, @NonNull SwitchMaterial switch_name) {
        editor.putBoolean(keyValue,!sharedPrefs.getBoolean(keyValue,false));
        editor.commit();
        switch_name.setChecked(sharedPrefs.getBoolean(keyValue,true));
    }

}
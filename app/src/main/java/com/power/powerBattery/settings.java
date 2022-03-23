package com.power.powerBattery;

import static android.content.Context.MODE_PRIVATE;
import static com.power.powerBattery.App.current_speed_state;
import static com.power.powerBattery.App.high_battery_notification_state;
import static com.power.powerBattery.App.high_temperature_notification_state;
import static com.power.powerBattery.App.high_voltage_notification_state;
import static com.power.powerBattery.App.low_battery_notification_state;
import static com.power.powerBattery.App.permanent_notification_state;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.MobileAds;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

interface RewardListener {
    void rewardChange();
}

public class settings extends Fragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    SwitchMaterial full_charge_notification, current_speed, permanent_notification, high_voltage_notification, high_temperature_notification, battery_low_notification;
    SharedPreferences sharedPrefs;
    SharedPreferences.Editor editor;
    TextView oreo_text, reward_value;
    public static RewardListener rewardListener;
    View v;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_settings, container, false);
        initialiseViews();
        initialisePrefs();
        rewardListener();
        full_charge_notification.setChecked(sharedPrefs.getBoolean(high_battery_notification_state, false));
        permanent_notification.setChecked(sharedPrefs.getBoolean(permanent_notification_state, false));
        setUpNotification();
        updateCheckedStatus(current_speed, sharedPrefs.getBoolean(current_speed_state, false), high_voltage_notification, sharedPrefs.getBoolean(high_voltage_notification_state, false), battery_low_notification, sharedPrefs.getBoolean(low_battery_notification_state, false), sharedPrefs.getBoolean(high_temperature_notification_state, false));
        if (sharedPrefs.getBoolean(permanent_notification_state, false)) {
            startPermanentNotification();
        } else
            current_speed.setEnabled(false);
        updateSwitchStatuses();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            switchChanges(current_speed, current_speed_state);
            oreo_text.setVisibility(View.VISIBLE);
        } else {
            current_speed.setVisibility(View.GONE);
            oreo_text.setVisibility(View.GONE);
        }
        permanent_notification.setOnClickListener(v1 -> {
            permanent_notification.setChecked(!sharedPrefs.getBoolean(permanent_notification_state, false));
            getPermanent_notification();
            editor.putBoolean(permanent_notification_state, !sharedPrefs.getBoolean(permanent_notification_state, false));
            editor.apply();
        });
        MobileAds.initialize(requireContext(), initializationStatus -> {
        });
        return v;
    }

    private void startPermanentNotification() {
        current_speed.setChecked(sharedPrefs.getBoolean(current_speed_state, false));
        Intent serviceIntent = new Intent(getContext(), Battery_service.class);
        ContextCompat.startForegroundService(requireContext(), serviceIntent);
    }

    private void updateSwitchStatuses() {
        switchChanges(current_speed, current_speed_state);
        switchChanges(full_charge_notification, high_battery_notification_state);
        switchChanges(battery_low_notification, low_battery_notification_state);
        switchChanges(high_voltage_notification, high_voltage_notification_state);
        switchChanges(high_temperature_notification, high_temperature_notification_state);
    }

    private void updateCheckedStatus(SwitchMaterial current_speed, boolean aBoolean, SwitchMaterial high_voltage_notification, boolean aBoolean2, SwitchMaterial battery_low_notification, boolean aBoolean3, boolean aBoolean4) {
        current_speed.setChecked(aBoolean);
        high_voltage_notification.setChecked(aBoolean2);
        battery_low_notification.setChecked(aBoolean3);
        high_temperature_notification.setChecked(aBoolean4);
    }

    private void setUpNotification() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (!permanent_notification.isChecked()) {
                changeFieldEnabledStatus(false, high_voltage_notification, high_temperature_notification, full_charge_notification);
            } else {
                high_voltage_notification.setEnabled(true);
                battery_low_notification.setEnabled(true);
                high_temperature_notification.setEnabled(true);
                full_charge_notification.setEnabled(true);
            }
        }
    }

    private void initialisePrefs() {
        editor = requireActivity().getSharedPreferences("com.power.powerBattery", MODE_PRIVATE).edit();
        sharedPrefs = requireActivity().getSharedPreferences("com.power.powerBattery", MODE_PRIVATE);
    }

    private void rewardListener() {
        reward_value.setText(sharedPrefs.getLong("rewards", 0) + " " + reward_value.getText());
        rewardListener = this::rewardChange;
    }

    private void initialiseViews() {
        permanent_notification = v.findViewById(R.id.permanent_notification);
        battery_low_notification = v.findViewById(R.id.battery_low_charge_notification);
        full_charge_notification = v.findViewById(R.id.battery_full_charge_notification);
        high_temperature_notification = v.findViewById(R.id.battery_high_temperature_notification);
        reward_value = v.findViewById(R.id.reward_value);
        high_voltage_notification = v.findViewById(R.id.battery_high_voltage_notification);
        current_speed = v.findViewById(R.id.current_speed);
        oreo_text = v.findViewById(R.id.oreo_text);
    }

    public void getPermanent_notification() {
        if (!permanent_notification.isChecked()) {
            disableFields();
            updateSwitchStatuses();
            Intent serviceIntent = new Intent(getContext(), Battery_service.class);
            requireContext().stopService(serviceIntent);
        } else {
            current_speed.setChecked(false);
            changeFieldEnabledStatus(true, full_charge_notification, high_voltage_notification, high_temperature_notification);
            updateSwitchStatuses();
            updateCheckedStatus(full_charge_notification, false, battery_low_notification, false, high_voltage_notification, false, false);
            Intent serviceIntent = new Intent(getContext(), Battery_service.class);
            ContextCompat.startForegroundService(requireContext(), serviceIntent);
        }
    }

    private void changeFieldEnabledStatus(boolean b, @NonNull SwitchMaterial full_charge_notification, @NonNull SwitchMaterial high_voltage_notification, @NonNull SwitchMaterial high_temperature_notification) {
        current_speed.setEnabled(b);
        full_charge_notification.setEnabled(b);
        battery_low_notification.setEnabled(b);
        high_voltage_notification.setEnabled(b);
        high_temperature_notification.setEnabled(b);
    }

    private void disableFields() {
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
    }

    void switchChanges(@NonNull SwitchMaterial switch_name, String keyValue) {
        switch_name.setOnCheckedChangeListener((compoundButton, b) -> {
            rewardCheck(switch_name, keyValue, compoundButton);
        });
    }

    private void rewardCheck(@NonNull SwitchMaterial switch_name, String keyValue, android.widget.CompoundButton compoundButton) {
        if (sharedPrefs.getLong("rewards", 0) < 50) {
            if (compoundButton.isChecked()) {
                Snackbar.make(v, "Earn at least 50 points to unlock this feature!", Snackbar.LENGTH_LONG).show();
                compoundButton.setChecked(false);
            }
        } else {
            deductReward(keyValue);
            editorCommit(keyValue, switch_name);
        }
    }

    private void deductReward(String keyValue) {
        if (!sharedPrefs.getBoolean(keyValue, false)) {
            Snackbar.make(v, "50 points detected!", Snackbar.LENGTH_LONG).show();
            editor.putLong("rewards", sharedPrefs.getLong("rewards", 0) - 50);
            editor.commit();
            rewardListener.rewardChange();
        }
    }

    private void editorCommit(String keyValue, @NonNull SwitchMaterial switch_name) {
        editor.putBoolean(keyValue, !sharedPrefs.getBoolean(keyValue, false));
        editor.commit();
        switch_name.setChecked(sharedPrefs.getBoolean(keyValue, true));
    }

    private void rewardChange() {
        reward_value.setText(sharedPrefs.getLong("rewards", 0) + getString(R.string.wallet_text));
    }
}
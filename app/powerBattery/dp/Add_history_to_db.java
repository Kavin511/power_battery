package com.power.powerBattery.dp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Add_history_to_db {
    BatteryHistoryViewModel batteryHistoryViewModel;
    BatteryHistoryAdapter adapter;
    final DateFormat simple = new SimpleDateFormat("hh:mm:ss  [dd/MM/yyyy]", Locale.ENGLISH);
    public void addHistoryToDB(int finalLevel, @NonNull SharedPreferences sharedPreferences, @NonNull SharedPreferences.Editor editor, @NonNull String state, boolean charging, @NonNull Context context)
    {
        batteryHistoryViewModel=new BatteryHistoryViewModel((Application) context.getApplicationContext());
        adapter=new BatteryHistoryAdapter(context);
        if (state.equals("charging") && !charging) {
            final long time = Calendar.getInstance().getTime().getTime();
            long startingTime = sharedPreferences.getLong("startingTime", 0);
            int initialLevel = sharedPreferences.getInt("initialLevel", 0);
            Date initial_time = new Date(startingTime);
            Date final_time = new Date(time);
            if (startingTime != time) {
                String history =  " <b>Charged From : </b><br></br>" + simple.format(initial_time) + "<br></br><b>To : </b><br></br>" + simple.format(final_time) + "<br></br><b>Initial charge Level : </b>"+ initialLevel + "%<br></br><b>Final charge Level : </b>" + finalLevel+"%";
                final BatteryHistoryDB batteryHistoryDB = new BatteryHistoryDB(history,time);
                batteryHistoryViewModel.insert(batteryHistoryDB);
                adapter.notifyDataSetChanged();
            }
            editor.putString("charging_state", "discharging");
            editor.putLong("startingTime", time);
            editor.putInt("initialLevel", finalLevel);
            editor.apply();

        } else if (state.equals("discharging") && charging) {
            final long time = Calendar.getInstance().getTime().getTime();
            editor.putLong("endingTime", time);
            editor.apply();
            long startingTime = sharedPreferences.getLong("startingTime", 0);
            int initialLevel = sharedPreferences.getInt("initialLevel", 0);
            long endingTime = sharedPreferences.getLong("endingTime", 0);
            final Date initial_time = new Date(startingTime);
            final Date final_time = new Date(endingTime);
            String history = "<b>Discharged From : </b><br></br>"+ simple.format(initial_time)+"<br></br><b>To : </b><br></br>" + simple.format(final_time) + "<br></br><b>Initial charge level : </b>"+ initialLevel+"%" + "<br></br><b>Final charge level : </b>"+ finalLevel+"%";
            final BatteryHistoryDB batteryHistoryDB = new BatteryHistoryDB(history,time);
            batteryHistoryViewModel.insert(batteryHistoryDB);
            adapter.notifyDataSetChanged();
            editor.putString("charging_state", "charging");
            editor.putLong("startingTime", time);
            editor.putInt("initialLevel", finalLevel);
            editor.commit();
        }
    }
}

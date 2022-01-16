package com.power.powerBattery;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.core.content.ContextCompat;

import java.util.Objects;

public class App extends Application{
    public static final String CHANNEL_ID1="1",CHANNEL_ID2="2",CHANNEL_ID3="3",CHANNEL_ID4="4";
    public static final String low_battery_notification_state="low_battery_notification_state";
    public static final String high_battery_notification_state="high_battery_notification_state";
    public static final String current_speed_state="current_speed";
    public static final String permanent_notification_state="permanent_notification";
    public static final String high_voltage_notification_state="high_voltage_notification_state";
    public static final String high_temperature_notification_state="high_temperature_notification_state";
    public static boolean alreadyFullDisplayed=false;
    public static boolean alreadyLowDisplayed=false;
    public static boolean alreadyTemperatureDisplayed=false;
    public static boolean alreadyVoltageDisplayed=false;
    @Override
    public void onCreate() {
        super.onCreate();
//        startingBatteryLevelReceiver();
        createNotificationChannel();
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            Intent serviceIntent = new Intent(getApplicationContext(), Battery_service.class);
            ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
        }
    }
    private void createNotificationChannel() {
        NotificationChannel notificationChannel= null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(
                    CHANNEL_ID1,"Battery data", NotificationManager.IMPORTANCE_DEFAULT
            );
        }
        NotificationManager manager= null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            manager = getSystemService(NotificationManager.class);
        }
        NotificationChannel notificationChannel1= null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel1 = new NotificationChannel(
                    CHANNEL_ID2,"Speed of battery", NotificationManager.IMPORTANCE_NONE
            );
            notificationChannel1.setSound(null,null);
        }
        NotificationChannel notificationChannel2= null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel2 = new NotificationChannel(
                    CHANNEL_ID3,"Full charge notification", NotificationManager.IMPORTANCE_DEFAULT
            );
        }
        NotificationChannel notificationChannel3= null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel3 = new NotificationChannel(
                    CHANNEL_ID4,"Low charge notification", NotificationManager.IMPORTANCE_DEFAULT
            );
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel.setDescription("Show information about battery");
            notificationChannel1.setDescription("Speed of charging or discharging");
            notificationChannel2.getSound();
            notificationChannel2.setImportance(NotificationManager.IMPORTANCE_HIGH);
            notificationChannel2.setDescription("Notifies when charge is full");
            notificationChannel3.setDescription("Notifies when charge is low");
            manager.createNotificationChannel(notificationChannel);
            manager.createNotificationChannel(notificationChannel1);
            manager.createNotificationChannel(notificationChannel2);
            manager.createNotificationChannel(notificationChannel3);
        }
    }
   public  void startingBatteryLevelReceiver() {
        final IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        new Thread(() -> {
            while (true)
            {
                BatteryLevelReceiver batteryLevelReceiver=new BatteryLevelReceiver();
                Objects.requireNonNull(getApplicationContext()).registerReceiver(batteryLevelReceiver,intentFilter);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}

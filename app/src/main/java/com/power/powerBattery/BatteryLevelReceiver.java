package com.power.powerBattery;

import static android.content.Context.MODE_PRIVATE;
import static com.power.powerBattery.App.CHANNEL_ID1;
import static com.power.powerBattery.App.CHANNEL_ID3;
import static com.power.powerBattery.App.CHANNEL_ID4;
import static com.power.powerBattery.App.alreadyFullDisplayed;
import static com.power.powerBattery.App.alreadyLowDisplayed;
import static com.power.powerBattery.App.alreadyTemperatureDisplayed;
import static com.power.powerBattery.App.alreadyVoltageDisplayed;
import static com.power.powerBattery.App.high_battery_notification_state;
import static com.power.powerBattery.App.high_temperature_notification_state;
import static com.power.powerBattery.App.low_battery_notification_state;
import static com.power.powerBattery.App.permanent_notification_state;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Build;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.power.powerBattery.dp.Add_history_to_db;

import java.util.Calendar;
import java.util.Objects;

public class BatteryLevelReceiver extends BroadcastReceiver  {
    BatteryManager batteryManager;
    PendingIntent pendingIntent ;
    // --Commented out by Inspection (02-11-2020 17:42):RemoteViews remoteViews;
    SharedPreferences sharedPrefs;
    // --Commented out by Inspection (02-11-2020 17:42):BatteryHistoryViewModel batteryHistoryViewModel;
    // --Commented out by Inspection (02-11-2020 17:42):BatteryHistoryAdapter adapter;
    BatteryNotification batteryNotification;
//     --Commented out by Inspection (02-11-2020 17:42):final DateFormat simple = new SimpleDateFormat("hh:mm:ss  [dd/MM/yyyy]",Locale.ENGLISH);
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(@NonNull final Context context, @NonNull Intent intent) {
        batteryManager= (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        sharedPrefs = context.getSharedPreferences("com.power.powerBattery", MODE_PRIVATE);
//        batteryHistoryViewModel=new BatteryHistoryViewModel((Application) context.getApplicationContext());
//        adapter=new BatteryHistoryAdapter(context);
        batteryNotification=new BatteryNotification();
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        pendingIntent = PendingIntent.getActivity(context, 0,
                new Intent(context,MainActivity.class), 0);
            int level=intent.getIntExtra("level",0);
            int health_constant=intent.getIntExtra("health",0);
            float temperature= (float) intent.getIntExtra("temperature", 0);
            int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
            boolean wireless=chargePlug==BatteryManager.BATTERY_PLUGGED_WIRELESS;
        int battery_status= 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            battery_status = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS);
        }
        String tech= Objects.requireNonNull(intent.getExtras()).getString(BatteryManager.EXTRA_TECHNOLOGY);
            String  voltage = String.valueOf(intent.getIntExtra("voltage", 0));
            if (voltage.length()>=3)
            voltage=voltage.substring(0,voltage.length()-3)+"."+voltage.substring(voltage.length()-3);
            Intent i=new Intent("BatteryLevelReceiver");
            i.putExtra("level",level);
            i.putExtra("temperature",temperature);
            i.putExtra("voltage",voltage);
            i.putExtra("tech",tech);
            i.putExtra("charging",isCharging);
        if (battery_status==2)
            {
                i.putExtra("status","Charging");
                i.putExtra("chargingColor", R.color.design_default_color_primary_dark);
            }
            else if (battery_status==3)
            {
                i.putExtra("status","Discharging");
                i.putExtra("chargingColor",Color.DKGRAY);
            }
            else if (battery_status==5)
            {
                i.putExtra("status","Battery full");
                i.putExtra("chargingColor", Color.parseColor("#38853B"));
            }
            else if(battery_status==4)
            {
                i.putExtra("status","Battery not charging");
            }
            else if (battery_status==1)
            {
                i.putExtra("status","Statistics unknown");
            }
            if(usbCharge)
            {
                i.putExtra("plugged_source","USB ");
            }
            else if(acCharge)
            {
                i.putExtra("plugged_source","AC source");
            }
            else if (wireless)
            {
                i.putExtra("plugged_source","Wirelessly");
            }
            else
            {
                i.putExtra("plugged_source","On battery");
            }
            switch (health_constant){
                    case 7:
                        i.putExtra("health","Cold");
                        i.putExtra("health_color",Color.parseColor("#19f79e"));
                        break;
                    case  4:
                        i.putExtra("health","Dead");
                        i.putExtra("health_color",Color.parseColor("#403f3e"));
                        break;
                    case 2:
                        i.putExtra("health","Good");
                        i.putExtra("health_color",Color.parseColor("#016308"));
                        break;
                    case 3:
                        i.putExtra("health","Heated");
                        i.putExtra("health_color",Color.parseColor("#f5390a"));
                        break;
                    case 5:
                        i.putExtra("health","High voltage");
                        i.putExtra("health_color",Color.parseColor("#f5390a"));
                        break;
                    case 1:
                        i.putExtra("health","Unknown");
                        i.putExtra("health_color",Color.parseColor("#000"));
                        break;
                }
            long rate=rate_now();

        if (isCharging&&rate<0)
        {
            if (level!=100)
            i.putExtra("remaining_time","<b>Charging very  slowly</b>");
            else
                i.putExtra("remaining_time","<b>Charged </b>");
        }
        else if (isCharging)
        {
            double remaining_percent=100-level;
            if (remaining_percent!=0)
            {
                remaining_percent*=10000.0;
                remaining_percent/=((float)(rate/1000)*5);
                int hrs=(int) remaining_percent/60;
                int min=(int)(remaining_percent-(hrs*60));

                String result=(hrs>0&&hrs<48)?hrs+"h ":"";
                String minutes=""+((int)remaining_percent-(hrs*60))+"m";
                if (minutes.equals("0")&&hrs==0)
                    result+="<b>Few sec left</b>";
                else if (hrs<48)
                    result+=minutes;
                if (result.length()>0)
                    i.putExtra("remaining_time","<b>Charging Time Left</b><br></br>"+result);
                else
                    i.putExtra("remaining_time","<b>Charging Time Left</b><br></br>24hrs");
            }
            else
            {
                i.putExtra("remaining_time","<b>Battery Full</b>");
            }
        }
        else
        {
            double remaining_percent=level;
            remaining_percent*=10000.0;
            remaining_percent/=(Math.abs((float) rate/1000)*5);
            int hrs=(int) remaining_percent/60;
            int min=(int)(remaining_percent-(hrs*60));
            String result=(hrs>0&&hrs<100)?hrs+"h ":"";
            String minutes=""+((int)remaining_percent-(hrs*60))+"m";
            if (minutes.equals("0")&&hrs==0)
                result+="<b>Few sec left</b>";
            else
            result+=minutes;
            i.putExtra("remaining_time","<b>Charge Lasts For</b><br></br>"+result);
        }
        if (level==100)
        {
            i.putExtra("remaining_time","<b>Battery full</b>");
        }

            i.putExtra("rate",rate/1000);
            context.sendBroadcast(i);
          Notification notification=batteryNotification.sendNotification(context,level,i,sharedPrefs,pendingIntent,temperature);
        if (notification!=null&&sharedPrefs.getBoolean(permanent_notification_state,false))
        {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(1, notification);
        }
        if (level==100)
        {
            if ((!alreadyFullDisplayed)&&sharedPrefs.getBoolean(high_battery_notification_state,false)) {
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                        new Intent(context, MainActivity.class), 0);
                batteryNotification.sendAlertNotification("Battery Alert", "Battery charged fully ", CHANNEL_ID3, level, context, pendingIntent);
                alreadyFullDisplayed=true;
            }
        }
        else
        {
            alreadyFullDisplayed=false;
        }
          if (sharedPrefs.getBoolean(high_battery_notification_state,false)&&(!alreadyVoltageDisplayed)&&health_constant==5)
          {
              batteryNotification.sendAlertNotification("Voltage Alert","High voltage detected",CHANNEL_ID3,level,context,pendingIntent);
              alreadyVoltageDisplayed=true;
          }
          else if (health_constant!=5)
          {
              alreadyVoltageDisplayed=false;
          }
          if (level<20&&(!alreadyLowDisplayed)&&sharedPrefs.getBoolean(low_battery_notification_state,false))
          {
              batteryNotification.sendAlertNotification("Battery alert","Low battery "+level+"%",CHANNEL_ID4,level,context,pendingIntent);
              alreadyLowDisplayed=true;
          }
          else if (level>20)
          {
              alreadyLowDisplayed=false;
          }
          if (temperature>460&&(!alreadyTemperatureDisplayed)&&sharedPrefs.getBoolean(high_temperature_notification_state,false))
          {
              batteryNotification.sendAlertNotification("Temperature alert","High temperature detected "+(temperature/10.0)+"ºC",CHANNEL_ID1,level,context,pendingIntent);
              alreadyTemperatureDisplayed=true;
          }
          else if (temperature<320)
          {
              alreadyTemperatureDisplayed=false;
          }

        final SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor=sharedPreferences.edit();
        String state=sharedPreferences.getString("charging_state","NEW");
        assert state != null;
        if (state.equals("NEW"))
        {
            final long start_time=Calendar.getInstance().getTime().getTime();
            if (isCharging)
                editor.putString("charging_state","charging");
            else
                editor.putString("charging_state","discharging");
            editor.putLong("startingTime",start_time);
            editor.putInt("initialLevel",level);
            editor.apply();
        }
        else
        {
            Add_history_to_db add_history_to_db=new Add_history_to_db();
            add_history_to_db.addHistoryToDB(level,sharedPreferences,editor,state,isCharging,context);
        }
    }


    private long rate_now() {
        return (batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)*-1);
    }
}

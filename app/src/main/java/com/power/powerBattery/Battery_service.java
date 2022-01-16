package com.power.powerBattery;

import static com.power.powerBattery.App.CHANNEL_ID2;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class Battery_service extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        final Intent notificationIntent=new Intent(this,MainActivity.class);
        final PendingIntent pendingIntent=PendingIntent.getActivity(this,0,notificationIntent,0);
        sendNotification(getBaseContext(),pendingIntent);
        return START_REDELIVER_INTENT;
    }
    private void sendNotification(Context context, PendingIntent pendingIntent) {
        Notification notification;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification=new Notification.Builder(context,CHANNEL_ID2)
                    .setSmallIcon(R.drawable.ic_notification_small_icon)
                    .setContentText("Computing Data! ")
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(1,notification);
        }
        else
        {
            Log.d("foreground", "sendNotification: ");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
    
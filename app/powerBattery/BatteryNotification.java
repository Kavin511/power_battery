package com.power.powerBattery;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Icon;
import android.media.RingtoneManager;
import android.os.Build;
import android.text.Html;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import static com.power.powerBattery.App.CHANNEL_ID2;
import static com.power.powerBattery.App.permanent_notification_state;

public class BatteryNotification extends Notification {

   @Nullable
   public Notification sendNotification(@NonNull Context context, int level, @NonNull Intent i, @NonNull SharedPreferences sharedPrefs, PendingIntent pendingIntent, Float temperature) {
        String package_name="com.power.powerBattery";
        String unit;
        long rate=i.getLongExtra("rate",0);
        if ((rate>-999)&&rate<999)
            unit="mA";
        else
        {
            rate/=1000;
            unit="Amp";
        }
        RemoteViews remoteViews=new RemoteViews(package_name,R.layout.notification_layout);
        remoteViews.setTextViewText(R.id.temperature_view,""+temperature/10.0+"ºC");
        remoteViews.setTextViewText(R.id.current_view,""+rate+unit);
        remoteViews.setTextViewText(R.id.remaining_time_view, Html.fromHtml(i.getStringExtra("remaining_time")));
        if (sharedPrefs.getBoolean(permanent_notification_state,false))
        {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                if (sharedPrefs.getBoolean("current_speed",false))
                {
                    return new Builder(context,CHANNEL_ID2)
                            .setContentIntent(pendingIntent)
                            .setCustomContentView(remoteViews)
                            .setSmallIcon(getIndicatorIcon(""+rate,unit))
                            .setOngoing(true)
                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))
                            .setContentIntent(pendingIntent).build();
                }
                else
                {
                    return new Builder(context,CHANNEL_ID2)
                            .setCustomContentView(remoteViews)
                            .setSmallIcon(R.drawable.ic_notification_small_icon)
                            .setContentIntent(pendingIntent)
                            .setOngoing(true)
                            .build();
                }
            }
            else
            {
                remoteViews.setTextViewText(R.id.current_view,""+level+"%");
                remoteViews.setTextViewText(R.id.remaining_time_view, Html.fromHtml(i.getStringExtra("voltage")));
                NotificationCompat.Builder builder=new NotificationCompat.Builder(context)
                        .setOngoing(sharedPrefs.getBoolean(permanent_notification_state,false))
                        .setCustomContentView(remoteViews)
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(R.drawable.ic_notification_small_icon);
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(1, builder.build());
            }
        }
        else
        {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(1);
        }
        return null;

    }
    public void  sendAlertNotification(String s, String subtext, String channelId, int level, @NonNull Context context, PendingIntent pendingIntent) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification notification = new Notification.Builder(context,channelId)
                    .setContentTitle(s)
                    .setContentText(subtext)
                    .setSmallIcon(R.drawable.ic_notification_small_icon)
                    .setPriority(PRIORITY_MAX)
                    .setContentIntent(pendingIntent).build();
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(level, notification);
        }
        else
        {
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID2);
            builder.setSmallIcon(R.drawable.ic_notification_small_icon)
                    .setContentTitle(s)
                    .setContentText(subtext)
                    .setPriority(PRIORITY_MAX)
                    .setContentIntent(pendingIntent);
            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(level, builder.build());
        }
    }


    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.M)
    Canvas mIconCanvas=new Canvas();
    Paint mIconSpeedPaint,mIconUnitPaint;
    Bitmap mIconBitmap;
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setupIndicatorIconGenerator() {
        mIconSpeedPaint = new Paint();
        mIconSpeedPaint.setColor(Color.WHITE);
        mIconSpeedPaint.setAntiAlias(true);
        mIconSpeedPaint.setTextSize(55);
        mIconSpeedPaint.setTextAlign(Paint.Align.CENTER);
        mIconSpeedPaint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        mIconUnitPaint = new Paint();
        mIconUnitPaint.setColor(Color.WHITE);
        mIconUnitPaint.setTextSize(35);
        mIconUnitPaint.setTextAlign(Paint.Align.CENTER);
        mIconUnitPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mIconBitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888);
        mIconCanvas = new Canvas(mIconBitmap);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private Icon getIndicatorIcon(@NonNull String speedValue, @NonNull String speedUnit) {
        setupIndicatorIconGenerator();
        mIconCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        if (Integer.parseInt(speedValue)<-999)
        {
            mIconSpeedPaint.setTextSize(50);
        }
        mIconCanvas.drawText(speedValue, 50, 50, mIconSpeedPaint);
        mIconCanvas.drawText(speedUnit, 48, 95, mIconUnitPaint);
        return Icon.createWithBitmap(mIconBitmap);
    }

}

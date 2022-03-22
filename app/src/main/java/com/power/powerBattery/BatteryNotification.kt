package com.power.powerBattery

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.*
import android.graphics.drawable.Icon
import android.media.RingtoneManager
import android.os.Build
import android.text.Html
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class BatteryNotification : Notification() {
    fun sendNotification(
        context: Context,
        level: Int,
        i: Intent,
        sharedPrefs: SharedPreferences,
        pendingIntent: PendingIntent?,
        temperature: Float
    ): Notification? {
        val package_name = "com.power.powerBattery"
        val unit: String
        var rate = i.getLongExtra("rate", 0)
        if (rate > -999 && rate < 999) unit = "mA" else {
            rate /= 1000
            unit = "Amp"
        }
        val remoteViews = RemoteViews(package_name, R.layout.notification_layout)
        remoteViews.setTextViewText(R.id.temperature_view, "" + temperature / 10.0 + "ºC")
        remoteViews.setTextViewText(R.id.current_view, "" + rate + unit)
        remoteViews.setTextViewText(
            R.id.remaining_time_view,
            Html.fromHtml(i.getStringExtra("remaining_time"))
        )
        if (sharedPrefs.getBoolean(App.permanent_notification_state, false)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return if (sharedPrefs.getBoolean("current_speed", false)) {
                    Builder(context, App.CHANNEL_ID2)
                        .setContentIntent(pendingIntent)
                        .setCustomContentView(remoteViews)
                        .setSmallIcon(getIndicatorIcon("" + rate, unit))
                        .setOngoing(true)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))
                        .setContentIntent(pendingIntent).build()
                } else {
                    Builder(context, App.CHANNEL_ID2)
                        .setCustomContentView(remoteViews)
                        .setSmallIcon(R.drawable.ic_notification_small_icon)
                        .setContentIntent(pendingIntent)
                        .setOngoing(true)
                        .build()
                }
            } else {
                remoteViews.setTextViewText(R.id.current_view, "$level%")
                remoteViews.setTextViewText(
                    R.id.remaining_time_view,
                    Html.fromHtml(i.getStringExtra("voltage"))
                )
                val builder = NotificationCompat.Builder(context)
                    .setOngoing(sharedPrefs.getBoolean(App.permanent_notification_state, false))
                    .setCustomContentView(remoteViews)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_notification_small_icon)
                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.notify(1, builder.build())
            }
        } else {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancel(1)
        }
        return null
    }

    fun sendAlertNotification(
        s: String?,
        subtext: String?,
        channelId: String?,
        level: Int,
        context: Context,
        pendingIntent: PendingIntent?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notification = Builder(context, channelId)
                .setContentTitle(s)
                .setContentText(subtext)
                .setSmallIcon(R.drawable.ic_notification_small_icon)
                .setPriority(PRIORITY_MAX)
                .setContentIntent(pendingIntent).build()
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(level, notification)
        } else {
            val builder = NotificationCompat.Builder(context, App.CHANNEL_ID2)
            builder.setSmallIcon(R.drawable.ic_notification_small_icon)
                .setContentTitle(s)
                .setContentText(subtext)
                .setPriority(PRIORITY_MAX)
                .setContentIntent(pendingIntent)
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(level, builder.build())
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    var mIconCanvas = Canvas()
    lateinit var mIconSpeedPaint: Paint
    lateinit var mIconUnitPaint: Paint
    lateinit var mIconBitmap: Bitmap

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun setupIndicatorIconGenerator() {
        mIconSpeedPaint = Paint()
        mIconSpeedPaint.color = Color.WHITE
        mIconSpeedPaint.isAntiAlias = true
        mIconSpeedPaint.textSize = 55f
        mIconSpeedPaint.textAlign = Paint.Align.CENTER
        mIconSpeedPaint.typeface = Typeface.create("sans-serif", Typeface.BOLD)
        mIconUnitPaint = Paint()
        mIconUnitPaint.color = Color.WHITE
        mIconUnitPaint.textSize = 35f
        mIconUnitPaint.textAlign = Paint.Align.CENTER
        mIconUnitPaint.typeface = Typeface.DEFAULT_BOLD
        mIconBitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
        mIconCanvas = Canvas(mIconBitmap)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getIndicatorIcon(speedValue: String, speedUnit: String): Icon {
        setupIndicatorIconGenerator()
        mIconCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        if (speedValue.toInt() < -999) {
            mIconSpeedPaint.textSize = 50f
        }
        mIconCanvas.drawText(speedValue, 50f, 50f, mIconSpeedPaint!!)
        mIconCanvas.drawText(speedUnit, 48f, 95f, mIconUnitPaint!!)
        return Icon.createWithBitmap(mIconBitmap)
    }
}
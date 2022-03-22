package com.power.powerBattery

import android.app.Application
import android.content.Intent
import com.power.powerBattery.Battery_service
import androidx.core.content.ContextCompat
import android.app.NotificationChannel
import com.power.powerBattery.App
import android.app.NotificationManager
import android.content.IntentFilter
import android.os.Build
import com.power.powerBattery.BatteryLevelReceiver
import java.util.*

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            val serviceIntent = Intent(applicationContext, Battery_service::class.java)
            ContextCompat.startForegroundService(applicationContext, serviceIntent)
        }
    }

    private fun createNotificationChannel() {
        var notificationChannel: NotificationChannel? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(
                CHANNEL_ID1, "Battery data", NotificationManager.IMPORTANCE_DEFAULT
            )
        }
        var manager: NotificationManager? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager = getSystemService(NotificationManager::class.java)
        }
        var notificationChannel1: NotificationChannel? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel1 = NotificationChannel(
                CHANNEL_ID2, "Speed of battery", NotificationManager.IMPORTANCE_NONE
            )
            notificationChannel1.setSound(null, null)
        }
        var notificationChannel2: NotificationChannel? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel2 = NotificationChannel(
                CHANNEL_ID3, "Full charge notification", NotificationManager.IMPORTANCE_DEFAULT
            )
        }
        var notificationChannel3: NotificationChannel? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel3 = NotificationChannel(
                CHANNEL_ID4, "Low charge notification", NotificationManager.IMPORTANCE_DEFAULT
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel!!.description = "Show information about battery"
            notificationChannel1!!.description = "Speed of charging or discharging"
            notificationChannel2!!.sound
            notificationChannel2.importance = NotificationManager.IMPORTANCE_HIGH
            notificationChannel2.description = "Notifies when charge is full"
            notificationChannel3!!.description = "Notifies when charge is low"
            manager!!.createNotificationChannel(notificationChannel)
            manager.createNotificationChannel(notificationChannel1)
            manager.createNotificationChannel(notificationChannel2)
            manager.createNotificationChannel(notificationChannel3)
        }
    }

    fun startingBatteryLevelReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
        Thread {
            while (true) {
                val batteryLevelReceiver = BatteryLevelReceiver()
                Objects.requireNonNull(applicationContext)
                    .registerReceiver(batteryLevelReceiver, intentFilter)
                try {
                    Thread.sleep(10000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    companion object {
        const val CHANNEL_ID1 = "1"
        const val CHANNEL_ID2 = "2"
        const val CHANNEL_ID3 = "3"
        const val CHANNEL_ID4 = "4"
        const val low_battery_notification_state = "low_battery_notification_state"
        const val high_battery_notification_state = "high_battery_notification_state"
        const val current_speed_state = "current_speed"
        const val permanent_notification_state = "permanent_notification"
        const val high_voltage_notification_state = "high_voltage_notification_state"
        const val high_temperature_notification_state = "high_temperature_notification_state"
        var alreadyFullDisplayed = false
        var alreadyLowDisplayed = false
        var alreadyTemperatureDisplayed = false
        var alreadyVoltageDisplayed = false
    }
}
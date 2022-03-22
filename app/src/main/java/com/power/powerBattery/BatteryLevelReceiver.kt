package com.power.powerBattery

import android.os.BatteryManager
import android.app.PendingIntent
import android.content.SharedPreferences
import com.power.powerBattery.BatteryNotification
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import com.power.powerBattery.MainActivity
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import com.power.powerBattery.R
import com.power.powerBattery.App
import androidx.core.app.NotificationManagerCompat
import com.power.powerBattery.dp.Add_history_to_db
import java.util.*

class BatteryLevelReceiver : BroadcastReceiver() {
    var batteryManager: BatteryManager? = null
    var pendingIntent: PendingIntent? = null

    private lateinit var sharedPrefs: SharedPreferences

    var batteryNotification: BatteryNotification? = null

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        sharedPrefs = context.getSharedPreferences("com.power.powerBattery", Context.MODE_PRIVATE)
        batteryNotification = BatteryNotification()
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
        pendingIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java), 0
        )
        val level = intent.getIntExtra("level", 0)
        val health_constant = intent.getIntExtra("health", 0)
        val temperature = intent.getIntExtra("temperature", 0).toFloat()
        val chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB
        val acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC
        val wireless = chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS
        var battery_status = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            battery_status = batteryManager!!.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
        }
        val tech = intent.extras?.getString(BatteryManager.EXTRA_TECHNOLOGY)
        var voltage = intent.getIntExtra("voltage", 0).toString()
        if (voltage.length >= 3) voltage =
            voltage.substring(0, voltage.length - 3) + "." + voltage.substring(voltage.length - 3)
        val i = Intent("BatteryLevelReceiver")
        i.putExtra("level", level)
        i.putExtra("temperature", temperature)
        i.putExtra("voltage", voltage)
        i.putExtra("tech", tech)
        i.putExtra("charging", isCharging)
        if (battery_status == 2) {
            i.putExtra("status", "Charging")
            i.putExtra("chargingColor", R.color.design_default_color_primary_dark)
        } else if (battery_status == 3) {
            i.putExtra("status", "Discharging")
            i.putExtra("chargingColor", Color.DKGRAY)
        } else if (battery_status == 5) {
            i.putExtra("status", "Battery full")
            i.putExtra("chargingColor", Color.parseColor("#38853B"))
        } else if (battery_status == 4) {
            i.putExtra("status", "Battery not charging")
        } else if (battery_status == 1) {
            i.putExtra("status", "Statistics unknown")
        }
        if (usbCharge) {
            i.putExtra("plugged_source", "USB ")
        } else if (acCharge) {
            i.putExtra("plugged_source", "AC source")
        } else if (wireless) {
            i.putExtra("plugged_source", "Wirelessly")
        } else {
            i.putExtra("plugged_source", "On battery")
        }
        when (health_constant) {
            7 -> {
                i.putExtra("health", "Cold")
                i.putExtra("health_color", Color.parseColor("#19f79e"))
            }
            4 -> {
                i.putExtra("health", "Dead")
                i.putExtra("health_color", Color.parseColor("#403f3e"))
            }
            2 -> {
                i.putExtra("health", "Good")
                i.putExtra("health_color", Color.parseColor("#016308"))
            }
            3 -> {
                i.putExtra("health", "Heated")
                i.putExtra("health_color", Color.parseColor("#f5390a"))
            }
            5 -> {
                i.putExtra("health", "High voltage")
                i.putExtra("health_color", Color.parseColor("#f5390a"))
            }
            1 -> {
                i.putExtra("health", "Unknown")
                i.putExtra("health_color", Color.parseColor("#000"))
            }
        }
        val rate = rate_now()
        if (isCharging && rate < 0) {
            if (level != 100) i.putExtra(
                "remaining_time",
                "<b>Charging very  slowly</b>"
            ) else i.putExtra("remaining_time", "<b>Charged </b>")
        } else if (isCharging) {
            var remaining_percent = (100 - level).toDouble()
            if (remaining_percent != 0.0) {
                remaining_percent *= 10000.0
                remaining_percent /= ((rate / 1000).toFloat() * 5).toDouble()
                val hrs = remaining_percent.toInt() / 60
                val min = (remaining_percent - hrs * 60).toInt()
                var result = if (hrs > 0 && hrs < 48) hrs.toString() + "h " else ""
                val minutes = "" + (remaining_percent.toInt() - hrs * 60) + "m"
                if (minutes == "0" && hrs == 0) result += "<b>Few sec left</b>" else if (hrs < 48) result += minutes
                if (result.length > 0) i.putExtra(
                    "remaining_time",
                    "<b>Charging Time Left</b><br></br>$result"
                ) else i.putExtra("remaining_time", "<b>Charging Time Left</b><br></br>24hrs")
            } else {
                i.putExtra("remaining_time", "<b>Battery Full</b>")
            }
        } else {
            var remaining_percent = level.toDouble()
            remaining_percent *= 10000.0
            remaining_percent /= (Math.abs(rate.toFloat() / 1000) * 5).toDouble()
            val hrs = remaining_percent.toInt() / 60
            val min = (remaining_percent - hrs * 60).toInt()
            var result = if (hrs > 0 && hrs < 100) hrs.toString() + "h " else ""
            val minutes = "" + (remaining_percent.toInt() - hrs * 60) + "m"
            result += if (minutes == "0" && hrs == 0) "<b>Few sec left</b>" else minutes
            i.putExtra("remaining_time", "<b>Charge Lasts For</b><br></br>$result")
        }
        if (level == 100) {
            i.putExtra("remaining_time", "<b>Battery full</b>")
        }
        i.putExtra("rate", rate / 1000)
        context.sendBroadcast(i)
        val notification = batteryNotification!!.sendNotification(
            context,
            level,
            i,
            sharedPrefs,
            pendingIntent,
            temperature
        )
        if (notification != null && sharedPrefs.getBoolean(
                App.permanent_notification_state,
                false
            )
        ) {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(1, notification)
        }
        if (level == 100) {
            if (!App.alreadyFullDisplayed && sharedPrefs.getBoolean(
                    App.high_battery_notification_state,
                    false
                )
            ) {
                val pendingIntent = PendingIntent.getActivity(
                    context, 0,
                    Intent(context, MainActivity::class.java), 0
                )
                batteryNotification!!.sendAlertNotification(
                    "Battery Alert",
                    "Battery charged fully ",
                    App.CHANNEL_ID3,
                    level,
                    context,
                    pendingIntent
                )
                App.alreadyFullDisplayed = true
            }
        } else {
            App.alreadyFullDisplayed = false
        }
        if (sharedPrefs.getBoolean(
                App.high_battery_notification_state,
                false
            ) && !App.alreadyVoltageDisplayed && health_constant == 5
        ) {
            batteryNotification!!.sendAlertNotification(
                "Voltage Alert",
                "High voltage detected",
                App.CHANNEL_ID3,
                level,
                context,
                pendingIntent
            )
            App.alreadyVoltageDisplayed = true
        } else if (health_constant != 5) {
            App.alreadyVoltageDisplayed = false
        }
        if (level < 20 && !App.alreadyLowDisplayed && sharedPrefs.getBoolean(
                App.low_battery_notification_state,
                false
            )
        ) {
            batteryNotification!!.sendAlertNotification(
                "Battery alert",
                "Low battery $level%",
                App.CHANNEL_ID4,
                level,
                context,
                pendingIntent
            )
            App.alreadyLowDisplayed = true
        } else if (level > 20) {
            App.alreadyLowDisplayed = false
        }
        if (temperature > 460 && !App.alreadyTemperatureDisplayed && sharedPrefs.getBoolean(
                App.high_temperature_notification_state,
                false
            )
        ) {
            batteryNotification!!.sendAlertNotification(
                "Temperature alert",
                "High temperature detected " + temperature / 10.0 + "ºC",
                App.CHANNEL_ID1,
                level,
                context,
                pendingIntent
            )
            App.alreadyTemperatureDisplayed = true
        } else if (temperature < 320) {
            App.alreadyTemperatureDisplayed = false
        }
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()
        val state = sharedPreferences.getString("charging_state", "NEW")!!
        if (state == "NEW") {
            val start_time = Calendar.getInstance().time.time
            if (isCharging) editor.putString(
                "charging_state",
                "charging"
            ) else editor.putString("charging_state", "discharging")
            editor.putLong("startingTime", start_time)
            editor.putInt("initialLevel", level)
            editor.apply()
        } else {
            val add_history_to_db = Add_history_to_db()
            add_history_to_db.addHistoryToDB(
                level,
                sharedPreferences,
                editor,
                state,
                isCharging,
                context
            )
        }
    }

    private fun rate_now(): Long {
        return batteryManager!!.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) * -1
    }
}
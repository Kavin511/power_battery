package com.power.powerBattery

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import com.power.powerBattery.dp.BatteryHistoryAdapter
import com.power.powerBattery.dp.BatteryHistoryViewModel
import me.itangqi.waveloadingview.WaveLoadingView

class home : Fragment() {
    var low_battery_notification: SwitchCompat? = null
    var low_battery_alarm: SwitchCompat? = null
    var high_battery_alarm: SwitchCompat? = null
    var high_battery_notification: SwitchCompat? = null
    var waveLoadingView: WaveLoadingView? = null
    var temperature: TextView? = null
    var current: TextView? = null
    var health: TextView? = null
    var remaining_time: TextView? = null
    var plugged: TextView? = null
    var voltage: TextView? = null
    var total_capacity: TextView? = null
    var technology: TextView? = null
    var available_charge: TextView? = null
    var batteryManager: BatteryManager? = null
    var batteryCapacity = 0.0
    var batteryHistoryViewModel: BatteryHistoryViewModel? = null
    var k = 0
    var adapter: BatteryHistoryAdapter? = null

    @SuppressLint("PrivateApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireContext().registerReceiver(broadcastReceiver, IntentFilter("BatteryLevelReceiver"))
        val v = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) inflater.inflate(
            R.layout.fragment_home,
            container,
            false
        ) else inflater.inflate(R.layout.home_below_oreo, container, false)
        initialise()
        initialiseViews(v)
        setBatteryCapacity()
        MobileAds.initialize(requireContext()) { initializationStatus: InitializationStatus? -> }
        return v
    }

    private fun loadAd(v: View) {
//        mAdView = v.findViewById(R.id.adView)
//        mAdView.loadAd(AdRequest.Builder().build())
    }

    private fun setBatteryCapacity() {
        val POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile"
        try {
            val mPowerProfile = Class.forName(POWER_PROFILE_CLASS)
                .getConstructor(Context::class.java)
                .newInstance(context)
            batteryCapacity = Class
                .forName(POWER_PROFILE_CLASS)
                .getMethod("getBatteryCapacity")
                .invoke(mPowerProfile) as Double
        } catch (e: Exception) {
            e.printStackTrace()
        }
        var total_charge = "" + batteryCapacity.toInt()
        total_charge += "<em> mAh</em>"
        total_capacity!!.text = Html.fromHtml(total_charge)
    }

    private fun initialise() {
        adapter = BatteryHistoryAdapter(context)
        batteryManager =
            requireActivity().getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        batteryHistoryViewModel = ViewModelProvider(this).get(
            BatteryHistoryViewModel::class.java
        )
    }

    private fun initialiseViews(v: View) {
        high_battery_notification = v.findViewById(R.id.battery_full_charge_notification)
        temperature = v.findViewById(R.id.temperature)
        current = v.findViewById(R.id.current)
        health = v.findViewById(R.id.health)
        remaining_time = v.findViewById(R.id.remaining_time)
        waveLoadingView = v.findViewById(R.id.waveLoadingView)
        plugged = v.findViewById(R.id.plugged)
        voltage = v.findViewById(R.id.voltage)
        total_capacity = v.findViewById(R.id.total_capacity)
        technology = v.findViewById(R.id.technology)
        available_charge = v.findViewById(R.id.available_capacity)
    }

    val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val b = intent.extras!!
            val rate = b.getLong("rate")
            val level = b.getInt("level")
            val voltage_str = b.getString("voltage")
            val tech = b.getString("tech")
            val charging = b.getBoolean("charging")
            //            String status = b.getString("status");
            val health_color = b.getInt("health_color")
            val health_str = "<b>Health</b>" + "<br>" + b.getString("health") + "</br>"
            health!!.text = Html.fromHtml(health_str)
            health!!.setTextColor(health_color)
            setTextViewDrawableColor(health!!, health_color)
            waveLoadingView!!.progressValue = level
            val sourceString =
                "<b>" + "Battery " + (if (rate > 0) "charging" else "discharging") + " at " + "</b><br>" + rate + " mA"
            current!!.text = Html.fromHtml(sourceString)
            if (rate < 0) {
                setTextViewDrawableColor(current!!, Color.parseColor("#ff6347"))
            } else {
                setTextViewDrawableColor(current!!, Color.parseColor("#388538"))
            }
            if (charging) {
                waveLoadingView!!.startAnimation()
            } else {
                waveLoadingView!!.pauseAnimation()
            }
            waveLoadingView!!.centerTitle = "$level%"
            plugged!!.text = b.getString("plugged_source")
            voltage!!.text = String.format("%s V", voltage_str)
            technology!!.text = Build.DEVICE
            technology!!.text = tech
            val available_c = level * batteryCapacity.toInt() / 100
            val charge_text = "<b>Battery<br></br></b>$available_c <em> mAh</em>"
            available_charge!!.text = Html.fromHtml(charge_text)
            val remaining_time_val = b.getString("remaining_time", "<b>24h left</b>")
            remaining_time!!.text = Html.fromHtml(remaining_time_val)
            val temperature_value = b.getFloat("temperature") / 10
            val temperature_text = "<b>Temperature</b><br></br>$temperature_value°C"
            temperature!!.text = Html.fromHtml(temperature_text)
            if (temperature_value < 30) {
                setTextViewDrawableColor(temperature!!, Color.parseColor("#75aec7"))
            } else if (temperature_value > 30 && temperature_value < 42) {
                setTextViewDrawableColor(temperature!!, Color.parseColor("#F89451"))
            } else {
                setTextViewDrawableColor(temperature!!, Color.parseColor("#D84315"))
            }
            k = 1
        }
    }

    private fun setTextViewDrawableColor(textView: TextView, color: Int) {
        for (drawable in textView.compoundDrawables) {
            drawable?.setTint(color)
        }
        textView.setTextColor(color)
    }
}
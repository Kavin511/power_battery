package com.power.powerBattery

import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import me.itangqi.waveloadingview.WaveLoadingView
import android.widget.TextView
import android.os.BatteryManager
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.power.powerBattery.BatteryLevelReceiver
import com.power.powerBattery.AuthPagerAdapter
import com.power.powerBattery.Battery_service
import android.content.SharedPreferences
import com.google.android.gms.ads.rewarded.RewardedAd
import android.os.Bundle
import com.power.powerBattery.R
import com.power.powerBattery.App
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import android.content.IntentFilter
import android.content.Intent
import androidx.core.content.ContextCompat
import com.power.powerBattery.home
import com.power.powerBattery.settings
import com.power.powerBattery.stats
import com.google.android.gms.ads.rewarded.RewardItem
import android.widget.Toast
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import android.net.ConnectivityManager
import android.view.MenuItem
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.gms.ads.*
import java.lang.Exception
import java.util.*

class MainActivity : AppCompatActivity() {
    var menuItem: MenuItem? = null
    var viewPager: ViewPager? = null
    var bottomNavigationView: BottomNavigationView? = null
    var waveLoadingView: WaveLoadingView? = null
    var temperature: TextView? = null
    var current: TextView? = null
    var health: TextView? = null
    var remaining_time: TextView? = null
    var batteryManager: BatteryManager? = null
    var builder: NotificationCompat.Builder? = null
    var rewards: ExtendedFloatingActionButton? = null
    var cnt = 0
    var batteryLevelReceiver = BatteryLevelReceiver()
    val adapter = AuthPagerAdapter(supportFragmentManager)
    private val battery_service = Battery_service()
    var sharedPrefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var home: Fragment? = null
    var settings: Fragment? = null
    var stats: Fragment? = null
    var adRequest: AdRequest? = null
    var rewardedAdResponse: RewardedAd? = null
    private var isLookingForReward = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initialise()
        initialiseAdapter()
        loadRewardedAd()
        initialiseClick()
        intialiseSharedPrefs()
        addDefaultRewardToWallet()
        if (sharedPrefs!!.getBoolean(App.permanent_notification_state, false)) {
            startPermanentNotification()
        } else {
            startBatteryBroadCast()
        }
        initialiseMenu()
        initialiseBottomNavigationListener()
        initialisePageSwipe()
    }

    private fun initialiseMenu() {
        bottomNavigationView!!.inflateMenu(R.menu.navigation_ment)
    }

    private fun initialisePageSwipe() {
        viewPager!!.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                bottomNavigationView!!.menu.getItem(position).isChecked = true
                menuItem = bottomNavigationView!!.menu.getItem(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun initialiseBottomNavigationListener() {
        bottomNavigationView!!.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.action_home     -> viewPager!!.currentItem = 0
                R.id.action_stats    -> viewPager!!.currentItem = 1
                R.id.action_settings -> viewPager!!.currentItem = 2
            }
            true
        }
    }

    private fun startBatteryBroadCast() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
        Thread {
            while (true) {
                val batteryLevelReceiver = BatteryLevelReceiver()
                Objects.requireNonNull(applicationContext)
                    .registerReceiver(batteryLevelReceiver, intentFilter)
                try {
                    Thread.sleep(7000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    private fun startPermanentNotification() {
        val serviceIntent = Intent(applicationContext, Battery_service::class.java)
        ContextCompat.startForegroundService(applicationContext, serviceIntent)
        startBatteryBroadCast()
    }

    private fun addDefaultRewardToWallet() {
        if (!sharedPrefs!!.contains("rewards")) {
            addReward(1000)
        }
    }

    private fun intialiseSharedPrefs() {
        sharedPrefs =
            applicationContext.getSharedPreferences("com.power.powerBattery", MODE_PRIVATE)
        editor =
            applicationContext.getSharedPreferences("com.power.powerBattery", MODE_PRIVATE).edit()
    }

    private fun initialiseAdapter() {
        adapter.addFragment(home)
        adapter.addFragment(stats)
        adapter.addFragment(settings)
        viewPager!!.adapter = adapter
    }

    private fun initialise() {
        home = home()
        settings = settings()
        stats = stats()
        viewPager = findViewById(R.id.viewPager)
        bottomNavigationView = findViewById(R.id.bottom_nav_bar)
        rewards = findViewById(R.id.rewards)
    }

    private fun initialiseClick() {
        rewards!!.setOnClickListener { view: View? ->
            if (rewardedAdResponse != null) {
                rewardedAdResponse!!.show(this@MainActivity) { rewardItem: RewardItem? ->
                    addReward(
                        100
                    )
                }
                isLookingForReward = false
                rewardedAdCallBack()
            } else if (isInternetNotConnected) {
                isLookingForReward = true
                loadRewardedAd()
                Toast.makeText(
                    applicationContext,
                    getString(R.string.check_network),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                isLookingForReward = true
                loadRewardedAd()
                Toast.makeText(
                    applicationContext,
                    getString(R.string.no_ads_available),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun rewardedAdCallBack() {
        rewardedAdResponse!!.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedAdResponse = null
                Toast.makeText(applicationContext, "Ad failed to show!", Toast.LENGTH_SHORT).show()
            }

            override fun onAdShowedFullScreenContent() {
                rewardedAdResponse = null
            }

            override fun onAdDismissedFullScreenContent() {
                rewardedAdResponse = null
            }

            override fun onAdImpression() {
                rewardedAdResponse = null
            }

            override fun onAdClicked() {
                rewardedAdResponse = null
            }
        }
        loadRewardedAd()
    }

    private fun loadRewardedAd() {
        MobileAds.initialize(this) { initializationStatus: InitializationStatus? -> }
        adRequest = AdRequest.Builder().build()
        val rewardedAdLoadCallback: RewardedAdLoadCallback = object : RewardedAdLoadCallback() {
            override fun onAdLoaded(rewardedAd: RewardedAd) {
                super.onAdLoaded(rewardedAd)
                rewardedAdResponse = rewardedAd
                if (isLookingForReward) {
                    rewardedAdResponse!!.show(this@MainActivity) { rewardItem: RewardItem? ->
                        addReward(
                            100
                        )
                    }
                    isLookingForReward = false
                    loadRewardedAd()
                    rewardedAdResponse = null
                }
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                rewardedAdResponse = null
                if (isLookingForReward && !isInternetNotConnected) {
                    addReward(50)
                    Toast.makeText(
                        applicationContext,
                        loadAdError.message + ". So, 50 coins added to wallet! ",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        if (rewardedAdResponse == null) {
            RewardedAd.load(
                this,
                getString(R.string.REWARDED_AD_ID),
                adRequest,
                rewardedAdLoadCallback
            )
        }
    }

    private val isInternetNotConnected: Boolean
        private get() {
            val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            return !(connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!
                .isConnected)
        }

    private fun addReward(amount: Long) {
        editor!!.putLong("rewards", sharedPrefs!!.getLong("rewards", 0) + amount)
        editor!!.apply()
        editor!!.commit()
        try {
            com.power.powerBattery.settings.rewardListener.rewardChange()
        } catch (ignored: Exception) {
        }
    }

    companion object {
        const val NEW_WORD_ACTIVITY_REQUEST_CODE = 1
    }
}

class AuthPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    private val Authpager = ArrayList<Fragment>()
    override fun getItem(i: Int): Fragment {
        return Authpager[i]
    }

    override fun getCount(): Int {
        return Authpager.size
    }

    fun addFragment(fragment: Fragment?) {
        Authpager.add(fragment!!)
    }
}
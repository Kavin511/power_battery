package com.power.powerBattery;

import static com.power.powerBattery.App.permanent_notification_state;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Objects;

import me.itangqi.waveloadingview.WaveLoadingView;


public class MainActivity extends AppCompatActivity {
    SwitchCompat low_battery_notification, low_battery_alarm, high_battery_alarm, high_battery_notification;
    @NonNull
    Boolean battery_high_notification = false, battery_low_notification = false, battery_high_alarm = false, battery_low_alarm = false;
    MenuItem menuItem;
    ViewPager viewPager;
    BottomNavigationView bottomNavigationView;
    WaveLoadingView waveLoadingView;
    TextView temperature, current, health, remaining_time;
    BatteryManager batteryManager;
    NotificationCompat.Builder builder;
    ExtendedFloatingActionButton rewards;
    int cnt = 0;
    public static final int NEW_WORD_ACTIVITY_REQUEST_CODE = 1;
    @NonNull
    BatteryLevelReceiver batteryLevelReceiver = new BatteryLevelReceiver();
    final AuthPagerAdapter adapter = new AuthPagerAdapter(getSupportFragmentManager());
    @NonNull
    private final Battery_service battery_service = new Battery_service();
    SharedPreferences sharedPrefs;
    private SharedPreferences.Editor editor;
    Fragment home;
    Fragment settings;
    Fragment stats;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        home = new home();
        settings = new settings();
        stats = new stats();
        viewPager = findViewById(R.id.viewPager);
        bottomNavigationView = findViewById(R.id.bottom_nav_bar);
        rewards = findViewById(R.id.rewards);
        adapter.addFragment(home);
        adapter.addFragment(stats);
        adapter.addFragment(settings);
        viewPager.setAdapter(adapter);
        rewardsInitialize();
        sharedPrefs = getApplicationContext().getSharedPreferences("com.power.powerBattery", MODE_PRIVATE);
        editor = getApplicationContext().getSharedPreferences("com.power.powerBattery", MODE_PRIVATE).edit();
        if (!sharedPrefs.contains("rewards")) {
            addReward(1000L);
        }
        if (sharedPrefs.getBoolean(permanent_notification_state, false)) {
            Intent serviceIntent = new Intent(getApplicationContext(), Battery_service.class);
            ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
            new Thread(() -> {
                while (true) {
                    BatteryLevelReceiver batteryLevelReceiver = new BatteryLevelReceiver();
                    Objects.requireNonNull(getApplicationContext()).registerReceiver(batteryLevelReceiver, intentFilter);
                    try {
                        Thread.sleep(7000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
            new Thread(() -> {
                while (true) {
                    BatteryLevelReceiver batteryLevelReceiver = new BatteryLevelReceiver();
                    Objects.requireNonNull(getApplicationContext()).registerReceiver(batteryLevelReceiver, intentFilter);
                    try {
                        Thread.sleep(7000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }


        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_home:
                    viewPager.setCurrentItem(0);
                    break;
                case R.id.action_stats:
                    viewPager.setCurrentItem(1);
                    break;
                case R.id.action_settings:
                    viewPager.setCurrentItem(2);
                    break;
            }

            return true;
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                menuItem = bottomNavigationView.getMenu().getItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void rewardsInitialize() {
        rewards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Loading Ad", Snackbar.LENGTH_LONG).show();
                RewardedAd rewardedAd = new RewardedAd(getApplicationContext(),
                        "ca-app-pub-3798430149757150/6529752356");
                RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
                    @Override
                    public void onRewardedAdLoaded() {
                        Activity activityContext = MainActivity.this;
                        RewardedAdCallback adCallback = new RewardedAdCallback() {
                            @Override
                            public void onRewardedAdOpened() {
                            }

                            @Override
                            public void onRewardedAdClosed() {
                            }

                            @Override
                            public void onUserEarnedReward(@NonNull RewardItem reward) {
                                addReward(100L);
                                Toast.makeText(getApplicationContext(), "100 points earned!!", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onRewardedAdFailedToShow(AdError adError) {
                                Toast.makeText(activityContext, "Failed to load ad. 100 points added for free :)", Toast.LENGTH_SHORT).show();
                                addReward(100L);
                            }
                        };
                        rewardedAd.show(activityContext, adCallback);
                    }

                    @Override
                    public void onRewardedAdFailedToLoad(LoadAdError adError) {
                        // Ad failed to load.
                    }
                };
                rewardedAd.loadAd(new AdRequest.Builder().build(), adLoadCallback);
            }
        });
    }

    private void addReward(Long value) {
        editor.putLong("rewards", sharedPrefs.getLong("rewards", 0) + value);
        editor.apply();
        editor.commit();
        try {
            com.power.powerBattery.settings.rewardListener.rewardChange();
        } catch (Exception e) {
        }
    }
}

class AuthPagerAdapter extends FragmentPagerAdapter {
    private final ArrayList<Fragment> Authpager = new ArrayList<>();

    public AuthPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    public Fragment getItem(int i) {
        return Authpager.get(i);
    }

    public int getCount() {
        return Authpager.size();
    }

    void addFragment(Fragment fragment) {
        Authpager.add(fragment);
    }
}
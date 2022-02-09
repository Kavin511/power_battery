package com.power.powerBattery;

import static com.power.powerBattery.App.permanent_notification_state;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.Objects;

import me.itangqi.waveloadingview.WaveLoadingView;


public class MainActivity extends AppCompatActivity {
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
    Fragment home, settings, stats;
    AdRequest adRequest;
    RewardedAd rewardedAdResponse;
    private boolean isLookingForReward;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialise();
        initialiseAdapter();
        loadRewardedAd();
        initialiseClick();
        intialiseSharedPrefs();
        addDefaultRewardToWallet();
        if (sharedPrefs.getBoolean(permanent_notification_state, false)) {
            startPermanentNotification();
        } else {
            startBatteryBroadCast();
        }
        initialiseMenu();
        initialiseBottomNavigationListener();
        initialisePageSwipe();
    }

    private void initialiseMenu() {
        bottomNavigationView.inflateMenu(R.menu.navigation_ment);
    }

    private void initialisePageSwipe() {
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

    private void initialiseBottomNavigationListener() {
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
    }

    private void startBatteryBroadCast() {
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

    private void startPermanentNotification() {
        Intent serviceIntent = new Intent(getApplicationContext(), Battery_service.class);
        ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
        startBatteryBroadCast();
    }

    private void addDefaultRewardToWallet() {
        if (!sharedPrefs.contains("rewards")) {
            addReward(1000);
        }
    }

    private void intialiseSharedPrefs() {
        sharedPrefs = getApplicationContext().getSharedPreferences("com.power.powerBattery", MODE_PRIVATE);
        editor = getApplicationContext().getSharedPreferences("com.power.powerBattery", MODE_PRIVATE).edit();
    }

    private void initialiseAdapter() {
        adapter.addFragment(home);
        adapter.addFragment(stats);
        adapter.addFragment(settings);
        viewPager.setAdapter(adapter);
    }

    private void initialise() {
        home = new home();
        settings = new settings();
        stats = new stats();
        viewPager = findViewById(R.id.viewPager);
        bottomNavigationView = findViewById(R.id.bottom_nav_bar);
        rewards = findViewById(R.id.rewards);
    }

    private void initialiseClick() {
        rewards.setOnClickListener(view -> {
            if (rewardedAdResponse != null) {
                rewardedAdResponse.show(MainActivity.this, rewardItem -> {
                    addReward(100);
                });
                isLookingForReward = false;
                rewardedAdCallBack();
            } else if (isInternetNotConnected()) {
                isLookingForReward = true;
                loadRewardedAd();
                Toast.makeText(getApplicationContext(), getString(R.string.check_network), Toast.LENGTH_SHORT).show();
            } else {
                isLookingForReward = true;
                loadRewardedAd();
                Toast.makeText(getApplicationContext(), getString(R.string.no_ads_available), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void rewardedAdCallBack() {
        rewardedAdResponse.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                rewardedAdResponse = null;
                Toast.makeText(getApplicationContext(), "Ad failed to show!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdShowedFullScreenContent() {
                rewardedAdResponse = null;
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                rewardedAdResponse = null;

            }

            @Override
            public void onAdImpression() {
                rewardedAdResponse = null;
            }

            @Override
            public void onAdClicked() {
                rewardedAdResponse = null;
            }
        });
        loadRewardedAd();
    }

    private void loadRewardedAd() {
        MobileAds.initialize(this, initializationStatus -> {
        });
        adRequest = new AdRequest.Builder().build();
        RewardedAdLoadCallback rewardedAdLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                super.onAdLoaded(rewardedAd);
                rewardedAdResponse = rewardedAd;

                if (isLookingForReward) {
                    rewardedAdResponse.show(MainActivity.this, rewardItem -> {
                        addReward(100);
                    });
                    isLookingForReward = false;
                    loadRewardedAd();
                    rewardedAdResponse = null;
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                rewardedAdResponse = null;
                if (isLookingForReward && !isInternetNotConnected()) {
                    Toast.makeText(getApplicationContext(), loadAdError.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        };
        if (rewardedAdResponse == null) {
            RewardedAd.load(this,getString(R.string.REWARDED_AD_ID), adRequest, rewardedAdLoadCallback);
        }
    }


    private boolean isInternetNotConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        return !(connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected());
    }

    private void addReward(long amount) {
        editor.putLong("rewards", sharedPrefs.getLong("rewards", 0) + amount);
        editor.apply();
        editor.commit();
        try {
            com.power.powerBattery.settings.rewardListener.rewardChange();
        } catch (Exception ignored) {
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
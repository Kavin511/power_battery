package com.power.powerBattery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.widget.SwitchCompat;

import android.widget.TextView;

import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Objects;

import me.itangqi.waveloadingview.WaveLoadingView;

import static com.power.powerBattery.App.permanent_notification_state;


public class MainActivity extends AppCompatActivity {
    SwitchCompat low_battery_notification, low_battery_alarm, high_battery_alarm,high_battery_notification;
    @NonNull
    Boolean battery_high_notification=false,battery_low_notification=false,battery_high_alarm=false,battery_low_alarm=false;
    MenuItem menuItem;
    ViewPager viewPager;
    BottomNavigationView bottomNavigationView;
    WaveLoadingView waveLoadingView;
    TextView temperature,current,health,remaining_time;
    BatteryManager batteryManager;
    NotificationCompat.Builder builder;
    int cnt=0;
    public static final int NEW_WORD_ACTIVITY_REQUEST_CODE = 1;
    @NonNull
    BatteryLevelReceiver batteryLevelReceiver = new BatteryLevelReceiver();
   final AuthPagerAdapter adapter = new AuthPagerAdapter(getSupportFragmentManager());
    @NonNull
    private final Battery_service battery_service=new Battery_service();
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fragment home=new home();
        Fragment settings=new settings();
        Fragment stats=new stats();
        viewPager=findViewById(R.id.viewPager);
        bottomNavigationView=findViewById(R.id.bottom_nav_bar);
        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences("com.power.powerBattery", MODE_PRIVATE);
        if (sharedPrefs.getBoolean(permanent_notification_state,false))
        {
            Intent serviceIntent=new Intent(getApplicationContext(),Battery_service.class);
            ContextCompat.startForegroundService(getApplicationContext(),serviceIntent);
            final IntentFilter intentFilter=new IntentFilter();
            intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
            new Thread(() -> {
                while (true)
                {
                    BatteryLevelReceiver batteryLevelReceiver=new BatteryLevelReceiver();
                    Objects.requireNonNull(getApplicationContext()).registerReceiver(batteryLevelReceiver,intentFilter);
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        else
        {
            final IntentFilter intentFilter=new IntentFilter();
            intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
            new Thread(() -> {
                while (true)
                {
                    BatteryLevelReceiver batteryLevelReceiver=new BatteryLevelReceiver();
                    Objects.requireNonNull(getApplicationContext()).registerReceiver(batteryLevelReceiver,intentFilter);
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }


        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId())
            {
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
                menuItem=bottomNavigationView.getMenu().getItem(position);
            }
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        adapter.addFragment(home);
        adapter.addFragment(stats);
        adapter.addFragment(settings);
        viewPager.setAdapter(adapter);
    }
}
class AuthPagerAdapter extends FragmentPagerAdapter {
    private final ArrayList<Fragment> Authpager= new ArrayList<>();
    public AuthPagerAdapter(@NonNull FragmentManager fm)
    {
        super(fm);
    }
    @NonNull
    public  Fragment getItem(int i)
    {
        return Authpager.get(i);
    }
    public int getCount()
    {
        return Authpager.size();
    }
    void addFragment(Fragment fragment)
    {
        Authpager.add(fragment);
    }
}
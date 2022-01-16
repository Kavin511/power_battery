package com.power.powerBattery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class splash_screen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        int SPLASH_SCREEN_TIME_OUT = 300;
        new Handler().postDelayed(() -> {
            Intent start=new Intent(splash_screen.this,MainActivity.class);
            startActivity(start);
            finish();
        }, SPLASH_SCREEN_TIME_OUT);
    }
}
package com.shopcart.shopcart;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import gr.net.maroulis.library.EasySplashScreen;

/**
 * Created by John on 22/1/2018.
 */

public class SplashScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EasySplashScreen splashScreen = new EasySplashScreen(SplashScreen.this)
                .withFullScreen()
                .withSplashTimeOut(3000)
                .withBackgroundResource(R.drawable.app_background)
                .withTargetActivity(MainActivity.class)
                .withLogo(R.drawable.logo);

        View view = splashScreen.create();
        setContentView(view);
    }
}


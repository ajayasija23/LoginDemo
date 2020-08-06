package com.asijaandroidsolution.myapplication.activity.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.Nullable;

import com.asijaandroidsolution.myapplication.R;
import com.asijaandroidsolution.myapplication.activity.BaseActivity;
import com.asijaandroidsolution.myapplication.activity.login.LoginActivity;

public class SplashActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
        },2000);
    }
}

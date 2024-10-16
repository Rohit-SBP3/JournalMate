package com.example.journalapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Logo and Text Animation
        ImageView logo = findViewById(R.id.imgLogo);
        TextView appTitle = findViewById(R.id.tvAppTitle);
        ProgressBar progressBar = findViewById(R.id.progressBarSS);

        // Load animation
        Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.fade_in_animation);
        logo.startAnimation(scaleUp);
        appTitle.startAnimation(scaleUp);

        // Navigate to Main Activity after a delay (e.g., 3 seconds)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(intent);
                finish();  // Close splash screen
            }
        }, 3000); // 3 seconds delay
    }



}

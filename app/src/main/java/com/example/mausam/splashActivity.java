package com.example.mausam;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

public class splashActivity extends AppCompatActivity {

    Animation topAnim,downAnim;

    LottieAnimationView logoAmin;
    TextView appName,copyright;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        topAnim = AnimationUtils.loadAnimation(this,R.anim.top_animation);
        downAnim = AnimationUtils.loadAnimation(this,R.anim.down_animation);

        logoAmin = findViewById(R.id.weathericon);
        appName = findViewById(R.id.AppName);
        copyright = findViewById(R.id.copyright);

        logoAmin.setAnimation(downAnim);
        appName.setAnimation(topAnim);

        Thread thread = new Thread(){
            @Override
            public void run() {
                try{
                    sleep(5000);
                }catch (Exception e){
                    e.printStackTrace();
                }
                finally {
                    Intent intent = new Intent(splashActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };thread.start();
    }
}
package com.example.myapplication.Ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class Sqluashscreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sqluashscreen);

        new Handler().postDelayed(() -> {
            SharedPreferences preferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
            boolean isLoggedIn = preferences.getBoolean("is_logged_in", false);

            if (isLoggedIn) {
                startActivity(new Intent(Sqluashscreen.this, HomeScreen.class));
            } else {
                startActivity(new Intent(Sqluashscreen.this, LoginActivity.class));
            }
            finish();
        }, 2000); // 2 seconds delay
    }
}

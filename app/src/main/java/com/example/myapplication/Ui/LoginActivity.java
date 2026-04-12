package com.example.myapplication.Ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Data.AppDatabase;
import com.example.myapplication.Data.User;
import com.example.myapplication.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister, btnLangEn, btnLangAr;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        loadLocale();
        setContentView(R.layout.activity_login);

        db = AppDatabase.getInstance(this);
        initViews();

        btnLogin.setOnClickListener(v -> handleLogin());
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            finish();
        });

        btnLangEn.setOnClickListener(v -> setLocale("en"));
        btnLangAr.setOnClickListener(v -> setLocale("ar"));
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        btnLangEn = findViewById(R.id.btnLangEn);
        btnLangAr = findViewById(R.id.btnLangAr);
    }

    private void setLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        SharedPreferences preferences = getSharedPreferences("Settings", MODE_PRIVATE);
        preferences.edit().putString("My_Lang", langCode).apply();

        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public void loadLocale() {
        SharedPreferences preferences = getSharedPreferences("Settings", MODE_PRIVATE);
        String language = preferences.getString("My_Lang", "ar");
        setInitialLocale(language);
    }

    private void setInitialLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "يرجى ملء جميع الحقول", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            User user = db.userDao().loginUser(email, password);
            if (user != null) {
                SharedPreferences preferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
                preferences.edit().putBoolean("is_logged_in", true).apply();
                preferences.edit().putString("user_email", email).apply();

                // التحقق هل هذه أول مرة يدخل فيها المستخدم (بعد التسجيل أو أول دخول)
                boolean isFirstTime = preferences.getBoolean("is_first_time", true);

                runOnUiThread(() -> {
                    Toast.makeText(this, "تم تسجيل الدخول بنجاح", Toast.LENGTH_SHORT).show();
                    
                    if (isFirstTime) {
                        // إذا كانت أول مرة، نذهب لصفحة التعريف Onboarding
                        // ونغير الحالة لكي لا تظهر مرة أخرى
                        preferences.edit().putBoolean("is_first_time", false).apply();
                        startActivity(new Intent(LoginActivity.this, Onboarding.class));
                    } else {
                        // إذا لم تكن المرة الأولى، نذهب للشاشة الرئيسية مباشرة
                        startActivity(new Intent(LoginActivity.this, HomeScreen.class));
                    }
                    finish();
                });
            } else {
                runOnUiThread(() -> Toast.makeText(this, "البريد أو كلمة المرور غير صحيحة", Toast.LENGTH_SHORT).show());
            }
        });
    }
}

package com.example.myapplication.Ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Data.AppDatabase;
import com.example.myapplication.Data.User;
import com.example.myapplication.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.Executors;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword, etConfirmPassword;
    private Button btnSignup;
    private TextView tvLogin;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        db = AppDatabase.getInstance(this);
        initViews();

        btnSignup.setOnClickListener(v -> handleSignup());
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignup = findViewById(R.id.btnSignup);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void handleSignup() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "يرجى ملء جميع الحقول", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "كلمات المرور غير متطابقة", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            User existingUser = db.userDao().getUserByEmail(email);
            if (existingUser != null) {
                runOnUiThread(() -> Toast.makeText(this, "هذا البريد الإلكتروني مسجل بالفعل", Toast.LENGTH_SHORT).show());
            } else {
                // إنشاء المستخدم الجديد
                User newUser = new User(email, password);
                db.userDao().registerUser(newUser);
                
                // مسح كافة البيانات السابقة لضمان أن يبدأ المستخدم الجديد ببيانات فارغة
                db.myDao().deleteAllTransactions();
                db.myDao().deleteAllBudgets();
                // ملاحظة: لا نمسح الفئات (Categories) لأنها عامة للتطبيق غالباً، 
                // ولكن إذا كانت مخصصة لكل مستخدم يمكن مسحها أيضاً باستخدام db.myDao().deleteAllCategories();

                // تعيين حالة "أول مرة" للمستخدم الجديد لضمان دخوله لصفحة Onboarding
                SharedPreferences preferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
                preferences.edit().putBoolean("is_first_time", true).apply();

                runOnUiThread(() -> {
                    Toast.makeText(this, "تم إنشاء الحساب بنجاح", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                    finish();
                });
            }
        });
    }
}

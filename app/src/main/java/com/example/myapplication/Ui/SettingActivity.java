package com.example.myapplication.Ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.ViewModel.AppViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.Locale;

public class SettingActivity extends AppCompatActivity {

    private TextView tvUserEmail;
    private MaterialSwitch themeSwitch;
    private Spinner languageSpinner, currencySpinner, weekStartSpinner;
    private MaterialButton btnBackupRestore, btnExport, btnReset, btnLogout;
    private AppViewModel viewModel;
    private SharedPreferences prefs;
    private boolean isInitialSelection = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        viewModel = new ViewModelProvider(this).get(AppViewModel.class);
        prefs = getSharedPreferences("Settings", MODE_PRIVATE);

        initViews();
        setupSpinners();
        loadUserSettings();

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                prefs.edit().putBoolean("dark_mode", true).apply();
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                prefs.edit().putBoolean("dark_mode", false).apply();
            }
        });

        btnReset.setOnClickListener(v -> showResetConfirmation());
        btnBackupRestore.setOnClickListener(v -> startActivity(new Intent(this, BackupActivity.class)));
        btnExport.setOnClickListener(v -> startActivity(new Intent(this, ExportActivity.class)));
        
        btnLogout.setOnClickListener(v -> {
            SharedPreferences appPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            appPrefs.edit().putBoolean("is_logged_in", false).apply();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvUserEmail = findViewById(R.id.tvUserEmail);
        themeSwitch = findViewById(R.id.themeSwitch);
        languageSpinner = findViewById(R.id.languageSpinner);
        currencySpinner = findViewById(R.id.currencySpinner);
        weekStartSpinner = findViewById(R.id.weekStartSpinner);
        btnBackupRestore = findViewById(R.id.btnBackupRestore);
        btnExport = findViewById(R.id.btnExport);
        btnReset = findViewById(R.id.btnReset);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupSpinners() {
        String[] languages = {"العربية", "English"};
        ArrayAdapter<String> langAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, languages);
        languageSpinner.setAdapter(langAdapter);

        String currentLang = prefs.getString("My_Lang", "ar");
        if (currentLang.equals("en")) {
            languageSpinner.setSelection(1);
        } else {
            languageSpinner.setSelection(0);
        }

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitialSelection) {
                    isInitialSelection = false;
                    return;
                }
                
                String langCode = (position == 1) ? "en" : "ar";
                String savedLang = prefs.getString("My_Lang", "ar");
                
                if (!langCode.equals(savedLang)) {
                    setLocale(langCode);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        String[] currencies = {"USD ($)", "EUR (€)", "EGP (ج.م)", "SAR (ر.س)"};
        ArrayAdapter<String> currAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, currencies);
        currencySpinner.setAdapter(currAdapter);

        String[] days = {getString(R.string.date_time_label)}; 
        ArrayAdapter<String> weekAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, days);
        weekStartSpinner.setAdapter(weekAdapter);
    }

    private void setLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        prefs.edit().putString("My_Lang", langCode).apply();

        Intent intent = new Intent(this, HomeScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void loadUserSettings() {
        SharedPreferences userPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String email = userPrefs.getString("user_email", "user@example.com");
        tvUserEmail.setText(email);
        
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        themeSwitch.setChecked(isDarkMode);
    }

    private void showResetConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.reset_confirmation_title)
                .setMessage(R.string.reset_confirmation_msg)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.resetAllData();
                    Toast.makeText(this, R.string.delete_success, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}

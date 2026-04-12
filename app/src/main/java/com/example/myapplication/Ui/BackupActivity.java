package com.example.myapplication.Ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.Data.AppDatabase;
import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executors;

public class BackupActivity extends AppCompatActivity {

    private MaterialButton btnBackup, btnRestore;
    private Toolbar toolbar;
    private static final String DATABASE_NAME = "app_database";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        initViews();
        setupToolbar();

        btnBackup.setOnClickListener(v -> createBackup());
        btnRestore.setOnClickListener(v -> restoreBackup());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        btnBackup = findViewById(R.id.btnBackup);
        btnRestore = findViewById(R.id.btnRestore);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private final ActivityResultLauncher<Intent> createBackupLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        performBackup(uri);
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> restoreBackupLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        performRestore(uri);
                    }
                }
            }
    );

    private void createBackup() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/octet-stream");
        intent.putExtra(Intent.EXTRA_TITLE, "MySpending_Backup_" + System.currentTimeMillis() + ".db");
        createBackupLauncher.launch(intent);
    }

    private void restoreBackup() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/octet-stream");
        restoreBackupLauncher.launch(intent);
    }

    private void performBackup(Uri destinationUri) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Close database before backup
                AppDatabase.getInstance(this).getOpenHelper().getWritableDatabase().close();
                
                File dbFile = getDatabasePath(DATABASE_NAME);
                try (InputStream in = new FileInputStream(dbFile);
                     OutputStream out = getContentResolver().openOutputStream(destinationUri)) {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                }
                runOnUiThread(() -> Toast.makeText(this, "تم إنشاء النسخة الاحتياطية بنجاح", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "فشل إنشاء النسخة الاحتياطية: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void performRestore(Uri sourceUri) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Close database before restore
                AppDatabase.getInstance(this).close();
                
                File dbFile = getDatabasePath(DATABASE_NAME);
                try (InputStream in = getContentResolver().openInputStream(sourceUri);
                     OutputStream out = new FileOutputStream(dbFile)) {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                }
                runOnUiThread(() -> {
                    Toast.makeText(this, "تم استعادة البيانات بنجاح. سيتم إعادة تشغيل التطبيق.", Toast.LENGTH_LONG).show();
                    restartApp();
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "فشل استعادة البيانات: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void restartApp() {
        Intent intent = new Intent(this, Sqluashscreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Runtime.getRuntime().exit(0);
    }
}

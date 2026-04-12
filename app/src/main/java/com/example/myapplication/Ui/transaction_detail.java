package com.example.myapplication.Ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.Data.AppDatabase;
import com.example.myapplication.Data.Transaction;
import com.example.myapplication.R;
import com.example.myapplication.ViewModel.AppViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

public class transaction_detail extends AppCompatActivity {

    private TextView tvAmount, tvType, tvCategory, tvDate, tvNote;
    private MaterialButton btnEdit, btnDelete;
    private ImageView ivTypeIcon;
    private MaterialCardView cardIcon;
    private AppViewModel viewModel;
    private int transactionId;
    private Transaction currentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);

        initViews();
        viewModel = new ViewModelProvider(this).get(AppViewModel.class);

        transactionId = getIntent().getIntExtra("TRANSACTION_ID", -1);

        if (transactionId != -1) {
            loadTransactionDetails();
        } else {
            Toast.makeText(this, "خطأ في تحميل البيانات", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
        btnEdit.setOnClickListener(v -> editTransaction());
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Transaction");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvAmount = findViewById(R.id.tvAmount);
        tvType = findViewById(R.id.tvType);
        tvCategory = findViewById(R.id.tvCategory);
        tvDate = findViewById(R.id.tvDate);
        tvNote = findViewById(R.id.tvNote);
        ivTypeIcon = findViewById(R.id.ivTypeIcon);
        cardIcon = findViewById(R.id.cardIcon);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
    }

    private void loadTransactionDetails() {
        Executors.newSingleThreadExecutor().execute(() -> {
            currentTransaction = viewModel.getTransactionById(transactionId);
            if (currentTransaction != null) {
                AppDatabase db = AppDatabase.getInstance(this);
                String categoryName = db.myDao().getCategoryNameById(currentTransaction.getCategoryId());
                
                runOnUiThread(() -> {
                    tvAmount.setText(String.format(Locale.getDefault(), "%.2f $", currentTransaction.getAmount()));
                    boolean isIncome = currentTransaction.getType().equalsIgnoreCase("Income");
                    
                    tvType.setText(isIncome ? "إيداع / دخل" : "سحب / مصروفات");
                    tvCategory.setText(categoryName != null ? categoryName : "عام");
                    tvNote.setText(currentTransaction.getNote().isEmpty() ? "لا توجد ملاحظات إضافية لهذه العملية." : currentTransaction.getNote());

                    if (isIncome) {
                        tvAmount.setTextColor(Color.parseColor("#2E7D32"));
                        cardIcon.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
                        ivTypeIcon.setImageResource(android.R.drawable.arrow_down_float);
                        ivTypeIcon.setColorFilter(Color.parseColor("#2E7D32"));
                    } else {
                        tvAmount.setTextColor(Color.parseColor("#E53E3E"));
                        cardIcon.setCardBackgroundColor(Color.parseColor("#FFF5F5"));
                        ivTypeIcon.setImageResource(android.R.drawable.arrow_up_float);
                        ivTypeIcon.setColorFilter(Color.parseColor("#E53E3E"));
                    }

                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, HH:mm", new Locale("ar"));
                    tvDate.setText(sdf.format(new Date(currentTransaction.getDate())));
                });
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("تأكيد الحذف")
                .setMessage("هل أنت متأكد أنك تريد حذف هذه العملية؟")
                .setPositiveButton("حذف", (dialog, which) -> deleteTransaction())
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void deleteTransaction() {
        if (currentTransaction != null) {
            viewModel.deleteTransaction(currentTransaction);
            Toast.makeText(this, "تم الحذف بنجاح", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void editTransaction() {
        Intent intent = new Intent(this, AddEditTransactionActivity.class);
        intent.putExtra("TRANSACTION_ID", transactionId);
        startActivity(intent);
        finish();
    }
}

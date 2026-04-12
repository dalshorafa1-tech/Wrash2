package com.example.myapplication.Ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.Data.Transaction;
import com.example.myapplication.ViewModel.AppViewModel;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;
import java.util.Locale;

public class Reportsdetails extends AppCompatActivity {

    private TextView tvCategoryName, tvTotalAmount, tvTransactionsLabel, tvTotalAmountLabel;
    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;
    private AppViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportsdetails);

        viewModel = new ViewModelProvider(this).get(AppViewModel.class);
        initViews();
        loadData();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvCategoryName = findViewById(R.id.tvCategoryName);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvTransactionsLabel = findViewById(R.id.tvTransactionsLabel);
        rvTransactions = findViewById(R.id.rvTransactions);

        adapter = new TransactionAdapter();
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(adapter);

        // تزويد الـ Adapter بالفئات لضمان ظهور الأسماء والألوان بشكل صحيح
        viewModel.getAllCategories().observe(this, categories -> {
            if (categories != null) {
                adapter.setCategories(categories);
            }
        });

        adapter.setOnTransactionClickListener(new TransactionAdapter.OnTransactionClickListener() {
            @Override
            public void onTransactionClick(Transaction transaction) {
                Intent intent = new Intent(Reportsdetails.this, transaction_detail.class);
                intent.putExtra("TRANSACTION_ID", transaction.getId());
                startActivity(intent);
            }

            @Override
            public void onEditClick(Transaction transaction) {
                Intent intent = new Intent(Reportsdetails.this, AddEditTransactionActivity.class);
                intent.putExtra("TRANSACTION_ID", transaction.getId());
                startActivity(intent);
            }
        });
    }

    private void loadData() {
        Intent intent = getIntent();
        String categoryName = intent.getStringExtra("CATEGORY_NAME");
        double totalAmount = intent.getDoubleExtra("TOTAL_AMOUNT", 0);
        int day = intent.getIntExtra("DAY", 0);
        int month = intent.getIntExtra("MONTH", 0);
        int year = intent.getIntExtra("YEAR", 0);
        String type = intent.getStringExtra("TYPE");

        // ترجمة اسم "الدخل" و "المصاريف" إذا كانت قادمة من "نظرة عامة"
        if ("Income".equals(categoryName)) {
            tvCategoryName.setText(R.string.income);
        } else if ("Expense".equals(categoryName)) {
            tvCategoryName.setText(R.string.expense);
        } else {
            tvCategoryName.setText(categoryName);
        }

        tvTotalAmount.setText(String.format(Locale.getDefault(), "%.2f $", totalAmount));

        LiveData<List<Transaction>> transactionsLiveData;

        // التحقق من النوع بشكل يدعم اللغتين
        if ("نظرة عامة".equals(type) || "Overview".equals(type)) {
            String transactionType = ("Income".equals(categoryName) || "الدخل".equals(categoryName)) ? "Income" : "Expense";
            transactionsLiveData = viewModel.getTransactionsByTypeAndDay(transactionType, day, month, year);
        } else {
            transactionsLiveData = viewModel.getTransactionsByDay(day, month, year); 
            // ملحوظة: يفضل إضافة دالة getTransactionsByCategoryAndDay في الـ ViewModel لفلترة أدق
        }

        transactionsLiveData.observe(this, transactions -> {
            if (transactions != null) {
                adapter.setTransactions(transactions);
            }
        });
    }
}

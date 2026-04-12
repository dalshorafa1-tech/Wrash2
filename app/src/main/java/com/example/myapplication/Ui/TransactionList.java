package com.example.myapplication.Ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Data.Category;
import com.example.myapplication.Data.Transaction;
import com.example.myapplication.R;
import com.example.myapplication.ViewModel.AppViewModel;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionList extends AppCompatActivity {

    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;
    private View tvEmptyState;
    private EditText etSearch;
    private ChipGroup chipGroup, sortChipGroup;
    
    private AppViewModel viewModel;
    private List<Transaction> allTransactions = new ArrayList<>();
    private Map<Integer, String> categoryMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_list);

        viewModel = new ViewModelProvider(this).get(AppViewModel.class);

        rvTransactions = findViewById(R.id.rvTransactions);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        chipGroup = findViewById(R.id.chipGroup);
        sortChipGroup = findViewById(R.id.sortChipGroup);
        etSearch = findViewById(R.id.etSearch);

        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter();
        rvTransactions.setAdapter(adapter);

        adapter.setOnTransactionClickListener(new TransactionAdapter.OnTransactionClickListener() {
            @Override
            public void onTransactionClick(Transaction transaction) {
                Intent intent = new Intent(TransactionList.this, transaction_detail.class);
                intent.putExtra("TRANSACTION_ID", transaction.getId());
                startActivity(intent);
            }

            @Override
            public void onEditClick(Transaction transaction) {
                Intent intent = new Intent(TransactionList.this, AddEditTransactionActivity.class);
                intent.putExtra("TRANSACTION_ID", transaction.getId());
                startActivity(intent);
            }
        });

        // مراقبة الفئات
        viewModel.getAllCategories().observe(this, categories -> {
            if (categories != null) {
                categoryMap.clear();
                for (Category cat : categories) {
                    categoryMap.put(cat.getId(), cat.getName());
                }
                adapter.setCategories(categories);
                applyFilters();
            }
        });

        // مراقبة المعاملات
        viewModel.getAllTransactions().observe(this, transactions -> {
            if (transactions != null) {
                allTransactions = transactions;
                applyFilters();
            }
        });

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> applyFilters());
        sortChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> applyFilters());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void applyFilters() {
        String searchQuery = etSearch.getText().toString().toLowerCase().trim();
        int checkedId = chipGroup.getCheckedChipId();
        
        List<Transaction> filteredList = new ArrayList<>();

        for (Transaction t : allTransactions) {
            boolean matchesType = true;
            boolean matchesSearch = true;

            if (checkedId == R.id.chipIncome) {
                matchesType = "Income".equalsIgnoreCase(t.getType());
            } else if (checkedId == R.id.chipExpense) {
                matchesType = "Expense".equalsIgnoreCase(t.getType());
            }

            if (!searchQuery.isEmpty()) {
                String note = t.getNote() != null ? t.getNote().toLowerCase() : "";
                String categoryName = categoryMap.get(t.getCategoryId());
                if (categoryName == null) categoryName = "";
                categoryName = categoryName.toLowerCase();

                matchesSearch = note.contains(searchQuery) || categoryName.contains(searchQuery);
            }

            if (matchesType && matchesSearch) {
                filteredList.add(t);
            }
        }

        sortList(filteredList);
        updateUI(filteredList);
    }

    private void sortList(List<Transaction> list) {
        int sortId = sortChipGroup.getCheckedChipId();
        if (sortId == R.id.chipNewest) {
            Collections.sort(list, (t1, t2) -> Long.compare(t2.getDate(), t1.getDate()));
        } else if (sortId == R.id.chipOldest) {
            Collections.sort(list, (t1, t2) -> Long.compare(t1.getDate(), t2.getDate()));
        } else if (sortId == R.id.chipAmountMax) {
            Collections.sort(list, (t1, t2) -> Double.compare(t2.getAmount(), t1.getAmount()));
        } else if (sortId == R.id.chipAmountMin) {
            Collections.sort(list, (t1, t2) -> Double.compare(t1.getAmount(), t2.getAmount()));
        }
    }

    private void updateUI(List<Transaction> list) {
        if (list.isEmpty()) {
            rvTransactions.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            adapter.setTransactions(list);
            rvTransactions.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
        }
    }
}

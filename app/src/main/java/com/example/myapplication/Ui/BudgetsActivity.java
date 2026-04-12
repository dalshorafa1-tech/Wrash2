package com.example.myapplication.Ui;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Data.AppDatabase;
import com.example.myapplication.Data.Budget;
import com.example.myapplication.R;
import com.example.myapplication.ViewModel.AppViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class BudgetsActivity extends AppCompatActivity {

    private ProgressBar pbTotalBudget;
    private TextView tvTotalStatus, tvBudgetText, tvCurrentMonth, tvRemainingBudget, tvBudgetWarning;
    private TextView btnSetBudget;
    private ImageButton btnPrevMonth, btnNextMonth;
    private RecyclerView rvCategoryBudgets;
    private BudgetAdapter adapter;
    
    private Calendar currentCalendar;
    private AppViewModel viewModel;
    private double totalBudgetLimit = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        currentCalendar = Calendar.getInstance();
        viewModel = new ViewModelProvider(this).get(AppViewModel.class);

        initViews();
        setupRecyclerView();
        updateMonthDisplay();
        loadBudgets();
    }

    private void initViews() {
        pbTotalBudget = findViewById(R.id.pbTotalBudget);
        tvTotalStatus = findViewById(R.id.tvTotalStatus);
        tvBudgetText = findViewById(R.id.tvBudgetText);
        tvCurrentMonth = findViewById(R.id.tvCurrentMonth);
        tvRemainingBudget = findViewById(R.id.tvRemainingBudget);
        tvBudgetWarning = findViewById(R.id.tvBudgetWarning);
        btnSetBudget = findViewById(R.id.btnSetBudget);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        rvCategoryBudgets = findViewById(R.id.rvCategoryBudgets);

        btnPrevMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateMonthDisplay();
            loadBudgets();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateMonthDisplay();
            loadBudgets();
        });

        btnSetBudget.setOnClickListener(v -> showBudgetOptionsDialog());
    }

    private void setupRecyclerView() {
        adapter = new BudgetAdapter();
        rvCategoryBudgets.setLayoutManager(new LinearLayoutManager(this));
        rvCategoryBudgets.setAdapter(adapter);
    }

    private void updateMonthDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvCurrentMonth.setText(sdf.format(currentCalendar.getTime()));
    }

    private void showBudgetOptionsDialog() {
        String[] options = {getString(R.string.monthly_total_budget), getString(R.string.category_specific_budget)};
        new AlertDialog.Builder(this)
                .setTitle(R.string.set_budget_option_title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showSetBudgetDialog(0, getString(R.string.monthly_total_budget));
                    } else {
                        showCategorySelectDialog();
                    }
                })
                .show();
    }

    private void showCategorySelectDialog() {
        viewModel.getAllCategories().observe(this, categories -> {
            if (categories == null || categories.isEmpty()) {
                Toast.makeText(this, R.string.add_category, Toast.LENGTH_SHORT).show();
                return;
            }

            String[] catNames = new String[categories.size()];
            for (int i = 0; i < categories.size(); i++) {
                catNames[i] = categories.get(i).getName();
            }

            new AlertDialog.Builder(this)
                    .setTitle(R.string.select_category)
                    .setItems(catNames, (dialog, which) -> {
                        showSetBudgetDialog(categories.get(which).getId(), categories.get(which).getName());
                    })
                    .show();
        });
    }

    private void showSetBudgetDialog(int catId, String title) {
        final EditText etAmount = new EditText(this);
        etAmount.setHint(R.string.enter_amount_hint);
        etAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etAmount.setPadding(60, 40, 60, 40);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(etAmount)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String amountStr = etAmount.getText().toString().trim();
                    if (!amountStr.isEmpty()) {
                        saveBudget(catId, amountStr);
                    } else {
                        Toast.makeText(this, R.string.enter_amount_hint, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void saveBudget(int catId, String amountStr) {
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(this, R.string.invalid_amount_error, Toast.LENGTH_SHORT).show();
                return;
            }

            int month = currentCalendar.get(Calendar.MONTH) + 1;
            int year = currentCalendar.get(Calendar.YEAR);

            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(this);
                Budget generalBudget = db.budgetDao().getBudgetByCategory(0, month, year);
                double currentTotalBudget = (generalBudget != null) ? generalBudget.budgetlimit : 0.0;

                if (catId != 0) {
                    if (currentTotalBudget <= 0) {
                        runOnUiThread(() -> Toast.makeText(this, R.string.set_total_budget_first, Toast.LENGTH_LONG).show());
                        return;
                    }
                    Double totalOtherCategories = db.budgetDao().getTotalCategoryBudgets(month, year);
                    double currentAllocated = (totalOtherCategories != null) ? totalOtherCategories : 0.0;
                    Budget existingBudget = db.budgetDao().getBudgetByCategory(catId, month, year);
                    if (existingBudget != null) currentAllocated -= existingBudget.budgetlimit;

                    if (currentAllocated + amount > currentTotalBudget) {
                        runOnUiThread(() -> Toast.makeText(this, R.string.budget_exceed_total_error, Toast.LENGTH_LONG).show());
                        return;
                    }
                } else {
                    Double totalCategories = db.budgetDao().getTotalCategoryBudgets(month, year);
                    double currentAllocated = (totalCategories != null) ? totalCategories : 0.0;
                    if (amount < currentAllocated) {
                        runOnUiThread(() -> Toast.makeText(this, R.string.total_less_than_categories_error, Toast.LENGTH_LONG).show());
                        return;
                    }
                }

                Budget b = new Budget();
                b.categoryId = catId;
                b.budgetlimit = amount;
                b.month = month;
                b.year = year;
                viewModel.insertOrUpdateBudget(b);
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.add_success, Toast.LENGTH_SHORT).show();
                    loadBudgets();
                });
            });
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.invalid_amount_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadBudgets() {
        Executors.newSingleThreadExecutor().execute(() -> {
            int month = currentCalendar.get(Calendar.MONTH) + 1;
            int year = currentCalendar.get(Calendar.YEAR);

            AppDatabase db = AppDatabase.getInstance(this);
            Budget generalBudget = db.budgetDao().getBudgetByCategory(0, month, year);
            totalBudgetLimit = (generalBudget != null) ? generalBudget.budgetlimit : 0.0;
            
            String monthStr = String.format(Locale.US, "%02d", month);
            Double spending = db.myDao().getTotalExpenseByMonth(monthStr);
            final double currentSpending = (spending != null) ? spending : 0.0;

            List<BudgetSummary> summaries = db.budgetDao().getBudgetsStatus(month, year);

            runOnUiThread(() -> {
                updateUI(currentSpending);
                if (adapter != null) adapter.setBudgetSummaries(summaries);
            });
        });
    }

    private void updateUI(double currentSpending) {
        if (pbTotalBudget == null || tvTotalStatus == null || tvBudgetText == null || tvRemainingBudget == null) return;

        if (totalBudgetLimit <= 0) {
            tvTotalStatus.setText(R.string.not_set_yet);
            tvBudgetText.setText(getString(R.string.spent_so_far, String.format(Locale.getDefault(), "%.2f", currentSpending)));
            pbTotalBudget.setProgress(0);
            tvRemainingBudget.setText(getString(R.string.remaining_label, "--"));
            if (tvBudgetWarning != null) tvBudgetWarning.setVisibility(View.GONE);
            return;
        }

        double progressPercent = (currentSpending / totalBudgetLimit) * 100;
        int progress = (int) progressPercent;
        pbTotalBudget.setProgress(Math.min(progress, 100));
        
        tvTotalStatus.setText(getString(R.string.budget_format, String.format(Locale.getDefault(), "%.2f", totalBudgetLimit)));
        tvBudgetText.setText(getString(R.string.spent_of_format, String.format(Locale.getDefault(), "%.2f", currentSpending), String.format(Locale.getDefault(), "%.2f", totalBudgetLimit)));

        int progressColor;
        if (progressPercent < 70) {
            progressColor = Color.parseColor("#10B981");
        } else if (progressPercent < 90) {
            progressColor = Color.parseColor("#F59E0B");
        } else {
            progressColor = Color.parseColor("#EF4444");
        }
        
        pbTotalBudget.setProgressTintList(ColorStateList.valueOf(progressColor));
        tvRemainingBudget.setTextColor(progressColor);

        double remaining = totalBudgetLimit - currentSpending;
        if (remaining >= 0) {
            tvRemainingBudget.setText(getString(R.string.remaining_label, String.format(Locale.getDefault(), "%.2f", remaining)));
            if (tvBudgetWarning != null) tvBudgetWarning.setVisibility(View.GONE);
        } else {
            tvRemainingBudget.setText(getString(R.string.over_limit_label, String.format(Locale.getDefault(), "%.2f", Math.abs(remaining))));
            if (tvBudgetWarning != null) tvBudgetWarning.setVisibility(View.VISIBLE);
        }
    }
}

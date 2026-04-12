package com.example.myapplication.Ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.Data.Transaction;
import com.example.myapplication.ViewModel.AppViewModel;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeScreen extends AppCompatActivity {
    private TextView tvTotalBalance, tvIncomeAmount, tvExpenseAmount, btnViewAll, tvDetailsAnalysis, tvSelectedDate;
    private FloatingActionButton btnAddTransaction;
    private ImageButton btnSettings;
    private MaterialCardView cardBudgetTip;
    private PieChart homePieChart;
    private RecyclerView rvRecentTransactions;
    private TransactionAdapter transactionAdapter;
    private AppViewModel viewModel;

    private int selectedDay, selectedMonth, selectedYear;
    private boolean isFiltered = false;
    private double overallIncome = 0.0, overallExpenses = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        viewModel = new ViewModelProvider(this).get(AppViewModel.class);

        initViews();
        setupRecyclerView();
        setupPieChartConfig();

        Calendar cal = Calendar.getInstance();
        selectedDay = cal.get(Calendar.DAY_OF_MONTH);
        selectedMonth = cal.get(Calendar.MONTH) + 1;
        selectedYear = cal.get(Calendar.YEAR);

        setupObservers();
        setupListeners();

        loadAllTimeSummary();
    }

    private void initViews() {
        tvTotalBalance = findViewById(R.id.tvTotalBalance);
        tvIncomeAmount = findViewById(R.id.tvIncomeAmount);
        tvExpenseAmount = findViewById(R.id.tvExpenseAmount);
        btnViewAll = findViewById(R.id.btnViewAll);
        tvDetailsAnalysis = findViewById(R.id.tvDetailsAnalysis);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        btnAddTransaction = findViewById(R.id.btnAdd);
        btnSettings = findViewById(R.id.btnSettings);
        homePieChart = findViewById(R.id.homePieChart);
        cardBudgetTip = findViewById(R.id.cardBudgetTip);
        rvRecentTransactions = findViewById(R.id.rvRecentTransactions);
    }

    private void setupListeners() {
        tvSelectedDate.setOnClickListener(v -> showDatePicker());
        cardBudgetTip.setOnClickListener(v -> startActivity(new Intent(this, BudgetsActivity.class)));
        btnAddTransaction.setOnClickListener(v -> startActivity(new Intent(this, AddEditTransactionActivity.class)));
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingActivity.class)));
        tvDetailsAnalysis.setOnClickListener(v -> startActivity(new Intent(this, Reports.class)));
        btnViewAll.setOnClickListener(v -> startActivity(new Intent(this, TransactionList.class)));
    }

    private void setupRecyclerView() {
        rvRecentTransactions.setLayoutManager(new LinearLayoutManager(this));
        transactionAdapter = new TransactionAdapter();
        rvRecentTransactions.setAdapter(transactionAdapter);
        
        transactionAdapter.setOnTransactionClickListener(new TransactionAdapter.OnTransactionClickListener() {
            @Override
            public void onTransactionClick(Transaction transaction) {
                Intent intent = new Intent(HomeScreen.this, transaction_detail.class);
                intent.putExtra("TRANSACTION_ID", transaction.getId());
                startActivity(intent);
            }

            @Override
            public void onEditClick(Transaction transaction) {
                Intent intent = new Intent(HomeScreen.this, AddEditTransactionActivity.class);
                intent.putExtra("TRANSACTION_ID", transaction.getId());
                startActivity(intent);
            }
        });
    }

    private void setupPieChartConfig() {
        homePieChart.setUsePercentValues(true);
        homePieChart.getDescription().setEnabled(false);
        homePieChart.setDrawHoleEnabled(true);
        homePieChart.setHoleColor(Color.WHITE);
        homePieChart.setEntryLabelColor(Color.BLACK);
        homePieChart.setCenterTextSize(12f);
        homePieChart.getLegend().setEnabled(false);
    }

    private void setupObservers() {
        viewModel.getAllCategories().observe(this, categories -> {
            if (categories != null) transactionAdapter.setCategories(categories);
        });

        viewModel.getAllTransactions().observe(this, transactions -> {
            if (transactions != null) transactionAdapter.setTransactions(transactions);
        });

        viewModel.getTotalIncome().observe(this, totalIncome -> {
            overallIncome = (totalIncome != null) ? totalIncome : 0.0;
            updateTotalBalanceDisplay();
            if (!isFiltered) tvIncomeAmount.setText(String.format(Locale.getDefault(), "+%.2f $", overallIncome));
        });

        viewModel.getTotalExpenses().observe(this, totalExpenses -> {
            overallExpenses = (totalExpenses != null) ? totalExpenses : 0.0;
            updateTotalBalanceDisplay();
            if (!isFiltered) tvExpenseAmount.setText(String.format(Locale.getDefault(), "-%.2f $", overallExpenses));
        });
    }

    private void updateTotalBalanceDisplay() {
        tvTotalBalance.setText(String.format(Locale.getDefault(), "%.2f $", overallIncome - overallExpenses));
    }

    private void loadAllTimeSummary() {
        isFiltered = false;
        tvSelectedDate.setText(R.string.period_all);
        viewModel.getAllTimeExpenseDistribution().observe(this, this::updatePieChart);
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedYear = year;
            selectedMonth = month + 1;
            selectedDay = dayOfMonth;
            isFiltered = true;
            tvSelectedDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth, selectedYear));
            updateDashboardData();
        }, selectedYear, selectedMonth - 1, selectedDay).show();
    }

    private void updateDashboardData() {
        viewModel.getIncomeByDay(selectedDay, selectedMonth, selectedYear).observe(this, income -> {
            if (isFiltered) tvIncomeAmount.setText(String.format(Locale.getDefault(), "+%.2f $", (income != null) ? income : 0.0));
        });

        viewModel.getExpensesByDay(selectedDay, selectedMonth, selectedYear).observe(this, expenses -> {
            if (isFiltered) tvExpenseAmount.setText(String.format(Locale.getDefault(), "-%.2f $", (expenses != null) ? expenses : 0.0));
        });

        viewModel.getOverviewReportByDay(selectedDay, selectedMonth, selectedYear).observe(this, this::updatePieChart);
    }

    private void updatePieChart(List<CategoryReport> reportList) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        if (reportList == null || reportList.isEmpty()) {
            homePieChart.clear();
            return;
        }

        for (CategoryReport item : reportList) {
            String name = item.categoryName;
            if ("Income".equals(name)) name = getString(R.string.income);
            if ("Expense".equals(name)) name = getString(R.string.expense);
            entries.add(new PieEntry((float) item.totalAmount, name));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(2f);
        dataSet.setColors(isFiltered ? new int[]{Color.rgb(76, 175, 80), Color.rgb(244, 67, 54)} : ColorTemplate.MATERIAL_COLORS);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(10f);
        homePieChart.setData(data);
        homePieChart.animateY(600);
        homePieChart.invalidate();
    }
}

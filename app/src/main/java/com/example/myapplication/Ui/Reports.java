package com.example.myapplication.Ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Utils.CustomMarkerView;
import com.example.myapplication.R;
import com.example.myapplication.Data.Transaction;
import com.example.myapplication.ViewModel.AppViewModel;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Reports extends AppCompatActivity {

    private PieChart pieChart;
    private TabLayout tabLayout;
    private RecyclerView rvBreakdown;
    private TransactionAdapter transactionAdapter;
    private TextView tvReportsDate;
    private AppViewModel viewModel;
    
    private int selectedDay;
    private int selectedMonth;
    private int selectedYear;
    private LiveData<List<CategoryReport>> currentReportLiveData;
    private LiveData<List<Transaction>> currentTransactionsLiveData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        viewModel = new ViewModelProvider(this).get(AppViewModel.class);
        initViews();
        setupInitialDate();
        setupPieChartConfig();
        setupCategoryObserver();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateReport();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void initViews() {
        pieChart = findViewById(R.id.pieChart);
        tabLayout = findViewById(R.id.tabLayout);
        rvBreakdown = findViewById(R.id.rvBreakdown);
        tvReportsDate = findViewById(R.id.tvReportsDate);

        transactionAdapter = new TransactionAdapter();
        rvBreakdown.setLayoutManager(new LinearLayoutManager(this));
        rvBreakdown.setAdapter(transactionAdapter);

        transactionAdapter.setOnTransactionClickListener(new TransactionAdapter.OnTransactionClickListener() {
            @Override
            public void onTransactionClick(Transaction transaction) {
                Intent intent = new Intent(Reports.this, transaction_detail.class);
                intent.putExtra("TRANSACTION_ID", transaction.getId());
                startActivity(intent);
            }

            @Override
            public void onEditClick(Transaction transaction) {
                Intent intent = new Intent(Reports.this, AddEditTransactionActivity.class);
                intent.putExtra("TRANSACTION_ID", transaction.getId());
                startActivity(intent);
            }
        });

        tvReportsDate.setOnClickListener(v -> showDatePicker());
    }

    private void setupCategoryObserver() {
        viewModel.getAllCategories().observe(this, categories -> {
            if (categories != null) {
                transactionAdapter.setCategories(categories);
            }
        });
    }

    private void setupInitialDate() {
        Calendar cal = Calendar.getInstance();
        selectedDay = cal.get(Calendar.DAY_OF_MONTH);
        selectedMonth = cal.get(Calendar.MONTH) + 1;
        selectedYear = cal.get(Calendar.YEAR);
        updateDateText();
        updateReport();
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedYear = year;
            selectedMonth = month + 1;
            selectedDay = dayOfMonth;
            updateDateText();
            updateReport();
        }, selectedYear, selectedMonth - 1, selectedDay).show();
    }

    private void updateDateText() {
        tvReportsDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth, selectedYear));
    }

    private void setupPieChartConfig() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setCenterTextSize(18f);

        CustomMarkerView marker = new CustomMarkerView(this, R.layout.custom_marker_view);
        marker.setChartView(pieChart);
        pieChart.setMarker(marker);
        
        pieChart.setTouchEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);
        pieChart.setRotationEnabled(true);

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {}
            @Override
            public void onNothingSelected() {}
        });
    }

    private void updateReport() {
        int tabPos = tabLayout.getSelectedTabPosition();
        
        if (currentReportLiveData != null) currentReportLiveData.removeObservers(this);
        if (currentTransactionsLiveData != null) currentTransactionsLiveData.removeObservers(this);

        if (tabPos == 0) {
            currentReportLiveData = viewModel.getOverviewReportByDay(selectedDay, selectedMonth, selectedYear);
            currentTransactionsLiveData = viewModel.getTransactionsByDay(selectedDay, selectedMonth, selectedYear);
            
            currentReportLiveData.observe(this, list -> updatePieChart(list, "نظرة عامة"));
            currentTransactionsLiveData.observe(this, list -> transactionAdapter.setTransactions(list));
            
        } else if (tabPos == 1) {
            currentReportLiveData = viewModel.getReportByDay("Expense", selectedDay, selectedMonth, selectedYear);
            currentTransactionsLiveData = viewModel.getTransactionsByTypeAndDay("Expense", selectedDay, selectedMonth, selectedYear);
            
            currentReportLiveData.observe(this, list -> updatePieChart(list, "المصاريف"));
            currentTransactionsLiveData.observe(this, list -> transactionAdapter.setTransactions(list));
            
        } else {
            currentReportLiveData = viewModel.getReportByDay("Income", selectedDay, selectedMonth, selectedYear);
            currentTransactionsLiveData = viewModel.getTransactionsByTypeAndDay("Income", selectedDay, selectedMonth, selectedYear);
            
            currentReportLiveData.observe(this, list -> updatePieChart(list, "الدخل"));
            currentTransactionsLiveData.observe(this, list -> transactionAdapter.setTransactions(list));
        }
    }

    private void updatePieChart(List<CategoryReport> reportList, String label) {
        if (reportList == null) return;

        ArrayList<PieEntry> entries = new ArrayList<>();
        double total = 0;

        for (CategoryReport item : reportList) {
            String name = item.categoryName;
            if ("Income".equals(name)) name = "الدخل";
            if ("Expense".equals(name)) name = "المصاريف";
            entries.add(new PieEntry((float) item.totalAmount, name));
            total += item.totalAmount;
        }

        pieChart.setCenterText(label + "\n" + String.format(Locale.getDefault(), "%.0f $", total));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(10f); 

        if ("نظرة عامة".equals(label)) {
            dataSet.setColors(new int[]{Color.rgb(76, 175, 80), Color.rgb(244, 67, 54)});
        } else {
            dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        }

        PieData data = new PieData(dataSet);
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.BLACK);

        pieChart.setData(data);
        pieChart.animateY(800);
        pieChart.invalidate();
    }
}

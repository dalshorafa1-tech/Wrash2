package com.example.myapplication.Ui;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.Data.AppDatabase;
import com.example.myapplication.Data.Budget;
import com.example.myapplication.Data.Category;
import com.example.myapplication.R;
import com.example.myapplication.Data.Transaction;
import com.example.myapplication.ViewModel.AppViewModel;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class AddEditTransactionActivity extends AppCompatActivity {

    private EditText etAmount, etNote;
    private Spinner spinnerCategory;
    private Button btnDatePicker, btnSave;
    private MaterialButtonToggleGroup toggleType;
    private Toolbar toolbar;

    private long selectedDateTimestamp = 0;
    private int transactionId = -1;
    private boolean isEditMode = false;

    private AppViewModel viewModel;
    private List<Category> categoryList = new ArrayList<>();
    private int selectedCategoryId = -1;
    private final String CREATE_NEW_CATEGORY = "+ قم بإنشاء تصنيف جديد";
    private final String SELECT_HINT = "اختر التصنيف";
    private static final int PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edittransaction);

        viewModel = new ViewModelProvider(this).get(AppViewModel.class);
        initViews();
        loadCategories();
        checkNotificationPermission();

        if (getIntent().hasExtra("TRANSACTION_ID")) {
            isEditMode = true;
            transactionId = getIntent().getIntExtra("TRANSACTION_ID", -1);
            loadTransactionData(transactionId);
            
            if (toolbar != null) {
                toolbar.setTitle("تعديل معاملة");
            }
            btnSave.setText("تعديل");
        } else {
            if (toolbar != null) {
                toolbar.setTitle("إضافة معاملة جديدة");
            }
            selectedDateTimestamp = System.currentTimeMillis();
            updateDateButtonText();
            btnSave.setText("حفظ");
        }

        btnSave.setOnClickListener(v -> saveTransaction());
        btnDatePicker.setOnClickListener(v -> showDatePicker());
        
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void loadCategories() {
        viewModel.getAllCategories().observe(this, categories -> {
            if (categories != null) {
                categoryList = categories;
                List<String> categoryNames = new ArrayList<>();
                categoryNames.add(SELECT_HINT);
                for (Category category : categories) {
                    categoryNames.add(category.name);
                }
                categoryNames.add(CREATE_NEW_CATEGORY);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, categoryNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(adapter);

                spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedName = categoryNames.get(position);
                        if (selectedName.equals(CREATE_NEW_CATEGORY)) {
                            startActivity(new Intent(AddEditTransactionActivity.this, CategoriesManadment.class));
                            spinnerCategory.setSelection(0);
                        } else if (selectedName.equals(SELECT_HINT)) {
                            selectedCategoryId = -1;
                        } else {
                            selectedCategoryId = categoryList.get(position - 1).id;
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        selectedCategoryId = -1;
                    }
                });

                if (isEditMode && transactionId != -1) {
                    selectCurrentCategoryInSpinner();
                }
            }
        });
    }

    private void selectCurrentCategoryInSpinner() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            Transaction transaction = db.myDao().getTransactionById(transactionId);
            if (transaction != null) {
                runOnUiThread(() -> {
                    for (int i = 0; i < categoryList.size(); i++) {
                        if (categoryList.get(i).id == transaction.getCategoryId()) {
                            spinnerCategory.setSelection(i + 1);
                            selectedCategoryId = transaction.getCategoryId();
                            break;
                        }
                    }
                });
            }
        });
    }

    private void loadTransactionData(int id) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            Transaction transaction = db.myDao().getTransactionById(id);
            if (transaction != null) {
                runOnUiThread(() -> {
                    etAmount.setText(String.valueOf(transaction.getAmount()));
                    etNote.setText(transaction.getNote());
                    selectedDateTimestamp = transaction.getDate();
                    updateDateButtonText();
                    if ("Income".equalsIgnoreCase(transaction.getType())) {
                        toggleType.check(R.id.btnIncome);
                    } else {
                        toggleType.check(R.id.btnExpense);
                    }
                });
            }
        });
    }

    private void saveTransaction() {
        String amountStr = etAmount.getText().toString();
        String type = (toggleType.getCheckedButtonId() == R.id.btnIncome) ? "Income" : "Expense";

        if (amountStr.isEmpty() || Double.parseDouble(amountStr) <= 0) {
            etAmount.setError("يرجى إدخال مبلغ صحيح");
            return;
        }

        if (selectedCategoryId == -1) {
            Toast.makeText(this, "يرجى اختيار فئة", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String note = etNote.getText().toString();
        Transaction transaction = new Transaction(amount, type, selectedCategoryId, selectedDateTimestamp, note, "");

        if (isEditMode) {
            transaction.setId(transactionId);
            viewModel.updateTransaction(transaction);
        } else {
            viewModel.insertTransaction(transaction);
            
            // فحص تجاوز الميزانية عند إضافة مصروف جديد
            if (type.equals("Expense")) {
                checkBudgetLimit(selectedCategoryId, amount, selectedDateTimestamp);
            }
        }
        finish();
    }

    private void checkBudgetLimit(int catId, double newAmount, long timestamp) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(timestamp);
            int month = cal.get(Calendar.MONTH) + 1;
            int year = cal.get(Calendar.YEAR);

            // 1. فحص ميزانية الفئة المحددة
            Budget catBudget = db.budgetDao().getBudgetByCategory(catId, month, year);
            if (catBudget != null) {
                double spent = db.budgetDao().getSpentByCategory(catId, month, year);
                // ملاحظة: spent هنا قد لا تشمل العملية الحالية بعد إذا كانت الـ insert لا تزال تعمل، 
                // لذا نجمع المبلغ الجديد يدوياً للتأكد
                if ((spent + newAmount) > catBudget.budgetlimit) {
                    runOnUiThread(() -> NotificationHelper.showBudgetAlert(this, "تنبيه تجاوز ميزانية الفئة",
                        "لقد تجاوزت الميزانية المحددة لهذه الفئة!"));
                }
            }

            // 2. فحص الميزانية الكلية للشهر
            Budget totalBudget = db.budgetDao().getBudgetByCategory(0, month, year);
            if (totalBudget != null) {
                Double totalSpent = db.budgetDao().getTotalSpent(month, year);
                double currentTotal = (totalSpent != null ? totalSpent : 0.0) + newAmount;
                if (currentTotal > totalBudget.budgetlimit) {
                    runOnUiThread(() -> NotificationHelper.showBudgetAlert(this, "تنبيه الميزانية الكلية", 
                        "لقد تجاوزت إجمالي الميزانية المحددة لهذا الشهر!"));
                }
            }
        });
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        if (selectedDateTimestamp != 0) cal.setTimeInMillis(selectedDateTimestamp);
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.set(year, month, dayOfMonth);
            selectedDateTimestamp = selectedCal.getTimeInMillis();
            updateDateButtonText();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateButtonText() {
        String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(selectedDateTimestamp));
        btnDatePicker.setText("التاريخ: " + dateStr);
    }

    private void initViews() {
        etAmount = findViewById(R.id.etAmount);
        etNote = findViewById(R.id.etNote);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnDatePicker = findViewById(R.id.btnDatePicker);
        btnSave = findViewById(R.id.btnSave);
        toggleType = findViewById(R.id.toggleGroupType);
        toolbar = findViewById(R.id.toolbar);
    }
}

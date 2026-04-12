package com.example.myapplication.Ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Data.AppDatabase;
import com.example.myapplication.Data.Category;
import com.example.myapplication.R;
import com.example.myapplication.ViewModel.AppViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.concurrent.Executors;

public class CategoriesManadment extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener {

    private RecyclerView rvCategories;
    private CategoryAdapter adapter;
    private AppViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        viewModel = new ViewModelProvider(this).get(AppViewModel.class);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvCategories = findViewById(R.id.rvCategories);
        rvCategories.setLayoutManager(new GridLayoutManager(this, 2));
        
        adapter = new CategoryAdapter(new ArrayList<>(), this);
        rvCategories.setAdapter(adapter);

        setupObservers();

        ExtendedFloatingActionButton fabAdd = findViewById(R.id.fabAddCategory);
        fabAdd.setOnClickListener(v -> showAddCategoryDialog());
    }

    private void setupObservers() {
        viewModel.getAllCategories().observe(this, categories -> {
            if (categories != null) {
                adapter.setCategories(categories);
            }
        });
    }

    private void showAddCategoryDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        EditText etName = view.findViewById(R.id.etCategoryName);

        new AlertDialog.Builder(this)
                .setTitle("إضافة تصنيف جديد")
                .setView(view)
                .setPositiveButton("إضافة", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (!name.isEmpty()) {
                        addNewCategory(name);
                    } else {
                        Toast.makeText(this, "يرجى إدخال اسم", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void addNewCategory(String name) {
        // نستخدم Executor للتحقق من الوجود المسبق لعدم توفرها في ViewModel حالياً
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            if (db.categoryDao().isCategoryExists(name) > 0) {
                runOnUiThread(() -> Toast.makeText(this, "هذا الاسم موجود مسبقاً!", Toast.LENGTH_SHORT).show());
            } else {
                viewModel.insertCategory(new Category(name, "category", "#9E9E9E"));
                runOnUiThread(() -> Toast.makeText(this, "تمت الإضافة بنجاح", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public void onDeleteClick(Category category) {
        new AlertDialog.Builder(this)
                .setTitle("حذف التصنيف")
                .setMessage("هل أنت متأكد من حذف " + category.getName() + "؟")
                .setPositiveButton("حذف", (dialog, which) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        AppDatabase db = AppDatabase.getInstance(this);
                        int count = db.categoryDao().countTransactionsForCategory(category.getId());
                        if (count > 0) {
                            runOnUiThread(() -> {
                                new AlertDialog.Builder(this)
                                        .setTitle("لا يمكن الحذف")
                                        .setMessage("هذا التصنيف مرتبط بـ " + count + " عمليات. يرجى حذف العمليات أو تغيير تصنيفها أولاً.")
                                        .setPositiveButton("حسناً", null)
                                        .show();
                            });
                        } else {
                            viewModel.deleteCategory(category);
                            runOnUiThread(() -> Toast.makeText(this, "تم حذف التصنيف", Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    @Override
    public void onEditClick(Category category) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        EditText etName = view.findViewById(R.id.etCategoryName);
        etName.setText(category.getName());

        new AlertDialog.Builder(this)
                .setTitle("تعديل التصنيف")
                .setView(view)
                .setPositiveButton("حفظ", (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        updateCategory(category, newName);
                    } else {
                        Toast.makeText(this, "يرجى إدخال اسم", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void updateCategory(Category category, String newName) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            if (!category.getName().equals(newName) && db.categoryDao().isCategoryExists(newName) > 0) {
                runOnUiThread(() -> Toast.makeText(this, "هذا الاسم موجود مسبقاً!", Toast.LENGTH_SHORT).show());
            } else {
                category.setName(newName);
                viewModel.updateCategory(category);
                runOnUiThread(() -> Toast.makeText(this, "تم التعديل بنجاح", Toast.LENGTH_SHORT).show());
            }
        });
    }
}

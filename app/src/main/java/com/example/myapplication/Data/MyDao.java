package com.example.myapplication.Data;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MyDao {

    // استعلام لجلب مجموع المصاريف لفئة محددة خلال شهر معين



    // استعلام لجلب إجمالي المصاريف لكل الفئات في شهر معين
    @Query("SELECT SUM(amount) FROM transactions " +
            "WHERE type = 'Expense' " +
            "AND strftime('%m', date / 1000, 'unixepoch') = :month")
    Double getTotalExpenseByMonth(String month);


    @Insert
    void insertTransaction(Transaction transaction);

    @Update
    void updateTransaction(Transaction transaction);

    @Delete
    void deleteTransaction(Transaction transaction);

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    Transaction getTransactionById(int id);

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    LiveData<List<Transaction>> getAllTransactions();

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'Income'")
    LiveData<Double> getTotalIncome();

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'Expense'")
    LiveData<Double> getTotalExpenses();

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'Income' " +
            "AND CAST(strftime('%m', date / 1000, 'unixepoch') AS INTEGER) = :month " +
            "AND CAST(strftime('%Y', date / 1000, 'unixepoch') AS INTEGER) = :year")
    LiveData<Double> getIncomeByMonth(int month, int year);

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'Expense' " +
            "AND CAST(strftime('%m', date / 1000, 'unixepoch') AS INTEGER) = :month " +
            "AND CAST(strftime('%Y', date / 1000, 'unixepoch') AS INTEGER) = :year")
    LiveData<Double> getExpensesByMonth(int month, int year);

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'Income' " +
            "AND CAST(strftime('%d', date / 1000, 'unixepoch') AS INTEGER) = :day " +
            "AND CAST(strftime('%m', date / 1000, 'unixepoch') AS INTEGER) = :month " +
            "AND CAST(strftime('%Y', date / 1000, 'unixepoch') AS INTEGER) = :year")
    LiveData<Double> getIncomeByDay(int day, int month, int year);

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'Expense' " +
            "AND CAST(strftime('%d', date / 1000, 'unixepoch') AS INTEGER) = :day " +
            "AND CAST(strftime('%m', date / 1000, 'unixepoch') AS INTEGER) = :month " +
            "AND CAST(strftime('%Y', date / 1000, 'unixepoch') AS INTEGER) = :year")
    LiveData<Double> getExpensesByDay(int day, int month, int year);

    @Insert
    void insertCategory(Category category);

    @Query("SELECT * FROM categories")
    LiveData<List<Category>> getAllCategories();

    @Query("SELECT name FROM categories WHERE id = :catId LIMIT 1")
    String getCategoryNameById(int catId);

    // --- عمليات الميزانية ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateBudget(Budget budget);

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year AND categoryId = :catId LIMIT 1")
    Budget getBudget(int month, int year, int catId);

    // --- عمليات الحذف الشامل (Reset) ---
    @Query("DELETE FROM transactions")
    void deleteAllTransactions();

    @Query("DELETE FROM categories")
    void deleteAllCategories();

    @Query("DELETE FROM budgets")
    void deleteAllBudgets();
}

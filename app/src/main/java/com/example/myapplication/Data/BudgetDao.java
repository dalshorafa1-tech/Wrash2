package com.example.myapplication.Data;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.Ui.BudgetSummary;

import java.util.List;

@Dao
public interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateBudget(Budget budget);

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND month = :month AND year = :year LIMIT 1")
    Budget getBudgetByCategory(int categoryId, int month, int year);

    @Query("SELECT c.name as categoryName, b.budgetlimit as limitAmt, " +
            "(SELECT SUM(amount) FROM transactions t WHERE t.categoryId = c.id AND t.type = 'Expense' " +
            "AND CAST(strftime('%m', t.date / 1000, 'unixepoch') AS INTEGER) = :month " +
            "AND CAST(strftime('%Y', t.date / 1000, 'unixepoch') AS INTEGER) = :year) as spentAmt " +
            "FROM categories c " +
            "INNER JOIN budgets b ON c.id = b.categoryId " +
            "WHERE b.month = :month AND b.year = :year AND b.categoryId != 0")
    List<BudgetSummary> getBudgetsStatus(int month, int year);

    @Query("DELETE FROM budgets WHERE month = :month AND year = :year")
    void resetMonthBudgets(int month, int year);

    @Query("SELECT SUM(amount) FROM transactions WHERE categoryId = :catId AND type = 'Expense' " +
            "AND CAST(strftime('%m', date / 1000, 'unixepoch') AS INTEGER) = :month " +
            "AND CAST(strftime('%Y', date / 1000, 'unixepoch') AS INTEGER) = :year")
    double getSpentByCategory(int catId, int month, int year);

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'Expense' " +
            "AND CAST(strftime('%m', date / 1000, 'unixepoch') AS INTEGER) = :month " +
            "AND CAST(strftime('%Y', date / 1000, 'unixepoch') AS INTEGER) = :year")
    Double getTotalSpent(int month, int year);

    @Query("SELECT SUM(budgetlimit) FROM budgets WHERE month = :month AND year = :year AND categoryId != 0")
    Double getTotalCategoryBudgets(int month, int year);
}

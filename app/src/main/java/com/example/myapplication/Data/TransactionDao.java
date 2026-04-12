package com.example.myapplication.Data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.example.myapplication.Ui.CategoryReport;

import java.util.List;

@Dao
public interface TransactionDao {
    @Query("SELECT categories.name AS categoryName, SUM(transactions.amount) AS totalAmount " +
            "FROM transactions " +
            "INNER JOIN categories ON transactions.categoryId = categories.id " +
            "WHERE transactions.type = :type " +
            "AND CAST(strftime('%d', transactions.date / 1000, 'unixepoch') AS INTEGER) = :day " +
            "AND CAST(strftime('%m', transactions.date / 1000, 'unixepoch') AS INTEGER) = :month " +
            "AND CAST(strftime('%Y', transactions.date / 1000, 'unixepoch') AS INTEGER) = :year " +
            "GROUP BY categories.name")
    LiveData<List<CategoryReport>> getReportByDay(String type, int day, int month, int year);

    @Query("SELECT type AS categoryName, SUM(amount) AS totalAmount " +
            "FROM transactions " +
            "WHERE CAST(strftime('%d', date / 1000, 'unixepoch') AS INTEGER) = :day " +
            "AND CAST(strftime('%m', date / 1000, 'unixepoch') AS INTEGER) = :month " +
            "AND CAST(strftime('%Y', date / 1000, 'unixepoch') AS INTEGER) = :year " +
            "GROUP BY type")
    LiveData<List<CategoryReport>> getOverviewReportByDay(int day, int month, int year);

    @Query("SELECT * FROM transactions WHERE " +
            "CAST(strftime('%d', date / 1000, 'unixepoch') AS INTEGER) = :day " +
            "AND CAST(strftime('%m', date / 1000, 'unixepoch') AS INTEGER) = :month " +
            "AND CAST(strftime('%Y', date / 1000, 'unixepoch') AS INTEGER) = :year " +
            "ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByDay(int day, int month, int year);

    @Query("SELECT type AS categoryName, SUM(amount) AS totalAmount " +
            "FROM transactions " +
            "GROUP BY type")
    LiveData<List<CategoryReport>> getOverviewReportAllTime();
    
    @Query("SELECT categories.name AS categoryName, SUM(transactions.amount) AS totalAmount " +
            "FROM transactions " +
            "INNER JOIN categories ON transactions.categoryId = categories.id " +
            "WHERE transactions.type = 'Expense' " +
            "GROUP BY categories.name")
    LiveData<List<CategoryReport>> getAllTimeExpenseDistribution();

    @Query("SELECT transactions.* FROM transactions " +
            "INNER JOIN categories ON transactions.categoryId = categories.id " +
            "WHERE categories.name = :categoryName " +
            "AND CAST(strftime('%d', transactions.date / 1000, 'unixepoch') AS INTEGER) = :day " +
            "AND CAST(strftime('%m', transactions.date / 1000, 'unixepoch') AS INTEGER) = :month " +
            "AND CAST(strftime('%Y', transactions.date / 1000, 'unixepoch') AS INTEGER) = :year " +
            "ORDER BY transactions.date DESC")
    LiveData<List<Transaction>> getTransactionsByCategoryAndDay(String categoryName, int day, int month, int year);

    @Query("SELECT * FROM transactions WHERE type = :type " +
            "AND CAST(strftime('%d', date / 1000, 'unixepoch') AS INTEGER) = :day " +
            "AND CAST(strftime('%m', date / 1000, 'unixepoch') AS INTEGER) = :month " +
            "AND CAST(strftime('%Y', date / 1000, 'unixepoch') AS INTEGER) = :year " +
            "ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByTypeAndDay(String type, int day, int month, int year);

    @Query("SELECT transactions.* FROM transactions " +
            "INNER JOIN categories ON transactions.categoryId = categories.id " +
            "WHERE categories.name = :categoryName " +
            "AND CAST(strftime('%m', transactions.date / 1000, 'unixepoch') AS INTEGER) = :month " +
            "AND CAST(strftime('%Y', transactions.date / 1000, 'unixepoch') AS INTEGER) = :year " +
            "ORDER BY transactions.date DESC")
    LiveData<List<Transaction>> getTransactionsByCategoryAndMonth(String categoryName, int month, int year);

    @Query("SELECT * FROM transactions WHERE type = :type " +
            "AND CAST(strftime('%m', date / 1000, 'unixepoch') AS INTEGER) = :month " +
            "AND CAST(strftime('%Y', date / 1000, 'unixepoch') AS INTEGER) = :year " +
            "ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByTypeAndMonth(String type, int month, int year);

    @Query("SELECT * FROM transactions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    List<Transaction> getTransactionsBetweenDates(long startDate, long endDate);

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    List<Transaction> getAllTransactions();
    @Query("SELECT SUM(amount) FROM transactions " +
            "WHERE type = 'Expense' " +
            "AND strftime('%m', date / 1000, 'unixepoch') = :month")
    Double getTotalExpenseByMonth(String month);
}

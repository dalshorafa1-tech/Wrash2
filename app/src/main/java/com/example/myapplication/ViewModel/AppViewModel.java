package com.example.myapplication.ViewModel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.myapplication.Ui.CategoryReport;
import com.example.myapplication.Data.AppRepository;
import com.example.myapplication.Data.Budget;
import com.example.myapplication.Data.Category;
import com.example.myapplication.Data.Transaction;

import java.util.List;

public class AppViewModel extends AndroidViewModel {
    private final AppRepository repository;
    private final LiveData<List<Transaction>> allTransactions;
    private final LiveData<List<Category>> allCategories;
    private final LiveData<Double> totalIncome;
    private final LiveData<Double> totalExpenses;

    public AppViewModel(@NonNull Application application) {
        super(application);
        repository = new AppRepository(application);
        allTransactions = repository.getAllTransactions();
        allCategories = repository.getAllCategories();
        totalIncome = repository.getTotalIncome();
        totalExpenses = repository.getTotalExpenses();
    }

    // --- Transactions ---
    public LiveData<List<Transaction>> getAllTransactions() {
        return allTransactions;
    }

    public Transaction getTransactionById(int id) {
        return repository.getTransactionById(id);
    }

    public void insertTransaction(Transaction transaction) {
        repository.insertTransaction(transaction);
    }

    public void updateTransaction(Transaction transaction) {
        repository.updateTransaction(transaction);
    }

    public void deleteTransaction(Transaction transaction) {
        repository.deleteTransaction(transaction);
    }

    public LiveData<List<Transaction>> getTransactionsByDay(int day, int month, int year) {
        return repository.getTransactionsByDay(day, month, year);
    }

    public LiveData<List<Transaction>> getTransactionsByTypeAndDay(String type, int day, int month, int year) {
        return repository.getTransactionsByTypeAndDay(type, day, month, year);
    }

    // --- Categories ---
    public LiveData<List<Category>> getAllCategories() {
        return allCategories;
    }

    public void insertCategory(Category category) {
        repository.insertCategory(category);
    }

    public void updateCategory(Category category) {
        repository.updateCategory(category);
    }

    public void deleteCategory(Category category) {
        repository.deleteCategory(category);
    }

    // --- Budgets ---
    public void insertOrUpdateBudget(Budget budget) {
        repository.insertOrUpdateBudget(budget);
    }

    public Budget getBudgetByCategory(int categoryId, int month, int year) {
        return repository.getBudgetByCategory(categoryId, month, year);
    }

    // --- Summaries & Reports ---
    public LiveData<Double> getTotalIncome() {
        return totalIncome;
    }

    public LiveData<Double> getTotalExpenses() {
        return totalExpenses;
    }

    public LiveData<Double> getIncomeByDay(int day, int month, int year) {
        return repository.getIncomeByDay(day, month, year);
    }

    public LiveData<Double> getExpensesByDay(int day, int month, int year) {
        return repository.getExpensesByDay(day, month, year);
    }

    public LiveData<List<CategoryReport>> getOverviewReportByDay(int day, int month, int year) {
        return repository.getOverviewReportByDay(day, month, year);
    }

    public LiveData<List<CategoryReport>> getReportByDay(String type, int day, int month, int year) {
        return repository.getReportByDay(type, day, month, year);
    }

    public LiveData<List<CategoryReport>> getAllTimeExpenseDistribution() {
        return repository.getAllTimeExpenseDistribution();
    }

    // --- System ---
    public void resetAllData() {
        repository.deleteAllTransactions();
        repository.deleteAllBudgets();
    }
}

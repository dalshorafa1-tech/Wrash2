package com.example.myapplication.Data;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.example.myapplication.Ui.CategoryReport;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppRepository {
    private final MyDao myDao;
    private final TransactionDao transactionDao;
    private final categoryDao categoryDao;
    private final BudgetDao budgetDao;
    private final UserDao userDao;
    private final ExecutorService executorService;

    public AppRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        myDao = db.myDao();
        transactionDao = db.transactionDao();
        categoryDao = db.categoryDao();
        budgetDao = db.budgetDao();
        userDao = db.userDao();
        executorService = Executors.newFixedThreadPool(4);
    }

    // --- Transactions ---
    public LiveData<List<Transaction>> getAllTransactions() {
        return myDao.getAllTransactions();
    }

    public Transaction getTransactionById(int id) {
        return myDao.getTransactionById(id);
    }

    public void insertTransaction(Transaction transaction) {
        executorService.execute(() -> myDao.insertTransaction(transaction));
    }

    public void updateTransaction(Transaction transaction) {
        executorService.execute(() -> myDao.updateTransaction(transaction));
    }

    public void deleteTransaction(Transaction transaction) {
        executorService.execute(() -> myDao.deleteTransaction(transaction));
    }

    public void deleteAllTransactions() {
        executorService.execute(myDao::deleteAllTransactions);
    }

    public LiveData<List<Transaction>> getTransactionsByDay(int day, int month, int year) {
        return transactionDao.getTransactionsByDay(day, month, year);
    }

    public LiveData<List<Transaction>> getTransactionsByTypeAndDay(String type, int day, int month, int year) {
        return transactionDao.getTransactionsByTypeAndDay(type, day, month, year);
    }

    // --- Categories ---
    public LiveData<List<Category>> getAllCategories() {
        return myDao.getAllCategories();
    }

    public void insertCategory(Category category) {
        executorService.execute(() -> categoryDao.insert(category));
    }

    public void updateCategory(Category category) {
        executorService.execute(() -> categoryDao.update(category));
    }

    public void deleteCategory(Category category) {
        executorService.execute(() -> categoryDao.delete(category));
    }

    // --- Budgets ---
    public void insertOrUpdateBudget(Budget budget) {
        executorService.execute(() -> budgetDao.insertOrUpdateBudget(budget));
    }

    public Budget getBudgetByCategory(int categoryId, int month, int year) {
        return budgetDao.getBudgetByCategory(categoryId, month, year);
    }

    public double getSpentByCategory(int catId, int month, int year) {
        return budgetDao.getSpentByCategory(catId, month, year);
    }

    public void deleteAllBudgets() {
        executorService.execute(myDao::deleteAllBudgets);
    }

    // --- Summaries & Reports ---
    public LiveData<Double> getTotalIncome() {
        return myDao.getTotalIncome();
    }

    public LiveData<Double> getTotalExpenses() {
        return myDao.getTotalExpenses();
    }

    public LiveData<Double> getIncomeByDay(int day, int month, int year) {
        return myDao.getIncomeByDay(day, month, year);
    }

    public LiveData<Double> getExpensesByDay(int day, int month, int year) {
        return myDao.getExpensesByDay(day, month, year);
    }

    public LiveData<List<CategoryReport>> getOverviewReportByDay(int day, int month, int year) {
        return transactionDao.getOverviewReportByDay(day, month, year);
    }

    public LiveData<List<CategoryReport>> getReportByDay(String type, int day, int month, int year) {
        return transactionDao.getReportByDay(type, day, month, year);
    }

    public LiveData<List<CategoryReport>> getAllTimeExpenseDistribution() {
        return transactionDao.getAllTimeExpenseDistribution();
    }

    // --- User Operations ---
    public User getUserByEmail(String email) {
        return userDao.getUserByEmail(email);
    }

    public void registerUser(User user) {
        executorService.execute(() -> userDao.registerUser(user));
    }
}

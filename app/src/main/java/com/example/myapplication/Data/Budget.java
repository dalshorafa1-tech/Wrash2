package com.example.myapplication.Data;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "budgets")
public class Budget {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int month;
    public int year;
    public int categoryId;
    public double budgetlimit;
}

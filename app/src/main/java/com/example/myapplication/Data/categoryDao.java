package com.example.myapplication.Data;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface categoryDao {

    @Insert
    void insert(Category category);

    @Update
    void update(Category category);

    @Delete
    void delete(Category category);

    @Query("SELECT * FROM categories ORDER BY name ASC")
    List<Category> getAllCategories();

    // القيمة الأهم: التأكد من وجود معاملات مرتبطة بهذه الفئة
    @Query("SELECT COUNT(*) FROM transactions WHERE categoryId = :catId")
    int countTransactionsForCategory(int  catId);

    // التأكد من عدم تكرار الاسم (Validation Rule)
    @Query("SELECT COUNT(*) FROM categories WHERE name = :catName")
    int isCategoryExists(String catName);
}

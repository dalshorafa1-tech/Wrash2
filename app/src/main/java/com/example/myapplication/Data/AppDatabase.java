package com.example.myapplication.Data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.Executors;

@Database(entities = {Transaction.class, Category.class, Budget.class, User.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract MyDao myDao();
    public abstract categoryDao categoryDao();
    public abstract BudgetDao budgetDao();
    public abstract TransactionDao transactionDao();
    public abstract UserDao userDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "app_database")
                            .addCallback(sRoomDatabaseCallback)
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase database = INSTANCE;
                if (database != null) {
                    MyDao dao = database.myDao();
                    dao.insertCategory(new Category("طعام", "restaurant", "#FF5722"));
                    dao.insertCategory(new Category("نقل", "directions_bus", "#2196F3"));
                    dao.insertCategory(new Category("تسوق", "shopping_cart", "#9C27B0"));
                    dao.insertCategory(new Category("راتب", "attach_money", "#4CAF50"));
                    dao.insertCategory(new Category("إيجار", "home", "#795548"));
                    dao.insertCategory(new Category("ترفيه", "movie", "#E91E63"));
                }
            });
        }
    };
}

package com.example.myapplication.Ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Data.Category;
import com.example.myapplication.Data.Transaction;
import com.example.myapplication.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList = new ArrayList<>();
    private Map<Integer, Category> categoryMap = new HashMap<>();
    private OnTransactionClickListener listener;

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
        default void onEditClick(Transaction transaction) {}
    }

    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.listener = listener;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactionList = transactions;
        notifyDataSetChanged();
    }

    public void setCategories(List<Category> categories) {
        categoryMap.clear();
        if (categories != null) {
            for (Category category : categories) {
                categoryMap.put(category.getId(), category);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.homeitem, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        Category category = categoryMap.get(transaction.getCategoryId());

        // عرض اسم الفئة بدلاً من العنوان
        if (category != null) {
            holder.tvTitle.setText(category.getName());
            // تعيين الأيقونة واللون إذا توفرا
            if (category.getColor() != null) {
                holder.iconBackground.getBackground().setTint(Color.parseColor(category.getColor()));
                holder.iconBackground.setAlpha(0.15f); // شفافية خفيفة للخلفية
                holder.ivCategoryIcon.setColorFilter(Color.parseColor(category.getColor()));
            }
        } else {
            holder.tvTitle.setText("غير مصنف");
        }
        
        // تنسيق التاريخ
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy • hh:mm a", new Locale("ar"));
        holder.tvDate.setText(sdf.format(new Date(transaction.getDate())));

        // تنسيق المبلغ واللون
        if ("Income".equalsIgnoreCase(transaction.getType())) {
            holder.tvAmount.setText(String.format(Locale.getDefault(), "+%.2f $", transaction.getAmount()));
            holder.tvAmount.setTextColor(Color.parseColor("#10B981")); // أخضر عصري
        } else {
            holder.tvAmount.setText(String.format(Locale.getDefault(), "-%.2f $", transaction.getAmount()));
            holder.tvAmount.setTextColor(Color.parseColor("#EF4444")); // أحمر عصري
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTransactionClick(transaction);
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(transaction);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvAmount;
        ImageView ivCategoryIcon;
        View iconBackground;
        ImageButton btnEdit;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTransactionTitle);
            tvDate = itemView.findViewById(R.id.tvTransactionDate);
            tvAmount = itemView.findViewById(R.id.tvTransactionAmount);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            iconBackground = itemView.findViewById(R.id.iconBackground);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}

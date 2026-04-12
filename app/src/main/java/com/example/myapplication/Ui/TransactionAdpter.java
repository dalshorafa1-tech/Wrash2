package com.example.myapplication.Ui;


import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Data.Transaction;
import com.example.myapplication.R;

import java.util.List;

public class TransactionAdpter extends RecyclerView.Adapter<TransactionAdpter.TransactionViewHolder> {

    private List<Transaction> transactionList;
    private OnItemClickListener listener;

    // واجهة (Interface) للتعامل مع نقرات العناصر (التعديل أو التفاصيل)
    public interface OnItemClickListener {
        void onItemClick(Transaction transaction);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public TransactionAdpter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.transaction_item, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction currentItem = transactionList.get(position);

        // 1. عرض العنوان
        holder.tvTitle.setText(currentItem.getTitle());

        // 2. عرض التصنيف والتاريخ مدمجين
        String details = " • " + currentItem.getDate();
        holder.tvDetails.setText(details);

        holder.tvDetails.setText(details);

        // 3. تنسيق المبلغ واللون بناءً على النوع (Income / Expense)
        // نفترض أن النوع مخزن في قاعدة البيانات كـ "Income" أو "Expense"
        if ("Income".equalsIgnoreCase(currentItem.getType())) {
            holder.tvAmount.setText("+ " + String.format("%.2f", currentItem.getAmount()) + " $");
            holder.tvAmount.setTextColor(Color.parseColor("#2E7D32")); // لون أخضر
            holder.viewCategoryColor.setBackgroundColor(Color.parseColor("#2E7D32"));
        } else {
            holder.tvAmount.setText("- " + String.format("%.2f", currentItem.getAmount()) + " $");
            holder.tvAmount.setTextColor(Color.parseColor("#C62828")); // لون أحمر
            holder.viewCategoryColor.setBackgroundColor(Color.parseColor("#C62828"));
        }
    }

    @Override
    public int getItemCount() {
        return transactionList != null ? transactionList.size() : 0;
    }

    // دالة لتحديث القائمة عند البحث أو الفلترة
    public void setTransactions(List<Transaction> newList) {
        this.transactionList = newList;
        notifyDataSetChanged();
    }

    // كلاس الـ ViewHolder لتعريف العناصر البرمجية
    class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDetails, tvAmount;
        View viewCategoryColor;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTransactionTitle);
            tvDetails = itemView.findViewById(R.id.tvTransactionDetails);
            tvAmount = itemView.findViewById(R.id.tvTransactionAmount);
            viewCategoryColor = itemView.findViewById(R.id.viewCategoryColor);

            // إرسال حدث النقر للـ Activity
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(transactionList.get(position));
                }
            });
        }
    }
}

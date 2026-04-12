package com.example.myapplication.Ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class CategoryReportAdapter extends RecyclerView.Adapter<CategoryReportAdapter.ViewHolder> {

    private List<CategoryReport> reports = new ArrayList<>();

    public void setReports(List<CategoryReport> reports) {
        this.reports = reports;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryReport report = reports.get(position);
        holder.tvName.setText(report.categoryName);
        holder.tvAmount.setText(String.format("%.2f $", report.totalAmount));
        
        // إخفاء الأزرار غير المستخدمة في التقرير
        holder.itemView.findViewById(R.id.btnEditCategory).setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.btnDeleteCategory).setVisibility(View.GONE);
        
        if (report.categoryName != null && !report.categoryName.isEmpty()) {
            holder.tvIcon.setText(report.categoryName.substring(0, 1));
        }
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvName, tvAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvCategoryIcon);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            tvAmount = itemView.findViewById(R.id.tvCategoryType);
        }
    }
}

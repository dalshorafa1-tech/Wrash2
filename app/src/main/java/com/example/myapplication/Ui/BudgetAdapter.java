package com.example.myapplication.Ui;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private List<BudgetSummary> budgetSummaries;

    public void setBudgetSummaries(List<BudgetSummary> budgetSummaries) {
        this.budgetSummaries = budgetSummaries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget_category, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        BudgetSummary summary = budgetSummaries.get(position);
        holder.tvCategoryName.setText(summary.categoryName);
        
        double spent = (summary.spentAmt != null) ? summary.spentAmt : 0;
        holder.tvBudgetAmount.setText(String.format("%.2f / %.0f $", spent, summary.limitAmt));
        
        int progress = summary.getProgress();
        holder.pbCategoryBudget.setProgress(Math.min(progress, 100));
        
        double remaining = summary.limitAmt - spent;
        if (remaining >= 0) {
            holder.tvRemaining.setText(String.format("متبقي: %.2f $", remaining));
            holder.tvRemaining.setTextColor(Color.parseColor("#718096"));
        } else {
            holder.tvRemaining.setText(String.format("تجاوزت بمقدار: %.2f $", Math.abs(remaining)));
            holder.tvRemaining.setTextColor(Color.RED);
        }

        if (summary.isOverBudget()) {
            holder.pbCategoryBudget.setProgressTintList(ColorStateList.valueOf(Color.RED));
        } else if (progress > 80) {
            holder.pbCategoryBudget.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FFA500")));
        } else {
            holder.pbCategoryBudget.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#2E7D32")));
        }
    }

    @Override
    public int getItemCount() {
        return budgetSummaries != null ? budgetSummaries.size() : 0;
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName, tvBudgetAmount, tvRemaining;
        ProgressBar pbCategoryBudget;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvBudgetAmount = itemView.findViewById(R.id.tvBudgetAmount);
            tvRemaining = itemView.findViewById(R.id.tvRemaining);
            pbCategoryBudget = itemView.findViewById(R.id.pbCategoryBudget);
        }
    }
}

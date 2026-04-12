package com.example.myapplication.Ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Data.Category;
import com.example.myapplication.R;

import java.util.List;

public class CategoryAdpter extends RecyclerView.Adapter<CategoryAdpter.CategoryViewHolder> {

    private List<Category> categoryList;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onDeleteClick(Category category);
        void onItemClick(Category category);
    }

    public CategoryAdpter(List<Category> categoryList, OnCategoryClickListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);

        holder.tvName.setText(category.getName());

        // عرض أول حرف من الاسم كأيقونة
        if (!category.getName().isEmpty()) {
            holder.tvIcon.setText(category.getName().substring(0, 1).toUpperCase());
        }

        // لون الأيقونة بناءً على النوع

        holder.tvIcon.getBackground().setTint(R.color.colorPrimary);

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(category));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(category));
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public void setCategories(List<Category> newList) {
        this.categoryList = newList;
        notifyDataSetChanged();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvType, tvIcon;
        ImageButton btnDelete;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = findViewById(R.id.tvCategoryName);
            tvType = findViewById(R.id.tvCategoryType);
            tvIcon = findViewById(R.id.tvCategoryIcon);
            btnDelete = findViewById(R.id.btnDeleteCategory);
        }

        private <T extends View> T findViewById(int id) {
            return itemView.findViewById(id);
        }
    }
}

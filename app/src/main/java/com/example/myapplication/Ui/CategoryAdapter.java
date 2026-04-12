package com.example.myapplication.Ui;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories;
    private OnCategoryClickListener listener;

    // قائمة ألوان عشوائية جذابة (Material Colors)
    private final String[] palette = {
            "#F44336", "#E91E63", "#9C27B0", "#673AB7",
            "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
            "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
            "#FFC107", "#FF9800", "#FF5722", "#795548",
            "#607D8B", "#455A64"
    };

    public interface OnCategoryClickListener {
        void onDeleteClick(Category category);
        void onEditClick(Category category);
    }

    public CategoryAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.tvName.setText(category.getName());
        
        if (category.getName() != null && !category.getName().isEmpty()) {
            holder.tvIcon.setText(category.getName().substring(0, 1).toUpperCase());
        }

        // إنشاء خلفية دائرية بلون عشوائي ثابت بناءً على اسم التصنيف
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        
        int colorIndex = Math.abs(category.getName().hashCode()) % palette.length;
        String colorHex = (category.getColor() != null && !category.getColor().equals("#9E9E9E")) 
                          ? category.getColor() : palette[colorIndex];
        
        try {
            drawable.setColor(Color.parseColor(colorHex));
        } catch (Exception e) {
            drawable.setColor(Color.parseColor(palette[0]));
        }
        holder.tvIcon.setBackground(drawable);

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(category);
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvName, tvType;
        ImageButton btnDelete, btnEdit;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvCategoryIcon);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            tvType = itemView.findViewById(R.id.tvCategoryType);
            btnDelete = itemView.findViewById(R.id.btnDeleteCategory);
            btnEdit = itemView.findViewById(R.id.btnEditCategory);
        }
    }
}

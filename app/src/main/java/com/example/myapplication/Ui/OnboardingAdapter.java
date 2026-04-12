package com.example.myapplication.Ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private List<OnboardingItem> onboardingItems;

    public OnboardingAdapter(List<OnboardingItem> onboardingItems) {
        this.onboardingItems = onboardingItems;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OnboardingViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_onboarding, parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        holder.setOnboardingData(onboardingItems.get(position));
    }

    @Override
    public int getItemCount() {
        return onboardingItems.size();
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        private TextView textTitle;
        private TextView textDescription;
        private ImageView imageOnboarding;
        private Spinner currencySpinner;

        OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.tvTitle);
            textDescription = itemView.findViewById(R.id.tvDescription);
            imageOnboarding = itemView.findViewById(R.id.ivOnboarding);
            currencySpinner = itemView.findViewById(R.id.currencySpinner);
        }

        void setOnboardingData(OnboardingItem onboardingItem) {
            textTitle.setText(onboardingItem.getTitle());
            textDescription.setText(onboardingItem.getDescription());
            imageOnboarding.setImageResource(onboardingItem.getImage());

            if (onboardingItem.isShowSpinner()) {
                currencySpinner.setVisibility(View.VISIBLE);
                String[] currencies = {"اختر العملة", "دولار أمريكي ($)", "يورو (€)", "ريال سعودي (SAR)", "درهم إماراتي (AED)", "دينار كويتي (KWD)", "جنيه مصري (EGP)"};
                ArrayAdapter<String> adapter = new ArrayAdapter<>(itemView.getContext(),
                        android.R.layout.simple_spinner_item, currencies);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                currencySpinner.setAdapter(adapter);
            } else {
                currencySpinner.setVisibility(View.GONE);
            }
        }
        
        public String getSelectedCurrency() {
            if (currencySpinner.getVisibility() == View.VISIBLE) {
                return currencySpinner.getSelectedItem().toString();
            }
            return null;
        }
    }
}
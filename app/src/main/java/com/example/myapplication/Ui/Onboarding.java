package com.example.myapplication.Ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class Onboarding extends AppCompatActivity {

    private OnboardingAdapter onboardingAdapter;
    private ViewPager2 viewPager;
    private Button btnNext, btnSkip, btnBudget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);

        TabLayout tabLayout = findViewById(R.id.tabLayout);

        setupOnboardingItems();

        viewPager.setAdapter(onboardingAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // Dots indicator
        }).attach();



        // تعديل زر التخطي للانتقال إلى الصفحة الثالثة (Index 2)
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // الانتقال المباشر للتبويب الثالث
                if (onboardingAdapter.getItemCount() >= 3) {
                    viewPager.setCurrentItem(2);
                } else {
                    viewPager.setCurrentItem(onboardingAdapter.getItemCount() - 1);
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewPager.getCurrentItem() + 1 < onboardingAdapter.getItemCount()) {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                } else {
                    // This is the last page (Finish)
                    validateAndFinish();
                }
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == onboardingAdapter.getItemCount() - 1) {
                    btnNext.setText("إنهاء");
                    btnSkip.setVisibility(View.GONE);
                } else {
                    btnNext.setText("التالي");
                    btnSkip.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void validateAndFinish() {
        RecyclerView.ViewHolder viewHolder = ((RecyclerView) viewPager.getChildAt(0))
                .findViewHolderForAdapterPosition(viewPager.getCurrentItem());

        if (viewHolder instanceof OnboardingAdapter.OnboardingViewHolder) {
            String selected = ((OnboardingAdapter.OnboardingViewHolder) viewHolder).getSelectedCurrency();
            
            if (selected == null || selected.equals("اختر العملة")) {
                Toast.makeText(this, "يرجى اختيار العملة للمتابعة", Toast.LENGTH_SHORT).show();
            } else {
                SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                editor.putString("currency", selected);
                editor.apply();

                Intent intent = new Intent(Onboarding.this, HomeScreen.class);
                startActivity(intent);
                finish();
            }
        }
    }

    private void setupOnboardingItems() {
        List<OnboardingItem> onboardingItems = new ArrayList<>();

        onboardingItems.add(new OnboardingItem(
                "اموالك تحت سيطرتك",
                "توقف عن التساؤل اين ذهب راتبي سجل مصاريفك اليومية بضغطة زر وشاهد الصورة كاملة لمدخراتك",
                R.drawable.bg_w,
                false
        ));

        onboardingItems.add(new OnboardingItem(
                "اجعل ارقامك تتجدث ",
                "لا مزيد من الحسابات المعقدة سنقوم بتحليل انفاقك تلقاءيا ونرسم لك الطريق نحو ميزانية ذكية ومستدامة",
                R.drawable.bg_w,
                false
        ));

        onboardingItems.add(new OnboardingItem(
                "ابدأ رحلتك المالية الآن",
                "اختر العملة التي تفضل التعامل معها ودعنا نساعدك في بناء مستقبل مالي أفضل",
                R.drawable.bg_w,
                true
        ));

        onboardingAdapter = new OnboardingAdapter(onboardingItems);
    }
}
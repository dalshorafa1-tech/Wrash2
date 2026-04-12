package com.example.myapplication.Ui;

public class BudgetSummary {
    // يجب أن تطابق هذه الأسماء تماماً الأسماء المستعارة (Aliases) في استعلام الـ SQL
    public String categoryName;
    public double limitAmt;     // الحد الأقصى للميزانية
    public Double spentAmt;     // المبلغ المستهلك (استخدمنا Double ليتعامل مع null)

    // دالة مساعدة لحساب النسبة المئوية لشريط التقدم
    public int getProgress() {
        if (limitAmt <= 0) return 0;
        double spent = (spentAmt != null) ? spentAmt : 0;
        return (int) ((spent / limitAmt) * 100);
    }

    // دالة للتحقق من تجاوز الميزانية
    public boolean isOverBudget() {
        double spent = (spentAmt != null) ? spentAmt : 0;
        return spent > limitAmt;
    }
}

package com.example.myapplication.Ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.util.Pair;

import com.example.myapplication.Data.AppDatabase;
import com.example.myapplication.R;
import com.example.myapplication.Data.Transaction;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ExportActivity extends AppCompatActivity {

    private Button btnSelectDateRange, btnGenerateAndShare;
    private TextView tvSelectedRange;
    private RadioGroup radioGroupFormat;
    private Toolbar toolbar;
    private long startDate = 0, endDate = System.currentTimeMillis();
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        db = AppDatabase.getInstance(this);
        initViews();
        setupToolbar();

        btnSelectDateRange.setOnClickListener(v -> showDateRangePicker());
        btnGenerateAndShare.setOnClickListener(v -> generateReport());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        btnSelectDateRange = findViewById(R.id.btnSelectDateRange);
        btnGenerateAndShare = findViewById(R.id.btnGenerateAndShare);
        tvSelectedRange = findViewById(R.id.tvSelectedRange);
        radioGroupFormat = findViewById(R.id.radioGroupFormat);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            }
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    private void showDateRangePicker() {
        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("اختر الفترة الزمنية")
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            startDate = selection.first;
            endDate = selection.second;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            tvSelectedRange.setText("الفترة: " + sdf.format(new Date(startDate)) + " - " + sdf.format(new Date(endDate)));
        });

        picker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void generateReport() {
        boolean isPdf = radioGroupFormat.getCheckedRadioButtonId() == R.id.radioPDF;
        
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Transaction> transactions;
            if (startDate == 0) {
                transactions = db.transactionDao().getAllTransactions();
            } else {
                transactions = db.transactionDao().getTransactionsBetweenDates(startDate, endDate);
            }

            if (transactions.isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, "لا توجد بيانات للفترة المحددة", Toast.LENGTH_SHORT).show());
                return;
            }

            File file;
            if (isPdf) {
                file = createPdfReport(transactions);
            } else {
                file = createCsvReport(transactions);
            }

            if (file != null) {
                runOnUiThread(() -> shareFile(file));
            } else {
                runOnUiThread(() -> Toast.makeText(this, "فشل في إنشاء الملف", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private File createPdfReport(List<Transaction> transactions) {
        try {
            File path = new File(getCacheDir(), "reports");
            if (!path.exists()) path.mkdirs();
            File file = new File(path, "report_" + System.currentTimeMillis() + ".pdf");
            
            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("تقرير المعاملات").setFontSize(18).setBold());
            
            Table table = new Table(new float[]{2, 2, 2, 3});
            table.addCell("التاريخ");
            table.addCell("النوع");
            table.addCell("المبلغ");
            table.addCell("العنوان");

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            for (Transaction t : transactions) {
                table.addCell(sdf.format(new Date(t.date)));
                table.addCell(t.type);
                table.addCell(String.valueOf(t.amount));
                table.addCell(t.title != null ? t.title : "");
            }

            document.add(table);
            document.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private File createCsvReport(List<Transaction> transactions) {
        try {
            File path = new File(getCacheDir(), "reports");
            if (!path.exists()) path.mkdirs();
            File file = new File(path, "report_" + System.currentTimeMillis() + ".csv");
            
            FileOutputStream out = new FileOutputStream(file);
            StringBuilder sb = new StringBuilder();
            sb.append("Title,Amount,Type,Date,Note\n");
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            for (Transaction t : transactions) {
                sb.append(t.title).append(",")
                  .append(t.amount).append(",")
                  .append(t.type).append(",")
                  .append(sdf.format(new Date(t.date))).append(",")
                  .append(t.note != null ? t.note : "").append("\n");
            }
            
            out.write(sb.toString().getBytes());
            out.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void shareFile(File file) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(getContentResolver().getType(uri));
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "مشاركة التقرير عبر"));
    }
}

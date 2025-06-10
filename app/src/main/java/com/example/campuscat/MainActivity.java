package com.example.test;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CafeteriaActivity extends AppCompatActivity {
    private TextView textContent, textTitle;
    private Button btnBreakfast, btnLunch, btnDinner;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cafeteria);

        // 툴바 설정 (← 뒤로가기 화살표 포함)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // ← 버튼 활성화
            getSupportActionBar().setDisplayShowTitleEnabled(false); // 타이틀 숨김
        }

        textTitle = findViewById(R.id.textTitle);
        textContent = findViewById(R.id.textContent);
        btnBreakfast = findViewById(R.id.btnBreakfast);
        btnLunch = findViewById(R.id.btnLunch);
        btnDinner = findViewById(R.id.btnDinner);

        Intent intent = getIntent();
        selectedDate = intent.getStringExtra("selectedDate");

        if (selectedDate == null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
            selectedDate = sdf.format(new Date());
        }

        if (intent.getStringExtra("selectedDate") == null) {
            textTitle.setText("오늘의 학식");
        } else {
            textTitle.setText(selectedDate + " 학식");
        }

        btnBreakfast.setOnClickListener(v -> textContent.setText(getMenuForDate(selectedDate, "아침")));
        btnLunch.setOnClickListener(v -> textContent.setText(getMenuForDate(selectedDate, "점심")));
        btnDinner.setOnClickListener(v -> textContent.setText(getMenuForDate(selectedDate, "저녁")));

        FloatingActionButton fabCalendar = findViewById(R.id.fabCalendar);
        fabCalendar.setOnClickListener(v -> {
            Intent goToCalendar = new Intent(CafeteriaActivity.this, CalendarActivity.class);
            startActivity(goToCalendar);
        });
    }

    // ← 버튼 클릭 시 종료
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // 현재 액티비티 종료
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getMenuForDate(String date, String meal) {
        if (date == null) return getDefaultMenu(밥);

        switch (date) {
            case "2025-6-9":
                switch (밥) {
                    case "아침": return "⦁ 삶은 계란\n⦁ 식빵\n⦁ 우유";
                    case "점심": return "⦁ 된장찌개\n⦁ 고등어구이\n⦁ 밥, 김치";
                    case "저녁": return "⦁ 라면\n⦁ 김밥\n⦁ 단무지";
                }
                break;
            case "2025-7-3":
                switch (밥) {
                    case "아침": return "⦁ 토스트\n⦁ 우유\n⦁ 삶은 계란";
                    case "점심": return "⦁ 돈까스\n⦁ 미역국\n⦁ 샐러드\n⦁ 밥, 김치";
                    case "저녁": return "⦁ 카레라이스\n⦁ 단무지\n⦁ 요구르트";
                }
                break;
            case "2025-7-4":
                switch (밥) {
                    case "아침": return "⦁ 북어국\n⦁ 멸치볶음\n⦁ 밥, 김치";
                    case "점심": return "⦁ 김치찌개\n⦁ 오징어볶음\n⦁ 계란말이\n⦁ 밥, 김치";
                    case "저녁": return "⦁ 비빔밥\n⦁ 미소된장국\n⦁ 오렌지";
                }
                break;
            case "2025-7-5":
                switch (밥) {
                    case "아침": return "⦁ 씨리얼\n⦁ 바나나\n⦁ 우유";
                    case "점심": return "⦁ 부대찌개\n⦁ 단무지\n⦁ 콩자반\n⦁ 밥, 김치";
                    case "저녁": return "⦁ 잔치국수\n⦁ 김치전\n⦁ 식혜";
                }
                break;
        }
        return getDefaultMenu(밥);
    }

    private String getDefaultMenu(String meal) {
        switch (밥) {
            case "아침": return "⦁ 북어국\n⦁ 계란말이\n⦁ 밥, 김치";
            case "점심": return "⦁ 김치찌개\n⦁ 제육볶음\n⦁ 계란찜\n⦁ 밥, 김치";
            case "저녁": return "⦁ 된장국\n⦁ 생선구이\n⦁ 나물\n⦁ 밥, 김치";
        }
        return "";
    }
}

package com.example.campuscat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CafeteriaFragment extends Fragment {

    private TextView textContent, textTitle;
    private Button btnBreakfast, btnLunch, btnDinner;
    private String selectedDate;

    public CafeteriaFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cafeteria_layout, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
            if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
            setHasOptionsMenu(true); // 옵션 메뉴에 뒤로가기 표시 허용
        }

        textTitle = view.findViewById(R.id.textTitle);
        textContent = view.findViewById(R.id.textContent);
        btnBreakfast = view.findViewById(R.id.btnBreakfast);
        btnLunch = view.findViewById(R.id.btnLunch);
        btnDinner = view.findViewById(R.id.btnDinner);

        Bundle args = getArguments();
        if (args != null) {
            selectedDate = args.getString("selectedDate");
        }

        if (selectedDate == null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
            selectedDate = sdf.format(new Date());
        }

        if (args == null || args.getString("selectedDate") == null) {
            textTitle.setText("오늘의 학식");
        } else {
            textTitle.setText(selectedDate + " 학식");
        }

        btnBreakfast.setOnClickListener(v -> textContent.setText(getMenuForDate(selectedDate, "아침")));
        btnLunch.setOnClickListener(v -> textContent.setText(getMenuForDate(selectedDate, "점심")));
        btnDinner.setOnClickListener(v -> textContent.setText(getMenuForDate(selectedDate, "저녁")));

        FloatingActionButton fabCalendar = view.findViewById(R.id.fabCalendar);
        fabCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CalendarActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            requireActivity().getSupportFragmentManager().popBackStack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getMenuForDate(String date, String meal) {
        if (date == null) return getDefaultMenu(meal); // '밥' 대신 'meal' 사용

        switch (date) {
            case "2025-6-9":
                switch (meal) { // '밥' 대신 'meal' 사용
                    case "아침": return "\u29D1 삶은 계란\n\u29D1 식빵\n\u29D1 우유";
                    case "점심": return "\u29D1 된장찌개\n\u29D1 고등어구이\n\u29D1 밥, 김치";
                    case "저녁": return "\u29D1 라면\n\u29D1 김밥\n\u29D1 단무지";
                }
                break;
            case "2025-7-3":
                switch (meal) { // '밥' 대신 'meal' 사용
                    case "아침": return "\u29D1 토스트\n\u29D1 우유\n\u29D1 삶은 계란";
                    case "점심": return "\u29D1 돈까스\n\u29D1 미역국\n\u29D1 샐러드\n\u29D1 밥, 김치";
                    case "저녁": return "\u29D1 카레라이스\n\u29D1 단무지\n\u29D1 요구르트";
                }
                break;
            case "2025-7-4":
                switch (meal) { // '밥' 대신 'meal' 사용
                    case "아침": return "\u29D1 북어국\n\u29D1 멸치볶음\n\u29D1 밥, 김치";
                    case "점심": return "\u29D1 김치찌개\n\u29D1 오징어볶음\n\u29D1 계란말이\n\u29D1 밥, 김치";
                    case "저녁": return "\u29D1 비빔밥\n\u29D1 미소된장국\n\u29D1 오렌지";
                }
                break;
            case "2025-7-5":
                switch (meal) { // '밥' 대신 'meal' 사용
                    case "아침": return "\u29D1 씨리얼\n\u29D1 바나나\n\u29D1 우유";
                    case "점심": return "\u29D1 부대찌개\n\u29D1 단무지\n\u29D1 콩자반\n\u29D1 밥, 김치";
                    case "저녁": return "\u29D1 잔치국수\n\u29D1 김치전\n\u29D1 식혜";
                }
                break;
        }
        return getDefaultMenu(meal); // '밥' 대신 'meal' 사용
    }

    private String getDefaultMenu(String meal) {
        switch (meal) { // '밥' 대신 'meal' 사용
            case "아침": return "\u29D1 북어국\n\u29D1 계란말이\n\u29D1 밥, 김치";
            case "점심": return "\u29D1 김치찌개\n\u29D1 제육볶음\n\u29D1 계란찜\n\u29D1 밥, 김치";
            case "저녁": return "\u29D1 된장국\n\u29D1 생선구이\n\u29D1 나물\n\u29D1 밥, 김치";
        }
        return "";
    }
}
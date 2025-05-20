package com.example.campuscat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private TextView textGuide;
    private LinearLayout layoutMemoSection;
    private TextView textSelectedDate;
    private EditText editMemo;

    private String selectedDateText = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        textGuide = view.findViewById(R.id.textGuide);
        layoutMemoSection = view.findViewById(R.id.layoutMemoSection);
        textSelectedDate = view.findViewById(R.id.textSelectedDate);
        editMemo = view.findViewById(R.id.editMemo);

        layoutMemoSection.setVisibility(View.GONE);
        textGuide.setVisibility(View.VISIBLE);

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            selectedDateText = year + "년 " + (month + 1) + "월 " + dayOfMonth + "일";

            textGuide.setVisibility(View.GONE);
            layoutMemoSection.setVisibility(View.VISIBLE);

            textSelectedDate.setText(selectedDateText);
            editMemo.setText("");
        });

        return view;
    }
}

package com.example.campuscat;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;
import java.util.Random;

public class TimetableFragment extends Fragment {

    private LinearLayout layoutEmpty;
    private ScrollView layoutContent;
    private LinearLayout layoutTimeLabels, layoutDayLabels, layoutTimetableList;
    private FrameLayout timetableGrid;
    private Button btnAddFirst, btnAddMore;

    private static final String[] COLORS = {
            "#FFB6B6", "#FFE275", "#A0E7E5", "#B5EAD7", "#C7CEEA", "#FFDAC1"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_timetable, container, false);

        // View 연결
        layoutEmpty = view.findViewById(R.id.layout_empty);
        layoutContent = view.findViewById(R.id.layout_content);
        layoutTimeLabels = view.findViewById(R.id.layout_time_labels);
        layoutDayLabels = view.findViewById(R.id.layout_day_labels);
        layoutTimetableList = view.findViewById(R.id.layoutTimetableList);
        timetableGrid = view.findViewById(R.id.timetable_grid);
        btnAddFirst = view.findViewById(R.id.btn_add_first);
        btnAddMore = view.findViewById(R.id.btn_add_more);

        btnAddFirst.setOnClickListener(v -> showAddDialog());
        btnAddMore.setOnClickListener(v -> showAddDialog());

        // 시간 라벨 생성
        for (int hour = 9; hour <= 18; hour++) {
            TextView time = new TextView(getContext());
            time.setText(String.format(Locale.KOREA, "%02d:00", hour));
            time.setHeight(100);
            time.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            layoutTimeLabels.addView(time);
        }

        // 요일 라벨 생성
        String[] days = {"월", "화", "수", "목", "금"};
        for (String d : days) {
            TextView day = new TextView(getContext());
            day.setText(d);
            day.setGravity(Gravity.CENTER);
            day.setTextSize(14);
            day.setTextColor(Color.parseColor("#1A274D"));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            day.setLayoutParams(params);
            layoutDayLabels.addView(day);
        }

        return view;
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_timetable, null);
        builder.setView(dialogView);
        builder.setTitle("시간표 항목 추가");

        EditText editSubject = dialogView.findViewById(R.id.edit_subject);
        EditText editDay = dialogView.findViewById(R.id.edit_day);
        EditText editStartTime = dialogView.findViewById(R.id.edit_start_time);
        EditText editEndTime = dialogView.findViewById(R.id.edit_end_time);

        builder.setPositiveButton("추가", (dialog, which) -> {
            String subject = editSubject.getText().toString().trim();
            String day = editDay.getText().toString().trim();
            String start = editStartTime.getText().toString().trim();
            String end = editEndTime.getText().toString().trim();

            // 요일 전처리
            if (day.length() >= 1) day = day.substring(0, 1);

            if (!subject.isEmpty() && !day.isEmpty() && start.contains(":") && end.contains(":")) {
                String time = start + " ~ " + end;
                TimetableItem item = new TimetableItem(subject, day, time);
                addTimetableItem(item);
            } else {
                Toast.makeText(getContext(), "모든 항목을 정확히 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("취소", null);
        builder.show();
    }

    private void addTimetableItem(TimetableItem item) {
        if (layoutEmpty.getVisibility() == View.VISIBLE) {
            layoutEmpty.setVisibility(View.GONE);
            layoutContent.setVisibility(View.VISIBLE);
        }

        // 셀 생성
        TextView cell = new TextView(getContext());
        cell.setText(Html.fromHtml("<b>" + item.getSubject() + "</b><br><small>" + item.getTime() + "</small>"));
        cell.setTextColor(Color.BLACK);
        cell.setGravity(Gravity.CENTER);
        cell.setPadding(12, 8, 12, 8);

        int colorIndex = new Random().nextInt(COLORS.length);
        cell.setBackgroundColor(Color.parseColor(COLORS[colorIndex]));

        float startTime = parseTimeFloat(item.getTime().split("~")[0]); // 09:00 → 9.0
        float endTime = parseTimeFloat(item.getTime().split("~")[1]);   // 10:30 → 10.5

        int top = (int) ((startTime - 9f) * 100);
        int height = (int) ((endTime - startTime) * 100);
        int left = dayToIndex(item.getDay()) * 80;

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(80, height);
        params.leftMargin = left;
        params.topMargin = top;
        cell.setLayoutParams(params);

        timetableGrid.addView(cell);

        // 카드 리스트에 추가
        View cardView = LayoutInflater.from(getContext()).inflate(R.layout.item_timetable, layoutTimetableList, false);
        TextView text = cardView.findViewById(R.id.text_timetable_item);
        Button btnDelete = cardView.findViewById(R.id.btn_delete_timetable_item);

        text.setText(item.getSubject() + " / " + item.getDay() + "요일 / " + item.getTime());

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("삭제 확인")
                    .setMessage("해당 강의를 삭제하시겠습니까?")
                    .setPositiveButton("삭제", (dialog, which) -> {
                        layoutTimetableList.removeView(cardView);
                        timetableGrid.removeView(cell);
                        if (layoutTimetableList.getChildCount() == 0) {
                            layoutContent.setVisibility(View.GONE);
                            layoutEmpty.setVisibility(View.VISIBLE);
                        }
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });

        layoutTimetableList.addView(cardView);
    }

    private int dayToIndex(String day) {
        switch (day) {
            case "월": return 0;
            case "화": return 1;
            case "수": return 2;
            case "목": return 3;
            case "금": return 4;
            default: return 0;
        }
    }

    private float parseTimeFloat(String time) {
        try {
            String[] parts = time.trim().split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            return hour + (minute / 60f);
        } catch (Exception e) {
            return 9f;
        }
    }
}

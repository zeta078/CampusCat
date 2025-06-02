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

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class TimetableFragment extends Fragment {

    private LinearLayout layoutEmpty;
    private ScrollView layoutContent;
    private LinearLayout layoutTimeLabels, layoutDayLabels, layoutTimetableList;
    private FrameLayout timetableGrid;
    private Button btnAddFirst, btnAddMore;

    // 랜덤 색상 팔레트 (원하는 색으로 바꿔도 됩니다)
    private static final String[] COLORS = {
            "#FFB6B6", "#FFE275", "#A0E7E5", "#B5EAD7", "#C7CEEA", "#FFDAC1"
    };

    // 실제 데이터 저장(추가된 시간표 항목들)
    private ArrayList<TimetableItem> timetableItems = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_timetable, container, false);

        // 1) XML에서 뷰 연결 (아이디가 반드시 XML과 일치해야 함)
        layoutEmpty = view.findViewById(R.id.layout_empty);
        layoutContent = view.findViewById(R.id.layout_content);
        layoutTimeLabels = view.findViewById(R.id.layout_time_labels);
        layoutDayLabels = view.findViewById(R.id.layout_day_labels);
        layoutTimetableList = view.findViewById(R.id.layout_timetable_list);
        timetableGrid = view.findViewById(R.id.timetable_grid);
        btnAddFirst = view.findViewById(R.id.btn_add_first);
        btnAddMore = view.findViewById(R.id.btn_add_more);

        // 2) “추가하기” 버튼 클릭 시 다이얼로그 띄우기
        btnAddFirst.setOnClickListener(v -> showAddDialog());
        btnAddMore.setOnClickListener(v -> showAddDialog());

        // 3) 초기 상태: 화면에는 “시간표 없음”만 보여줌
        layoutEmpty.setVisibility(View.VISIBLE);
        layoutContent.setVisibility(View.GONE);

        // 4) 시간 라벨을 30분 단위로 생성 (09:00~18:00)
        //    총 18칸(30분씩), 각 칸 높이는 50dp
        for (float t = 9.0f; t <= 18.0f; t += 0.5f) {
            int hour = (int) t;
            int minute = (t % 1.0f == 0.5f) ? 30 : 0;
            String label = String.format(Locale.KOREA, "%02d:%02d", hour, minute);

            // 4-1) 시간 텍스트 뷰
            TextView tv = new TextView(getContext());
            tv.setText(label);
            tv.setHeight(dpToPx(50));
            tv.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            tv.setTextColor(Color.parseColor("#888888"));
            tv.setTextSize(13);
            layoutTimeLabels.addView(tv);

            // 4-2) 30분마다 가로줄 (하얀 배경 위에 희미한 회색 선)
            View line = new View(getContext());
            LinearLayout.LayoutParams lpLine = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(1)
            );
            line.setLayoutParams(lpLine);
            line.setBackgroundColor(Color.parseColor("#DDDDDD"));
            layoutTimeLabels.addView(line);
        }

        // 5) 요일 라벨 (월~금, 총 5개) 생성
        String[] days = {"월", "화", "수", "목", "금"};
        for (String d : days) {
            TextView day = new TextView(getContext());
            day.setText(d);
            day.setGravity(Gravity.CENTER);
            day.setTextSize(18);
            day.setTextColor(Color.parseColor("#1A274D"));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            day.setLayoutParams(params);
            layoutDayLabels.addView(day);
        }

        return view;
    }

    // 다이얼로그를 띄우고 사용자가 입력하면 새 TimetableItem을 생성하여
    // 그래프 + 카드뷰 리스트에 반영해 준다
    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_timetable, null);
        builder.setView(dialogView);
        builder.setTitle("시간표 항목 추가");

        EditText editSubject = dialogView.findViewById(R.id.edit_subject);
        EditText editPlace   = dialogView.findViewById(R.id.edit_place);
        EditText editDay     = dialogView.findViewById(R.id.edit_day);
        EditText editStart   = dialogView.findViewById(R.id.edit_start_time);
        EditText editEnd     = dialogView.findViewById(R.id.edit_end_time);

        builder.setPositiveButton("추가", (dialog, which) -> {
            String subject = editSubject.getText().toString().trim();
            String place   = editPlace.getText().toString().trim();
            String dayTxt  = editDay.getText().toString().trim();
            String start   = editStart.getText().toString().trim();
            String end     = editEnd.getText().toString().trim();

            // “월요일” → “월”만 남기기(첫 글자만)
            if (dayTxt.length() >= 1) {
                dayTxt = dayTxt.substring(0, 1);
            }

            // 빈 칸 있으면 경고
            if (subject.isEmpty() || dayTxt.isEmpty() || start.isEmpty() || end.isEmpty()) {
                Toast.makeText(getContext(), "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // TimetableItem 객체 생성 후 추가
            TimetableItem item = new TimetableItem(subject, place, dayTxt, start, end);
            timetableItems.add(item);
            addTimetableItem(item);
        });

        builder.setNegativeButton("취소", null);
        builder.show();
    }

    // 실제로 “그래프(프레임레이아웃)” 영역에 블록을 그리고,
    // 동시에 “카드뷰 리스트”에도 아이템을 추가한다
    private void addTimetableItem(TimetableItem item) {
        // 1) 초기에 아무 항목도 없었다면 레이아웃 전환
        if (layoutEmpty.getVisibility() == View.VISIBLE) {
            layoutEmpty.setVisibility(View.GONE);
            layoutContent.setVisibility(View.VISIBLE);
        }

        // 2) 그래프 위에 표시할 블록(TextView) 생성
        TextView cell = new TextView(getContext());
        cell.setText(Html.fromHtml("<b>" + item.getSubject() + "</b><br><small>" + item.getPlace() + "</small>"));
        cell.setTextColor(Color.WHITE);
        cell.setGravity(Gravity.CENTER);
        cell.setPadding(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6));
        cell.setTextSize(15);

        // 배경 색상을 팔레트에서 랜덤 선택
        int colorIndex = new Random().nextInt(COLORS.length);
        cell.setBackgroundColor(Color.parseColor(COLORS[colorIndex]));
        cell.setAlpha(0.9f);

        // 3) “시작시간”/“종료시간”을 float으로 파싱 (예: "09:00"→9.0f, "10:30"→10.5f)
        float startF = parseTimeFloat(item.getStartTime());
        float endF   = parseTimeFloat(item.getEndTime());

        // 4) 30분 단위로 “칸 높이” 계산 (1칸 = 30분 = 50dp)
        //    (그래프 높이: 총 18칸(09:00~18:00) → 18 * 50dp = 900dp)
        int rowHeightPx = dpToPx(50);    // 30분 = 50dp
        int startRow = (int) ((startF - 9.0f) * 2f); // ex: 9.0→0, 9.5→1, 10.0→2
        int endRow   = (int) ((endF   - 9.0f) * 2f); // ex: 10.5→3

        if (startRow < 0) startRow = 0;
        if (endRow > 18) endRow = 18;    // 18칸을 넘어가면 최대치로 제한

        int topPx    = startRow * rowHeightPx;
        int heightPx = (endRow - startRow) * rowHeightPx;
        if (heightPx < rowHeightPx) heightPx = rowHeightPx; // 최소 1칸은 차지

        // 5) “요일” → 컬럼 계산 (월:0, 화:1, 수:2, 목:3, 금:4)
        int colIdx = dayToIndex(item.getDay());
        int totalWidth = timetableGrid.getWidth() > 0 ? timetableGrid.getWidth() : getResources().getDisplayMetrics().widthPixels;
        int colWidth   = totalWidth / 5; // 화면 폭을 5개로 나눔
        int leftPx     = colIdx * colWidth;

        // 6) FrameLayout.LayoutParams로 위치/크기 부여
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(colWidth, heightPx);
        params.leftMargin = leftPx;
        params.topMargin  = topPx;
        cell.setLayoutParams(params);

        // 7) 그래프 영역에 블록 추가
        timetableGrid.addView(cell);

        // 8) “카드뷰 리스트”에도 동일한 항목 추가
        View cardView = LayoutInflater.from(getContext()).inflate(R.layout.item_timetable, layoutTimetableList, false);
        TextView tvText = cardView.findViewById(R.id.text_timetable_item);
        Button  btnDel  = cardView.findViewById(R.id.btn_delete_timetable_item);

        // 카드뷰 텍스트: “과목명 / 요일요일 / 시작~종료 / 장소”
        tvText.setText(item.getSubject() + " / "
                + item.getDay() + "요일 / "
                + item.getStartTime() + " ~ "
                + item.getEndTime()
                + (item.getPlace().isEmpty() ? "" : " / " + item.getPlace()));

        // 삭제 버튼 클릭 시 “그래프 블록” + “카드뷰” 두 개 모두 삭제
        btnDel.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("삭제 확인")
                    .setMessage("해당 강의를 삭제하시겠습니까?")
                    .setPositiveButton("삭제", (dlg, idx) -> {
                        layoutTimetableList.removeView(cardView);
                        timetableGrid.removeView(cell);
                        timetableItems.remove(item);

                        // 카드뷰가 0개면 다시 “초기화면”으로 전환
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

    // 요일(월~금)을 0~4로 변환
    private int dayToIndex(String day) {
        switch (day) {
            case "월": return 0;
            case "화": return 1;
            case "수": return 2;
            case "목": return 3;
            case "금": return 4;
            default:  return 0;
        }
    }

    // “09:00” 혹은 “9:00” 혹은 “09” 혹은 “9” 등 자유롭게 입력 받아
    // float(시간) 형식으로 반환 (예: "10:30"→10.5f)
    private float parseTimeFloat(String time) {
        try {
            String cleaned = time.replaceAll("[^0-9:]", "");
            String[] parts = cleaned.split(":");
            int hour   = Integer.parseInt(parts[0]);
            int minute = (parts.length > 1) ? Integer.parseInt(parts[1]) : 0;
            return hour + (minute / 60f);
        } catch (Exception e) {
            return 9.0f; // 파싱 실패 시 기본 9시
        }
    }

    // dp 단위를 pixel로 변환
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}

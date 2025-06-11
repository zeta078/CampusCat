package com.example.campuscat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;


public class CalendarFragment extends Fragment {
    public CalendarFragment() {
        super(R.layout.fragment_calendar);
    }


    private CalendarView calendarView;
    private TextView textSelectedDate, textSavedMemo;
    private EditText editMemo;
    private Button btnSave, btnDelete;
    private String selectedDateKey;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView     = view.findViewById(R.id.calendarView);
        textSelectedDate = view.findViewById(R.id.textSelectedDate);
        textSavedMemo    = view.findViewById(R.id.textSavedMemo);
        editMemo         = view.findViewById(R.id.editMemo);
        btnSave          = view.findViewById(R.id.btnSave);
        btnDelete        = view.findViewById(R.id.btnDelete);

        prefs = requireContext().getSharedPreferences("calendar_memo", Context.MODE_PRIVATE);

        // 1) 오늘 KST 자정 밀리초 계산
        Calendar todayKST = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));
        todayKST.set(Calendar.HOUR_OF_DAY, 0);
        todayKST.set(Calendar.MINUTE, 0);
        todayKST.set(Calendar.SECOND, 0);
        todayKST.set(Calendar.MILLISECOND, 0);
        long todayMillisKST = todayKST.getTimeInMillis();

        // 2) CalendarView에 오늘(KST 자정) 날짜를 설정
        calendarView.setDate(todayMillisKST, /* animate= */ false, /* center= */ true);

        // 3) 화면 업데이트 (오늘 날짜 기반)
        updateUIWithKSTDate(todayMillisKST);

        // 4) 사용자가 달력에서 날짜를 선택했을 때
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            // month는 0-based
            Calendar selectedKST = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));
            selectedKST.set(year, month, dayOfMonth, 0, 0, 0);
            selectedKST.set(Calendar.MILLISECOND, 0);
            long selectedMillisKST = selectedKST.getTimeInMillis();
            updateUIWithKSTDate(selectedMillisKST);
        });

        btnSave.setOnClickListener(v -> {
            String memo = editMemo.getText().toString().trim();
            if (!memo.isEmpty()) {
                prefs.edit().putString(selectedDateKey, memo).apply();
                Toast.makeText(getContext(), "메모가 저장되었습니다", Toast.LENGTH_SHORT).show();
                updateSavedMemoView(memo);
                editMemo.setText("");
            }
        });

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("메모 삭제")
                    .setMessage("정말로 이 메모를 삭제하시겠습니까?")
                    .setPositiveButton("삭제", (dialog, which) -> {
                        prefs.edit().remove(selectedDateKey).apply();
                        Toast.makeText(getContext(), "메모가 삭제되었습니다", Toast.LENGTH_SHORT).show();
                        textSavedMemo.setVisibility(View.GONE);
                        btnDelete.setVisibility(View.GONE);
                        editMemo.setText("");
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });

        return view;
    }

    /**
     * KST 자정 기준 밀리초(kstMillis)를 받아서
     * 1) selectedDateKey("yyyyMMdd") 생성
     * 2) “선택된 날짜” 텍스트 표시
     * 3) editMemo 힌트 갱신
     * 4) SharedPreferences에서 저장된 메모 불러와서 보여주기
     */
    private void updateUIWithKSTDate(long kstMillis) {
        // 1) key 생성: "yyyyMMdd"
        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyyMMdd", Locale.KOREA);
        keyFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        selectedDateKey = keyFormat.format(kstMillis);

        // 2) 화면용 날짜 문자열: "yyyy년 M월 d일"
        SimpleDateFormat viewFormat = new SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA);
        viewFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String viewDate = viewFormat.format(kstMillis);

        textSelectedDate.setText("선택된 날짜: " + viewDate);
        editMemo.setHint(viewDate + "에 일정 추가");

        // 3) 저장된 메모 불러오기
        String saved = prefs.getString(selectedDateKey, null);
        if (saved != null) {
            updateSavedMemoView(saved);
        } else {
            textSavedMemo.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            editMemo.setText("");
        }
    }

    private void updateSavedMemoView(String memo) {
        textSavedMemo.setText(memo);
        textSavedMemo.setVisibility(View.VISIBLE);
        btnDelete.setVisibility(View.VISIBLE);
    }
}


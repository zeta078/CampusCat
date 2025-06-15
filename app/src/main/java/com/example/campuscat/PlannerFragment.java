package com.example.campuscat;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PlannerFragment extends Fragment {

    private TableLayout tableLayoutSubjects;
    private Button buttonAddSubject;
    private Button buttonRemoveSubject; // 새로운 삭제 버튼 변수
    private TextView textToday;

    public PlannerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.planner_layout, container, false);

        // UI 요소 초기화
        tableLayoutSubjects = view.findViewById(R.id.tableLayoutSubjects);
        buttonAddSubject = view.findViewById(R.id.buttonAddSubject);
        buttonRemoveSubject = view.findViewById(R.id.buttonRemoveSubject);
        textToday = view.findViewById(R.id.textToday);

        // 앱 시작 시 빈 과목 줄 하나를 먼저 추가
        if (tableLayoutSubjects.getChildCount() == 1) { // 헤더 행(인덱스 0)만 있을 경우
            addSubjectRow();
        }

        // + 버튼 클릭 리스너 설정
        buttonAddSubject.setOnClickListener(v -> addSubjectRow());

        // - 버튼 클릭 리스너 설정
        buttonRemoveSubject.setOnClickListener(v -> {
            // 헤더 행 (인덱스 0)을 제외하고 최소한 1개의 과목 행이 있는지 확인
            if (tableLayoutSubjects.getChildCount() > 1) {
                // 가장 마지막 과목 행 제거 (헤더 다음 행이 인덱스 1이므로, count-1이 마지막 행)
                tableLayoutSubjects.removeViewAt(tableLayoutSubjects.getChildCount() - 1);
                Toast.makeText(requireContext(), "가장 최근 과목이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "더 이상 삭제할 과목이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });


        // 저장 버튼 처리
        Button buttonSave = view.findViewById(R.id.buttonSave);
        if (buttonSave != null) {
            buttonSave.setOnClickListener(v -> {
                for (int i = 1; i < tableLayoutSubjects.getChildCount(); i++) {
                    TableRow row = (TableRow) tableLayoutSubjects.getChildAt(i);

                    EditText subjectEt = (EditText) row.getChildAt(0);
                    Spinner goalSpinner = (Spinner) row.getChildAt(1);
                    TextView studyTimeTv = (TextView) row.getChildAt(2);
                    // 성취도 및 개별 삭제 버튼은 더 이상 참조하지 않음

                    Log.d("PlannerData", "과목: " + subjectEt.getText().toString() +
                            ", 목표시간: " + goalSpinner.getSelectedItem().toString() +
                            ", 공부시간: " + studyTimeTv.getText().toString());
                }
                Log.d("Planner", "저장 버튼 클릭됨.");
                Toast.makeText(requireContext(), "플래너가 저장되었습니다!", Toast.LENGTH_SHORT).show();
            });
        }

        return view;
    }


    private void addSubjectRow() {
        // 새로운 TableRow 생성
        TableRow newRow = new TableRow(requireContext());
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        newRow.setLayoutParams(rowParams);

        // 1. 과목 EditText 추가 (과목 칸을 길게)
        EditText newSubjectEditText = new EditText(requireContext());
        // 과목 칸의 가중치를 3.0f로 설정하여 가장 많은 공간을 차지하도록 합니다.
        TableRow.LayoutParams etParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 4.0f);
        etParams.setMargins(dpToPx(0), dpToPx(8), dpToPx(8), dpToPx(8));
        newSubjectEditText.setLayoutParams(etParams);
        newSubjectEditText.setHint("예: 모프");
        newSubjectEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        newSubjectEditText.setSingleLine(true);
        newSubjectEditText.setPadding(dpToPx(0), dpToPx(8), dpToPx(8), dpToPx(8));
        newSubjectEditText.setBackgroundResource(android.R.drawable.edit_text);

        // 2. 목표 시간 Spinner 추가
        Spinner newGoalTimeSpinner = new Spinner(requireContext());
        // 목표 시간 칸의 가중치를 1.0f로 설정 (과목보다 짧게)
        TableRow.LayoutParams spinnerParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f);
        spinnerParams.setMargins(dpToPx(4), dpToPx(8), dpToPx(0), dpToPx(8));
        newGoalTimeSpinner.setLayoutParams(spinnerParams);
        newGoalTimeSpinner.setPadding(dpToPx(4), dpToPx(8), dpToPx(0), dpToPx(8));
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.plannertime_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        newGoalTimeSpinner.setAdapter(adapter);


        // 새 Row에 뷰들 추가 (삭제 버튼 없음)
        newRow.addView(newSubjectEditText);
        newRow.addView(newGoalTimeSpinner);

        // TableLayout에 새 Row 추가
        tableLayoutSubjects.addView(newRow);

        newSubjectEditText.requestFocus();
    }

    // dp 값을 px 값으로 변환하는 헬퍼 메서드
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}
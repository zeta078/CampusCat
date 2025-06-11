package com.example.campuscat;

import android.os.Bundle;
<<<<<<< HEAD
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PlannerFragment extends Fragment {
    public PlannerFragment() {
        super(R.layout.fragment_planner);
=======
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PlannerFragment extends Fragment {

    private TableLayout tableLayoutSubjects;
    private Button buttonAddSubject; // 새로 추가된 버튼 참조

    public PlannerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.planner_layout, container, false);

        // UI 요소 초기화
        tableLayoutSubjects = view.findViewById(R.id.tableLayoutSubjects);
        buttonAddSubject = view.findViewById(R.id.buttonAddSubject); // + 버튼 참조

        // 앱 시작 시 빈 과목 줄 하나를 먼저 추가
        addSubjectRow();

        // + 버튼 클릭 리스너 설정
        buttonAddSubject.setOnClickListener(v -> addSubjectRow());

        // 저장 버튼 처리 (기존 코드와 동일)
        Button buttonSave = view.findViewById(R.id.buttonSave);
        if (buttonSave != null) {
            buttonSave.setOnClickListener(v -> {
                // 이 부분에서 tableLayoutSubjects에 있는 모든 과목 줄의 데이터를 수집해야 합니다.
                // 첫 번째 행(헤더)을 제외하고 반복합니다.
                for (int i = 1; i < tableLayoutSubjects.getChildCount(); i++) {
                    TableRow row = (TableRow) tableLayoutSubjects.getChildAt(i);

                    // 각 뷰가 null이 아닌지 확인 후 접근
                    EditText subjectEt = (EditText) row.getChildAt(0);
                    Spinner goalSpinner = (Spinner) row.getChildAt(1);
                    TextView studyTimeTv = (TextView) row.getChildAt(2);
                    TextView achievementTv = (TextView) row.getChildAt(3);

                    Log.d("PlannerData", "과목: " + subjectEt.getText().toString() +
                            ", 목표시간: " + goalSpinner.getSelectedItem().toString() +
                            ", 공부시간: " + studyTimeTv.getText().toString() +
                            ", 성취도: " + achievementTv.getText().toString());
                }
                Log.d("Planner", "저장 버튼 클릭됨.");
            });
        }

        return view;
    }

    /**
     * 새로운 과목 입력 줄을 동적으로 추가하는 메서드
     */
    private void addSubjectRow() {
        // 새로운 TableRow 생성
        TableRow newRow = new TableRow(requireContext());
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        newRow.setLayoutParams(rowParams);
        // 필요하다면 배경색 설정
        // newRow.setBackgroundColor(getResources().getColor(android.R.color.white));

        // 1. 과목 EditText 추가
        EditText newSubjectEditText = new EditText(requireContext());
        TableRow.LayoutParams etParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f);
        etParams.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        newSubjectEditText.setLayoutParams(etParams);
        newSubjectEditText.setHint("예: 모프");
        newSubjectEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        newSubjectEditText.setSingleLine(true);
        newSubjectEditText.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        newSubjectEditText.setBackgroundResource(android.R.drawable.edit_text);
        // TextWatcher는 이제 여기에 추가하지 않습니다.

        // 2. 목표 시간 Spinner 추가
        Spinner newGoalTimeSpinner = new Spinner(requireContext());
        TableRow.LayoutParams spinnerParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f);
        spinnerParams.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        newGoalTimeSpinner.setLayoutParams(spinnerParams);
        newGoalTimeSpinner.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.plannertime_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        newGoalTimeSpinner.setAdapter(adapter);

        // 3. 공부 시간 TextView 추가 (필요하면 EditText로 변경)
        TextView newStudyTimeTextView = new TextView(requireContext());
        TableRow.LayoutParams tvStudyParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f);
        tvStudyParams.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        newStudyTimeTextView.setLayoutParams(tvStudyParams);
        newStudyTimeTextView.setText("0시간");
        newStudyTimeTextView.setGravity(Gravity.CENTER);
        newStudyTimeTextView.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));

        // 4. 성취도 TextView 추가 (필요하면 EditText로 변경)
        TextView newAchievementTextView = new TextView(requireContext());
        TableRow.LayoutParams tvAchieveParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f);
        tvAchieveParams.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        newAchievementTextView.setLayoutParams(tvAchieveParams);
        newAchievementTextView.setText("0%");
        newAchievementTextView.setGravity(Gravity.CENTER);
        newAchievementTextView.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));

        // 새 Row에 뷰들 추가
        newRow.addView(newSubjectEditText);
        newRow.addView(newGoalTimeSpinner);
        newRow.addView(newStudyTimeTextView);
        newRow.addView(newAchievementTextView);

        // TableLayout에 새 Row 추가
        tableLayoutSubjects.addView(newRow);

        // 새롭게 추가된 EditText에 자동으로 포커스 이동 (선택 사항)
        newSubjectEditText.requestFocus();
    }

    // dp 값을 px 값으로 변환하는 헬퍼 메서드
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
>>>>>>> origin/feature/planner
    }
}
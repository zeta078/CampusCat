package com.example.campuscat;

import android.os.Bundle;
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
    private Button buttonAddSubject;

    public PlannerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.planner_layout, container, false);

        tableLayoutSubjects = view.findViewById(R.id.tableLayoutSubjects);
        buttonAddSubject = view.findViewById(R.id.buttonAddSubject);

        addSubjectRow();

        buttonAddSubject.setOnClickListener(v -> addSubjectRow());

        Button buttonSave = view.findViewById(R.id.buttonSave);
        if (buttonSave != null) {
            buttonSave.setOnClickListener(v -> {
                for (int i = 1; i < tableLayoutSubjects.getChildCount(); i++) {
                    TableRow row = (TableRow) tableLayoutSubjects.getChildAt(i);

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

    private void addSubjectRow() {
        TableRow newRow = new TableRow(requireContext());
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        newRow.setLayoutParams(rowParams);

        EditText newSubjectEditText = new EditText(requireContext());
        TableRow.LayoutParams etParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f);
        etParams.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        newSubjectEditText.setLayoutParams(etParams);
        newSubjectEditText.setHint("예: 모프");
        newSubjectEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        newSubjectEditText.setSingleLine(true);
        newSubjectEditText.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        newSubjectEditText.setBackgroundResource(android.R.drawable.edit_text);

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

        TextView newStudyTimeTextView = new TextView(requireContext());
        TableRow.LayoutParams tvStudyParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f);
        tvStudyParams.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        newStudyTimeTextView.setLayoutParams(tvStudyParams);
        newStudyTimeTextView.setText("0시간");
        newStudyTimeTextView.setGravity(Gravity.CENTER);
        newStudyTimeTextView.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));

        TextView newAchievementTextView = new TextView(requireContext());
        TableRow.LayoutParams tvAchieveParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f);
        tvAchieveParams.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        newAchievementTextView.setLayoutParams(tvAchieveParams);
        newAchievementTextView.setText("0%");
        newAchievementTextView.setGravity(Gravity.CENTER);
        newAchievementTextView.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));

        newRow.addView(newSubjectEditText);
        newRow.addView(newGoalTimeSpinner);
        newRow.addView(newStudyTimeTextView);
        newRow.addView(newAchievementTextView);

        tableLayoutSubjects.addView(newRow);
        newSubjectEditText.requestFocus();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}

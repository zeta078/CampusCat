package com.example.campuscat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PlannerFragment extends Fragment {

    private TableLayout tableLayoutSubjects;
    private Button buttonAddSubject;
    private Button buttonRemoveSubject;
    private TextView textToday;
    private TextView textTotalGoalTime; // **** 총 목표 시간 TextView (새로 추가) ****
    private TextView textTotalTime; // **** 기존 ID 그대로, 실제 학습 시간 TextView로 사용 ****
    private EditText editPlan;
    private Button buttonSave;

    private ProgressBar totalStudyProgress; // **** ProgressBar 추가 ****
    private TextView totalStudyPercentage; // **** 성취도 퍼센트 TextView 추가 ****


    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "PlannerData";
    private static final String KEY_PLANNER_DATE = "planner_date";
    private static final String KEY_PLANNER_DATA = "planner_data";
    private static final String KEY_STUDY_PLAN = "study_plan";
    private static final String KEY_IS_FROZEN = "is_frozen";
    private static final String KEY_ACTUAL_STUDY_TIME = "actual_study_time"; // 실제 학습 시간 저장 키 추가

    private int currentStudyTimeInHours = 0; // 실제 학습 시간 (시간 단위)

    private int calculatedExperience = 0;
    private static final String CHANNEL_ID = "planner_notification_channel";
    private static final int NOTIFICATION_ID = 1001;


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
        textTotalGoalTime = view.findViewById(R.id.textTotalGoalTime); // **** 새로운 TextView 초기화 ****
        textTotalTime = view.findViewById(R.id.textTotalTime); // **** 기존 ID 그대로 사용, 실제 학습 시간 용도 ****
        editPlan = view.findViewById(R.id.editPlan);
        buttonSave = view.findViewById(R.id.buttonSave);
        totalStudyProgress = view.findViewById(R.id.totalStudyProgress); // **** ProgressBar 초기화 ****
        totalStudyPercentage = view.findViewById(R.id.totalStudyPercentage); // **** 성취도 퍼센트 TextView 초기화 ****

        // SharedPreferences 초기화
        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // 중요: 날짜 확인 및 초기화 로직은 가장 먼저 실행되어 현재 날짜의 상태를 확정해야 함
        displayTodayDate();
        checkAndInitializePlanner(); // 이 시점에서 KEY_IS_FROZEN이 오늘 날짜에 맞게 설정됨

        // 데이터 로드
        loadPlannerData();

        // + 버튼 클릭 리스너 설정
        buttonAddSubject.setOnClickListener(v -> {
            int currentTotalGoalHours = calculateCurrentTotalGoalHours();

            if (tableLayoutSubjects.getChildCount() - 1 < 3) { // 헤더 행 제외 최대 3개 과목
                if (currentTotalGoalHours < 6) { // 현재 총 목표 시간이 6시간 미만일 때만 추가 허용
                    addSubjectRow();
                    updateTotalGoalTime(); // 과목 추가 시 총 목표 시간 UI 업데이트
                    updateAchievement(); // 성취도 업데이트
                } else {
                    Toast.makeText(requireContext(), "총 목표 시간이 6시간이므로 더 이상 과목을 추가할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "최대 추가 가능 과목은 3개입니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // - 버튼 클릭 리스너 설정
        buttonRemoveSubject.setOnClickListener(v -> {
            if (tableLayoutSubjects.getChildCount() > 1) { // 헤더 행 제외 최소 1개 과목
                tableLayoutSubjects.removeViewAt(tableLayoutSubjects.getChildCount() - 1);
                Toast.makeText(requireContext(), "가장 최근 과목이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                updateTotalGoalTime(); // 과목 삭제 시 총 목표 시간 UI 업데이트
                updateAchievement(); // 성취도 업데이트
            } else {
                Toast.makeText(requireContext(), "더 이상 삭제할 과목이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // 저장 버튼 클릭 리스너 설정
        buttonSave.setOnClickListener(v -> {
            savePlannerData();
        });

        // 스피너 값 변경 리스너 설정: 로드된 과목들에 대해 리스너 설정
        for (int i = 1; i < tableLayoutSubjects.getChildCount(); i++) {
            TableRow row = (TableRow) tableLayoutSubjects.getChildAt(i);
            Spinner goalSpinner = (Spinner) row.getChildAt(1);
            setupGoalTimeSpinnerListener(goalSpinner);
        }

        // 초기 총 목표 시간 및 실제 학습 시간 UI 업데이트
        updateTotalGoalTime();
        updateActualStudyTimeUI();
        updateAchievement(); // 초기 성취도 업데이트

        // 모든 UI 요소 설정 후 최종적으로 동결 상태 적용
        applyFrozenState();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 포그라운드로 돌아올 때마다 날짜 확인 및 초기화 로직 다시 실행
        displayTodayDate();
        checkAndInitializePlanner();
        loadPlannerData();
        applyFrozenState();
        updateActualStudyTimeUI(); // 실제 학습 시간 UI 업데이트
        updateAchievement(); // 성취도 업데이트
    }

    private void displayTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = sdf.format(new Date());
        textToday.setText("today : " + todayDate); // 레이아웃에 맞춰 "today : " 추가
    }

    private void checkAndInitializePlanner() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = sdf.format(new Date());
        String savedDate = sharedPreferences.getString(KEY_PLANNER_DATE, "");

        if (!todayDate.equals(savedDate)) {
            // 날짜가 바뀌었으면 플래너 초기화 및 알림
            initializePlannerData(savedDate);
            Toast.makeText(requireContext(), "새로운 날짜입니다. 플래너가 초기화됩니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void schedulePlannerResetAlarm() {
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(requireContext(), PlannerResetReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                Log.d("PlannerFragment", "다음날 00시 플래너 초기화 알람 예약됨 (Exact, Android 12+): " + calendar.getTime());
            } else {
                Intent exactAlarmPermissionIntent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                requireContext().startActivity(exactAlarmPermissionIntent);
                Toast.makeText(requireContext(), "정확한 알람 설정을 위해 권한이 필요합니다. 설정에서 '알람 및 미리 알림' 권한을 허용해주세요.", Toast.LENGTH_LONG).show();
                Log.w("PlannerFragment", "SCHEDULE_EXACT_ALARM 권한이 없어 알람을 예약할 수 없습니다. 사용자에게 요청합니다.");
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Log.d("PlannerFragment", "다음날 00시 플래너 초기화 알람 예약됨 (ExactAndAllowWhileIdle): " + calendar.getTime());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Log.d("PlannerFragment", "다음날 00시 플래너 초기화 알람 예약됨 (Exact): " + calendar.getTime());
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Log.d("PlannerFragment", "다음날 00시 플래너 초기화 알람 예약됨 (Old): " + calendar.getTime());
        }
    }

    private void initializePlannerData(String previousDate) {
        // 이전 날짜의 실제 학습 시간 로드 (경험치 계산을 위해)
        int previousStudyTime = sharedPreferences.getInt(KEY_ACTUAL_STUDY_TIME, 0);
        loadPlannerDataForExperience(previousDate, previousStudyTime);


        sharedPreferences.edit()
                .remove(KEY_PLANNER_DATA)
                .remove(KEY_STUDY_PLAN)
                .remove(KEY_IS_FROZEN)
                .remove(KEY_ACTUAL_STUDY_TIME) // 실제 학습 시간도 초기화
                .apply();

        sharedPreferences.edit().putString(KEY_PLANNER_DATE, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date())).apply();
        setPlannerFrozen(false);

        tableLayoutSubjects.removeViews(1, tableLayoutSubjects.getChildCount() - 1);
        addSubjectRow();
        editPlan.setText("");
        currentStudyTimeInHours = 0; // 실제 학습 시간 변수 초기화
        updateTotalGoalTime(); // 총 목표 시간 UI 초기화
        updateActualStudyTimeUI(); // 실제 학습 시간 UI 초기화
        updateAchievement(); // 성취도 초기화

        schedulePlannerResetAlarm();
    }

    // 경험치 계산을 위해 이전 날짜의 데이터 로드 및 실제 학습 시간 전달
    private void loadPlannerDataForExperience(String dateToLoad, int actualStudyTimeForPrevDate) {
        if (dateToLoad == null || dateToLoad.isEmpty()) {
            calculatedExperience = 0;
            return;
        }

        String plannerJson = sharedPreferences.getString(KEY_PLANNER_DATA, "[]");
        boolean wasFrozen = sharedPreferences.getBoolean(KEY_IS_FROZEN, false);

        if (!wasFrozen) {
            calculatedExperience = 0;
            return;
        }

        int totalGoalHours = 0;
        try {
            JSONArray jsonArray = new JSONArray(plannerJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                totalGoalHours += obj.getInt("goalTime");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("ExperienceCalc", "Total Goal Hours: " + totalGoalHours + ", Actual Study Hours (Previous): " + actualStudyTimeForPrevDate);
        calculateExperience(totalGoalHours, actualStudyTimeForPrevDate); // 이전 날짜의 실제 학습 시간으로 계산
    }


    private void calculateExperience(int totalGoalHours, int actualStudyHours) {
        if (totalGoalHours <= 0) {
            calculatedExperience = 0;
            return;
        }

        double achievementRate = (double) actualStudyHours / totalGoalHours;
        Log.d("ExperienceCalc", "Achievement Rate: " + achievementRate);

        int baseExperience = actualStudyHours * 20;

        if (achievementRate >= 1.0) {
            calculatedExperience = baseExperience * 2;
            Log.d("ExperienceCalc", "100% 달성! 경험치 두 배 지급: " + calculatedExperience);
        } else {
            calculatedExperience = baseExperience;
            Log.d("ExperienceCalc", "경험치 지급: " + calculatedExperience);
        }
    }

    // 외부에서 호출하여 자습 시간 업데이트 및 UI 반영 (StudyFragment에서 호출될 것임)
    public void updateStudyTime(int hours) {
        this.currentStudyTimeInHours = hours;
        Log.d("PlannerFragment", "자습 시간 업데이트: " + hours + "시간");
        // SharedPreferences에 실제 학습 시간 저장
        sharedPreferences.edit().putInt(KEY_ACTUAL_STUDY_TIME, hours).apply();
        updateActualStudyTimeUI(); // 실제 학습 시간 UI 업데이트
        updateAchievement(); // 성취도 업데이트
    }

    // 실제 학습 시간 UI만 업데이트하는 헬퍼 메서드
    private void updateActualStudyTimeUI() {
        textTotalTime.setText(currentStudyTimeInHours + "시간"); // **** textTotalTime에 실제 학습 시간 표시 ****
    }

    // 성취도 업데이트 메서드
    private void updateAchievement() {
        int totalGoalHours = calculateCurrentTotalGoalHours();
        int actualStudyHours = currentStudyTimeInHours; // 현재 프래그먼트의 실제 학습 시간

        int percentage = 0;
        if (totalGoalHours > 0) {
            percentage = (int) ( (double) actualStudyHours / totalGoalHours * 100);
            if (percentage > 100) percentage = 100; // 성취도는 최대 100%
        }

        totalStudyProgress.setProgress(percentage);
        totalStudyPercentage.setText(percentage + "%");
    }

    public int getCalculatedExperience() {
        return calculatedExperience;
    }


    private void addSubjectRow() {
        TableRow newRow = new TableRow(requireContext());
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        newRow.setLayoutParams(rowParams);

        // 과목 EditText
        EditText newSubjectEditText = new EditText(requireContext());
        TableRow.LayoutParams etParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 3.0f); // layout_weight 조정
        newSubjectEditText.setLayoutParams(etParams);
        newSubjectEditText.setHint("과목명");
        newSubjectEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        newSubjectEditText.setSingleLine(true);
        newSubjectEditText.setBackgroundResource(android.R.drawable.edit_text);
        newSubjectEditText.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10)); // padding 조정
        newSubjectEditText.setEnabled(!sharedPreferences.getBoolean(KEY_IS_FROZEN, false));

        // 목표 시간 Spinner
        Spinner newGoalTimeSpinner = new Spinner(requireContext());
        TableRow.LayoutParams spinnerParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f); // layout_weight 조정
        newGoalTimeSpinner.setLayoutParams(spinnerParams);
        newGoalTimeSpinner.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10)); // padding 조정
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.plannertime_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        newGoalTimeSpinner.setAdapter(adapter);
        newGoalTimeSpinner.setEnabled(!sharedPreferences.getBoolean(KEY_IS_FROZEN, false));

        setupGoalTimeSpinnerListener(newGoalTimeSpinner);

        newRow.addView(newSubjectEditText);
        newRow.addView(newGoalTimeSpinner);

        tableLayoutSubjects.addView(newRow);

        newSubjectEditText.requestFocus();
    }

    private int calculateCurrentTotalGoalHours() {
        int totalGoalHours = 0;
        for (int i = 1; i < tableLayoutSubjects.getChildCount(); i++) { // 헤더 행 제외
            TableRow row = (TableRow) tableLayoutSubjects.getChildAt(i);
            Spinner goalSpinner = (Spinner) row.getChildAt(1);
            try {
                String selectedItem = goalSpinner.getSelectedItem().toString();
                totalGoalHours += Integer.parseInt(selectedItem.replaceAll("[^\\d]", ""));
            } catch (NumberFormatException e) {
                Log.e("PlannerFragment", "Spinner item parsing error during current total calculation: " + e.getMessage());
            }
        }
        return totalGoalHours;
    }

    private void setupGoalTimeSpinnerListener(Spinner spinner) {
        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updateTotalGoalTime(); // 스피너 값 변경 시 총 목표 시간 업데이트
                updateAchievement(); // 스피너 값 변경 시 성취도 업데이트
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    // 총 목표 시간 계산 및 UI 업데이트 (textTotalGoalTime에 표시)
    private void updateTotalGoalTime() {
        int totalGoalHours = 0;
        for (int i = 1; i < tableLayoutSubjects.getChildCount(); i++) { // 헤더 행 제외
            TableRow row = (TableRow) tableLayoutSubjects.getChildAt(i);
            Spinner goalSpinner = (Spinner) row.getChildAt(1);
            try {
                String selectedItem = goalSpinner.getSelectedItem().toString();
                totalGoalHours += Integer.parseInt(selectedItem.replaceAll("[^\\d]", ""));
            } catch (NumberFormatException e) {
                Log.e("PlannerFragment", "Spinner item parsing error: " + e.getMessage());
            }
        }
        textTotalGoalTime.setText("총 목표 시간: " + totalGoalHours + "시간");
    }

    private void savePlannerData() {
        if (sharedPreferences.getBoolean(KEY_IS_FROZEN, false)) {
            Toast.makeText(requireContext(), "플래너는 이미 저장되어 변경할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        int totalGoalHours = 0;
        JSONArray jsonArray = new JSONArray();
        for (int i = 1; i < tableLayoutSubjects.getChildCount(); i++) {
            TableRow row = (TableRow) tableLayoutSubjects.getChildAt(i);
            EditText subjectEt = (EditText) row.getChildAt(0);
            Spinner goalSpinner = (Spinner) row.getChildAt(1);

            String subject = subjectEt.getText().toString();
            String goalTimeStr = goalSpinner.getSelectedItem().toString();
            int goalTime = 0;
            try {
                goalTime = Integer.parseInt(goalTimeStr.replaceAll("[^\\d]", ""));
            } catch (NumberFormatException e) {
                Log.e("PlannerFragment", "Goal time parsing error on save: " + e.getMessage());
            }

            totalGoalHours += goalTime;

            JSONObject subjectObj = new JSONObject();
            try {
                subjectObj.put("subject", subject);
                subjectObj.put("goalTime", goalTime);
                jsonArray.put(subjectObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (totalGoalHours > 6) {
            Toast.makeText(requireContext(), "총 목표 시간은 6시간을 초과할 수 없습니다. 저장 실패.", Toast.LENGTH_SHORT).show();
            return;
        }

        String studyPlan = editPlan.getText().toString();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PLANNER_DATA, jsonArray.toString());
        editor.putString(KEY_STUDY_PLAN, studyPlan);
        editor.putString(KEY_PLANNER_DATE, textToday.getText().toString().replace("today : ", "")); // 날짜 형식에 맞춰 저장
        editor.apply();

        setPlannerFrozen(true);

        schedulePlannerResetAlarm();

        Log.d("Planner", "플래너 저장됨.");
        Toast.makeText(requireContext(), "플래너가 저장되었습니다!", Toast.LENGTH_SHORT).show();
    }

    private void loadPlannerData() {
        String plannerJson = sharedPreferences.getString(KEY_PLANNER_DATA, "[]");
        String studyPlan = sharedPreferences.getString(KEY_STUDY_PLAN, "");
        boolean isFrozen = sharedPreferences.getBoolean(KEY_IS_FROZEN, false);
        currentStudyTimeInHours = sharedPreferences.getInt(KEY_ACTUAL_STUDY_TIME, 0); // 실제 학습 시간 로드

        tableLayoutSubjects.removeViews(1, tableLayoutSubjects.getChildCount() - 1);

        try {
            JSONArray jsonArray = new JSONArray(plannerJson);
            if (jsonArray.length() == 0) {
                if (!isFrozen) {
                    addSubjectRow();
                }
            } else {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String subject = obj.getString("subject");
                    int goalTime = obj.getInt("goalTime");

                    TableRow newRow = new TableRow(requireContext());
                    TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    newRow.setLayoutParams(rowParams);

                    EditText newSubjectEditText = new EditText(requireContext());
                    TableRow.LayoutParams etParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 3.0f); // weight 조정
                    newSubjectEditText.setLayoutParams(etParams);
                    newSubjectEditText.setHint("과목명");
                    newSubjectEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                    newSubjectEditText.setSingleLine(true);
                    newSubjectEditText.setBackgroundResource(android.R.drawable.edit_text);
                    newSubjectEditText.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));
                    newSubjectEditText.setText(subject);
                    newSubjectEditText.setEnabled(!isFrozen);

                    Spinner newGoalTimeSpinner = new Spinner(requireContext());
                    TableRow.LayoutParams spinnerParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f); // weight 조정
                    newGoalTimeSpinner.setLayoutParams(spinnerParams);
                    newGoalTimeSpinner.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                            requireContext(),
                            R.array.plannertime_array,
                            android.R.layout.simple_spinner_item
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    newGoalTimeSpinner.setAdapter(adapter);

                    int spinnerPosition = adapter.getPosition(goalTime + "시간");
                    if (spinnerPosition >= 0) {
                        newGoalTimeSpinner.setSelection(spinnerPosition);
                    }
                    setupGoalTimeSpinnerListener(newGoalTimeSpinner);
                    newGoalTimeSpinner.setEnabled(!isFrozen);

                    newRow.addView(newSubjectEditText);
                    newRow.addView(newGoalTimeSpinner);
                    tableLayoutSubjects.addView(newRow);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            if (!isFrozen) {
                addSubjectRow();
            }
        }

        editPlan.setText(studyPlan);
        editPlan.setEnabled(!isFrozen);
        updateTotalGoalTime(); // 로드 후 총 목표 시간 UI 업데이트
        updateActualStudyTimeUI(); // 로드 후 실제 학습 시간 UI 업데이트
        updateAchievement(); // 로드 후 성취도 업데이트
    }

    private void setPlannerFrozen(boolean frozen) {
        sharedPreferences.edit().putBoolean(KEY_IS_FROZEN, frozen).apply();
        applyFrozenState();
    }

    private void applyFrozenState() {
        boolean isFrozen = sharedPreferences.getBoolean(KEY_IS_FROZEN, false);
        Log.d("PlannerDebug", "applyFrozenState called. isFrozen: " + isFrozen);

        buttonAddSubject.setEnabled(!isFrozen);
        buttonRemoveSubject.setEnabled(!isFrozen);
        editPlan.setEnabled(!isFrozen);
        buttonSave.setEnabled(!isFrozen);

        for (int i = 1; i < tableLayoutSubjects.getChildCount(); i++) {
            TableRow row = (TableRow) tableLayoutSubjects.getChildAt(i);
            EditText subjectEt = (EditText) row.getChildAt(0);
            Spinner goalSpinner = (Spinner) row.getChildAt(1);

            subjectEt.setEnabled(!isFrozen);
            goalSpinner.setEnabled(!isFrozen);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}
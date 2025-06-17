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
    private TextView textTotalTime; // 총 공부 시간을 표시할 TextView 추가
    private EditText editPlan; // 공부 계획 EditText 추가
    private Button buttonSave; // 저장 버튼 변수 추가

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "PlannerData";
    private static final String KEY_PLANNER_DATE = "planner_date";
    private static final String KEY_PLANNER_DATA = "planner_data";
    private static final String KEY_STUDY_PLAN = "study_plan";
    private static final String KEY_IS_FROZEN = "is_frozen"; // 플래너 변경 불가 상태 여부

    // 자습 시간 (시간 단위) - 외부에서 업데이트 예정
    private int currentStudyTimeInHours = 0;

    // 경험치 변수 (다른 Fragment와의 연동을 위해 public getter 제공)
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
        textTotalTime = view.findViewById(R.id.textTotalTime);
        editPlan = view.findViewById(R.id.editPlan);
        buttonSave = view.findViewById(R.id.buttonSave);

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
                // 다음 과목을 추가하더라도 총 목표 시간이 6시간을 초과하지 않도록 함 (새 과목의 최소 목표 시간을 0시간으로 가정)
                // 만약 새로 추가되는 과목의 스피너 기본값이 '1시간'이라면, currentTotalGoalHours + 1 < 6 으로 변경 고려
                if (currentTotalGoalHours < 6) { // 현재 총 목표 시간이 6시간 미만일 때만 추가 허용
                    addSubjectRow();
                    updateTotalGoalTime(); // 과목 추가 시 총 목표 시간 업데이트
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
                updateTotalGoalTime(); // 과목 삭제 시 총 목표 시간 업데이트
            } else {
                Toast.makeText(requireContext(), "더 이상 삭제할 과목이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // 저장 버튼 클릭 리스너 설정
        buttonSave.setOnClickListener(v -> {
            // savePlannerData() 내부에서 성공 시에만 setPlannerFrozen(true) 호출
            savePlannerData();
        });

        // 스피너 값 변경 리스너 설정: 로드된 과목들에 대해 리스너 설정
        for (int i = 1; i < tableLayoutSubjects.getChildCount(); i++) {
            TableRow row = (TableRow) tableLayoutSubjects.getChildAt(i);
            Spinner goalSpinner = (Spinner) row.getChildAt(1);
            setupGoalTimeSpinnerListener(goalSpinner);
        }

        // 초기 총 목표 시간 계산
        updateTotalGoalTime();

        // 모든 UI 요소 설정 후 최종적으로 동결 상태 적용
        applyFrozenState();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 포그라운드로 돌아올 때마다 날짜 확인 및 초기화 로직 다시 실행
        displayTodayDate();
        checkAndInitializePlanner(); // 여기서 날짜 기반 초기화 및 setPlannerFrozen(false) 호출될 수 있음
        loadPlannerData(); // 초기화되었든 아니든 최신 데이터 로드
        applyFrozenState(); // 가장 최신의 동결 상태를 UI에 적용
    }

    private void displayTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = sdf.format(new Date());
        textToday.setText(todayDate);
    }

    private void checkAndInitializePlanner() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = sdf.format(new Date());
        String savedDate = sharedPreferences.getString(KEY_PLANNER_DATE, "");

        if (!todayDate.equals(savedDate)) {
            // 날짜가 바뀌었으면 플래너 초기화 및 알림
            initializePlannerData(savedDate); // 이전 날짜를 인자로 넘겨 경험치 계산에 활용
            Toast.makeText(requireContext(), "새로운 날짜입니다. 플래너가 초기화됩니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // AlarmManager를 사용하여 다음 날 00시에 초기화되도록 예약하는 메서드
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
        calendar.add(Calendar.DAY_OF_YEAR, 1); // 다음 날로 설정
        calendar.set(Calendar.HOUR_OF_DAY, 0); // 00시 00분
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 (API 31) 이상
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                Log.d("PlannerFragment", "다음날 00시 플래너 초기화 알람 예약됨 (Exact, Android 12+): " + calendar.getTime());
            } else {
                // 권한이 없는 경우, 사용자에게 권한을 요청하는 인텐트를 시작
                Intent exactAlarmPermissionIntent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                requireContext().startActivity(exactAlarmPermissionIntent);
                Toast.makeText(requireContext(), "정확한 알람 설정을 위해 권한이 필요합니다. 설정에서 '알람 및 리마인더' 권한을 허용해주세요.", Toast.LENGTH_LONG).show();
                Log.w("PlannerFragment", "SCHEDULE_EXACT_ALARM 권한이 없어 알람을 예약할 수 없습니다. 사용자에게 요청합니다.");
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Android 6.0 (API 23) ~ Android 11 (API 30)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Log.d("PlannerFragment", "다음날 00시 플래너 초기화 알람 예약됨 (ExactAndAllowWhileIdle): " + calendar.getTime());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // Android 4.4 (API 19) ~ Android 5.1 (API 22)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Log.d("PlannerFragment", "다음날 00시 플래너 초기화 알람 예약됨 (Exact): " + calendar.getTime());
        } else { // Android 4.3 (API 18) 이하
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Log.d("PlannerFragment", "다음날 00시 플래너 초기화 알람 예약됨 (Old): " + calendar.getTime());
        }
    }

    // 플래너 초기화 로직
    private void initializePlannerData(String previousDate) {
        // 이전 날짜의 데이터 로드 (경험치 계산을 위해)
        loadPlannerDataForExperience(previousDate);

        // SharedPreferences에서 플래너 데이터, 공부 계획, 동결 상태 모두 초기화
        sharedPreferences.edit()
                .remove(KEY_PLANNER_DATA)
                .remove(KEY_STUDY_PLAN)
                .remove(KEY_IS_FROZEN) // 이전에 저장된 동결 상태 제거
                .apply();

        // 새 날짜에는 플래너 변경 가능한 상태로 시작
        sharedPreferences.edit().putString(KEY_PLANNER_DATE, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date())).apply();
        setPlannerFrozen(false); // 초기화 시에는 항상 false로 설정 (핵심 변경점)

        // UI 초기화
        tableLayoutSubjects.removeViews(1, tableLayoutSubjects.getChildCount() - 1); // 헤더 제외 모두 제거
        // 초기화 후에는 빈 과목 줄 하나만 추가
        addSubjectRow();
        editPlan.setText(""); // 공부 계획 초기화
        updateTotalGoalTime(); // 총 목표 시간 초기화 (0으로)
        currentStudyTimeInHours = 0; // 자습 시간 초기화

        // 초기화 후 다음날 00시 알람 다시 예약
        schedulePlannerResetAlarm();
    }

    // 이전 날짜의 플래너 데이터를 로드하여 경험치 계산 (초기화 시 호출)
    private void loadPlannerDataForExperience(String dateToLoad) {
        if (dateToLoad == null || dateToLoad.isEmpty()) {
            calculatedExperience = 0; // 이전 날짜 정보 없으면 경험치 0
            return;
        }

        // 특정 날짜의 데이터를 저장하는 키를 다르게 관리해야 할 경우, KEY_PLANNER_DATA + dateToLoad 와 같은 방식 고려
        // 현재는 PREF_NAME에 당일 데이터만 저장하는 것으로 가정하고, 초기화 직전의 데이터로 계산
        String plannerJson = sharedPreferences.getString(KEY_PLANNER_DATA, "[]");
        boolean wasFrozen = sharedPreferences.getBoolean(KEY_IS_FROZEN, false); // 이전 날짜가 저장되었는지 확인

        if (!wasFrozen) { // 저장되지 않았거나, 변경 불가 상태가 아니었으면 경험치 없음
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

        // **현재는 currentStudyTimeInHours 변수의 현재 값으로 가정합니다.**
        // **실제 자습 시간 연동 방식이 구현되면 이 부분은 수정되어야 합니다.**
        // 자습 시간은 SharedPreferences에 저장되거나, 외부에서 정확히 전달되어야 합니다.
        Log.d("ExperienceCalc", "Total Goal Hours: " + totalGoalHours + ", Current Study Hours: " + currentStudyTimeInHours);

        calculateExperience(totalGoalHours, currentStudyTimeInHours);
    }

    // 경험치 계산 메서드
    private void calculateExperience(int totalGoalHours, int actualStudyHours) {
        if (totalGoalHours <= 0) {
            calculatedExperience = 0;
            return;
        }

        double achievementRate = (double) actualStudyHours / totalGoalHours;
        Log.d("ExperienceCalc", "Achievement Rate: " + achievementRate);

        int baseExperience = actualStudyHours * 20; // 시간당 20 경험치

        if (achievementRate >= 1.0) { // 달성률 100% 이상이면 두 배
            calculatedExperience = baseExperience * 2;
            Log.d("ExperienceCalc", "100% 달성! 경험치 두 배 지급: " + calculatedExperience);
        } else {
            calculatedExperience = baseExperience;
            Log.d("ExperienceCalc", "경험치 지급: " + calculatedExperience);
        }
        // 이 calculatedExperience 값은 다른 Fragment에서 getCalculatedExperience()를 통해 가져갈 수 있습니다.
    }

    // 외부에서 호출하여 자습 시간 업데이트
    public void updateStudyTime(int hours) {
        this.currentStudyTimeInHours = hours;
        Log.d("PlannerFragment", "자습 시간 업데이트: " + hours + "시간");
        // UI에 총 공부 시간 업데이트 (필요하다면)
        //textTotalTime.setText(hours + "시간"); // 이 부분은 목표 시간이 아닌 자습 시간 표시용이므로 주석 처리
    }

    // 다른 Fragment에서 경험치 값을 가져갈 수 있는 메서드
    public int getCalculatedExperience() {
        return calculatedExperience;
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
        TableRow.LayoutParams etParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 4.0f);
        etParams.setMargins(dpToPx(0), dpToPx(8), dpToPx(8), dpToPx(8));
        newSubjectEditText.setLayoutParams(etParams);
        newSubjectEditText.setHint("예: 모바일프로그래밍");
        newSubjectEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        newSubjectEditText.setSingleLine(true);
        newSubjectEditText.setPadding(dpToPx(0), dpToPx(8), dpToPx(8), dpToPx(8));
        newSubjectEditText.setBackgroundResource(android.R.drawable.edit_text);
        newSubjectEditText.setEnabled(!sharedPreferences.getBoolean(KEY_IS_FROZEN, false)); // 동결 상태에 따라 활성화/비활성화

        // 2. 목표 시간 Spinner 추가
        Spinner newGoalTimeSpinner = new Spinner(requireContext());
        TableRow.LayoutParams spinnerParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f);
        spinnerParams.setMargins(dpToPx(4), dpToPx(8), dpToPx(0), dpToPx(8));
        newGoalTimeSpinner.setLayoutParams(spinnerParams);
        newGoalTimeSpinner.setPadding(dpToPx(4), dpToPx(8), dpToPx(0), dpToPx(8));
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.plannertime_array, // 시간 단위 (0~6시간) 스피너 배열
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        newGoalTimeSpinner.setAdapter(adapter);
        newGoalTimeSpinner.setEnabled(!sharedPreferences.getBoolean(KEY_IS_FROZEN, false)); // 동결 상태에 따라 활성화/비활성화

        // Spinner 값 변경 리스너 설정
        setupGoalTimeSpinnerListener(newGoalTimeSpinner);

        newRow.addView(newSubjectEditText);
        newRow.addView(newGoalTimeSpinner);

        tableLayoutSubjects.addView(newRow);

        newSubjectEditText.requestFocus();
    }

    // updateTotalGoalTime() 메서드와 유사하게 현재 TableLayout에 있는 과목들의 목표 시간을 합산하는 새로운 메서드
    private int calculateCurrentTotalGoalHours() {
        int totalGoalHours = 0;
        for (int i = 1; i < tableLayoutSubjects.getChildCount(); i++) { // 헤더 행 제외
            TableRow row = (TableRow) tableLayoutSubjects.getChildAt(i);
            Spinner goalSpinner = (Spinner) row.getChildAt(1);
            try {
                // 스피너에서 선택된 문자열에서 숫자 부분만 추출 (예: "2시간" -> 2)
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
                updateTotalGoalTime();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    // 총 목표 시간 계산 및 UI 업데이트
    private void updateTotalGoalTime() {
        int totalGoalHours = 0;
        for (int i = 1; i < tableLayoutSubjects.getChildCount(); i++) { // 헤더 행 제외
            TableRow row = (TableRow) tableLayoutSubjects.getChildAt(i);
            Spinner goalSpinner = (Spinner) row.getChildAt(1);
            try {
                // 스피너에서 선택된 문자열에서 숫자 부분만 추출 (예: "2시간" -> 2)
                String selectedItem = goalSpinner.getSelectedItem().toString();
                totalGoalHours += Integer.parseInt(selectedItem.replaceAll("[^\\d]", ""));
            } catch (NumberFormatException e) {
                Log.e("PlannerFragment", "Spinner item parsing error: " + e.getMessage());
            }
        }
        // UI에는 계산된 값을 표시
        textTotalTime.setText(totalGoalHours + "시간");
    }

    // 플래너 데이터 저장
    private void savePlannerData() {
        if (sharedPreferences.getBoolean(KEY_IS_FROZEN, false)) {
            Toast.makeText(requireContext(), "플래너는 이미 저장되어 변경할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 총 목표 시간 유효성 검사 및 데이터 수집
        int totalGoalHours = 0;
        JSONArray jsonArray = new JSONArray();
        for (int i = 1; i < tableLayoutSubjects.getChildCount(); i++) { // 헤더 행 제외
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

        // 유효성 검사: 총 목표 시간이 6시간 초과하는지 확인
        if (totalGoalHours > 6) {
            Toast.makeText(requireContext(), "총 목표 시간은 6시간을 초과할 수 없습니다. 저장 실패.", Toast.LENGTH_SHORT).show();
            // 저장이 실패했으므로 플래너는 동결되지 않고 수정 가능 상태를 유지
            return; // 여기서 함수 종료
        }

        // 공부 계획 저장
        String studyPlan = editPlan.getText().toString();

        // SharedPreferences에 저장
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PLANNER_DATA, jsonArray.toString());
        editor.putString(KEY_STUDY_PLAN, studyPlan);
        editor.putString(KEY_PLANNER_DATE, textToday.getText().toString()); // 현재 날짜 저장
        editor.apply();

        // **저장이 성공적으로 완료된 후에만 플래너를 동결 상태로 설정**
        setPlannerFrozen(true); // 이 부분이 유효성 검사 통과 후로 이동

        // 저장 후 알람 예약
        schedulePlannerResetAlarm();

        Log.d("Planner", "플래너 저장됨.");
        Toast.makeText(requireContext(), "플래너가 저장되었습니다!", Toast.LENGTH_SHORT).show(); // 저장 성공 토스트는 여기에
    }

    // 저장된 플래너 데이터 로드
    private void loadPlannerData() {
        String plannerJson = sharedPreferences.getString(KEY_PLANNER_DATA, "[]");
        String studyPlan = sharedPreferences.getString(KEY_STUDY_PLAN, "");
        boolean isFrozen = sharedPreferences.getBoolean(KEY_IS_FROZEN, false); // 현재 isFrozen 상태 가져오기

        // 기존 과목 행 모두 제거 (헤더 제외)
        tableLayoutSubjects.removeViews(1, tableLayoutSubjects.getChildCount() - 1);

        try {
            JSONArray jsonArray = new JSONArray(plannerJson);
            if (jsonArray.length() == 0) {
                // 저장된 데이터가 없으면 빈 과목 줄 하나 추가
                // 단, 플래너가 동결되지 않은 상태일 경우에만 (즉, 오늘 날짜인데 아직 저장 안 한 경우)
                if (!isFrozen) { // 이 조건이 중요합니다.
                    addSubjectRow();
                }
            } else {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String subject = obj.getString("subject");
                    int goalTime = obj.getInt("goalTime");

                    // 새로운 TableRow 생성 및 값 설정
                    TableRow newRow = new TableRow(requireContext());
                    TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT
                    );
                    newRow.setLayoutParams(rowParams);

                    EditText newSubjectEditText = new EditText(requireContext());
                    TableRow.LayoutParams etParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 4.0f);
                    etParams.setMargins(dpToPx(0), dpToPx(8), dpToPx(8), dpToPx(8));
                    newSubjectEditText.setLayoutParams(etParams);
                    newSubjectEditText.setHint("예: 모바일프로그래밍");
                    newSubjectEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                    newSubjectEditText.setSingleLine(true);
                    newSubjectEditText.setPadding(dpToPx(0), dpToPx(8), dpToPx(8), dpToPx(8));
                    newSubjectEditText.setBackgroundResource(android.R.drawable.edit_text);
                    newSubjectEditText.setText(subject);
                    newSubjectEditText.setEnabled(!isFrozen); // 동결 상태에 따라 활성화/비활성화

                    Spinner newGoalTimeSpinner = new Spinner(requireContext());
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

                    // 스피너 값 설정 (선택된 시간을 찾아서 설정)
                    int spinnerPosition = adapter.getPosition(goalTime + "시간"); // "X시간" 형식으로 찾기
                    if (spinnerPosition >= 0) {
                        newGoalTimeSpinner.setSelection(spinnerPosition);
                    }
                    setupGoalTimeSpinnerListener(newGoalTimeSpinner); // 로드된 스피너에도 리스너 설정
                    newGoalTimeSpinner.setEnabled(!isFrozen); // 동결 상태에 따라 활성화/비활성화

                    newRow.addView(newSubjectEditText);
                    newRow.addView(newGoalTimeSpinner);
                    tableLayoutSubjects.addView(newRow);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            // JSON 파싱 오류 시에도 초기화되지 않은 상태라면 (저장되지 않았다면) 빈 줄 하나 추가
            if (!isFrozen) {
                addSubjectRow();
            }
        }

        editPlan.setText(studyPlan);
        editPlan.setEnabled(!isFrozen); // editPlan에도 동결 상태 적용
        updateTotalGoalTime(); // 로드 후 총 목표 시간 업데이트
    }

    // 플래너 변경 불가 상태 설정
    private void setPlannerFrozen(boolean frozen) {
        sharedPreferences.edit().putBoolean(KEY_IS_FROZEN, frozen).apply();
        applyFrozenState(); // 상태 변경 후 바로 UI에 적용
    }

    // 플래너 동결 상태에 따라 UI 활성화/비활성화
    private void applyFrozenState() {
        boolean isFrozen = sharedPreferences.getBoolean(KEY_IS_FROZEN, false);
        Log.d("PlannerDebug", "applyFrozenState called. isFrozen: " + isFrozen); // 디버깅 로그 추가

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


    // dp 값을 px 값으로 변환하는 헬퍼 메서드
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}
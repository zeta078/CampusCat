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
    private TextView textTotalGoalTime;
    private TextView textTotalTime;
    private EditText editPlan;
    private Button buttonSave;

    private ProgressBar totalStudyProgress;
    private TextView totalStudyPercentage;

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "PlannerData";
    private static final String KEY_PLANNER_DATE = "planner_date";
    private static final String KEY_PLANNER_DATA = "planner_data";
    private static final String KEY_STUDY_PLAN = "study_plan";
    private static final String KEY_IS_FROZEN = "is_frozen";
    private static final String KEY_ACTUAL_STUDY_TIME = "actual_study_time"; // 실제 학습 시간 저장 키
    private static final String KEY_LAST_XP_CALC_DATE = "last_xp_calc_date"; // 경험치 계산 마지막 날짜

    private int currentStudyTimeInHours = 0; // 실제 학습 시간 (시간 단위)
    private int calculatedExperience = 0;

    // AlarmManager 관련 상수는 사용하지 않는다면 제거해도 무방합니다.
    // private static final String CHANNEL_ID = "planner_notification_channel";
    // private static final int NOTIFICATION_ID = 1001;


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
        textTotalGoalTime = view.findViewById(R.id.textTotalGoalTime);
        textTotalTime = view.findViewById(R.id.textTotalTime);
        editPlan = view.findViewById(R.id.editPlan);
        buttonSave = view.findViewById(R.id.buttonSave);
        totalStudyProgress = view.findViewById(R.id.totalStudyProgress);
        totalStudyPercentage = view.findViewById(R.id.totalStudyPercentage);

        // SharedPreferences 초기화
        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // 중요: 날짜 확인 및 초기화 로직은 가장 먼저 실행되어 현재 날짜의 상태를 확정해야 함
        displayTodayDate();
        // checkAndInitializePlanner() 대신 onResume()에서 모든 로직을 처리하도록 변경합니다.
        // 이는 프래그먼트가 다시 활성화될 때마다 최신 상태를 반영하기 위함입니다.

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

        // 초기 로드 (onResume에서 로드하므로 여기서는 필요 없음)
        // loadPlannerData();

        // 스피너 값 변경 리스너 설정: 로드된 과목들에 대해 리스너 설정 (loadPlannerData에서 이미 처리)
        // onCreateView에서는 초기 로드된 데이터가 없으므로 이 루프는 onResume의 loadPlannerData 후 실행되거나
        // loadPlannerData 내에서 처리되어야 합니다.
        // 현재는 loadPlannerData()가 호출될 때 각 스피너에 리스너를 설정하므로 이 코드는 제거합니다.
        // for (int i = 1; i < tableLayoutSubjects.getChildCount(); i++) {
        //     TableRow row = (TableRow) tableLayoutSubjects.getChildAt(i);
        //     Spinner goalSpinner = (Spinner) row.getChildAt(1);
        //     setupGoalTimeSpinnerListener(goalSpinner);
        // }


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 포그라운드로 돌아올 때마다 날짜 확인 및 초기화 로직 다시 실행
        displayTodayDate();
        checkDateChangeAndInitialize(); // 날짜 변경 및 초기화 로직
        loadPlannerData(); // 항상 최신 데이터 로드
        applyFrozenState(); // 로드된 데이터에 따라 동결 상태 적용
        updateActualStudyTimeUI(); // 실제 학습 시간 UI 업데이트
        updateAchievement(); // 성취도 업데이트
    }

    private void displayTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = sdf.format(new Date());
        textToday.setText("today : " + todayDate); // 레이아웃에 맞춰 "today : " 추가
    }

    // **** 핵심 변경: 날짜 변경 감지 및 플래너 초기화 로직 ****
    private void checkDateChangeAndInitialize() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = sdf.format(new Date());
        String savedDate = sharedPreferences.getString(KEY_PLANNER_DATE, "");
        String lastXpCalcDate = sharedPreferences.getString(KEY_LAST_XP_CALC_DATE, "");

        if (!todayDate.equals(savedDate)) {
            Log.d("PlannerFragment", "날짜 변경 감지! 이전 날짜: " + savedDate + ", 오늘 날짜: " + todayDate);

            // 이전 날짜 데이터로 경험치를 먼저 계산하고 HomeActivity로 전달 준비
            // 이 로직은 이전 날짜가 '동결'된 상태에서만 유효해야 합니다.
            if (!savedDate.isEmpty() && sharedPreferences.getBoolean(KEY_IS_FROZEN, false) && !lastXpCalcDate.equals(todayDate)) {
                Log.d("PlannerFragment", "이전 날짜 플래너가 동결되었고, 오늘 경험치 계산되지 않음. 경험치 정산 시작.");
                int previousTotalGoalHours = 0;
                int previousActualStudyTime = 0;

                // 이전 날짜의 플래너 데이터와 실제 학습 시간 로드
                String previousPlannerJson = sharedPreferences.getString(KEY_PLANNER_DATA, "[]");
                previousActualStudyTime = sharedPreferences.getInt(KEY_ACTUAL_STUDY_TIME, 0);

                try {
                    JSONArray jsonArray = new JSONArray(previousPlannerJson);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        previousTotalGoalHours += obj.getInt("goalTime");
                    }
                } catch (JSONException e) {
                    Log.e("PlannerFragment", "이전 날짜 플래너 데이터 파싱 오류: " + e.getMessage());
                }

                Log.d("PlannerFragment", "이전 날짜 총 목표 시간: " + previousTotalGoalHours + ", 실제 학습 시간: " + previousActualStudyTime);
                calculateExperience(previousTotalGoalHours, previousActualStudyTime); // 이전 날짜 데이터로 경험치 계산

                // 경험치 계산 완료 후, HomeActivity가 가져갈 수 있도록 플래그 설정 또는 직접 전달 (여기서는 멤버 변수에 저장)
                // 그리고 경험치 계산이 완료된 날짜를 저장하여 중복 계산 방지
                sharedPreferences.edit().putString(KEY_LAST_XP_CALC_DATE, todayDate).apply();
            } else {
                Log.d("PlannerFragment", "날짜는 바뀌었지만, 이전 날짜가 동결되지 않았거나 이미 경험치 계산됨. 경험치 스킵.");
                calculatedExperience = 0; // 경험치 계산 스킵 시 리셋
            }

            // 새로운 날짜이므로 플래너 초기화
            initializeCurrentPlanner();
            Toast.makeText(requireContext(), "새로운 날짜입니다. 플래너가 초기화됩니다.", Toast.LENGTH_SHORT).show();

        } else {
            // 같은 날짜라면 is_frozen 상태만 확인 (혹시 앱이 비정상 종료되었을 경우)
            // 그리고 오늘 날짜의 실제 학습 시간 로드
            currentStudyTimeInHours = sharedPreferences.getInt(KEY_ACTUAL_STUDY_TIME, 0);
            Log.d("PlannerFragment", "같은 날짜입니다. 현재 학습 시간 로드: " + currentStudyTimeInHours + "시간");
        }
        // 다음날 00시 초기화 알람 예약 (매번 onResume에서 실행되도록 유지하여 확실하게 예약)
        schedulePlannerResetAlarm();
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
        calendar.add(Calendar.DAY_OF_YEAR, 1); // 다음 날로 설정
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // 이미 예약된 알람이 있다면 취소하고 다시 예약 (중복 방지)
        alarmManager.cancel(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                Log.d("PlannerFragment", "다음날 00시 플래너 초기화 알람 예약됨 (Exact, Android 12+): " + calendar.getTime());
            } else {
                // 권한 요청은 앱 시작 시 한 번만 하는 것이 좋습니다. 여기서는 로그만 남깁니다.
                Log.w("PlannerFragment", "SCHEDULE_EXACT_ALARM 권한이 없어 알람을 예약할 수 없습니다. 사용자에게 요청해야 합니다.");
                // 사용자에게 권한 요청을 하고 싶다면 여기에 인텐트 실행 코드를 추가하세요.
                // Intent exactAlarmPermissionIntent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                // requireContext().startActivity(exactAlarmPermissionIntent);
                // Toast.makeText(requireContext(), "정확한 알람 설정을 위해 권한이 필요합니다. 설정에서 '알람 및 미리 알림' 권한을 허용해주세요.", Toast.LENGTH_LONG).show();
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

    // **** 핵심 변경: 현재 날짜의 플래너 데이터를 초기화하는 메서드 ****
    private void initializeCurrentPlanner() {
        sharedPreferences.edit()
                .remove(KEY_PLANNER_DATA)
                .remove(KEY_STUDY_PLAN)
                .remove(KEY_IS_FROZEN)
                .remove(KEY_ACTUAL_STUDY_TIME) // 실제 학습 시간도 초기화
                .apply();

        // 현재 날짜로 저장
        sharedPreferences.edit().putString(KEY_PLANNER_DATE, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date())).apply();
        setPlannerFrozen(false); // 플래너 동결 해제

        // UI 초기화
        tableLayoutSubjects.removeViews(1, tableLayoutSubjects.getChildCount() - 1); // 헤더 제외 모두 제거
        addSubjectRow(); // 기본 과목 행 하나 추가
        editPlan.setText("");
        currentStudyTimeInHours = 0; // 실제 학습 시간 변수 초기화
        updateTotalGoalTime(); // 총 목표 시간 UI 초기화
        updateActualStudyTimeUI(); // 실제 학습 시간 UI 초기화
        updateAchievement(); // 성취도 초기화

        // 초기화된 상태를 저장 (새로운 날짜의 빈 플래너 저장)
        savePlannerData(); // 초기화된 데이터를 SharedPreferences에 저장
    }


    // 경험치 계산 로직 (이전 날짜의 목표 시간과 실제 학습 시간으로)
    private void calculateExperience(int totalGoalHours, int actualStudyHours) {
        if (totalGoalHours <= 0) {
            calculatedExperience = 0;
            Log.d("ExperienceCalc", "목표 시간이 0이거나 음수입니다. 경험치 0.");
            return;
        }

        double achievementRate = (double) actualStudyHours / totalGoalHours;
        Log.d("ExperienceCalc", "Achievement Rate: " + achievementRate + " (Actual: " + actualStudyHours + ", Goal: " + totalGoalHours + ")");

        int baseExperience = actualStudyHours * 20; // 시간당 20 XP 기본
        int bonusExperience = 0;

        if (achievementRate >= 1.0) {
            bonusExperience = totalGoalHours * 10; // 목표 달성 시 목표 시간당 보너스 XP
            calculatedExperience = baseExperience + bonusExperience;
            Log.d("ExperienceCalc", "100% 달성! 기본 경험치: " + baseExperience + ", 보너스: " + bonusExperience + ", 총 경험치: " + calculatedExperience);
        } else {
            calculatedExperience = baseExperience;
            Log.d("ExperienceCalc", "목표 미달. 기본 경험치 지급: " + calculatedExperience);
        }
    }

    // 외부에서 호출하여 자습 시간 업데이트 및 UI 반영 (StudyFragment에서 호출될 것임)
    public void updateStudyTime(int hours) {
        // **** 핵심 변경: 실제 학습 시간을 누적하여 저장 ****
        this.currentStudyTimeInHours = hours; // StudyFragment에서 전달받은 총 시간으로 덮어씀 (누적은 StudyFragment에서 처리됨)
        Log.d("PlannerFragment", "자습 시간 최종 업데이트 (StudyFragment에서 전달): " + currentStudyTimeInHours + "시간");

        // SharedPreferences에 누적된 실제 학습 시간 저장
        // 이 부분은 StudyFragment에서 최종적으로 계산된 시간을 받아오는 것이므로, 덮어쓰는 것이 맞습니다.
        sharedPreferences.edit().putInt(KEY_ACTUAL_STUDY_TIME, currentStudyTimeInHours).apply();
        Log.d("PlannerFragment", "SharedPrefs에 KEY_ACTUAL_STUDY_TIME 저장됨: " + currentStudyTimeInHours + "시간");

        updateActualStudyTimeUI(); // 실제 학습 시간 UI 업데이트
        updateAchievement(); // 성취도 업데이트
    }


    // 실제 학습 시간 UI만 업데이트하는 헬퍼 메서드
    private void updateActualStudyTimeUI() {
        textTotalTime.setText(currentStudyTimeInHours + "시간");
    }

    // 성취도 업데이트 메서드
    private void updateAchievement() {
        int totalGoalHours = calculateCurrentTotalGoalHours();
        int actualStudyHours = currentStudyTimeInHours; // 현재 프래그먼트의 실제 학습 시간

        int percentage = 0;
        if (totalGoalHours > 0) {
            percentage = (int) ((double) actualStudyHours / totalGoalHours * 100);
            if (percentage > 100) percentage = 100; // 성취도는 최대 100%
        }

        totalStudyProgress.setProgress(percentage);
        totalStudyPercentage.setText(percentage + "%");
        Log.d("PlannerFragment", "성취도 업데이트: 목표 " + totalGoalHours + "h, 학습 " + actualStudyHours + "h, 달성률 " + percentage + "%");
    }

    // HomeActivity가 경험치를 가져갈 수 있도록 Getter 추가
    public int getCalculatedExperience() {
        int xpToReturn = calculatedExperience;
        calculatedExperience = 0; // 경험치 가져간 후 바로 리셋
        Log.d("PlannerFragment", "getCalculatedExperience 호출됨. 반환될 경험치: " + xpToReturn);
        return xpToReturn;
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
        TableRow.LayoutParams etParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 3.0f);
        newSubjectEditText.setLayoutParams(etParams);
        newSubjectEditText.setHint("과목명");
        newSubjectEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        newSubjectEditText.setSingleLine(true);
        newSubjectEditText.setBackgroundResource(android.R.drawable.edit_text);
        newSubjectEditText.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));
        newSubjectEditText.setEnabled(!sharedPreferences.getBoolean(KEY_IS_FROZEN, false));

        // 목표 시간 Spinner
        Spinner newGoalTimeSpinner = new Spinner(requireContext());
        TableRow.LayoutParams spinnerParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f);
        newGoalTimeSpinner.setLayoutParams(spinnerParams);
        newGoalTimeSpinner.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));
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
        editor.putBoolean(KEY_IS_FROZEN, true); // 플래너 저장 시 동결
        // editor.apply()는 모든 put 작업 후에 한 번만 호출하는 것이 효율적입니다.
        editor.apply();

        setPlannerFrozen(true); // UI 업데이트를 위해 호출
        Log.d("Planner", "플래너 저장됨.");
        Toast.makeText(requireContext(), "플래너가 저장되었습니다!", Toast.LENGTH_SHORT).show();

        // 플래너 저장 시 알람 예약 (다음 날 초기화를 위해)
        schedulePlannerResetAlarm();
    }

    private void loadPlannerData() {
        String plannerJson = sharedPreferences.getString(KEY_PLANNER_DATA, "[]");
        String studyPlan = sharedPreferences.getString(KEY_STUDY_PLAN, "");
        boolean isFrozen = sharedPreferences.getBoolean(KEY_IS_FROZEN, false);
        currentStudyTimeInHours = sharedPreferences.getInt(KEY_ACTUAL_STUDY_TIME, 0); // 실제 학습 시간 로드

        tableLayoutSubjects.removeViews(1, tableLayoutSubjects.getChildCount() - 1); // 기존 과목 행 제거

        try {
            JSONArray jsonArray = new JSONArray(plannerJson);
            if (jsonArray.length() == 0) {
                // 저장된 과목이 없지만, 동결되지 않았다면 기본 과목 행 추가
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
                    TableRow.LayoutParams etParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 3.0f);
                    newSubjectEditText.setLayoutParams(etParams);
                    newSubjectEditText.setHint("과목명");
                    newSubjectEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                    newSubjectEditText.setSingleLine(true);
                    newSubjectEditText.setBackgroundResource(android.R.drawable.edit_text);
                    newSubjectEditText.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));
                    newSubjectEditText.setText(subject);
                    newSubjectEditText.setEnabled(!isFrozen); // 동결 상태에 따라 활성화/비활성화

                    Spinner newGoalTimeSpinner = new Spinner(requireContext());
                    TableRow.LayoutParams spinnerParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f);
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
                    setupGoalTimeSpinnerListener(newGoalTimeSpinner); // 리스너 다시 설정
                    newGoalTimeSpinner.setEnabled(!isFrozen); // 동결 상태에 따라 활성화/비활성화

                    newRow.addView(newSubjectEditText);
                    newRow.addView(newGoalTimeSpinner);
                    tableLayoutSubjects.addView(newRow);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("PlannerFragment", "플래너 데이터 로드 중 JSON 파싱 오류: " + e.getMessage());
            // 오류 발생 시에도 동결되지 않았다면 기본 과목 행 추가
            if (!isFrozen) {
                addSubjectRow();
            }
        }

        editPlan.setText(studyPlan);
        editPlan.setEnabled(!isFrozen); // 동결 상태에 따라 활성화/비활성화
        updateTotalGoalTime(); // 로드 후 총 목표 시간 UI 업데이트
        updateActualStudyTimeUI(); // 로드 후 실제 학습 시간 UI 업데이트
        updateAchievement(); // 로드 후 성취도 업데이트
        Log.d("PlannerFragment", "플래너 데이터 로드 완료. 동결: " + isFrozen + ", 실제 학습: " + currentStudyTimeInHours + "시간");
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

        // 기존에 추가된 모든 과목 행의 EditText와 Spinner도 동결 상태에 따라 활성화/비활성화
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
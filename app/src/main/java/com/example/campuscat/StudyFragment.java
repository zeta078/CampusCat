package com.example.campuscat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class StudyFragment extends Fragment {

    private TextView textTimer;
    private Button buttonStartPause;
    private Button buttonEndStudy;

    private Handler handler;
    private Runnable runnable;
    private long startTimeMillis; // 타이머 시작 시간 (밀리초)
    private long elapsedTimeMillis = 0; // 경과 시간 (밀리초)
    private boolean isRunning = false; // 타이머 실행 중 여부

    // PlannerFragment 인스턴스를 가져오기 위함 (MainActivity에서 주입될 것임)
    private PlannerFragment plannerFragment;

    public StudyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.study_layout, container, false);



        textTimer = view.findViewById(R.id.textTimer);
        buttonStartPause = view.findViewById(R.id.buttonStartPause);
        buttonEndStudy = view.findViewById(R.id.buttonEndStudy);

        handler = new Handler(Looper.getMainLooper());

        // **** 변경된 부분: 직접 주입된 plannerFragment를 사용하도록 변경 ****
        // 이 부분에서는 plannerFragment가 null인지 확인하고, null이라면 오류 메시지를 표시합니다.
        // 이는 MainActivity에서 setPlannerFragment()를 호출하는 것이 필수임을 의미합니다.
        if (plannerFragment == null) {
            Log.e("StudyFragment", "PlannerFragment가 StudyFragment에 주입되지 않았습니다. MainActivity에서 setPlannerFragment()를 호출했는지 확인하세요.");
            Toast.makeText(requireContext(), "플래너 연동 설정 오류. 앱 개발자에게 문의하세요.", Toast.LENGTH_LONG).show();
            // 이 경우, 학습 종료 버튼을 비활성화하거나 다른 처리를 할 수 있습니다.
            buttonEndStudy.setEnabled(false);
            buttonStartPause.setEnabled(false);
        }
        // ******************************************************************


        // 시작/일시정지 버튼 클릭 리스너
        buttonStartPause.setOnClickListener(v -> {
            if (isRunning) {
                // 일시정지 상태
                pauseTimer();
            } else {
                // 시작 상태
                startTimer();
            }
        });

        // 학습 종료 버튼 클릭 리스너
        buttonEndStudy.setOnClickListener(v -> {
            endStudy();
        });

        // 초기 타이머 상태 업데이트
        updateTimerText();

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        // Fragment가 백그라운드로 갈 때 타이머 중단 (하지만 elapsed 시간은 유지)
        if (isRunning) {
            handler.removeCallbacks(runnable); // 타이머 콜백 제거
            Log.d("StudyFragment", "타이머 일시정지 (onPause)");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Fragment가 다시 포그라운드로 올 때 타이머 재개
        // (isRunning이 true일 때만, 즉 사용자가 수동으로 멈추지 않았다면 이어서 작동)
        if (isRunning) {
            startTimerRunnable();
            Log.d("StudyFragment", "타이머 재개 (onResume)");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(runnable); // 뷰가 파괴될 때 모든 콜백 제거
    }

    private void startTimer() {
        if (!isRunning) {
            elapsedTimeMillis = 20000000; // 테스트용 시간 정의
            startTimeMillis = System.currentTimeMillis() - elapsedTimeMillis;
            // *************************************************************************

            isRunning = true;
            buttonStartPause.setText("일시정지");
            startTimerRunnable();
            Log.d("StudyFragment", "타이머 시작");
        }
    }

    private void pauseTimer() {
        if (isRunning) {
            handler.removeCallbacks(runnable);
            isRunning = false;
            buttonStartPause.setText("시작");
            Log.d("StudyFragment", "타이머 일시정지");
        }
    }

    private void startTimerRunnable() {
        runnable = new Runnable() {
            @Override
            public void run() {
                elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;
                updateTimerText();
                handler.postDelayed(this, 1000); // 1초마다 업데이트
            }
        };
        handler.post(runnable); // 즉시 실행
    }

    private void updateTimerText() {
        long seconds = (elapsedTimeMillis / 1000) % 60;
        long minutes = (elapsedTimeMillis / (1000 * 60)) % 60;
        long hours = (elapsedTimeMillis / (1000 * 60 * 60));

        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        textTimer.setText(timeFormatted);
    }

    private void endStudy() {
        pauseTimer(); // 타이머 일시정지

        // 총 학습 시간 계산 (정수 시간 단위, 분 단위는 버림)
        int totalStudyHours = (int) (elapsedTimeMillis / (1000 * 60 * 60)); // 밀리초 -> 시간 (정수만)

        // PlannerFragment에 학습 시간 전달
        if (plannerFragment != null) {
            plannerFragment.updateStudyTime(totalStudyHours);
            Toast.makeText(requireContext(), "학습 시간 " + totalStudyHours + "시간이 플래너에 반영되었습니다!", Toast.LENGTH_LONG).show();
            Log.d("StudyFragment", "학습 시간 " + totalStudyHours + "시간이 PlannerFragment에 전달됨.");
        } else {
            Toast.makeText(requireContext(), "플래너를 찾을 수 없어 학습 시간 반영에 실패했습니다. (연동 오류)", Toast.LENGTH_SHORT).show();
            Log.e("StudyFragment", "PlannerFragment가 null입니다. 학습 시간을 반영할 수 없습니다.");
        }

        // 타이머 리셋
        elapsedTimeMillis = 0;
        updateTimerText();
        buttonStartPause.setText("시작"); // 버튼 텍스트 '시작'으로 재설정
        isRunning = false; // 타이머는 멈춘 상태로 시작

        Log.d("StudyFragment", "학습 종료 및 타이머 리셋됨.");
    }

    // MainHostActivity에서 PlannerFragment 인스턴스를 주입받기 위한 setter
    public void setPlannerFragment(PlannerFragment fragment) {
        this.plannerFragment = fragment;
        Log.d("StudyFragment", "PlannerFragment가 StudyFragment에 주입되었습니다.");
        // 주입된 후에 UI 활성화를 처리할 수도 있습니다.
        if (getView() != null && plannerFragment != null) {
            buttonEndStudy.setEnabled(true);
            buttonStartPause.setEnabled(true);
        }
    }
}
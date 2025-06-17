package com.example.campuscat;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;

public class StudyFragment extends Fragment {

    private TextView textTimer;
    private EditText editHours, editMinutes;
    private MaterialButton btnToggle, btnReset;

    private boolean isRunning = false;
    private boolean useCountdown = false;

    private long countdownMillis = 0;
    private long timeRemaining = 0;
    private long timeElapsed = 0;

    private Handler handler = new Handler();
    private Runnable timerRunnable;
    private CountDownTimer countDownTimer;

    // Fragment 생성자 (필수)
    public StudyFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // fragment_study.xml 레이아웃을 인플레이트합니다.
        View view = inflater.inflate(R.layout.fragment_study, container, false);

        // UI 요소들을 초기화합니다. (view.findViewById 사용)
        textTimer = view.findViewById(R.id.textTimer);
        editHours = view.findViewById(R.id.editHours);
        editMinutes = view.findViewById(R.id.editMinutes);
        btnToggle = view.findViewById(R.id.btnToggle);
        btnReset = view.findViewById(R.id.btnReset);

        // 액션바 관련 코드는 Activity에 종속되므로 Fragment에서는 제거합니다.
        // 필요하다면 Activity에서 Fragment를 호스팅할 때 액션바를 설정해야 합니다.

        // btnToggle 클릭 리스너 설정
        btnToggle.setOnClickListener(v -> {
            if (isRunning) {
                pauseTimer();
            } else {
                String hourStr = editHours.getText().toString();
                String minStr = editMinutes.getText().toString();

                boolean hasInput = !(TextUtils.isEmpty(hourStr) && TextUtils.isEmpty(minStr));

                if (hasInput) {
                    useCountdown = true;
                    int hours = TextUtils.isEmpty(hourStr) ? 0 : Integer.parseInt(hourStr);
                    int minutes = TextUtils.isEmpty(minStr) ? 0 : Integer.parseInt(minStr);
                    // 타이머가 리셋된 상태가 아니면 기존 timeRemaining 값을 유지
                    if (timeRemaining == 0 || (hours * 60 + minutes) * 60 * 1000 != countdownMillis) {
                        countdownMillis = (hours * 60 + minutes) * 60 * 1000;
                        timeRemaining = countdownMillis;
                    }
                    textTimer.setText(formatTime(timeRemaining)); // 시작 전에 현재 설정된 시간 표시
                } else {
                    useCountdown = false;
                    // 스톱워치 모드에서는 timeRemaining을 초기화
                    timeRemaining = 0;
                }
                startTimer();
            }
        });

        // btnReset 클릭 리스너 설정
        btnReset.setOnClickListener(v -> resetTimer());

        return view;
    }

    // Fragment가 화면에서 사라질 때 타이머를 정리 (메모리 누수 방지)
    @Override
    public void onPause() {
        super.onPause();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }
    }

    // onResume에서 타이머 상태를 복원하거나 새로 시작하지 않습니다.
    // 사용자가 다시 Fragment로 돌아왔을 때 버튼을 누르면 새로 시작하도록 설계되어 있습니다.

    // --- 기존 StudyActivity의 타이머 로직 ---

    private void startTimer() {
        isRunning = true;
        btnToggle.setText("일시정지"); // 토글 버튼 텍스트 변경

        if (useCountdown) {
            // 기존 타이머가 있으면 취소
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            countDownTimer = new CountDownTimer(timeRemaining, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timeRemaining = millisUntilFinished;
                    textTimer.setText(formatTime(millisUntilFinished));
                }

                @Override
                public void onFinish() {
                    isRunning = false;
                    timeRemaining = 0;
                    textTimer.setText("00:00:00");
                    btnToggle.setText("시작");
                    // 타이머 종료 후 입력 필드 초기화 (선택 사항)
                    editHours.setText("");
                    editMinutes.setText("");
                }
            };
            countDownTimer.start();
        } else {
            // 기존 Runnable이 있으면 제거
            if (timerRunnable != null) {
                handler.removeCallbacks(timerRunnable);
            }
            timerRunnable = new Runnable() {
                @Override
                public void run() {
                    timeElapsed += 1000;
                    textTimer.setText(formatTime(timeElapsed));
                    handler.postDelayed(this, 1000);
                }
            };
            handler.post(timerRunnable);
        }
    }

    private void pauseTimer() {
        isRunning = false;
        btnToggle.setText("시작"); // 토글 버튼 텍스트 변경

        if (useCountdown && countDownTimer != null) {
            countDownTimer.cancel();
        } else if (timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }
    }

    private void resetTimer() {
        pauseTimer(); // 먼저 타이머 중지
        timeRemaining = 0;
        timeElapsed = 0;
        textTimer.setText("00:00:00");
        btnToggle.setText("시작");
        // 입력 필드 초기화
        editHours.setText("");
        editMinutes.setText("");
    }

    private String formatTime(long millis) {
        int totalSeconds = (int)(millis / 1000);
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
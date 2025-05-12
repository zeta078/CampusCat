package com.example.test;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class StudyActivity extends AppCompatActivity {

    private TextView textTimer;
    private EditText editHours, editMinutes;
    private Button btnToggle, btnReset;

    private boolean isRunning = false;
    private boolean useCountdown = false; // 동적으로 결정됨

    private long countdownMillis = 0;
    private long timeRemaining = 0;
    private long timeElapsed = 0;

    private Handler handler = new Handler();
    private Runnable timerRunnable;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.study); // 너의 XML 이름이 study.xml일 경우

        textTimer = findViewById(R.id.textTimer);
        editHours = findViewById(R.id.editHours);
        editMinutes = findViewById(R.id.editMinutes);
        btnToggle = findViewById(R.id.btnToggle);
        btnReset = findViewById(R.id.btnReset);

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
                    if (timeRemaining == 0) {
                        countdownMillis = (hours * 60 + minutes) * 60 * 1000;
                        timeRemaining = countdownMillis;
                    }

                    textTimer.setText(formatTime(timeRemaining));

                } else {
                    useCountdown = false;
                }

                startTimer();
            }
        });

        btnReset.setOnClickListener(v -> resetTimer());
    }

    private void startTimer() {
        isRunning = true;
        btnToggle.setText("일시정지");

        if (useCountdown) {
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
                    textTimer.setText("00:00");
                    btnToggle.setText("시작");
                }
            };
            countDownTimer.start();
        } else {
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
        btnToggle.setText("시작");

        if (useCountdown && countDownTimer != null) {
            countDownTimer.cancel();
        } else if (timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }
    }

    private void resetTimer() {
        pauseTimer();
        timeRemaining = 0;
        timeElapsed = 0;
        textTimer.setText("00:00");
        btnToggle.setText("시작");
    }

    private String formatTime(long millis) {
        int totalSeconds = (int)(millis / 1000);
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

}

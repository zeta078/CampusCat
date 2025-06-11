package com.example.campuscat;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Random;

public class MiniGameActivity extends AppCompatActivity {

    private ConstraintLayout rootLayout;
    private ImageView catNormal, catCatch;
    private TextView currencyText, timerText;
    private ImageButton pauseButton;
    private Handler handler = new Handler();
    private Random random = new Random();
    private int screenHeight, screenWidth;
    private int currency = 0;
    private boolean isPaused = false;
    private final int GAME_DURATION = 30000; // 30초
    private long timeLeft = GAME_DURATION;
    private CountDownTimer gameTimer;
    private MediaPlayer catchSound;

    private final int SPAWN_INTERVAL = 1000; // 물고기 생성 간격
    private final int FISH_SIZE = 100; // dp

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mini_game);


        rootLayout = findViewById(R.id.rootLayout);
        catNormal = findViewById(R.id.catNormal);
        catCatch = findViewById(R.id.catCatch);
        currencyText = findViewById(R.id.currencyText);
        timerText = findViewById(R.id.timerText);
        pauseButton = findViewById(R.id.pauseButton);



        rootLayout.post(() -> {
            screenHeight = rootLayout.getHeight();
            screenWidth = rootLayout.getWidth();
            startGame();
        });

        pauseButton.setOnClickListener(v -> pauseGame());
    }

    private void startGame() {
        startTimer(GAME_DURATION);
        handler.post(spawnFishRunnable);
    }

    private void startTimer(long duration) {
        gameTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;
                timerText.setText("남은 시간: " + millisUntilFinished / 1000 + "초");
            }

            @Override
            public void onFinish() {
                timerText.setText("게임 종료!");
                handler.removeCallbacksAndMessages(null);
            }
        }.start();
    }

    private void pauseGame() {
        isPaused = true;
        handler.removeCallbacks(spawnFishRunnable);
        if (gameTimer != null) gameTimer.cancel();

        new AlertDialog.Builder(this)
                .setTitle("게임 일시정지")
                .setMessage("계속하시겠습니까?")
                .setPositiveButton("계속하기", (dialog, which) -> resumeGame())
                .setNegativeButton("종료하기", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void resumeGame() {
        isPaused = false;
        startTimer(timeLeft);
        handler.post(spawnFishRunnable);
    }

    private Runnable spawnFishRunnable = new Runnable() {
        @Override
        public void run() {
            spawnFish();
            handler.postDelayed(this, SPAWN_INTERVAL);
        }
    };

    private void spawnFish() {
        ImageView fish = new ImageView(this);
        fish.setImageResource(R.drawable.fish_blue);
        int sizeInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, FISH_SIZE, getResources().getDisplayMetrics());
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(sizeInPx, sizeInPx);
        fish.setLayoutParams(params);
        rootLayout.addView(fish);

        int startX = random.nextInt(screenWidth - sizeInPx);
        fish.setX(startX);
        fish.setY(0);

        ObjectAnimator fallAnimator = ObjectAnimator.ofFloat(fish, "translationY", screenHeight);
        fallAnimator.setDuration(1000); // 빠르게 낙하
        fallAnimator.setInterpolator(new LinearInterpolator());
        fallAnimator.start();

        fish.setOnClickListener(v -> {
            if (catchSound != null) catchSound.start();
            showCatCatch();
            currency++;
            currencyText.setText(String.valueOf(currency));
            animateCurrencyGain();
            rootLayout.removeView(fish);
        });

        fallAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                rootLayout.removeView(fish);
            }
        });
    }

    private void animateCurrencyGain() {
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(currencyText, "scaleX", 1f, 1.5f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(currencyText, "scaleY", 1f, 1.5f);
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(currencyText, "scaleX", 1.5f, 1f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(currencyText, "scaleY", 1.5f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(200);
        animatorSet.play(scaleUpX).with(scaleUpY);
        animatorSet.play(scaleDownX).with(scaleDownY).after(scaleUpX);
        animatorSet.start();
    }

    private void showCatCatch() {
        catNormal.setVisibility(View.GONE);
        catCatch.setVisibility(View.VISIBLE);
        handler.postDelayed(() -> {
            catCatch.setVisibility(View.GONE);
            catNormal.setVisibility(View.VISIBLE);
        }, 300);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (catchSound != null) {
            catchSound.release();
            catchSound = null;
        }
        if (gameTimer != null) gameTimer.cancel();
        handler.removeCallbacksAndMessages(null);
    }
}
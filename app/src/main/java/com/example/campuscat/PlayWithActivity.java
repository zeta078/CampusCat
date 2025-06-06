package com.example.campuscat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Random;

public class PlayWithActivity extends AppCompatActivity {

    private ImageView catImage;
    private ProgressBar expBar;
    private TextView xpText;
    private int level, xp;
    private final int[] thresholds = {0, 300, 1000, 2000};
    private int pawsCaught = 0;
    private final int totalPaws = 10;
    private Handler handler = new Handler();
    private Random random = new Random();
    private ImageView[] pawImages = new ImageView[10];
    private ConstraintLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_with);

        catImage = findViewById(R.id.playCatImage);
        expBar = findViewById(R.id.expBar);
        xpText = findViewById(R.id.xpText);
        rootLayout = findViewById(R.id.rootLayout);

        // CatDetailActivity에서 전달된 값 우선 사용, 없으면 SharedPreferences 사용
        Intent intent = getIntent();
        level = intent.getIntExtra("catlevel", -1);
        xp = intent.getIntExtra("catxp", -1);

        if (level == -1 || xp == -1) {
            SharedPreferences prefs = getSharedPreferences("CatPrefs", MODE_PRIVATE);
            level = prefs.getInt("catlevel", 1);
            xp = prefs.getInt("catxp", 10);
        }

        updateCatImage();
        updateXPUI();

        for (int i = 0; i < totalPaws; i++) {
            pawImages[i] = new ImageView(this);
            pawImages[i].setImageResource(R.drawable.paw);
            pawImages[i].setVisibility(View.INVISIBLE);
            final int index = i;
            pawImages[i].setOnClickListener(v -> catchPaw(index));

            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(150, 150);
            rootLayout.addView(pawImages[i], params);
        }

        Button startBtn = findViewById(R.id.btnStartGame);
        startBtn.setOnClickListener(v -> startGame());

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void startGame() {
        pawsCaught = 0;
        for (ImageView paw : pawImages) paw.setVisibility(View.INVISIBLE);

        for (int i = 0; i < totalPaws; i++) {
            int delay = 1000 + random.nextInt(4000);
            int finalI = i;
            handler.postDelayed(() -> {
                ImageView paw = pawImages[finalI];
                paw.setX(random.nextInt(600));
                paw.setY(500 + random.nextInt(800));
                paw.setVisibility(View.VISIBLE);
                handler.postDelayed(() -> paw.setVisibility(View.INVISIBLE), 1500);
            }, delay);
        }
    }

    private void catchPaw(int index) {
        if (pawImages[index].getVisibility() == View.VISIBLE) {
            pawImages[index].setVisibility(View.INVISIBLE);
            pawsCaught++;
            if (pawsCaught == totalPaws) {
                Toast.makeText(this, "고양이와 성공적으로 놀았어요! +10XP", Toast.LENGTH_SHORT).show();
                xp += 10;
                checkLevelUp();
                updateXPUI();
                saveXP();
            }
        }
    }

    private void updateCatImage() {
        int resId = CatDetailActivity.getCurrentCatImageRes(level);
        catImage.setImageResource(resId);
    }

    private void updateXPUI() {
        int nextLevelXP = thresholds[level];
        int prevLevelXP = thresholds[level - 1];
        int progress = xp - prevLevelXP;
        int maxProgress = nextLevelXP - prevLevelXP;
        expBar.setMax(maxProgress);
        expBar.setProgress(progress);
        xpText.setText(progress + " / " + maxProgress + " XP");
    }

    private void checkLevelUp() {
        while (level < thresholds.length - 1 && xp >= thresholds[level]) {
            level++;
            updateCatImage();
        }
    }

    private void saveXP() {
        SharedPreferences.Editor editor = getSharedPreferences("CatPrefs", MODE_PRIVATE).edit();
        editor.putInt("catlevel", level);
        editor.putInt("catxp", xp);
        editor.apply();
    }
}

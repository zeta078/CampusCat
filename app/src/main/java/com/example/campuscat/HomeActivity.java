package com.example.campuscat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private ImageView catImage;
    private ProgressBar expBar;
    private TextView xpText, tvCatLevel;

    private int level, xp;
    private final int[] thresholds = {0, 300, 1000, 2000};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 뷰 초기화
        catImage = findViewById(R.id.catImage);
        expBar = findViewById(R.id.expBar);
        xpText = findViewById(R.id.xpText);
        tvCatLevel = findViewById(R.id.tvCatLevel);

        // 클릭 시 CatDetailActivity로 이동
        catImage.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CatDetailActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCatData(); // 앱 돌아올 때마다 최신 상태 로딩
    }

    private void loadCatData() {
        SharedPreferences prefs = getSharedPreferences("CatPrefs", MODE_PRIVATE);
        level = prefs.getInt("catlevel", 1);
        xp = prefs.getInt("catxp", 0);

        Log.d("🐱HomeActivity", "불러온 level: " + level + ", XP: " + xp);
        updateUI();
    }

    private void updateUI() {
        int nextXP = thresholds[level];
        int prevXP = thresholds[level - 1];
        int progress = xp - prevXP;
        int maxProgress = nextXP - prevXP;

        // 텍스트 및 바 갱신
        tvCatLevel.setText("LEVEL " + level);
        xpText.setText(progress + " / " + maxProgress + " XP");
        expBar.setMax(maxProgress);
        expBar.setProgress(progress);

        // 이미지도 갱신
        int imageResId = CatDetailActivity.getCurrentCatImageRes(level);
        Log.d("🐱HomeActivity", "이미지 리소스 ID: " + imageResId);
        catImage.setImageResource(imageResId);
    }
}

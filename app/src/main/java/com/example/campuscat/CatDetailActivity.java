package com.example.campuscat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CatDetailActivity extends AppCompatActivity {

    private ImageView catImage, decorRug, decorToy, fishIcon;
    private TextView levelText, xpText, fishCountText;
    private ProgressBar expBar;
    private int currentLevel, currentXp;
    private final int[] thresholds = {0, 300, 1000, 2000};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cat_detail);

        catImage = findViewById(R.id.catImage);
        decorRug = findViewById(R.id.decorRug);
        decorToy = findViewById(R.id.decorToy);
        levelText = findViewById(R.id.levelText);
        xpText = findViewById(R.id.xpText);
        expBar = findViewById(R.id.expBar);
        fishCountText = findViewById(R.id.fishCountText);
        fishIcon = findViewById(R.id.fishIcon);

        SharedPreferences prefs = getSharedPreferences("CatPrefs", MODE_PRIVATE);
        currentLevel = prefs.getInt("catlevel", 1);
        currentXp = prefs.getInt("catxp", 10);

        updateUI();
        updateFishCount();
        applyDecorations();

        catImage.setOnClickListener(v -> {
            currentXp += 10;
            checkLevelUp();
            updateUI();
            saveXP();
            Toast.makeText(this, "출석 완료! +10XP", Toast.LENGTH_SHORT).show();
        });

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            saveXP();
            finish();
        });

        ImageButton playWithButton = findViewById(R.id.btnPlayWithBottom);
        playWithButton.setOnClickListener(v -> {
            Intent intent = new Intent(CatDetailActivity.this, PlayWithActivity.class);
            intent.putExtra("catlevel", currentLevel);
            intent.putExtra("catxp", currentXp);
            startActivity(intent);
        });

        ImageButton inventoryButton = findViewById(R.id.btnInventoryBottom);
        inventoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(CatDetailActivity.this, InventoryActivity.class);
            startActivity(intent);
        });

        ImageButton miniGameButton = findViewById(R.id.btnMiniGameBottom);
        miniGameButton.setOnClickListener(v -> {
            Intent intent = new Intent(CatDetailActivity.this, MiniGameActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveXP();
    }

    @Override
    public void onBackPressed() {
        saveXP();
        super.onBackPressed();
    }

    private void updateUI() {
        int nextLevelXP = thresholds[currentLevel];
        int prevLevelXP = thresholds[currentLevel - 1];
        int progress = currentXp - prevLevelXP;
        int maxProgress = nextLevelXP - prevLevelXP;

        levelText.setText("LEVEL " + currentLevel);
        xpText.setText(progress + " / " + maxProgress + " XP");
        expBar.setMax(maxProgress);
        expBar.setProgress(progress);

        int imageResId = getCurrentCatImageRes(currentLevel);
        Log.d("🐱CatDetail", "표시 이미지 ID: " + imageResId);
        catImage.setImageResource(imageResId);
    }

    private void updateFishCount() {
        SharedPreferences prefs = getSharedPreferences("CatPrefs", MODE_PRIVATE);
        int fishCount = prefs.getInt("fish_count", 0);

        fishCountText.setText(String.valueOf(fishCount));
    }

    private void checkLevelUp() {
        while (currentLevel < thresholds.length - 1 && currentXp >= thresholds[currentLevel]) {
            currentLevel++;
            Log.d("🐱레벨업", "레벨업! 현재 레벨: " + currentLevel);
        }
    }

    private void saveXP() {
        SharedPreferences.Editor editor = getSharedPreferences("CatPrefs", MODE_PRIVATE).edit();
        editor.putInt("catlevel", currentLevel);
        editor.putInt("catxp", currentXp);
        editor.apply();
        Log.d("🐱CatDetail", "저장된 level: " + currentLevel + ", XP: " + currentXp);
    }

    public static int getCurrentCatImageRes(int level) {
        switch (level) {
            case 1: return R.drawable.cat_lv1;
            case 2: return R.drawable.cat_lv2;
            case 3: return R.drawable.cat_lv3;
            default: return R.drawable.cat_lv1;
        }
    }

    private void applyDecorations() {
        SharedPreferences prefs = getSharedPreferences("CatPrefs", MODE_PRIVATE);
        boolean hasRug = prefs.getBoolean("decor_rug", false);
        boolean hasToy = prefs.getBoolean("decor_toy", false);

        if (hasRug) decorRug.setVisibility(ImageView.VISIBLE);
        if (hasToy) decorToy.setVisibility(ImageView.VISIBLE);
    }
}

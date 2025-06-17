package com.example.campuscat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MissionActivity extends AppCompatActivity {

    private Button btnPlayMission, btnInventoryMission;
    private SharedPreferences prefs;
    private final String PREFS_NAME = "MissionPrefs";
    private final String KEY_PLAY_DONE = "PlayMissionDone";
    private final String KEY_INVENTORY_DONE = "InventoryMissionDone";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission);

        btnPlayMission = findViewById(R.id.btnPlayMission);
        btnInventoryMission = findViewById(R.id.btnInventoryMission);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // ✅ 미션 완료 상태에 따라 버튼 초기화
        initMissionButton(btnPlayMission, KEY_PLAY_DONE, 15);
        initMissionButton(btnInventoryMission, KEY_INVENTORY_DONE, 5);

        // 🎮 미니게임 미션
        btnPlayMission.setOnClickListener(v -> {
            if (!isMissionCompleted(KEY_PLAY_DONE)) {
                rewardXp(15);
                completeMission(KEY_PLAY_DONE, btnPlayMission);
            }
        });

        // 🎒 인벤토리 열기 미션
        btnInventoryMission.setOnClickListener(v -> {
            if (!isMissionCompleted(KEY_INVENTORY_DONE)) {
                rewardXp(5);
                completeMission(KEY_INVENTORY_DONE, btnInventoryMission);
            }
        });
    }

    // 미션 완료 여부 확인
    private boolean isMissionCompleted(String key) {
        return prefs.getBoolean(key, false);
    }

    // 미션 버튼 초기화
    private void initMissionButton(Button button, String key, int xp) {
        if (isMissionCompleted(key)) {
            button.setText("완료됨");
            button.setEnabled(false);
        } else {
            button.setText("보상받기 (+" + xp + " XP)");
            button.setEnabled(true);
        }
    }

    // 미션 완료 처리
    private void completeMission(String key, Button button) {
        prefs.edit().putBoolean(key, true).apply();
        button.setText("완료됨");
        button.setEnabled(false);
    }

    // XP 보상 지급
    private void rewardXp(int xp) {
        SharedPreferences catPrefs = getSharedPreferences("CatPrefs", MODE_PRIVATE);
        int currentXp = catPrefs.getInt("cat_xp", 0);
        catPrefs.edit().putInt("cat_xp", currentXp + xp).apply();
        Toast.makeText(this, xp + " XP 획득!", Toast.LENGTH_SHORT).show();
    }
}

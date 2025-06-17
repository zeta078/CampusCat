package com.example.campuscat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log; // Log 클래스 임포트 추가

import androidx.appcompat.app.AppCompatActivity;

public class MissionActivity extends AppCompatActivity {

    private Button btnPlayMission, btnInventoryMission;
    private SharedPreferences prefs;
    private final String PREFS_NAME = "MissionPrefs";
    private final String KEY_PLAY_DONE = "PlayMissionDone";
    private final String KEY_INVENTORY_DONE = "InventoryMissionDone";

    // 로그 태그
    private static final String TAG = "MissionActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission);

        Log.d(TAG, "onCreate: MissionActivity started."); // onCreate 진입 로그

        btnPlayMission = findViewById(R.id.btnPlayMission);
        btnInventoryMission = findViewById(R.id.btnInventoryMission);

        // findViewById 결과 확인
        if (btnPlayMission == null) {
            Log.e(TAG, "onCreate: btnPlayMission is null! Check activity_mission.xml for ID.");
            Toast.makeText(this, "오류: '놀아주기' 버튼을 찾을 수 없습니다.", Toast.LENGTH_LONG).show();
        }
        if (btnInventoryMission == null) {
            Log.e(TAG, "onCreate: btnInventoryMission is null! Check activity_mission.xml for ID.");
            Toast.makeText(this, "오류: '인벤토리' 버튼을 찾을 수 없습니다.", Toast.LENGTH_LONG).show();
        }

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Log.d(TAG, "onCreate: SharedPreferences initialized.");

        // ✅ 미션 완료 상태에 따라 버튼 초기화
        initMissionButton(btnPlayMission, KEY_PLAY_DONE, 15);
        initMissionButton(btnInventoryMission, KEY_INVENTORY_DONE, 5);

        // 🎮 미니게임 미션
        if (btnPlayMission != null) { // null 체크 추가
            btnPlayMission.setOnClickListener(v -> {
                Log.d(TAG, "btnPlayMission clicked. Current PlayMissionDone: " + isMissionCompleted(KEY_PLAY_DONE));
                if (!isMissionCompleted(KEY_PLAY_DONE)) {
                    // PlayWithActivity로 이동
                    Intent intent = new Intent(MissionActivity.this, PlayWithActivity.class);
                    startActivity(intent);
                    Log.d(TAG, "Starting PlayWithActivity.");
                    // 여기서는 예시로 버튼 클릭 시 바로 XP를 지급하고 미션 완료 처리하도록 했습니다.
                    rewardXp(15);
                    completeMission(KEY_PLAY_DONE, btnPlayMission);
                    Log.d(TAG, "Play Mission completed and XP rewarded.");
                } else {
                    Log.d(TAG, "Play Mission: Already completed.");
                    Toast.makeText(this, "이미 완료된 미션입니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // 🎒 인벤토리 열기 미션
        if (btnInventoryMission != null) { // null 체크 추가
            btnInventoryMission.setOnClickListener(v -> {
                Log.d(TAG, "btnInventoryMission clicked. Current InventoryMissionDone: " + isMissionCompleted(KEY_INVENTORY_DONE));
                if (!isMissionCompleted(KEY_INVENTORY_DONE)) {
                    // InventoryActivity로 이동
                    Intent intent = new Intent(MissionActivity.this, InventoryActivity.class);
                    startActivity(intent);
                    Log.d(TAG, "Starting InventoryActivity.");
                    // 여기서는 예시로 버튼 클릭 시 바로 XP를 지급하고 미션 완료 처리하도록 했습니다.
                    rewardXp(5);
                    completeMission(KEY_INVENTORY_DONE, btnInventoryMission);
                    Log.d(TAG, "Inventory Mission completed and XP rewarded.");
                } else {
                    Log.d(TAG, "Inventory Mission: Already completed.");
                    Toast.makeText(this, "이미 완료된 미션입니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // 미션 완료 여부 확인
    private boolean isMissionCompleted(String key) {
        boolean completed = prefs.getBoolean(key, false);
        Log.d(TAG, "isMissionCompleted for key '" + key + "': " + completed);
        return completed;
    }

    // 미션 버튼 초기화
    private void initMissionButton(Button button, String key, int xp) {
        if (button == null) {
            Log.e(TAG, "initMissionButton: Button is null for key " + key);
            return; // null인 경우 더 이상 진행하지 않음
        }

        if (isMissionCompleted(key)) {
            button.setText("완료됨");
            button.setEnabled(false);
            Log.d(TAG, "initMissionButton: Mission '" + key + "' is completed. Button set to '완료됨'.");
        } else {
            button.setText("보상받기 (+" + xp + " XP)");
            button.setEnabled(true);
            Log.d(TAG, "initMissionButton: Mission '" + key + "' is not completed. Button set to '보상받기'.");
        }
    }

    // 미션 완료 처리
    private void completeMission(String key, Button button) {
        prefs.edit().putBoolean(key, true).apply();
        if (button != null) { // null 체크 추가
            button.setText("완료됨");
            button.setEnabled(false);
        }
        Log.d(TAG, "completeMission: Mission '" + key + "' marked as completed.");
    }

    // XP 보상 지급
    private void rewardXp(int xp) {
        SharedPreferences catPrefs = getSharedPreferences("CatPrefs", MODE_PRIVATE);
        int currentXp = catPrefs.getInt("cat_xp", 0);
        catPrefs.edit().putInt("cat_xp", currentXp + xp).apply();
        Toast.makeText(this, xp + " XP 획득!", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "rewardXp: " + xp + " XP rewarded. Current XP: " + (currentXp + xp));
    }
}
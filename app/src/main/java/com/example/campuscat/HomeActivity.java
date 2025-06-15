package com.example.campuscat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private Button btnMission, btnSettings;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);  // 최초 화면

        // 미션, 설정 버튼
        btnMission = findViewById(R.id.btnMission);
        btnSettings = findViewById(R.id.btnSettings);

        btnMission.setOnClickListener(v ->
                setContentView(R.layout.activity_mission));

        btnSettings.setOnClickListener(v ->
                setContentView(R.layout.activity_settings));

        // 고양이 이미지 클릭 → 상세화면으로 이동
        ImageView catImage = findViewById(R.id.catImage);
        catImage.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CatDetailActivity.class);
            startActivity(intent);
        });

        /*
        // 하단 바 이동 처리
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_calendar) {
                setContentView(R.layout.fragment_calendar);
            } else if (id == R.id.nav_planner) {
                setContentView(R.layout.planner_layout);
            } else if (id == R.id.nav_home) {
                setContentView(R.layout.activity_home);
            } else if (id == R.id.nav_study) {
                setContentView(R.layout.fragment_study);
            } else if (id == R.id.nav_more) {
                setContentView(R.layout.fragment_more);
            }

            return true;
        });
         */
    }
}

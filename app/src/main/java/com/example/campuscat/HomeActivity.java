package com.example.campuscat;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    Button btnCalendar, btnPlanner, btnHome, btnStudy, btnMore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnCalendar = findViewById(R.id.btnCalendar);
        btnPlanner = findViewById(R.id.btnPlanner);
        btnHome = findViewById(R.id.btnHome);
        btnStudy = findViewById(R.id.btnStudy);
        btnMore = findViewById(R.id.btnMore);

        // 기본 프래그먼트
        loadFragment(new HomeFragment());

        btnCalendar.setOnClickListener(v -> loadFragment(new CalendarFragment()));
        btnPlanner.setOnClickListener(v -> loadFragment(new PlannerFragment()));
        btnHome.setOnClickListener(v -> loadFragment(new HomeFragment()));
        btnStudy.setOnClickListener(v -> loadFragment(new StudyFragment()));
        btnMore.setOnClickListener(v -> loadFragment(new MoreFragment()));
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}

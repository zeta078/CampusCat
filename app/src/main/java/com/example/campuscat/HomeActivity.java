package com.example.campuscat;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 첫 화면에 기본 프래그먼트 설정
        loadFragment(new HomeFragment());

        // 바텀 네비게이션 연결 및 리스너 설정
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_view);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_calendar) {
                selectedFragment = new CalendarFragment();
            } else if (id == R.id.nav_planner) {
                selectedFragment = new PlannerFragment();
            } else if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_study) {
                selectedFragment = new StudyFragment();
            } else if (id == R.id.nav_more) {
                selectedFragment = new MoreFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });
    }

    // 프래그먼트를 교체하는 메서드
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment) // activity_home.xml에 fragment_container 있어야 함
                .commit();
    }
}

package com.example.campuscat;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        bottomNavigationView.setOnItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selected = null;
                        int id = item.getItemId();

                        if (id == R.id.nav_home) {
                            selected = new HomeFragment();
                        } else if (id == R.id.nav_calendar) {
                            selected = new CalendarFragment();
                        } else if (id == R.id.nav_planner) {
                            selected = new PlannerFragment();
                        } else if (id == R.id.nav_study) {
                            selected = new StudyFragment();
                        } else if (id == R.id.nav_more) {
                            selected = new MoreFragment();
                        }

                        if (selected != null) {
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment_container, selected)
                                    .commit();
                        }
                        return true;
                    }
                }
        );
    }
}

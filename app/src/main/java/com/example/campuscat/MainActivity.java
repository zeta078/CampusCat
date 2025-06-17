package com.example.campuscat;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
// FragmentTransaction은 이미 상위에 import 되어 있어 중복 제거

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    // **** 프래그먼트 인스턴스 선언 ****
    private HomeFragment homeFragment;
    private CalendarFragment calendarFragment;
    private PlannerFragment plannerFragment;
    private TimetableFragment timetableFragment;
    private MoreFragment moreFragment;
    private StudyFragment studyFragment; // StudyFragment 추가
    // ****************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.nav_home); // 앱 시작 시 홈 탭 선택

        FragmentManager fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            // **** 모든 프래그먼트 인스턴스 생성 (앱 시작 시 한 번만) ****
            homeFragment = new HomeFragment();
            calendarFragment = new CalendarFragment();
            plannerFragment = new PlannerFragment();
            timetableFragment = new TimetableFragment();
            moreFragment = new MoreFragment();
            studyFragment = new StudyFragment(); // StudyFragment 인스턴스 생성

            // **** 핵심: StudyFragment에 PlannerFragment 인스턴스 주입 ****
            // StudyFragment가 PlannerFragment의 updateStudyTime() 메서드를 호출할 수 있도록 연결합니다.
            studyFragment.setPlannerFragment(plannerFragment);
            // ************************************************************

            // 앱 시작 시 HomeFragment를 기본으로 표시
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, homeFragment, HomeFragment.class.getName())
                    .commit();
        } else {
            // 액티비티가 재생성될 때 (예: 화면 회전), 기존 프래그먼트 인스턴스를 찾아서 연결합니다.
            // replace 시 사용한 태그 (여기서는 클래스 이름)를 사용하여 찾습니다.
            homeFragment = (HomeFragment) fragmentManager.findFragmentByTag(HomeFragment.class.getName());
            calendarFragment = (CalendarFragment) fragmentManager.findFragmentByTag(CalendarFragment.class.getName());
            plannerFragment = (PlannerFragment) fragmentManager.findFragmentByTag(PlannerFragment.class.getName());
            timetableFragment = (TimetableFragment) fragmentManager.findFragmentByTag(TimetableFragment.class.getName());
            moreFragment = (MoreFragment) fragmentManager.findFragmentByTag(MoreFragment.class.getName());
            studyFragment = (StudyFragment) fragmentManager.findFragmentByTag(StudyFragment.class.getName());

            // 복원된 StudyFragment에 PlannerFragment 참조가 끊어졌을 수 있으므로 다시 주입을 시도합니다.
            if (studyFragment != null && plannerFragment != null) {
                studyFragment.setPlannerFragment(plannerFragment);
            }
        }

        bottomNavigationView.setOnItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selected = null;
                        int id = item.getItemId();

                        if (id == R.id.nav_home) {
                            selected = homeFragment;
                        } else if (id == R.id.nav_calendar) {
                            selected = calendarFragment;
                        } else if (id == R.id.nav_planner) {
                            selected = plannerFragment;
                        } else if (id == R.id.nav_timetable) {
                            selected = timetableFragment;
                        } else if (id == R.id.nav_more) {
                            selected = moreFragment;
                        }


                        if (selected != null) {
                            // 선택된 프래그먼트로 전환하고, 클래스 이름을 태그로 부여합니다.
                            fragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, selected, selected.getClass().getName())
                                    .commit();
                        }
                        return true;
                    }
                }
        );
    }

    // **** 새로 추가되는 부분: MoreFragment에서 StudyFragment로 전환을 요청할 메서드 ****
    public void showStudyFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, studyFragment, StudyFragment.class.getName())
                .addToBackStack(null) // '뒤로가기' 버튼으로 MoreFragment로 돌아갈 수 있도록 스택에 추가
                .commit();
    }
    // ********************************************************************************
}
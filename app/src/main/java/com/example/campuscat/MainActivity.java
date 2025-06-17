package com.example.campuscat;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ALL_PERMISSIONS = 1001;
    private static final int REQUEST_EXACT_ALARM_PERMISSION = 1002;

    // 미션 관련 프래그먼트 전환을 위한 상수 추가
    public static final String ACTION_SHOW_FRAGMENT = "com.example.campuscat.ACTION_SHOW_FRAGMENT";
    public static final String EXTRA_FRAGMENT_TO_SHOW = "fragment_to_show";
    public static final String FRAGMENT_MISSION = "mission_fragment"; // 미션 프래그먼트 표시를 위한 상수

    private BottomNavigationView bottomNavigationView;

    private HomeFragment homeFragment;
    private CalendarFragment calendarFragment;
    private PlannerFragment plannerFragment;
    private TimetableFragment timetableFragment;
    private MoreFragment moreFragment;
    private StudyFragment studyFragment;
    private MissionFragment missionFragment; // 새롭게 추가할 미션 프래그먼트

    private List<String> pendingPermissions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        FragmentManager fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            homeFragment = new HomeFragment();
            calendarFragment = new CalendarFragment();
            plannerFragment = new PlannerFragment();
            timetableFragment = new TimetableFragment();
            moreFragment = new MoreFragment();
            studyFragment = new StudyFragment();
            missionFragment = new MissionFragment(); // 미션 프래그먼트 초기화

            studyFragment.setPlannerFragment(plannerFragment);

            // 초기 프래그먼트 로드 (홈)
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, homeFragment, HomeFragment.class.getName())
                    .commit();

            requestAllPermissions();

        } else {
            homeFragment = (HomeFragment) fragmentManager.findFragmentByTag(HomeFragment.class.getName());
            calendarFragment = (CalendarFragment) fragmentManager.findFragmentByTag(CalendarFragment.class.getName());
            plannerFragment = (PlannerFragment) fragmentManager.findFragmentByTag(PlannerFragment.class.getName());
            timetableFragment = (TimetableFragment) fragmentManager.findFragmentByTag(TimetableFragment.class.getName());
            moreFragment = (MoreFragment) fragmentManager.findFragmentByTag(MoreFragment.class.getName());
            studyFragment = (StudyFragment) fragmentManager.findFragmentByTag(StudyFragment.class.getName());
            missionFragment = (MissionFragment) fragmentManager.findFragmentByTag(MissionFragment.class.getName()); // 미션 프래그먼트 찾기

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
                            fragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, selected, selected.getClass().getName())
                                    .commit();
                        }
                        return true;
                    }
                }
        );

        // Intent에서 프래그먼트 전환 요청 확인
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // 새로운 인텐트로 설정 (중요: 기존 인텐트를 대체)
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && ACTION_SHOW_FRAGMENT.equals(intent.getAction())) {
            String fragmentToShow = intent.getStringExtra(EXTRA_FRAGMENT_TO_SHOW);
            if (FRAGMENT_MISSION.equals(fragmentToShow)) {
                showMissionFragment(); // 미션 프래그먼트 표시
            }
            // 다른 프래그먼트도 필요하다면 여기에 추가
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager != null && alarmManager.canScheduleExactAlarms() && !pendingPermissions.isEmpty()) {
                requestRemainingPermissions();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void requestExactAlarmPermission() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(this, "정확한 알람을 위해 '알람 및 미리 알림' 권한을 허용해주세요.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.setData(Uri.fromParts("package", getPackageName(), null));
            startActivityForResult(intent, REQUEST_EXACT_ALARM_PERMISSION);
        } else {
            requestRemainingPermissions();
        }
    }

    private void requestAllPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingPermissions.clear();

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                pendingPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                pendingPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    pendingPermissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    pendingPermissions.add(Manifest.permission.POST_NOTIFICATIONS);
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestExactAlarmPermission();
            } else {
                requestRemainingPermissions();
            }
        } else {
            Toast.makeText(this, "이 기기는 런타임 권한 요청이 필요 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestRemainingPermissions() {
        if (!pendingPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    pendingPermissions.toArray(new String[0]),
                    REQUEST_ALL_PERMISSIONS);
            pendingPermissions.clear();
        } else {
            Toast.makeText(this, "필수 권한이 모두 허용되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EXACT_ALARM_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                if (alarmManager != null && alarmManager.canScheduleExactAlarms()) {
                    Toast.makeText(this, "정확한 알람 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "정확한 알람 권한이 거부되었습니다.", Toast.LENGTH_LONG).show();
                }
            }
            requestRemainingPermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ALL_PERMISSIONS) {
            boolean allGranted = true;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    String deniedPermission = permissions[i];
                    Toast.makeText(this, deniedPermission + " 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            if (allGranted) {
                Toast.makeText(this, "모든 필수 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "일부 필수 권한이 거부되었습니다. 앱 사용에 제한이 있을 수 있습니다.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void showStudyFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, studyFragment, StudyFragment.class.getName())
                .addToBackStack(null)
                .commit();
    }

    // MissionFragment를 표시하는 새로운 메서드 추가
    public void showMissionFragment() {
        // 하단 네비게이션 뷰에서 해당 항목이 선택된 것처럼 보이도록 설정할 수도 있습니다.
        // 예를 들어, MissionFragment가 MoreFragment의 서브 항목이라면 nav_more를 선택.
        // bottomNavigationView.setSelectedItemId(R.id.nav_more);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, missionFragment, MissionFragment.class.getName())
                .addToBackStack(null) // 뒤로 가기 버튼으로 이전 프래그먼트로 돌아갈 수 있도록
                .commit();
    }
}
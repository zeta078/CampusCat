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
    private static final int REQUEST_EXACT_ALARM_PERMISSION = 1002; // 정확한 알람 권한 요청 코드 추가

    private BottomNavigationView bottomNavigationView;

    private HomeFragment homeFragment;
    private CalendarFragment calendarFragment;
    private PlannerFragment plannerFragment;
    private TimetableFragment timetableFragment;
    private MoreFragment moreFragment;
    private StudyFragment studyFragment;

    // 보류된 권한 목록
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

            studyFragment.setPlannerFragment(plannerFragment);

            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, homeFragment, HomeFragment.class.getName())
                    .commit();

            // 앱 시작 시 권한 요청
            requestAllPermissions();

        } else {
            homeFragment = (HomeFragment) fragmentManager.findFragmentByTag(HomeFragment.class.getName());
            calendarFragment = (CalendarFragment) fragmentManager.findFragmentByTag(CalendarFragment.class.getName());
            plannerFragment = (PlannerFragment) fragmentManager.findFragmentByTag(PlannerFragment.class.getName());
            timetableFragment = (TimetableFragment) fragmentManager.findFragmentByTag(TimetableFragment.class.getName());
            moreFragment = (MoreFragment) fragmentManager.findFragmentByTag(MoreFragment.class.getName());
            studyFragment = (StudyFragment) fragmentManager.findFragmentByTag(StudyFragment.class.getName());

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 정확한 알람 권한 요청 후 앱으로 돌아왔을 때 남은 권한 요청 처리
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
            startActivityForResult(intent, REQUEST_EXACT_ALARM_PERMISSION); // startActivityForResult 사용
        } else {
            // 이미 권한이 있거나, S 버전 미만일 경우 바로 남은 권한 요청
            requestRemainingPermissions();
        }
    }

    private void requestAllPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingPermissions.clear(); // 초기화

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
                // S 버전 이상에서는 정확한 알람 권한을 먼저 요청하고, 그 다음에 다른 권한 요청
                requestExactAlarmPermission();
            } else {
                // S 버전 미만에서는 바로 남은 권한 요청
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
            pendingPermissions.clear(); // 요청 후 목록 비우기
        } else {
            Toast.makeText(this, "필수 권한이 모두 허용되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EXACT_ALARM_PERMISSION) {
            // 정확한 알람 권한 설정 화면에서 돌아왔을 때
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                if (alarmManager != null && alarmManager.canScheduleExactAlarms()) {
                    Toast.makeText(this, "정확한 알람 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "정확한 알람 권한이 거부되었습니다.", Toast.LENGTH_LONG).show();
                }
            }
            // 이제 남은 권한들을 요청합니다.
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
}
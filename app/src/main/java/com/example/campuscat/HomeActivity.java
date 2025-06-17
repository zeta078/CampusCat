package com.example.campuscat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private ImageView catImage;
    private ProgressBar expBar;
    private TextView xpText, tvCatLevel;

    private int level, xp;
    private final int[] thresholds = {0, 300, 1000, 2000};

    // 권한 요청 결과를 처리하기 위한 ActivityResultLauncher
    private ActivityResultLauncher<String[]> requestMultiplePermissionsLauncher;
    private ActivityResultLauncher<String> requestNotificationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 뷰 초기화
        catImage = findViewById(R.id.catImage);
        expBar = findViewById(R.id.expBar);
        xpText = findViewById(R.id.xpText);
        tvCatLevel = findViewById(R.id.tvCatLevel);

        // 클릭 시 CatDetailActivity로 이동
        catImage.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CatDetailActivity.class);
            startActivity(intent);
        });

        // 권한 요청 런처 초기화
        initPermissionLaunchers();

        // 서비스 시작을 위한 권한 확인 및 요청
        checkAndRequestPermissions();

        // AttendanceCheckerService 시작
        startAttendanceService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCatData(); // 앱 돌아올 때마다 최신 상태 로딩
        // 혹시 권한 상태가 변경되었을 수 있으니 다시 확인 (옵션)
        // checkAndRequestPermissions();
    }

    private void loadCatData() {
        SharedPreferences prefs = getSharedPreferences("CatPrefs", MODE_PRIVATE);
        level = prefs.getInt("catlevel", 1);
        xp = prefs.getInt("catxp", 0);

        Log.d("🐱HomeActivity", "불러온 level: " + level + ", XP: " + xp);
        updateUI();
    }

    private void updateUI() {
        // 현재 레벨이 thresholds 배열의 범위를 벗어나지 않도록 방어 로직 추가
        if (level < 1) level = 1;
        if (level >= thresholds.length) {
            // 최대 레벨 달성 시
            tvCatLevel.setText("LEVEL MAX");
            xpText.setText(xp + " XP"); // 총 XP 표시
            expBar.setMax(1); // 프로그레스 바는 꽉 차도록
            expBar.setProgress(1);
            int imageResId = CatDetailActivity.getCurrentCatImageRes(level); // 최대 레벨 이미지 가져오기
            catImage.setImageResource(imageResId);
            return;
        }

        int nextXP = thresholds[level];
        int prevXP = thresholds[level - 1];
        int progress = xp - prevXP;
        int maxProgress = nextXP - prevXP;

        // 경험치가 현재 레벨의 최대치를 넘어섰을 경우 (레벨업이 필요한 경우)
        // 이 부분은 CatDetailActivity나 별도의 레벨업 로직에서 처리하는 것이 좋지만,
        // UI 표시를 위해 임시적으로 여기서도 조정 가능
        if (progress < 0) progress = 0; // 혹시 XP가 낮아지는 경우 방지
        if (progress > maxProgress) progress = maxProgress; // 최대치 초과 방지

        // 텍스트 및 바 갱신
        tvCatLevel.setText("LEVEL " + level);
        xpText.setText(progress + " / " + maxProgress + " XP");
        expBar.setMax(maxProgress);
        expBar.setProgress(progress);

        // 이미지도 갱신
        int imageResId = CatDetailActivity.getCurrentCatImageRes(level);
        Log.d("🐱HomeActivity", "이미지 리소스 ID: " + imageResId);
        catImage.setImageResource(imageResId);
    }

    // --- 권한 요청 및 서비스 시작 로직 (추가/통합된 부분) ---

    private void initPermissionLaunchers() {
        requestMultiplePermissionsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    boolean fineLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                    boolean coarseLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                    boolean backgroundLocationGranted = true; // 기본값 true (API < 29)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        backgroundLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_BACKGROUND_LOCATION, false);
                    }

                    if (fineLocationGranted && coarseLocationGranted && backgroundLocationGranted) {
                        Toast.makeText(this, "위치 권한이 모두 허용되었습니다.", Toast.LENGTH_SHORT).show();
                        startAttendanceService(); // 권한 허용 시 서비스 시작
                    } else {
                        Toast.makeText(this, "자동 출석 기능을 위해 위치 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                        // 사용자가 권한을 거부한 경우, 설정으로 이동하라는 안내 등 추가 처리 가능
                        if (!fineLocationGranted || !coarseLocationGranted) {
                            showLocationPermissionDeniedDialog();
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !backgroundLocationGranted) {
                            showBackgroundLocationPermissionDeniedDialog();
                        }
                    }
                });

        requestNotificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Toast.makeText(this, "알림 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "출석 알림을 받으려면 알림 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        // Android 10 (API 29) 이상에서만 백그라운드 위치 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }

        if (!permissionsToRequest.isEmpty()) {
            // 이전에 권한 요청 다이얼로그를 보여줬는지 확인하고, 필요 시 사용자에게 교육적 설명을 제공
            // ActivityCompat.shouldShowRequestPermissionRationale() 사용
            requestMultiplePermissionsLauncher.launch(permissionsToRequest.toArray(new String[0]));
        }

        // Android 13 (API 33) 이상에서만 알림 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        // Android 12 (API 31) 이상에서 정확한 알람 권한 요청 (PlannerFragment의 schedulePlannerResetAlarm()에서 사용)
        // 앱이 정확한 알람을 예약할 수 있는지 확인 (setExactAndAllowWhileIdle 등)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                // 권한이 없으므로 사용자에게 설정 화면으로 이동하도록 유도
                new AlertDialog.Builder(this)
                        .setTitle("알람 권한 필요")
                        .setMessage("플래너 초기화 및 정확한 알람 기능을 위해 '알람 및 미리 알림' 권한이 필요합니다. 설정에서 허용해주세요.")
                        .setPositiveButton("설정으로 이동", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            startActivity(intent);
                        })
                        .setNegativeButton("취소", null)
                        .show();
            }
        }
    }

    private void startAttendanceService() {
        Intent serviceIntent = new Intent(this, AttendanceCheckerService.class);
        // 권한이 없으면 서비스가 제대로 작동하지 않으므로, 권한이 있을 때만 시작 시도
        // 또는 서비스 내부에서 권한을 다시 확인하고 기능 제한
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8.0 (API 26) 이상부터는 startService() 대신 startForegroundService() 사용
            // 단, startForegroundService() 호출 후 5초 이내에 서비스는 startForeground()를 호출해야 함.
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // 백그라운드 위치 권한 (API 29 이상)도 있으면 더 좋음
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    ContextCompat.startForegroundService(this, serviceIntent);
                    Log.d("HomeActivity", "AttendanceCheckerService 시작 요청 완료 (Foreground).");
                } else {
                    Log.w("HomeActivity", "백그라운드 위치 권한이 없어 AttendanceCheckerService를 포그라운드로 시작하지 않음.");
                    Toast.makeText(this, "자동 출석을 위해 '항상 허용' 위치 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.w("HomeActivity", "위치 권한이 없어 AttendanceCheckerService를 시작하지 않음.");
            }
        } else {
            // Android 7.1 (API 25) 이하 버전
            startService(serviceIntent);
            Log.d("HomeActivity", "AttendanceCheckerService 시작 요청 완료.");
        }
    }

    // 위치 권한이 영구적으로 거부되었을 때 사용자에게 설정 변경을 유도하는 다이얼로그
    private void showLocationPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("위치 권한 필요")
                .setMessage("자동 출석 기능을 사용하려면 위치 권한이 필요합니다. 앱 설정에서 권한을 허용해주세요.")
                .setPositiveButton("설정으로 이동", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("취소", null)
                .show();
    }

    // 백그라운드 위치 권한이 거부되었을 때 (Android 10+)
    private void showBackgroundLocationPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("백그라운드 위치 권한 필요")
                .setMessage("앱이 종료되어도 자동 출석 기능을 사용하려면 '항상 허용'으로 위치 권한을 설정해야 합니다. 앱 설정에서 변경해주세요.")
                .setPositiveButton("설정으로 이동", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("취소", null)
                .show();
    }
}
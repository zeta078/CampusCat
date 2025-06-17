package com.example.campuscat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AttendanceCheckerService extends Service {

    private static final String TAG = "AttendanceService";
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Gson gson = new Gson();

    // 지오펜싱 관련
    private com.google.android.gms.location.GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "AttendanceCheckerService onCreate");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geofencingClient = LocationServices.getGeofencingClient(this);

        createNotificationChannel();
        startForeground(Constants.ATTENDANCE_NOTIFICATION_ID, createForegroundNotification("출석 체크 서비스 실행 중..."));

        setupLocationCallback();
        requestLocationUpdates(); // 서비스 시작 시 위치 업데이트 요청
        addGeofences(); // 지오펜스 추가
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        // 지오펜스 이벤트 처리 (GeofenceBroadcastReceiver에서 호출될 수 있음)
        if (intent != null && GeofencingEvent.fromIntent(intent) != null) {
            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            if (geofencingEvent.hasError()) {
                Log.e(TAG, "GeofencingEvent Error: " + geofencingEvent.getErrorCode());
                return START_STICKY;
            }

            int geofenceTransition = geofencingEvent.getGeofenceTransition();
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.d(TAG, "캠퍼스 지오펜스 진입 감지!");
                Toast.makeText(this, "캠퍼스 진입 감지!", Toast.LENGTH_SHORT).show();
                checkAttendanceOnCampusEntry();
            } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                Log.d(TAG, "캠퍼스 지오펜스 이탈 감지!");
                Toast.makeText(this, "캠퍼스 이탈 감지!", Toast.LENGTH_SHORT).show();
            }
        } else {
            // 일반적인 서비스 시작 (예: 부팅 완료, 앱 실행 등)
            // 1. 현재 위치를 기반으로 캠퍼스 내에 있는지 확인
            // 2. 시간표 확인 로직 실행
            checkLocationAndAttendance();
        }

        return START_STICKY; // 서비스가 강제로 종료되어도 시스템이 재시작하도록
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    Constants.ATTENDANCE_NOTIFICATION_CHANNEL_ID,
                    Constants.ATTENDANCE_NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW // 포그라운드 서비스는 낮은 중요도로 충분
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private Notification createForegroundNotification(String contentText) {
        Intent notificationIntent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, Constants.ATTENDANCE_NOTIFICATION_CHANNEL_ID)
                .setContentTitle("CampusCat 출석 도우미")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // 앱 아이콘 등으로 변경
                .setContentIntent(pendingIntent)
                .setOngoing(true) // 사용자가 스와이프하여 제거할 수 없도록
                .build();
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.d(TAG, "새로운 위치: " + location.getLatitude() + ", " + location.getLongitude());
                    // 이 콜백은 지속적으로 호출되므로,
                    // 여기에 직접 출석 체크 로직을 넣기보다는 지오펜스 진입 시 트리거하는 것이 효율적입니다.
                    // 필요하다면 이곳에서 주기적으로 캠퍼스 내 위치 여부를 확인할 수도 있습니다.
                }
            }
        };
    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "위치 권한 없음. 위치 업데이트 요청 실패.");
            return;
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(TimeUnit.MINUTES.toMillis(10)); // 10분마다 위치 업데이트
        locationRequest.setFastestInterval(TimeUnit.MINUTES.toMillis(5)); // 가장 빠른 업데이트 5분
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY); // 배터리 효율성 고려

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(locationSettingsResponse -> {
            // 모든 위치 설정이 만족됨, 위치 업데이트 시작 가능
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            Log.d(TAG, "위치 업데이트 요청 성공.");
        });

        task.addOnFailureListener(e -> {
            // 위치 설정이 만족되지 않음 (예: GPS 꺼짐)
            Log.e(TAG, "위치 설정 확인 실패: " + e.getMessage());
        });
    }

    private void addGeofences() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "지오펜스 추가를 위한 위치 권한 없음.");
            return;
        }

        List<Geofence> geofenceList = new ArrayList<>();
        geofenceList.add(new Geofence.Builder()
                .setRequestId(Constants.GEOFENCE_REQUEST_ID)
                .setCircularRegion(
                        Constants.JEONJU_UNIV_LATITUDE,
                        Constants.JEONJU_UNIV_LONGITUDE,
                        Constants.JEONJU_UNIV_RADIUS_METERS
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE) // 영구 유지
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());

        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER) // 현재 위치가 지오펜스 안에 있다면 바로 트리거
                .addGeofences(geofenceList)
                .build();

        geofencingClient.addGeofences(geofencingRequest, getGeofencePendingIntent())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "지오펜스 추가 성공!"))
                .addOnFailureListener(e -> Log.e(TAG, "지오펜스 추가 실패: " + e.getMessage()));
    }

    private PendingIntent getGeofencePendingIntent() {
        // 이미 PendingIntent가 있다면 재사용
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        // GeofenceBroadcastReceiver를 통해 이벤트를 받음
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        return geofencePendingIntent;
    }

    // 서비스 시작 시 또는 지오펜스 진입 시 호출
    private void checkLocationAndAttendance() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "위치 권한이 없어 현재 위치 확인 불가.");
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        Log.d(TAG, "현재 위치: " + location.getLatitude() + ", " + location.getLongitude());
                        // 현재 위치가 전주대 캠퍼스 내부에 있는지 확인
                        float[] results = new float[1];
                        Location.distanceBetween(
                                Constants.JEONJU_UNIV_LATITUDE,
                                Constants.JEONJU_UNIV_LONGITUDE,
                                location.getLatitude(),
                                location.getLongitude(),
                                results
                        );
                        boolean isInCampus = results[0] <= Constants.JEONJU_UNIV_RADIUS_METERS;
                        Log.d(TAG, "캠퍼스 중심까지 거리: " + results[0] + "m, 캠퍼스 내부: " + isInCampus);

                        if (isInCampus) {
                            checkAttendanceLogic(); // 캠퍼스 내부에 있다면 출석 로직 실행
                        } else {
                            Log.d(TAG, "현재 캠퍼스 외부에 있습니다.");
                        }
                    } else {
                        Log.d(TAG, "마지막 위치를 가져올 수 없습니다. GPS가 꺼져 있거나 위치 정보가 없는 경우.");
                        Toast.makeText(this, "위치 정보를 가져올 수 없습니다. GPS를 켜주세요.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "마지막 위치 가져오기 실패: " + e.getMessage());
                });
    }

    // 지오펜스 진입 시 직접 호출 (지오펜스 이벤트는 이 서비스 자체로 오는 것이 아니므로, BootReceiver에서 호출)
    // 혹은 GeofenceBroadcastReceiver가 이 서비스를 시작시키고 이 함수를 호출하도록 구현
    private void checkAttendanceOnCampusEntry() {
        Log.d(TAG, "checkAttendanceOnCampusEntry 호출됨");
        checkAttendanceLogic();
    }


    // 시간표 확인 및 출석 처리 로직
    private void checkAttendanceLogic() {
        Log.d(TAG, "checkAttendanceLogic 실행");
        SharedPreferences timetablePrefs = getSharedPreferences(Constants.TIMETABLE_PREFS_NAME, Context.MODE_PRIVATE);
        String timetableJson = timetablePrefs.getString(Constants.TIMETABLE_ITEMS_KEY, "[]");

        Type type = new TypeToken<ArrayList<TimetableItem>>() {}.getType();
        List<TimetableItem> timetableItems = gson.fromJson(timetableJson, type);

        if (timetableItems == null || timetableItems.isEmpty()) {
            Log.d(TAG, "등록된 시간표가 없습니다.");
            return;
        }

        Calendar now = Calendar.getInstance();
        int currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK); // Calendar.MONDAY 등
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);
        int currentTimeInMinutes = currentHour * 60 + currentMinute;

        String todayDayString = getDayString(currentDayOfWeek);
        String todayDateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        SharedPreferences attendanceLogPrefs = getSharedPreferences(Constants.ATTENDANCE_LOG_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor logEditor = attendanceLogPrefs.edit();

        for (TimetableItem item : timetableItems) {
            // 오늘 요일에 해당하는 수업인지 확인
            if (item.getDay().equals(todayDayString)) {
                int startTimeInMinutes = parseTimeToMin(item.getStartTime());
                int endTimeInMinutes = parseTimeToMin(item.getEndTime());

                // 출석 체크 유예 시간 적용
                int checkStartTime = startTimeInMinutes - Constants.ATTENDANCE_CHECK_BEFORE_MINUTES;
                int checkEndTime = startTimeInMinutes + Constants.ATTENDANCE_CHECK_AFTER_MINUTES;

                // 이미 오늘 출석 처리된 과목인지 확인
                String attendanceKey = item.getSubject() + "_" + todayDateString;
                boolean alreadyAttended = attendanceLogPrefs.getBoolean(attendanceKey, false);

                if (currentTimeInMinutes >= checkStartTime && currentTimeInMinutes <= checkEndTime) {
                    if (!alreadyAttended) {
                        Log.d(TAG, "출석 대상 수업 발견: " + item.getSubject());
                        sendAttendanceNotification(item.getSubject()); // 알림 발송
                        increaseCatExperience(); // 고양이 경험치 증가
                        logEditor.putBoolean(attendanceKey, true); // 출석 처리 로그 기록
                        logEditor.apply();
                        Toast.makeText(this, item.getSubject() + " 출석 완료!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, item.getSubject() + " 출석이 자동 완료되었습니다.");
                    } else {
                        Log.d(TAG, item.getSubject() + " (이미 출석 처리됨)");
                    }
                } else {
                    Log.d(TAG, item.getSubject() + " (현재 출석 시간 아님)");
                }
            }
        }
        // 다음 수업 체크를 위해 이 서비스는 계속 실행되어야 함.
        // 또는 특정 시간에 맞춰 AlarmManager로 서비스를 깨울 수도 있음.
    }

    private void sendAttendanceNotification(String subjectName) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;

        // Android 13 (API 33) 이상에서는 알림 권한 확인
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // 권한이 없으면 알림을 보낼 수 없음
                Log.w(TAG, "POST_NOTIFICATIONS 권한이 없어 알림을 보낼 수 없습니다.");
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Constants.ATTENDANCE_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // 작은 아이콘 (필수)
                .setContentTitle("출석 시간입니다!")
                .setContentText("자동 출석이 완료 되었어요. (" + subjectName + ")")
                .setPriority(NotificationCompat.PRIORITY_HIGH) // 중요도 높게 설정
                .setAutoCancel(true); // 알림 클릭 시 사라지도록

        notificationManager.notify(Constants.ATTENDANCE_NOTIFICATION_ID + subjectName.hashCode(), builder.build());
        Log.d(TAG, "출석 완료 알림 발송: " + subjectName);
    }


    private void increaseCatExperience() {
        SharedPreferences catPrefs = getSharedPreferences(Constants.CAT_PREFS_NAME, Context.MODE_PRIVATE); // CatPrefs.xml
        int currentCatXp = catPrefs.getInt(Constants.CAT_XP_KEY, 0); // catxp
        int newCatXp = currentCatXp + Constants.XP_PER_ATTENDANCE;
        catPrefs.edit().putInt(Constants.CAT_XP_KEY, newCatXp).apply();
        Log.d(TAG, "고양이 경험치 " + Constants.XP_PER_ATTENDANCE + " 증가! 현재 XP: " + newCatXp);
    }

    // TimetableFragment의 parseTimeToMin과 유사
    private int parseTimeToMin(String raw) {
        if (raw == null) return 9 * 60; // 기본값
        String[] parts = raw.trim().split(":");
        try {
            return Integer.parseInt(parts[0].trim()) * 60 +
                    (parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0);
        } catch (NumberFormatException ex) {
            Log.e(TAG, "시간 파싱 오류: " + raw);
            return 9 * 60;
        }
    }

    private String getDayString(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY: return "월";
            case Calendar.TUESDAY: return "화";
            case Calendar.WEDNESDAY: return "수";
            case Calendar.THURSDAY: return "목";
            case Calendar.FRIDAY: return "금";
            case Calendar.SATURDAY: return "토";
            case Calendar.SUNDAY: return "일";
            default: return "";
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "AttendanceCheckerService onDestroy");
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            Log.d(TAG, "위치 업데이트 중지.");
        }
        // 지오펜스 제거는 앱이 완전히 종료될 때 고려하거나, 필요한 경우 수동으로 제거
        // geofencingClient.removeGeofences(getGeofencePendingIntent());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // 바인딩 서비스가 아니므로 null 반환
    }
}
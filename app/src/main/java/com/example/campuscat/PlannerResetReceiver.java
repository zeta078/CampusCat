package com.example.campuscat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PlannerResetReceiver extends BroadcastReceiver {

    private static final String PREF_NAME = "PlannerData";
    private static final String KEY_PLANNER_DATE = "planner_date";
    private static final String KEY_PLANNER_DATA = "planner_data";
    private static final String KEY_STUDY_PLAN = "study_plan";
    private static final String KEY_IS_FROZEN = "is_frozen";

    private static final String CHANNEL_ID = "planner_notification_channel";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("PlannerResetReceiver", "Alarm received: Planner reset initiated.");

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // 이전 날짜의 데이터 (경험치 계산을 위해 필요하다면 여기서 로드)
        // 현재는 PlannerFragment에서 초기화 시점에 이전 날짜 데이터를 활용하여 경험치를 계산합니다.
        // 여기서 직접 계산하려면, PlannerFragment의 loadPlannerDataForExperience 로직을 가져와야 합니다.
        // 여기서는 SharedPreferences를 초기화하고 알림을 띄우는 역할만 수행.

        // 플래너 데이터 초기화
        sharedPreferences.edit()
                .remove(KEY_PLANNER_DATA)
                .remove(KEY_STUDY_PLAN)
                .remove(KEY_IS_FROZEN)
                .apply();

        // 현재 날짜로 업데이트 (이 부분은 PlannerFragment의 checkAndInitializePlanner()에서도 처리됨)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = sdf.format(new Date());
        sharedPreferences.edit().putString(KEY_PLANNER_DATE, todayDate).apply();

        // 초기화 알림 생성 및 표시
        createNotificationChannel(context);
        sendNotification(context);
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "일일 플래너 알림";
            String description = "매일 00시에 플래너 초기화 알림";
            int importance = NotificationManager.IMPORTANCE_HIGH; // 중요도 높음
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification(Context context) {
        // 알림 클릭 시 앱의 메인 액티비티로 이동하도록 설정
        Intent intent = new Intent(context, MainActivity.class); // MainActivity는 앱의 메인 진입점
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE // FLAG_IMMUTABLE 추가
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // 앱 아이콘으로 대체 (적절한 아이콘으로 변경 필요)
                .setContentTitle("일일 플래너가 초기화 됐어요!")
                .setContentText("보상 지급을 확인해 볼까요?")
                .setPriority(NotificationCompat.PRIORITY_HIGH) // 중요도 높음
                .setContentIntent(pendingIntent) // 알림 클릭 시 실행될 인텐트
                .setAutoCancel(true); // 알림 클릭 시 자동으로 사라지게

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
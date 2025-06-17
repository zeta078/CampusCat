package com.example.campuscat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GeofenceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Geofencing Error: " + geofencingEvent.getErrorCode());
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // 지오펜스 진입/이탈 시 AttendanceCheckerService를 시작/재시작
            // onStartCommand에서 GeofencingEvent를 처리하도록
            Intent serviceIntent = new Intent(context, AttendanceCheckerService.class);
            // GeofencingEvent를 서비스로 전달
            serviceIntent.putExtras(intent); // GeofencingEvent가 포함된 인텐트의 Extras를 복사

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
            Log.d(TAG, "AttendanceCheckerService 시작 요청 (Geofence Event)");
        }
    }
}
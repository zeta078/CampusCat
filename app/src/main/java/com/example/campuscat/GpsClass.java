package com.example.campuscat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class GpsClass implements LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 300;
    private Context context;
    private LocationManager locationManager;
    private LocationCallback callback;
    private Activity activity; // 권한 요청을 위해 Activity Context 필요

    public interface LocationCallback {
        void onLocationUpdated(double latitude, double longitude);
        void onPermissionDenied();
    }

    public GpsClass(Activity activity, LocationCallback callback) {
        this.activity = activity;
        this.context = activity.getApplicationContext(); // Activity 대신 Application Context 사용
        this.callback = callback;
        checkPermissionsAndStart();
    }

    private void checkPermissionsAndStart() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                if (callback != null) {
                    callback.onPermissionDenied();
                }
            }
        }
    }

    private static final long MIN_TIME_BW_UPDATES = 1000 * 10; // 10 seconds
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    @Override
    public void onLocationChanged(Location location) {
        if (callback != null) {
            callback.onLocationUpdated(location.getLatitude(), location.getLongitude());
        }
        stopLocationUpdates(); // Once we get a location, we can stop for this simple example
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("SimpleLocation", provider + " provider enabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("SimpleLocation", provider + " provider disabled");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("SimpleLocation", provider + " status changed: " + status);
    }

    @SuppressLint("MissingPermission")
    public void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("SimpleLocation", "Location permissions not granted");
            if (callback != null) {
                callback.onPermissionDenied();
            }
            return;
        }

        try {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES,
                        this
                );
            }
            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES,
                        this
                );
            }
        } catch (SecurityException e) {
            Log.e("SimpleLocation", "Security exception while requesting location updates", e);
            if (callback != null) {
                callback.onPermissionDenied(); // Or a specific error callback
            }
        }
    }

    public void stopLocationUpdates() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }
}
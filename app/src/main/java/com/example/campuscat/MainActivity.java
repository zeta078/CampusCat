package com.example.campuscat;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView locationTextView;
    private GpsClass locationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationTextView = findViewById(R.id.locationTextView);

        locationProvider = new GpsClass(this, new GpsClass.LocationCallback() {
            @Override
            public void onLocationUpdated(double latitude, double longitude) {
                updateLocationDisplay(latitude, longitude);
            }

            @Override
            public void onPermissionDenied() {
                Toast.makeText(MainActivity.this, "위치 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
                locationTextView.setText("위치 권한 필요");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (locationProvider != null) {
            locationProvider.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void updateLocationDisplay(final double latitude, final double longitude) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                locationTextView.setText("Latitude: " + latitude + "\nLongitude: " + longitude);
                Toast.makeText(MainActivity.this, "Location Updated!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationProvider != null) {
            locationProvider.stopLocationUpdates();
        }
    }
}
//gps 기능 구현 메인 자바
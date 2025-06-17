package com.example.campuscat; // 자신의 패키지명으로 수정하세요

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.SeekBar;
import android.widget.TextView;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private SeekBar brightnessSeekBar;
    private TextView resetDataTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings); // XML 파일명 확인

        brightnessSeekBar = findViewById(R.id.brightnessSeekBar);
        resetDataTextView = findViewById(R.id.menu_reset_data);

        // 1. 현재 밝기 값으로 SeekBar 설정
        float currentBrightness = getWindow().getAttributes().screenBrightness;
        if (currentBrightness == -1.0f) {
            brightnessSeekBar.setProgress(128); // 기본 중간값
        } else {
            brightnessSeekBar.setProgress((int) (currentBrightness * 255));
        }

        // 2. SeekBar 리스너
        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float brightness = progress / 255.0f;
                WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
                layoutParams.screenBrightness = brightness;
                getWindow().setAttributes(layoutParams);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        // 3. 데이터 초기화 클릭 리스너
        resetDataTextView.setOnClickListener(v -> {
            showResetDataConfirmationDialog();
        });
    }

    // 설정화면으로 유도하는 확인 다이얼로그
    private void showResetDataConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("데이터 초기화")
                .setMessage("앱 데이터를 초기화하려면 설정 화면으로 이동해야 합니다.\n설정에서 \"저장 공간\" 또는 \"데이터 삭제\"를 선택하세요.")
                .setPositiveButton("설정으로 이동", (dialog, which) -> {
                    // 앱 설정 화면으로 이동
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("취소", (dialog, which) -> dialog.dismiss())
                .show();
    }
}

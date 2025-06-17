package com.example.campuscat;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    private Button btnMission, btnSettings;
    private TextView todaySchedule, tvCatLevel;
    private ImageView catImage;
    private ProgressBar expBar;

    // CatDetailActivity의 thresholds와 동일하게 맞춰줍니다.
    private final int[] thresholds = {0, 300, 1000, 2000};

    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {



        View view = inflater.inflate(R.layout.fragment_home, container, false);

        btnMission = view.findViewById(R.id.btnMission);
        btnSettings = view.findViewById(R.id.btnSettings);
        todaySchedule = view.findViewById(R.id.todaySchedule);
        tvCatLevel = view.findViewById(R.id.tvCatLevel);
        catImage = view.findViewById(R.id.catImage);
        expBar = view.findViewById(R.id.expBar);

        // 고양이 클릭시 CatDetailActivity intent
        catImage.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CatDetailActivity.class);
            startActivity(intent);
        });

        // ✅ 미션 버튼 → MissionFragment로 이동
        btnMission.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new MissionFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // ✅ 오늘 일정 텍스트 → CalendarFragment로 이동
        todaySchedule.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new CalendarFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // ✅ 설정 버튼 → SettingsFragment로 이동 (아직 없음 → 주석 처리)
        btnSettings.setOnClickListener(v -> {
            // requireActivity().getSupportFragmentManager()
            //         .beginTransaction()
            //         .replace(R.id.fragment_container, new SettingsFragment())
            //         .addToBackStack(null)
            //         .commit();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Fragment가 다시 활성화될 때마다 고양이 정보를 업데이트합니다.
        updateCatInfo();
    }

    private void updateCatInfo() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("CatPrefs", MODE_PRIVATE);
        int currentLevel = prefs.getInt("catlevel", 1);
        int currentXp = prefs.getInt("catxp", 10);

        // 레벨 텍스트 업데이트
        tvCatLevel.setText("LEVEL " + currentLevel);

        // 경험치 바 업데이트
        // CatDetailActivity의 로직과 동일하게 계산합니다.
        int nextLevelXP = thresholds[currentLevel]; // 다음 레벨에 필요한 총 경험치
        int prevLevelXP = thresholds[currentLevel - 1]; // 이전 레벨까지의 총 경험치
        int progress = currentXp - prevLevelXP; // 현재 레벨에서의 진행 경험치
        int maxProgress = nextLevelXP - prevLevelXP; // 현재 레벨에서 필요한 총 경험치 (바의 최대값)

        expBar.setMax(maxProgress);
        expBar.setProgress(progress);

        // 고양이 이미지 업데이트 (CatDetailActivity의 getCurrentCatImageRes() 메서드와 동일한 로직 사용)
        int imageResId = CatDetailActivity.getCurrentCatImageRes(currentLevel); // public static으로 변경했음을 가정
        catImage.setImageResource(imageResId);
    }
}
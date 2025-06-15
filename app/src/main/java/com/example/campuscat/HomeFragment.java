package com.example.campuscat;

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
}

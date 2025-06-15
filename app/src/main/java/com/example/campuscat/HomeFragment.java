package com.example.campuscat;

import android.widget.Button;

import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {
    public HomeFragment() {
        super(R.layout.fragment_home);// res/layout/fragment_home.xml 필요

        Button btnMission = view.findViewById(R.id.btnMission);
        btnMission.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new MissionFragment())
                    .addToBackStack(null)
                    .commit();
        });

    }
}

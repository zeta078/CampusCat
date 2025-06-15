package com.example.campuscat; // 본인의 패키지명에 맞게 변경해주세요.

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


public class MoreFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more, container, false); // fragment_more는 위에서 제공된 XML 파일명입니다.

        // 각 TextView에 클릭 리스너 설정
        TextView menuSettings = view.findViewById(R.id.menu_settings);
        TextView menuStudy = view.findViewById(R.id.menu_study);
        TextView menuCafeteria = view.findViewById(R.id.menu_cafeteria);
        TextView menuInventory = view.findViewById(R.id.menu_inventory);
        TextView menuMission = view.findViewById(R.id.menu_mission);

        // 설정 클릭 리스너 (주석 처리)
        menuSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 설정 프래그먼트로 이동하는 코드
                // replaceFragment(new SettingsFragment());
                // Log.d("MoreFragment", "설정 메뉴 클릭됨"); // 디버깅용
            }
        });

        // 자습 클릭 리스너
        menuStudy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new StudyFragment());
            }
        });

        // 학식 클릭 리스너 (주석 처리)
        menuCafeteria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 학식 프래그먼트로 이동하는 코드
                // replaceFragment(new CafeteriaFragment());
                // Log.d("MoreFragment", "학식 메뉴 클릭됨"); // 디버깅용
            }
        });

        // 인벤토리 클릭 리스너 (주석 처리)
        menuInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 인벤토리 프래그먼트로 이동하는 코드
                // replaceFragment(new InventoryFragment());
                // Log.d("MoreFragment", "인벤토리 메뉴 클릭됨"); // 디버깅용
            }
        });

        //메뉴 리스너
        menuMission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new MissionFragment());
            }
        });

        return view;
    }


    /**
     * 프래그먼트를 교체하는 헬퍼 메서드
     * @param fragment 교체할 프래그먼트 인스턴스
     */
    private void replaceFragment(Fragment fragment) {
        if (getParentFragmentManager() != null) {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment); // R.id.fragment_container는 프래그먼트가 표시될 컨테이너 ID입니다.
            transaction.addToBackStack(null); // 뒤로 가기 버튼으로 돌아갈 수 있도록 스택에 추가
            transaction.commit();
        }
    }
}
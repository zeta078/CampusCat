package com.example.campuscat; // 본인의 패키지명에 맞게 변경해주세요.

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.util.Log; // Log 사용을 위해 임포트 추가

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
// FragmentTransaction은 이제 직접 사용하지 않으므로 제거하거나 그대로 둬도 무방
// import androidx.fragment.app.FragmentTransaction;


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
                // replaceFragment(new SettingsFragment()); // 이 메서드는 아래에서 제거됩니다.
                Log.d("MoreFragment", "설정 메뉴 클릭됨"); // 디버깅용
            }
        });

        // 자습 클릭 리스너 (**** 이 부분이 핵심 수정 ****)
        menuStudy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 부모 액티비티가 MainActivity 인지 확인 후 showStudyFragment() 호출
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showStudyFragment();
                    Log.d("MoreFragment", "자습 메뉴 클릭됨 - StudyFragment로 전환 요청"); // 디버깅용
                } else {
                    Log.e("MoreFragment", "부모 액티비티가 MainActivity가 아닙니다. StudyFragment 전환 실패.");
                }
            }
        });

        // 학식 클릭 리스너 (주석 처리)
        menuCafeteria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 학식 프래그먼트로 이동하는 코드
                // replaceFragment(new CafeteriaFragment());
                Log.d("MoreFragment", "학식 메뉴 클릭됨"); // 디버깅용
            }
        });

        // 인벤토리 클릭 리스너 (주석 처리)
        menuInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 인벤토리 프래그먼트로 이동하는 코드
                // replaceFragment(new InventoryFragment());
                Log.d("MoreFragment", "인벤토리 메뉴 클릭됨"); // 디버깅용
            }
        });

        // 미션 클릭 리스너
        menuMission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // MissionFragment도 MainActivity에서 인스턴스를 관리하고 있다면
                // MainActivity에 showMissionFragment() 같은 메서드를 만들어서 호출하는 것이 좋습니다.
                // 일단은 기존 방식 유지 (새 인스턴스 생성)
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new MissionFragment(), MissionFragment.class.getName())
                            .addToBackStack(null)
                            .commit();
                    Log.d("MoreFragment", "미션 메뉴 클릭됨 - MissionFragment로 전환");
                }
            }
        });

        return view;
    }

    /**
     * 기존 replaceFragment 헬퍼 메서드는 더 이상 사용하지 않으므로 제거합니다.
     * 각 메뉴의 이동은 MainActivity가 담당하거나, 다른 프래그먼트 인스턴스를 관리하는 방식으로 변경되어야 합니다.
     * StudyFragment 전환은 MainActivity의 showStudyFragment()를 통해 이루어집니다.
     */
    // private void replaceFragment(Fragment fragment) {
    //     if (getParentFragmentManager() != null) {
    //         FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
    //         transaction.replace(R.id.fragment_container, fragment);
    //         transaction.addToBackStack(null);
    //         transaction.commit();
    //     }
    // }
}
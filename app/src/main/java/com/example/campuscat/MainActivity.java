package com.example.campuscat;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    // PlannerFragment와 StudyFragment 인스턴스를 액티비티 멤버 변수로 선언
    private PlannerFragment plannerFragment;
    private StudyFragment studyFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // activity_main.xml 레이아웃 사용

        // 액티비티가 처음 생성될 때만 프래그먼트를 추가 (화면 회전 시 중복 생성 방지)
        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            // 1. PlannerFragment 인스턴스 생성 및 태그 지정 (StudyFragment에서 참조할 수 있도록)
            plannerFragment = new PlannerFragment();
            // 2. StudyFragment 인스턴스 생성
            studyFragment = new StudyFragment();

            // **** 핵심: StudyFragment에 PlannerFragment 인스턴스 주입 ****
            // StudyFragment의 setPlannerFragment() 메서드를 호출하여 참조를 전달합니다.
            studyFragment.setPlannerFragment(plannerFragment);

            // 3. 처음에 보여줄 프래그먼트를 fragment_container에 추가 (여기서는 PlannerFragment를 기본으로)
            // replace()를 사용하면 기존 프래그먼트가 있다면 제거하고 새 프래그먼트로 교체합니다.
            // "PlannerFragmentTag"는 이 프래그먼트를 식별하는 데 사용될 고유한 태그입니다.
            fragmentTransaction.replace(R.id.fragment_container, plannerFragment, "PlannerFragmentTag");

            // 4. 트랜잭션 커밋
            fragmentTransaction.commit();

            // 참고: 만약 앱에 하단 네비게이션 바나 버튼이 있어서 프래그먼트를 전환해야 한다면
            // 그에 맞는 로직을 추가해야 합니다. 예시:
            /*
            // 예를 들어 버튼 클릭으로 StudyFragment로 전환
            findViewById(R.id.some_button_to_study_fragment).setOnClickListener(v -> {
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, studyFragment, "StudyFragmentTag")
                    .addToBackStack(null) // 이전 프래그먼트로 돌아갈 수 있도록 스택에 추가
                    .commit();
            });
            */
        } else {
            // 액티비티가 재생성될 때 (예: 화면 회전), 프래그먼트 매니저가 자동으로 프래그먼트를 복원합니다.
            // 복원된 프래그먼트 인스턴스를 다시 연결해줘야 합니다.
            plannerFragment = (PlannerFragment) getSupportFragmentManager().findFragmentByTag("PlannerFragmentTag");
            studyFragment = (StudyFragment) getSupportFragmentManager().findFragmentByTag("StudyFragmentTag"); // StudyFragment도 태그를 가지고 있어야 함

            // 복원된 StudyFragment에 PlannerFragment 참조가 끊어졌을 수 있으므로 다시 주입 시도
            if (studyFragment != null && plannerFragment != null) {
                studyFragment.setPlannerFragment(plannerFragment);
            }
        }
    }
}
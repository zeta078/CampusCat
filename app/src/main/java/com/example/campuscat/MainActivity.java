package com.example.campuscat;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 액티비티가 처음 생성될 때만 프래그먼트를 추가합니다.
        // 화면 회전 등으로 액티비티가 재생성될 때는 이미 프래그먼트가 복원되므로 중복 추가를 막습니다.
        if (savedInstanceState == null) {
            // 1. FragmentManager 객체를 가져옵니다.
            FragmentManager fragmentManager = getSupportFragmentManager();

            // 2. FragmentTransaction을 시작합니다. (프래그먼트 작업을 시작)
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            // 3. 프래그먼트를 생성합니다.
            StudyFragment dailyPlannerFragment = new StudyFragment();

            // 4. 프래그먼트를 컨테이너에 추가합니다.
            //    add(컨테이너뷰ID, 프래그먼트 객체)
            fragmentTransaction.add(R.id.fragment_container, dailyPlannerFragment);

            // 5. 트랜잭션을 커밋하여 변경사항을 적용합니다.
            fragmentTransaction.commit();
        }
    }
}
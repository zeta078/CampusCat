package com.example.campuscat;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TimetableFragment extends Fragment {

    private TextView textNoTimetable;
    private LinearLayout layoutTimetableList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // 레이아웃 연결 (fragment_timetable.xml 사용)
        View view = inflater.inflate(R.layout.fragment_timetable, container, false);

        textNoTimetable = view.findViewById(R.id.textNoTimetable);
        layoutTimetableList = view.findViewById(R.id.layoutTimetableList);
        Button buttonAdd = view.findViewById(R.id.btn_add_timetable);

        //버튼 클릭 시 다이얼로그 띄우기
        buttonAdd.setOnClickListener(v -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater dialogInflater = getLayoutInflater();
            View dialogView = dialogInflater.inflate(R.layout.dialog_add_timetable, null);
            builder.setView(dialogView);

            EditText subjectEdit = dialogView.findViewById(R.id.edit_subject);
            EditText dayEdit = dialogView.findViewById(R.id.edit_day);
            EditText timeEdit = dialogView.findViewById(R.id.edit_time);

            builder.setTitle("시간표 추가").setPositiveButton("저장", (dialog, which) -> {
                String subject = subjectEdit.getText().toString().trim();
                String day = dayEdit.getText().toString().trim();
                String time = timeEdit.getText().toString().trim();

                if (subject.isEmpty() || day.isEmpty() || time.isEmpty()) {
                    Toast.makeText(getActivity(), "모든 항목을 입력해주세요!", Toast.LENGTH_SHORT).show();
                    return;
                }

                TimetableItem newItem = new TimetableItem(subject, day, time);

                //등록 시, "시간표가 없습니다" 텍스트 숨기고 등록된 시간표 리스트 보이기
                textNoTimetable.setVisibility(View.GONE);
                layoutTimetableList.setVisibility(View.VISIBLE);

                //시간표 삭제 버튼 기능
                LinearLayout itemLayout = new LinearLayout(getContext());
                itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                itemLayout.setPadding(0, 10, 0, 10);

                //새 시간표 항목을 텍스트뷰로 만들어 추가
                TextView itemText = new TextView(getContext());
                itemText.setText(subject + " / " + day + " / " + time);
                itemText.setTextSize(16);
                itemText.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

                Button deleteButton = new Button(getContext());
                deleteButton.setText("삭제");
                deleteButton.setTextSize(12);

                // 삭제 버튼 클릭 시 경고 다이얼로그
                deleteButton.setOnClickListener(view1 -> {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("삭제 확인")
                            .setMessage("정말로 이 항목을 삭제하시겠습니까?")
                            .setPositiveButton("삭제", (dialogInterface, i) -> {
                                layoutTimetableList.removeView(itemLayout);

                                // 항목이 0개가 되면 다시 '시간표가 없습니다' 문구 보여줌
                                if (layoutTimetableList.getChildCount() == 0) {
                                    textNoTimetable.setVisibility(View.VISIBLE);
                                    layoutTimetableList.setVisibility(View.GONE);
                                }

                                //삭제 후 확인 안내 메세지
                                Toast.makeText(getActivity(), "해당 시간표가 정상적으로 삭제되었습니다", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("취소", null)
                            .show();
                });

                itemLayout.addView(itemText);
                itemLayout.addView(deleteButton);
                layoutTimetableList.addView(itemLayout);

                Toast.makeText(getActivity(), "시간표가 추가되었습니다.", Toast.LENGTH_SHORT).show();
            });

            builder.setNegativeButton("취소", (dialog, which) -> dialog.dismiss());
            builder.create().show();
        });

        return view;
    }
}






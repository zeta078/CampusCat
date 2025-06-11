package com.example.campuscat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class TimetableFragment extends Fragment {

    private static final String[] COLORS = {
            "#FFB6B6", "#FFE275", "#A0E7E5",
            "#B5EAD7", "#C7CEEA", "#FFDAC1"
    };
    private static final String PREFS_NAME = "timetable_prefs";
    private static final String KEY_ITEMS  = "timetable_items";
    private static final int ROW_HEIGHT_DP = 30; // 30분당 30dp

    private LinearLayout layoutEmpty,
            layoutTimeLabels,
            layoutDayLabels,
            layoutTimetableList;
    private ScrollView   layoutContent;
    private FrameLayout  timetableGrid;
    private Button       btnAddFirst,
            btnAddMore;

    private ArrayList<TimetableItem> timetableItems = new ArrayList<>();
    private SharedPreferences prefs;
    private Gson prefsGson = new Gson();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_timetable, container, false);

        // bind views
        layoutEmpty         = v.findViewById(R.id.layout_empty);
        layoutContent       = v.findViewById(R.id.layout_content);
        layoutTimeLabels    = v.findViewById(R.id.layout_time_labels);
        layoutDayLabels     = v.findViewById(R.id.layout_day_labels);
        timetableGrid       = v.findViewById(R.id.timetable_grid);
        layoutTimetableList = v.findViewById(R.id.layout_timetable_list);
        btnAddFirst         = v.findViewById(R.id.btn_add_first);
        btnAddMore          = v.findViewById(R.id.btn_add_more);

        prefs = requireContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        initLabels();
        loadTimetable();

        // if saved items exist, show and lay them out
        if (!timetableItems.isEmpty()) {
            layoutEmpty.setVisibility(View.GONE);
            layoutContent.setVisibility(View.VISIBLE);
            layoutContent.post(() -> {
                int gridPx = dpToPx(ROW_HEIGHT_DP * 18);
                int colW   = timetableGrid.getWidth() / 5;
                for (TimetableItem it : timetableItems) {
                    placeBlock(it, gridPx, colW);
                }
            });
        }

        btnAddFirst.setOnClickListener(x -> showAddDialog());
        btnAddMore .setOnClickListener(x -> showAddDialog());

        return v;
    }

    private void initLabels() {
        layoutEmpty.setVisibility(View.VISIBLE);
        layoutContent.setVisibility(View.GONE);

        // time labels 9:00–18:00, 1 hour steps
        for (int h = 9; h <= 18; h++) {
            TextView tv = new TextView(getContext());
            tv.setText(String.format(Locale.KOREA, "%02d:00", h));
            tv.setHeight(dpToPx(ROW_HEIGHT_DP * 2));
            tv.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            tv.setTextColor(Color.parseColor("#888888"));
            tv.setTextSize(13);
            layoutTimeLabels.addView(tv);

            View line = new View(getContext());
            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            dpToPx(1)
                    );
            line.setLayoutParams(lp);
            line.setBackgroundColor(Color.parseColor("#DDDDDD"));
            layoutTimeLabels.addView(line);
        }

        // day labels Mon–Fri
        String[] days = {"월","화","수","목","금"};
        for (String d : days) {
            TextView day = new TextView(getContext());
            day.setText(d);
            day.setGravity(Gravity.CENTER);
            day.setTextSize(18);
            day.setTextColor(Color.parseColor("#1A274D"));
            LinearLayout.LayoutParams p =
                    new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            day.setLayoutParams(p);
            layoutDayLabels.addView(day);
        }
    }

    private void showAddDialog() {
        View dlg = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_timetable, null);
        AlertDialog.Builder b = new AlertDialog.Builder(getContext())
                .setTitle("시간표 항목 추가")
                .setView(dlg);

        EditText es  = dlg.findViewById(R.id.edit_subject);
        EditText ep  = dlg.findViewById(R.id.edit_place);
        EditText ed  = dlg.findViewById(R.id.edit_day);
        EditText est = dlg.findViewById(R.id.edit_start_time);
        EditText en  = dlg.findViewById(R.id.edit_end_time);

        b.setPositiveButton("추가", (d,i) -> {
                    TimetableItem item = createFromInputs(es, ep, ed, est, en);
                    if (item == null) return;

                    timetableItems.add(item);
                    saveTimetable();
                    ensureContentVisible();

                    layoutContent.post(() -> {
                        int gridPx = dpToPx(ROW_HEIGHT_DP * 18);
                        int colW   = timetableGrid.getWidth() / 5;
                        placeBlock(item, gridPx, colW);
                    });
                }).setNegativeButton("취소", null)
                .show();
    }

    private void showEditDialog(TimetableItem item, TextView cell, View card) {
        View dlg = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_timetable, null);
        AlertDialog.Builder b = new AlertDialog.Builder(getContext())
                .setTitle("시간표 항목 수정")
                .setView(dlg);

        EditText es  = dlg.findViewById(R.id.edit_subject);
        EditText ep  = dlg.findViewById(R.id.edit_place);
        EditText ed  = dlg.findViewById(R.id.edit_day);
        EditText est = dlg.findViewById(R.id.edit_start_time);
        EditText en  = dlg.findViewById(R.id.edit_end_time);

        // fill current values
        es.setText(item.getSubject());
        ep.setText(item.getPlace());
        ed.setText(item.getDay());
        est.setText(item.getStartTime());
        en.setText(item.getEndTime());

        b.setPositiveButton("저장", (d,i) -> {
                    TimetableItem edited = createFromInputs(es, ep, ed, est, en);
                    if (edited == null) return;

                    item.setSubject(edited.getSubject());
                    item.setPlace(edited.getPlace());
                    item.setDay(edited.getDay());
                    item.setStartTime(edited.getStartTime());
                    item.setEndTime(edited.getEndTime());
                    saveTimetable();

                    // update cell layout & text
                    int gridPx = dpToPx(ROW_HEIGHT_DP * 18);
                    int colW   = timetableGrid.getWidth() / 5;
                    updateCellLayout(item, cell, gridPx, colW);

                    TextView tv = card.findViewById(R.id.text_timetable_item);
                    tv.setText(formatCardText(item));
                }).setNegativeButton("취소", null)
                .show();
    }

    private void showDeleteConfirm(TimetableItem it, View cell, View card) {
        new AlertDialog.Builder(getContext())
                .setTitle("삭제 확인")
                .setMessage("정말로 해당 강의를 삭제하시겠습니까?")
                .setPositiveButton("삭제", (d,idx) -> {
                    layoutTimetableList.removeView(card);
                    timetableGrid.removeView(cell);
                    timetableItems.remove(it);
                    saveTimetable();
                    if (timetableItems.isEmpty()) {
                        layoutContent.setVisibility(View.GONE);
                        layoutEmpty.setVisibility(View.VISIBLE);
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private TimetableItem createFromInputs(EditText es, EditText ep,
                                           EditText ed, EditText est,
                                           EditText en) {
        String s  = es .getText().toString().trim();
        String p  = ep .getText().toString().trim();
        String d  = ed .getText().toString().trim();
        String st = est.getText().toString().trim();
        String et = en .getText().toString().trim();
        if (s.isEmpty()||d.isEmpty()||st.isEmpty()||et.isEmpty()) {
            Toast.makeText(getContext(),
                    "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return null;
        }
        return new TimetableItem(s, p, d, st, et);
    }

    private void ensureContentVisible() {
        if (layoutEmpty.getVisibility() == View.VISIBLE) {
            layoutEmpty.setVisibility(View.GONE);
            layoutContent.setVisibility(View.VISIBLE);
        }
    }

    private void placeBlock(TimetableItem it, int gridPx, int colW) {
        // calculate position & size
        FrameLayout.LayoutParams flp = computeLayoutParams(it, gridPx, colW);

        // create cell view
        TextView cell = new TextView(getContext());
        cell.setText(Html.fromHtml(
                "<b>"+it.getSubject()+"</b><br><small>"+it.getPlace()+"</small>"
        ));
        cell.setGravity(Gravity.CENTER);
        cell.setTextColor(Color.WHITE);
        cell.setPadding(dpToPx(6),dpToPx(6),dpToPx(6),dpToPx(6));
        cell.setTextSize(15);
        cell.setBackgroundColor(Color.parseColor(
                COLORS[new Random().nextInt(COLORS.length)]));
        cell.setLayoutParams(flp);
        timetableGrid.addView(cell);

        // inflate card
        View card = LayoutInflater.from(getContext())
                .inflate(R.layout.item_timetable, layoutTimetableList, false);

        TextView tv = card.findViewById(R.id.text_timetable_item);
        Button del = card.findViewById(R.id.btn_delete_timetable_item);
        tv.setText(formatCardText(it));

        // delete listener
        del.setOnClickListener(v ->
                showDeleteConfirm(it, cell, card)
        );

        // edit listener on card root
        card.setOnClickListener(v ->
                showEditDialog(it, cell, card)
        );

        layoutTimetableList.addView(card);
    }

    private void updateCellLayout(TimetableItem it, TextView cell,
                                  int gridPx, int colW) {
        FrameLayout.LayoutParams flp = computeLayoutParams(it, gridPx, colW);
        cell.setLayoutParams(flp);
        cell.setText(Html.fromHtml(
                "<b>"+it.getSubject()+"</b><br><small>"+it.getPlace()+"</small>"
        ));
    }

    private FrameLayout.LayoutParams computeLayoutParams(TimetableItem it,
                                                         int gridPx, int colW) {
        int sMin = parseTimeToMin(it.getStartTime());
        int eMin = parseTimeToMin(it.getEndTime());
        float ppm = dpToPx(ROW_HEIGHT_DP) / 30f; // 1dp per 1 min
        int top  = Math.round((sMin - 9*60) * ppm);
        int hPx  = Math.round((eMin - sMin) * ppm);
        int col  = Math.max(0,
                "월화수목금".indexOf(it.getDay().substring(0,1))
        );

        FrameLayout.LayoutParams p =
                new FrameLayout.LayoutParams(colW, hPx);
        p.leftMargin = col * colW;
        p.topMargin  = top;
        return p;
    }

    private String formatCardText(TimetableItem it) {
        return it.getSubject()+" / "+
                it.getDay()+"요일 / "+
                it.getStartTime()+" ~ "+
                it.getEndTime()+
                (it.getPlace().isEmpty()?"":" / "+it.getPlace());
    }

    private int parseTimeToMin(String raw) {
        if (raw == null) return 9*60;
        String t = raw.trim();
        String[] p = t.split(":");
        try {
            int h = Integer.parseInt(p[0].trim());
            int m = (p.length>1)? Integer.parseInt(p[1].trim()):0;
            return h*60 + m;
        } catch(Exception ex) {
            return 9*60;
        }
    }

    private void saveTimetable() {
        prefs.edit()
                .putString(KEY_ITEMS, prefsGson.toJson(timetableItems))
                .apply();
    }

    private void loadTimetable() {
        String j = prefs.getString(KEY_ITEMS, "");
        if (!j.isEmpty()) {
            Type t = new TypeToken<ArrayList<TimetableItem>>(){}.getType();
            timetableItems = prefsGson.fromJson(j, t);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(
                dp * getResources().getDisplayMetrics().density
        );
    }
}

package com.example.campuscat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class InventoryActivity extends AppCompatActivity {

    private TextView tvFishCount;
    private ImageButton btnBack;
    private Button btnBuyRug, btnToggleRug, btnBuyToy, btnToggleToy;
    private SharedPreferences prefs;
    private int fishCount;
    private boolean hasRug, hasToy;
    private boolean rugInstalled, toyInstalled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        prefs = getSharedPreferences("CatPrefs", MODE_PRIVATE);
        fishCount = prefs.getInt("fish_count", 0);
        hasRug = prefs.getBoolean("has_rug", false);
        hasToy = prefs.getBoolean("has_toy", false);
        rugInstalled = prefs.getBoolean("decor_rug", false);
        toyInstalled = prefs.getBoolean("decor_toy", false);

        tvFishCount = findViewById(R.id.tvFishCount);
        btnBack = findViewById(R.id.btnBack);
        btnBuyRug = findViewById(R.id.btnBuyRug);
        btnToggleRug = findViewById(R.id.btnToggleRug);
        btnBuyToy = findViewById(R.id.btnBuyToy);
        btnToggleToy = findViewById(R.id.btnToggleToy);

        updateUI();

        btnBack.setOnClickListener(v -> finish());

        btnBuyRug.setOnClickListener(v -> {
            if (hasRug) {
                Toast.makeText(this, "이미 구매한 러그입니다.", Toast.LENGTH_SHORT).show();
            } else if (fishCount >= 200) {
                fishCount -= 200;
                hasRug = true;
                rugInstalled = false;
                prefs.edit()
                        .putInt("fish_count", fishCount)
                        .putBoolean("has_rug", true)
                        .putBoolean("decor_rug", false)
                        .apply();
                updateUI();
            } else {
                Toast.makeText(this, "재화가 부족합니다.", Toast.LENGTH_SHORT).show();
            }
        });

        btnToggleRug.setOnClickListener(v -> {
            if (!hasRug) {
                Toast.makeText(this, "먼저 러그를 구매하세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            rugInstalled = !rugInstalled;
            prefs.edit().putBoolean("decor_rug", rugInstalled).apply();
            updateUI();
        });

        btnBuyToy.setOnClickListener(v -> {
            if (hasToy) {
                Toast.makeText(this, "이미 구매한 장난감입니다.", Toast.LENGTH_SHORT).show();
            } else if (fishCount >= 300) {
                fishCount -= 300;
                hasToy = true;
                toyInstalled = false;
                prefs.edit()
                        .putInt("fish_count", fishCount)
                        .putBoolean("has_toy", true)
                        .putBoolean("decor_toy", false)
                        .apply();
                updateUI();
            } else {
                Toast.makeText(this, "재화가 부족합니다.", Toast.LENGTH_SHORT).show();
            }
        });

        btnToggleToy.setOnClickListener(v -> {
            if (!hasToy) {
                Toast.makeText(this, "먼저 장난감을 구매하세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            toyInstalled = !toyInstalled;
            prefs.edit().putBoolean("decor_toy", toyInstalled).apply();
            updateUI();
        });
    }

    private void updateUI() {
        tvFishCount.setText(String.valueOf(fishCount));

        btnBuyRug.setEnabled(!hasRug);
        btnToggleRug.setEnabled(hasRug);
        btnToggleRug.setText(rugInstalled ? "철거하기" : "설치하기");

        btnBuyToy.setEnabled(!hasToy);
        btnToggleToy.setEnabled(hasToy);
        btnToggleToy.setText(toyInstalled ? "철거하기" : "설치하기");
    }
}

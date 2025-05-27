package com.example.test;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import androidx.appcompat.content.res.AppCompatResources;

public class CafeteriaActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cafeteria);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#D5ECFF")));
            getSupportActionBar().setTitle("");

            Drawable upArrow = AppCompatResources.getDrawable(this, androidx.appcompat.R.drawable.abc_ic_ab_back_material);
            if (upArrow != null) {
                upArrow.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
                getSupportActionBar().setHomeAsUpIndicator(upArrow);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

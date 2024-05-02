package com.den.shak.pq.activity;

import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.den.shak.pq.R;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        ImageButton backButton = findViewById(R.id.settings_back_button);
        backButton.setOnClickListener(this::onClickMenu);
    }

    private void onClickMenu(View view) {
        finish();
    }
}

package com.den.shak.pricequestion.activity;

import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.den.shak.pricequestion.R;

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

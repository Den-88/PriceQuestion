package com.den.shak.pq.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

    public void onClickExit(View view) {
        SharedPreferences preferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear(); // Очистка всех данных
        editor.apply(); // Применение изменений

        Intent intent = new Intent(this, AuthActivity.class);
        startActivity(intent);
    }

}

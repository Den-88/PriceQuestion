package com.den.shak.pq.activity;

import static com.den.shak.pq.fragments.ListAdvertFragment.onlyMyOrders;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.den.shak.pq.R;
import com.den.shak.pq.adapters.FilterCheckBoxAdapter;

import java.util.Arrays;
import java.util.List;

public class FilterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_activity);

        // Получение ссылки на RecyclerView из макета активности
        RecyclerView recyclerView = findViewById(R.id.filter_recycler_viewer);
        // Установка менеджера макета для RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Получение списка категорий из ресурсов
        List<String> categories = Arrays.asList(getResources().getStringArray(R.array.category));
        // Создание адаптера для RecyclerView
        RecyclerView.Adapter<FilterCheckBoxAdapter.ViewHolder> adapter = new FilterCheckBoxAdapter(this, categories);
        // Установка адаптера для RecyclerView
        recyclerView.setAdapter(adapter);
    }

    // Метод обработки нажатия кнопки "Назад"
    public void onClickBack(View view) {
        // Закрытие активности
        finish();
    }


    // Метод обработки нажатия кнопки "Мои заказы"
    public void onClickMyOrders(View view) {
        // Установка значения onlyMyOrders в true
        onlyMyOrders = true;
        // Закрытие активности
        finish();
    }
}

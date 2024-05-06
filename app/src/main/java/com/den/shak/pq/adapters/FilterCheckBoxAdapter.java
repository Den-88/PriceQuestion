package com.den.shak.pq.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.den.shak.pq.R;

import java.util.ArrayList;
import java.util.List;

// Определение класса FilterCheckBoxAdapter, расширяющего RecyclerView.Adapter
public class FilterCheckBoxAdapter extends RecyclerView.Adapter<FilterCheckBoxAdapter.ViewHolder> {

    private final List<String> items; // Список элементов
    private final List<String> selectedItems; // Список выбранных элементов
    private final SharedPreferences sharedPreferences; // SharedPreferences для хранения выбранных элементов


    // Конструктор класса
    public FilterCheckBoxAdapter(Context context, List<String> items) {
        this.items = items;
        this.selectedItems = new ArrayList<>();
        this.sharedPreferences = context.getSharedPreferences("filter_selected_items", Context.MODE_PRIVATE);
        // Восстановление выбранных элементов из SharedPreferences
        restoreSelectedItems();
    }

    // Метод для восстановления выбранных элементов из SharedPreferences
    private void restoreSelectedItems() {
        selectedItems.clear();
        // Получение выбранных элементов из SharedPreferences
        for (String item : items) {
            boolean isSelected = sharedPreferences.getBoolean(item, false);
            if (isSelected) {
                selectedItems.add(item);
            }
        }
    }
    @NonNull
    @Override
    // Метод для создания нового экземпляра ViewHolder
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Создание нового View для элемента списка
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.filter_item_checkbox, parent, false);
        return new ViewHolder(view);
    }

    @Override
    // Метод для связывания данных с ViewHolder
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String item = items.get(position);
        holder.textView.setText(item);

        // Устанавливаем начальное состояние CheckBox
        holder.checkBox.setChecked(selectedItems.contains(item));

        // Устанавливаем слушатель событий для элемента CheckBox
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // При изменении состояния CheckBox, выполните нужные действия
            if (isChecked) {
                selectedItems.add(item);
            } else {
                selectedItems.remove(item);
            }
            // Сохранение выбранных элементов
            saveSelectedItems();
        });
    }

    // Метод для сохранения выбранных элементов в SharedPreferences
    private void saveSelectedItems() {
        // Очистка ранее сохраненных значений
        sharedPreferences.edit().clear().apply();
        // Сохранение выбранных элементов в SharedPreferences
        for (String item : selectedItems) {
            sharedPreferences.edit().putBoolean(item, true).apply();
        }
    }

    @Override
    // Метод для получения общего количества элементов
    public int getItemCount() {
        return items.size();
    }

    // Внутренний класс ViewHolder для отображения элементов списка
    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView textView;

        // Конструктор класса ViewHolder
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.filter_checkbox);
            textView = itemView.findViewById(R.id.filter_checkbox_text);
        }
    }
}

package com.den.shak.pq.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.den.shak.pq.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

// Определение класса PhotoAdapterURL, расширяющего RecyclerView.Adapter
public class PhotoAdapterURL extends RecyclerView.Adapter<PhotoAdapterURL.ViewHolder> {

    private final List<String> photoUrls; // Список URL изображений
    private final Context context; // Контекст приложения
    private PopupWindow popupWindow; // Всплывающее окно для отображения изображения в большом размере

    // Конструктор класса
    public PhotoAdapterURL(Context context, List<String> photoUrls) {
        this.context = context;
        this.photoUrls = photoUrls;
    }

    @NonNull
    @Override
    // Метод для создания нового экземпляра ViewHolder
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Создание нового View для элемента списка
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    // Метод для связывания данных с ViewHolder
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String photoUrl = photoUrls.get(position);
        // Загрузка изображения с помощью библиотеки Picasso
        Picasso.get()
                .load(photoUrl)
                .into(holder.imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        // Изображение успешно загружено
                    }

                    @Override
                    public void onError(Exception e) {
                        // Обработка ошибки загрузки изображения
                    }
                });

        // Установка слушателя нажатия на изображение
        holder.imageView.setOnClickListener(v -> {
            // Создание нового экземпляра PopupWindow
            popupWindow = new PopupWindow(context);

            // Надувание макета всплывающего окна
            @SuppressLint("InflateParams") View popupView = LayoutInflater.from(context).inflate(R.layout.popup_image, null);

            // Получите ImageView в макете всплывающего окна
            ImageView imageViewPopup = popupView.findViewById(R.id.imageViewPopup);
            // Загрузка изображения во всплывающее окно
            Picasso.get().load(photoUrl).into(imageViewPopup);

            // Получение кнопок в макете всплывающего окна
            Button button_back = popupView.findViewById(R.id.popButtonBack);
            button_back.setOnClickListener(v12 -> popupWindow.dismiss());
            Button button_delete = popupView.findViewById(R.id.popButtonDelete);
            // Скрытие кнопки удаления изображения (не используется)
            button_delete.setVisibility(View.GONE);

            // Установка размеров всплывающего окна
            popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            popupWindow.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);

            // Установка содержимого всплывающего окна
            popupWindow.setContentView(popupView);

            // Установка фона и анимации для всплывающего окна
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);

            // Показ всплывающего окна
            popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
        });
    }

    @Override
    // Метод для получения общего количества элементов в списке
    public int getItemCount() {
        return photoUrls.size();
    }

    // Внутренний класс ViewHolder для отображения элементов списка
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView; // ImageView для отображения изображения

        // Конструктор класса ViewHolder
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Инициализация ImageView
            imageView = itemView.findViewById(R.id.item_photo);
        }
    }
}

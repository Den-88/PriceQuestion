package com.den.shak.pq.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.den.shak.pq.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {
    private final List<String> imageUrls;
    private final Context context;

    private String kvID;

    private PopupWindow popupWindow;

    private final OnPhotoClickListener listener;




    public PhotoAdapter(List<String> imageUrls, Context context, OnPhotoClickListener listener) {
        this.imageUrls = imageUrls;
        this.context = context;
        this.listener = listener;

    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        // Масштабирование изображения
        Bitmap bitmap = decodeSampledBitmap(imageUrl);
        holder.imageView.setImageBitmap(bitmap);

        holder.imageView.setOnClickListener(v -> {
            // Создание нового экземпляра PopupWindow
            popupWindow = new PopupWindow(context);

            // Надувание макета всплывающего окна
            @SuppressLint("InflateParams") View popupView = LayoutInflater.from(context).inflate(R.layout.popup_image, null);

            // Получите ImageView в макете всплывающего окна
            ImageView imageViewPopup = popupView.findViewById(R.id.imageViewPopup);
            Bitmap bitmapfull = BitmapFactory.decodeFile(imageUrl);
            imageViewPopup.setImageBitmap(bitmapfull);

            // Получите кнопки в макете всплывающего окна
            Button button_back = popupView.findViewById(R.id.popButtonBack);
            Button button_delete = popupView.findViewById(R.id.popButtonDelete);

            // Установите обработчик нажатия для кнопки НАЗАД
            button_back.setOnClickListener(v12 -> popupWindow.dismiss());

            // Установите обработчик нажатия для кнопки УДАЛИТЬ
            button_delete.setOnClickListener(v1 -> {
                // Действия при нажатии на кнопку УДАЛИТЬ
                File file = new File(imageUrl);
                if (file.exists()) {
                    AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                            .setTitle("Подтвердите удаление")
                            .setMessage("Удалить текущее фото?")
                            .setPositiveButton("Удалить", (dialogInterface, i) -> {
                                // Удаление файла
                                file.delete();
                                // Закрытие PopupWindow
                                popupWindow.dismiss();
                                imageUrls.remove(imageUrl);
                                notifyItemRemoved(holder.getAdapterPosition());
                                // Вызов метода обратного вызова для уведомления об удалении фотографии
                                if (listener != null) {
                                    listener.onPhotoDelete(holder.getAdapterPosition());
                                }
                            })
                            .setNegativeButton("Отмена", (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                            })
                            .show();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
                }
            });

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
    public int getItemCount() {
        return imageUrls.size();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_photo);
        }
    }

    // Метод для масштабирования изображения
    private Bitmap decodeSampledBitmap(String imageUrl) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageUrl, options);
        int imageWidth = options.outWidth;
        int imageHeight = options.outHeight;
        int scaleFactor = Math.min(imageWidth / 200, imageHeight / 200);
        options.inJustDecodeBounds = false;
        options.inSampleSize = scaleFactor;
        return BitmapFactory.decodeFile(imageUrl, options);
    }
}

package com.den.shak.pq.cloud;

// Интерфейс для слушателя прогресса загрузки
public interface OnUploadProgressListener {
    // Метод для обновления прогресса загрузки
    void onProgressUpdated(UploadProgress percentage);
}


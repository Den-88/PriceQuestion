package com.den.shak.pq.cloud;

// Класс для отслеживания прогресса загрузки файлов
public class UploadProgress {
    // Количество загруженных файлов
    private final int uploadedFiles;
    // Общее количество файлов
    private final int totalFiles;

    // Конструктор класса
    public UploadProgress(int uploadedFiles, int totalFiles) {
        this.uploadedFiles = uploadedFiles;
        this.totalFiles = totalFiles;
    }

    // Метод для получения количества загруженных файлов
    public int getUploadedFiles() {
        return uploadedFiles;
    }

    // Метод для получения общего количества файлов
    public int getTotalFiles() {
        return totalFiles;
    }
}

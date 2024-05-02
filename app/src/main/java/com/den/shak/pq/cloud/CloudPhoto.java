package com.den.shak.pq.cloud;

import static com.den.shak.pq.ConfigReader.setAccessKey;
import static com.den.shak.pq.ConfigReader.setBucketName;
import static com.den.shak.pq.ConfigReader.setSecretKey;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CloudPhoto {

    public static class UploadTask {
        private final Context mContext;
        private final String mFolder;
        private File rootFolder;
        private OnUploadProgressListener progressListener;
        private OnUploadTaskCompletedListener uploadTaskCompletedListener;

        private final List<File> filesToUpload = new ArrayList<>();
        private int uploadedFiles = 0;

        // Конструктор класса UploadTask
        public UploadTask(Context context, String folder) {
            mContext = context;
            mFolder = folder;
        }

        // Метод для установки слушателя прогресса загрузки
        public void setProgressListener(OnUploadProgressListener listener) {
            this.progressListener = listener;
        }
        // Метод для установки слушателя завершения загрузки

        public void setOnUploadTaskCompletedListener(OnUploadTaskCompletedListener listener) {
            this.uploadTaskCompletedListener = listener;
        }


        // Метод для выполнения задачи загрузки файлов
        public void execute() {
            // Запуск выполнения задачи загрузки в отдельном потоке
            new Thread(this::uploadFiles).start();
        }

        // Метод для загрузки файлов в облачное хранилище Amazon S3
        private void uploadFiles() {
            // Получаем ключи доступа и другие параметры из настроек приложения
            if (mContext != null) {
                String accessKey = setAccessKey(mContext);
                String secretKey = setSecretKey(mContext);
                String bucketName = setBucketName(mContext);
                String objectPrefix = "orders/photos/" + mFolder + "/";
                assert accessKey != null;
                assert secretKey != null;

                // Создаем экземпляр AmazonS3Client с использованием ключей доступа
                BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
                AmazonS3 s3Client = new AmazonS3Client(credentials);

                // Устанавливаем конечную точку (endpoint) для облачного хранилища
                s3Client.setEndpoint("storage.yandexcloud.net");

                // Получаем корневую папку, из которой нужно загружать файлы
                rootFolder = new File(mContext.getFilesDir() + "/images/" + mFolder);
                if (!rootFolder.exists() || !rootFolder.isDirectory()) {
                    // Если корневая папка не существует или не является директорией, выводим сообщение об ошибке
                    Log.e("CloudPhoto", "Корневая папка не существует или не является директорией");
                    uploadTaskCompletedListener.onUploadTaskCompleted();
                    return;
                }

                // Собираем список файлов для загрузки
                collectFilesToUpload(rootFolder);

                if (!filesToUpload.isEmpty()) {
                    // Начинаем загрузку файла
                    uploadNextFile(s3Client, bucketName, objectPrefix);
                }
            } else {
                // Если контекст равен null, выводим сообщение об ошибке
                Log.e("CloudPhoto", "NULL CONTEXT");
            }
        }

        // Рекурсивный метод для сбора списка файлов для загрузки
        private void collectFilesToUpload(File folder) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        filesToUpload.add(file);
                    } else if (file.isDirectory()) {
                        collectFilesToUpload(file);
                    }
                }
            }
        }

        // Метод для загрузки следующего файла
        private void uploadNextFile(AmazonS3 s3Client, String bucketName, String objectPrefix) {
            if (uploadedFiles < filesToUpload.size()) {
                File fileToUpload = filesToUpload.get(uploadedFiles);
                String relativePath = fileToUpload.getAbsolutePath().replace(rootFolder.getAbsolutePath() + "/", "");
                String objectKey = objectPrefix + relativePath;

                // Создаем экземпляр TransferUtility для загрузки файла
                TransferUtility transferUtility = TransferUtility.builder()
                        .context(mContext)
                        .s3Client(s3Client)
                        .build();

                // Загружаем файл
                TransferObserver observer = transferUtility.upload(
                        bucketName,
                        objectKey,
                        fileToUpload
                );

                // Устанавливаем слушатель для отслеживания состояния загрузки
                observer.setTransferListener(new TransferListener() {
                    @Override
                    public void onStateChanged(int id, TransferState state) {
                        // Обработка изменения состояния загрузки
                        if (state == TransferState.COMPLETED) {
                            // Если загрузка завершена, увеличиваем счетчик загруженных файлов, обновляем прогресс и начинаем загрузку следующего файла
                            uploadedFiles++;
                            updateProgress();
                            uploadNextFile(s3Client, bucketName, objectPrefix);
                        }
                    }

                    @Override
                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                        // Обработка изменения прогресса загрузки
                        updateProgress();
                    }

                    @Override
                    public void onError(int id, Exception ex) {
                        Log.e("CloudPhoto", "Ошибка загрузки для " + objectKey + ": " + ex.getMessage());
                    }
                });
            } else {
                // Если все файлы загружены, вызываем слушатель завершения загрузки
                uploadTaskCompletedListener.onUploadTaskCompleted();
            }
        }

        // Метод для обновления прогресса загрузки
        private void updateProgress() {
            if (progressListener != null) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> progressListener.onProgressUpdated(new UploadProgress(uploadedFiles, filesToUpload.size())));
            }
        }
    }
}

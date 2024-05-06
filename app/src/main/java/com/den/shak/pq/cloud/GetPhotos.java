package com.den.shak.pq.cloud;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.den.shak.pq.ConfigReader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetPhotos {
    public interface GetPhotosCallback {
        void onGetPhotosResult(List<String> photosUrls);
    }

    public static void getPhotosData(final String order_id, final GetPhotos.GetPhotosCallback callback, Context context) {
        AsyncTask.execute(() -> {
            try {
                // Формируем параметры запроса
                Map<String, String> parameters = new HashMap<>();
                if (order_id != null) {
                    parameters.put("order_id", order_id);
                }

                // Создаем URL и соединение HTTP
                String requestUrl = ConfigReader.getPhotosGatewayUrl(context) + "?" + QueryStringBuilder.buildQueryString(parameters);
                URL url = new URL(requestUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                try {
                    // Устанавливаем метод GET
                    connection.setRequestMethod("GET");

                    // Устанавливаем API-ключ в заголовках запроса
                    connection.setRequestProperty("Authorization", "Api-Key " + ConfigReader.getApiKey(context));

                    // Получаем ответ от сервера
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Список фото получен успешно
                        Log.d("GetPhotos", "Список фото получен.");

                        // Чтение ответа от сервера
                        StringBuilder response = new StringBuilder();
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }
                        }
                        // Вывод ответа от сервера в лог
                        Log.d("GetPhotos", response.toString());

                        // Парсим JSON-ответ
                        if (response.toString().equals("[]")) {
                            callback.onGetPhotosResult(null);
                        } else {
                            JSONArray jsonResponse = new JSONArray(response.toString());
                            List<String> photosUrls = new ArrayList<>();
                            for (int i = 0; i < jsonResponse.length(); i++) {
                                JSONObject jsonObject = jsonResponse.getJSONObject(i);

                                String photosUrl = jsonObject.getString("file_link");

                                photosUrls.add(photosUrl);
                            }

                            // Вызываем колбэк с результатами
                            Activity activity = (Activity) context;
                            activity.runOnUiThread(() -> callback.onGetPhotosResult(photosUrls));                        }
                    } else {
                        // Произошла ошибка
                        Log.e("GetPhotos", "Ошибка. Код ошибки: " + responseCode);
                    }
                } finally {
                    connection.disconnect();
                }
            } catch (Exception e) {
                Log.e("GetPhotos", "An error occurred", e);
            }
        });
    }
}

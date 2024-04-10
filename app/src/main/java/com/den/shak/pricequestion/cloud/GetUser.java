package com.den.shak.pricequestion.cloud;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.den.shak.pricequestion.ConfigReader;
import com.den.shak.pricequestion.models.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class GetUser {
    public interface GetUserCallback {
        void onGetUserResult(User user);
    }

    public static void getUserData(final String id, final String phoneNumber, final GetUserCallback callback, Context context) {
        AsyncTask.execute(() -> {
            try {
                // Формируем параметры запроса
                Map<String, String> parameters = new HashMap<>();
                if (id != null) {
                    parameters.put("id", id);
                } else {
                    parameters.put("phone_number", phoneNumber);
                }

                // Создаем URL и соединение HTTP
                String requestUrl = ConfigReader.getUserGatewayUrl(context) + "?" + QueryStringBuilder.buildQueryString(parameters);
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
                        // Пользователь получен
                        Log.d("GetUser", "Пользователь получен.");

                        // Чтение ответа от сервера
                        StringBuilder response = new StringBuilder();
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }
                        }
                        // Вывод ответа от сервера в лог
                        Log.d("GetUser", response.toString());

                        // Парсим JSON-ответ
                        if (response.toString().equals("[]")) {
                            callback.onGetUserResult(null);
                        } else {
                            JSONObject jsonResponse = new JSONArray(response.toString()).getJSONObject(0);
                            User user = new User();
                            user.setId(jsonResponse.getString("id"));
                            user.setPhone(jsonResponse.getString("phone"));
                            user.setName(jsonResponse.getString("name"));
                            // Вызываем колбэк с результатами
                            callback.onGetUserResult(user);
                        }
                    } else {
                        // Произошла ошибка при отправке звонка
                        Log.e("GetUser", "Ошибка при отправке звонка. Код ошибки: " + responseCode);
                    }
                } finally {
                    connection.disconnect();
                }
            } catch (Exception e) {
                Log.e("GetUser", "An error occurred", e);
            }
        });
    }
}

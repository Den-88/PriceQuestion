package com.den.shak.pricequestion.cloud;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.den.shak.pricequestion.ConfigReader;
import com.den.shak.pricequestion.models.User;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SetUser {
    public interface SetUserCallback {
        void onSetUserResult();
    }

    public static void setUser(final User user, final SetUserCallback callback, Context context) {
        AsyncTask.execute(() -> {
            try {
                // Формируем параметры запроса
                Map<String, String> parameters = new HashMap<>();
                parameters.put("id", user.getId());
                parameters.put("phone_number", user.getPhone());
                parameters.put("name", user.getName());
                parameters.put("isPerformer", user.isPerformer().toString());

                // Создаем URL и соединение HTTP
                String requestUrl = ConfigReader.setUserGatewayUrl(context) + "?" + QueryStringBuilder.buildQueryString(parameters);
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
                        // Запись в БД выолнена
                        Log.d("SetUser", "Запись в БД выолнена.");
                        callback.onSetUserResult();
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

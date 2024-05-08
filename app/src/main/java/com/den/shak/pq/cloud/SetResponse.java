package com.den.shak.pq.cloud;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.den.shak.pq.ConfigReader;
import com.den.shak.pq.models.Response;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SetResponse {
    public interface SetResponseCallback {
        void onSetResponseResult();
    }

    public static void setResponse(final Response response, final SetResponseCallback callback, Context context) {
        AsyncTask.execute(() -> {
            try {
                // Формируем параметры запроса
                Map<String, String> parameters = new HashMap<>();
                parameters.put("id", response.getId());
                parameters.put("id_order", response.getIdOrder());
                parameters.put("id_performer", response.getIdPerformer());
                parameters.put("text", response.getText());
                parameters.put("price", String.valueOf(response.getPrice()));

                if (response.getAccepted() != null) {
                    parameters.put("is_accepted", response.getAccepted().toString());
                }

                // Создаем URL и соединение HTTP
                String requestUrl = ConfigReader.setResponseGatewayUrl(context) + "?" + QueryStringBuilder.buildQueryString(parameters);
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
                        Log.d("setResponse", "Запись в БД выолнена.");
                        callback.onSetResponseResult();
                    } else {
                        // Произошла ошибка
                        Log.e("setResponse", "Ошибка. Код ошибки: " + responseCode);
                    }
                } finally {
                    connection.disconnect();
                }
            } catch (Exception e) {
                Log.e("setResponse", "An error occurred", e);
            }
        });
    }

}

package com.den.shak.pq.cloud;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.den.shak.pq.ConfigReader;
import com.den.shak.pq.models.Order;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class DeleteOrder {
    public interface DeleteOrderCallback {
        void onDeleteOrderResult();
    }

    public static void deleteOrder(final Order order, final DeleteOrderCallback callback, Context context) {
        AsyncTask.execute(() -> {
            try {
                // Формируем параметры запроса
                Map<String, String> parameters = new HashMap<>();
                parameters.put("order_id", order.getId());

                // Создаем URL и соединение HTTP
                String requestUrl = ConfigReader.deleteOrderGatewayUrl(context) + "?" + QueryStringBuilder.buildQueryString(parameters);
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
                        Log.d("DeleteOrder", "Запись в БД выолнена.");
                        callback.onDeleteOrderResult();
                    } else {
                        // Произошла ошибка при отправке звонка
                        Log.e("DeleteOrder", "Ошибка. Код ошибки: " + responseCode);
                    }
                } finally {
                    connection.disconnect();
                }
            } catch (Exception e) {
                Log.e("DeleteOrder", "An error occurred", e);
            }
        });
    }

}

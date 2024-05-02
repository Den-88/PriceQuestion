package com.den.shak.pq.cloud;

import static com.den.shak.pq.activity.MainActivity.current_user;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.den.shak.pq.ConfigReader;
import com.den.shak.pq.models.Order;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

// Класс для установки заказа на сервере
public class SetOrder {
    // Интерфейс обратного вызова для результата установки заказа
    public interface SetOrderCallback {
        void onSetOrderResult();
    }

    // Метод для установки заказа на сервере
    public static void setOrder(final Order order, final SetOrderCallback callback, Context context) {
        AsyncTask.execute(() -> {
            try {
                // Формируем параметры запроса
                Map<String, String> parameters = new HashMap<>();
                parameters.put("order_id", order.getId());
                parameters.put("user_id", current_user.getId());
                parameters.put("category_id", String.valueOf(order.getCategoryID()));
                parameters.put("title", order.getTitle());
                parameters.put("description", order.getDescription());
                parameters.put("price", String.valueOf(order.getPrice()));
                parameters.put("location", String.valueOf(order.getLocation()));

                // Создаем URL и соединение HTTP
                String requestUrl = ConfigReader.setOrderGatewayUrl(context) + "?" + QueryStringBuilder.buildQueryString(parameters);
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
                        Log.d("SetOrder", "Запись в БД выолнена.");
                        callback.onSetOrderResult();
                    } else {
                        Log.e("SetOrder", "Ошибка записи в БД. Код ошибки: " + responseCode);
                    }
                } finally {
                    connection.disconnect();
                }
            } catch (Exception e) {
                Log.e("SetOrder", "An error occurred", e);
            }
        });
    }
}

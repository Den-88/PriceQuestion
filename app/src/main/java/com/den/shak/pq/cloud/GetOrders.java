package com.den.shak.pq.cloud;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.den.shak.pq.ConfigReader;
import com.den.shak.pq.models.Order;
import com.google.android.gms.maps.model.LatLng;

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

public class GetOrders {
    public interface GetOrdersCallback {
        void onGetOrdersResult(List<Order> orders);
    }

    public static void getOrdersData(final String search_text, final String order_id, final String user_id, final List<Integer> category_ids, final GetOrders.GetOrdersCallback callback, Context context) {
        AsyncTask.execute(() -> {
            try {
                // Формируем параметры запроса
                Map<String, String> parameters = new HashMap<>();
                if (order_id != null) {
                    parameters.put("order_id", order_id);
                } else
                if (search_text != null) {
                    parameters.put("search_text", search_text);
                } else
                if (user_id != null) {
                    parameters.put("user_id", user_id);
                } else if (category_ids != null) {
                    parameters.put("category_ids", category_ids.toString());
                }

                // Создаем URL и соединение HTTP
                String requestUrl = ConfigReader.getOrdersGatewayUrl(context) + "?" + QueryStringBuilder.buildQueryString(parameters);
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
                        // Список заявок получен успешно
                        Log.d("GetOrders", "Список заявок получен.");

                        // Чтение ответа от сервера
                        StringBuilder response = new StringBuilder();
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }
                        }
                        // Вывод ответа от сервера в лог
                        Log.d("GetOrders", response.toString());

                        // Парсим JSON-ответ
                        if (response.toString().equals("[]")) {
                            callback.onGetOrdersResult(null);
                        } else {
                            JSONArray jsonResponse = new JSONArray(response.toString());
                            List<Order> orders = new ArrayList<>();
                            for (int i = 0; i < jsonResponse.length(); i++) {
                                JSONObject jsonObject = jsonResponse.getJSONObject(i);

                                Order order = new Order();
                                order.setId(jsonObject.getString("id"));
                                order.setTitle(jsonObject.getString("title"));
                                if (!jsonObject.isNull("description")) {
                                    order.setDescription(jsonObject.getString("description"));
                                }

                                if (!jsonObject.isNull("price")) {
                                    order.setPrice(jsonObject.getInt("price"));
                                }
                                order.setCategory(jsonObject.getInt("category_id"));

                                if (!jsonObject.isNull("location")) {
                                    String locationString = jsonObject.getString("location");
                                    Log.d("GetOrders", locationString);
                                    JSONObject locationObject = new JSONObject(locationString);
                                    double latitude = locationObject.getDouble("latitude");
                                    double longitude = locationObject.getDouble("longitude");
                                    // Создаем объект LatLng
                                    LatLng latLng = new LatLng(latitude, longitude);
                                    order.setLocation(latLng);
                                }


                                orders.add(order);
                            }

                            // Вызываем колбэк с результатами
                            Activity activity = (Activity) context;
                            activity.runOnUiThread(() -> callback.onGetOrdersResult(orders));                        }
                    } else {
                        // Произошла ошибка
                        Log.e("GetOrders", "Ошибка. Код ошибки: " + responseCode);
                    }
                } finally {
                    connection.disconnect();
                }
            } catch (Exception e) {
                Log.e("GetOrders", "An error occurred", e);
            }
        });
    }
}
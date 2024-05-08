package com.den.shak.pq.cloud;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.den.shak.pq.ConfigReader;
import com.den.shak.pq.models.Order;
import com.den.shak.pq.models.Response;
import com.den.shak.pq.models.User;

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

public class GetResponses {
    public interface GetResponsesCallback {

        void onGetResponsesResult(List<Order> ordersMy, List<Response> responsesMy, List<User> usersMy, List<Order> ordersForMe, List<Response> responsesForMe, List<User> usersForMe);
    }

    public static void getResponsesData(final String user_id, final GetResponses.GetResponsesCallback callback, Context context) {
        AsyncTask.execute(() -> {
            try {
                // Формируем параметры запроса
                Map<String, String> parameters = new HashMap<>();
                parameters.put("user_id", user_id);

                // Создаем URL и соединение HTTP
                String requestUrl = ConfigReader.getResponsesGatewayUrl(context) + "?" + QueryStringBuilder.buildQueryString(parameters);
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
                        Log.d("GetResponses", "Список откликов получен.");

                        // Чтение ответа от сервера
                        StringBuilder response = new StringBuilder();
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }
                        }
                        // Вывод ответа от сервера в лог
                        Log.d("GetResponses", response.toString());

// Парсим JSON-ответ
                        JSONArray jsonResponse = new JSONArray(response.toString());
                        if (jsonResponse.length() == 2) {
                            List<Response> responsesMy = new ArrayList<>();
                            List<Order> ordersMy = new ArrayList<>();
                            List<User> usersMy = new ArrayList<>();

                            List<Response> responsesForMe = new ArrayList<>();
                            List<Order> ordersForMe = new ArrayList<>();
                            List<User> usersForMe = new ArrayList<>();

                            // Создаем массивы для разных типов ответов
                            List<List<Response>> allResponses = new ArrayList<>();
                            allResponses.add(responsesMy);
                            allResponses.add(responsesForMe);

                            List<List<Order>> allOrders = new ArrayList<>();
                            allOrders.add(ordersMy);
                            allOrders.add(ordersForMe);

                            List<List<User>> allUsers = new ArrayList<>();
                            allUsers.add(usersMy);
                            allUsers.add(usersForMe);

                            for (int k = 0; k < jsonResponse.length(); k++) {
                                JSONArray jsonArray = jsonResponse.getJSONArray(k);
                                List<Response> responses = allResponses.get(k);
                                List<Order> orders = allOrders.get(k);
                                List<User> users = allUsers.get(k);

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    Response respons = new Response();
                                    Order order = new Order();
                                    User user = new User();

                                    order.setId(jsonObject.getString("o.id"));
                                    order.setTitle(jsonObject.getString("o.title"));
                                    if (!jsonObject.isNull("o.price")) {
                                        order.setPrice(jsonObject.getInt("o.price"));
                                    }
                                    order.setCategory(jsonObject.getInt("o.category_id"));

                                    respons.setIdPerformer(jsonObject.getString("r.id_performer"));
                                    respons.setId(jsonObject.getString("r.id"));
                                    respons.setIdOrder(jsonObject.getString("o.id"));
                                    respons.setText(jsonObject.getString("r.text"));
                                    respons.setPrice(jsonObject.getInt("r.price"));
                                    if (!jsonObject.isNull("r.is_accepted")) {
                                        respons.setAccepted(jsonObject.getBoolean("r.is_accepted"));
                                    }

                                    if (!jsonObject.isNull("u.name")) {
                                        user.setName(jsonObject.getString("u.name"));
                                    }
                                    if (!jsonObject.isNull("u.phone")) {
                                        user.setPhone(jsonObject.getString("u.phone"));
                                    }
                                    orders.add(order);
                                    responses.add(respons);
                                    users.add(user);
                                }
                            }

                            // Вызываем колбэк с результатами
                            Activity activity = (Activity) context;
                            activity.runOnUiThread(() -> callback.onGetResponsesResult(ordersMy, responsesMy, usersMy, ordersForMe, responsesForMe, usersForMe));
                        } else {
                            // Пустой массив или неправильный формат данных, вызываем колбэк с пустыми данными
                            callback.onGetResponsesResult(null, null, null, null, null, null);
                        }

                    } else {
                        // Произошла ошибка
                        Log.e("GetResponses", "Ошибка. Код ошибки: " + responseCode);
                    }
                } finally {
                    connection.disconnect();
                }
            } catch (Exception e) {
                Log.e("GetResponses", "An error occurred", e);
            }
        });
    }
}
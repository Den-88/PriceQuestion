package com.den.shak.pq.cloud;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.den.shak.pq.ConfigReader;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class VoiceAuth {

    public interface VoiceAuthCallback {
        void onVoiceAuthResult(String status, String code);
    }

    public static void sendVoiceAuth(final String phoneNumber, final VoiceAuthCallback callback, Context context) {

//        //TODO Для тестирования! Удалить потом!
//        new Handler().postDelayed(() -> {
//            callback.onVoiceAuthResult("OK", String.valueOf(1111));
//        }, 3000); // 3000 миллисекунд (3 секунды)

        AsyncTask.execute(() -> {
            try {
                // Формируем параметры запроса
                Map<String, String> parameters = new HashMap<>();
                parameters.put("phone_number", phoneNumber);
                parameters.put("ip", IPService.getIPAddress());

                // Создаем URL и соединение HTTP
                String requestUrl = ConfigReader.getCallGatewayUrl(context) + "?" + QueryStringBuilder.buildQueryString(parameters);
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
                        // Звонок был успешно отправлен
                        Log.d("VoiceAuth", "Звонок отправлен успешно.");

                        // Чтение ответа от сервера
                        StringBuilder response = new StringBuilder();
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }
                        }
                        // Вывод ответа от сервера в лог
                        Log.d("VoiceAuth", response.toString());

                        // Парсим JSON-ответ
                        JSONObject jsonResponse = new JSONObject(response.toString());
                        String status = jsonResponse.getString("status");
                        String code = jsonResponse.getString("code");

                        // Вызываем колбэк с результатами
                        callback.onVoiceAuthResult(status, code);
                    } else {
                        // Произошла ошибка при отправке звонка
                        Log.e("VoiceAuth", "Ошибка при отправке звонка. Код ошибки: " + responseCode);

                        // Вызываем колбэк с ошибкой
                        callback.onVoiceAuthResult("ERROR", "");
                    }
                } finally {
                    connection.disconnect();
                }
            } catch (Exception e) {
                Log.e("VoiceAuth", "An error occurred", e);

                // Вызываем колбэк с ошибкой
                callback.onVoiceAuthResult("ERROR", "");
            }
        });
    }
}

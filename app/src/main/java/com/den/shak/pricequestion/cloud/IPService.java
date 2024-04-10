package com.den.shak.pricequestion.cloud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.util.Log;

public class IPService {

    // Метод для получения публичного IP-адреса
    public static String getIPAddress() {
        try {
            // Создаём URL для запроса к внешнему сервису
            URL url = new URL("https://api64.ipify.org?format=text");

            // Открываем соединение
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Получаем ответ от сервера
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // response.toString() содержит публичный IP-адрес
                return response.toString();
            }
        } catch (IOException e) {
            // В случае возникновения ошибки логируем исключение
            Log.e("IPService", "Error getting public IP address", e);
        }
        // Если не удалось получить публичный IP-адрес, возвращаем -1
        return "-1";
    }
}

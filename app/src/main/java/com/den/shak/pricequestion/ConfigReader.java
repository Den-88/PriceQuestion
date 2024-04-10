package com.den.shak.pricequestion;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import android.util.Log;

public class ConfigReader {
    private static final String TAG = "ConfigReader";

    // Метод для получения API-ключа из конфигурационного файла
    public static String getApiKey(Context context) {
        Properties properties = new Properties();
        try {
            // Открываем поток для чтения файла config.properties из ресурсов приложения
            InputStream inputStream = context.getResources().openRawResource(R.raw.config);
            // Загружаем свойства из файла в объект Properties
            properties.load(inputStream);
            // Возвращаем значение свойства "API_KEY"
            return properties.getProperty("API_KEY");
        } catch (IOException e) {
            // Если произошла ошибка при чтении файла, логируем её
            Log.e(TAG, "Error reading config file", e);
        }
        // Возвращаем null, если чтение не удалось
        return null;
    }

    // Метод для получения URL шлюза аутентификации из конфигурационного файла
    public static String getCallGatewayUrl(Context context) {
        Properties properties = new Properties();
        try {
            // Открываем поток для чтения файла config.properties из ресурсов приложения
            InputStream inputStream = context.getResources().openRawResource(R.raw.config);
            // Загружаем свойства из файла в объект Properties
            properties.load(inputStream);
            // Возвращаем значение свойства "AUTH_GATEWAY_URL"
            return properties.getProperty("AUTH_GATEWAY_URL");
        } catch (IOException e) {
            // Если произошла ошибка при чтении файла, логируем её
            Log.e(TAG, "Error reading config file", e);
        }
        // Возвращаем null, если чтение не удалось
        return null;
    }

    // Метод для получения URL шлюза данных пользователя из конфигурационного файла
    public static String getUserGatewayUrl(Context context) {
        Properties properties = new Properties();
        try {
            // Открываем поток для чтения файла config.properties из ресурсов приложения
            InputStream inputStream = context.getResources().openRawResource(R.raw.config);
            // Загружаем свойства из файла в объект Properties
            properties.load(inputStream);
            // Возвращаем значение свойства "GET_USER_GATEWAY_URL"
            return properties.getProperty("GET_USER_GATEWAY_URL");
        } catch (IOException e) {
            // Если произошла ошибка при чтении файла, логируем её
            Log.e(TAG, "Error reading config file", e);
        }
        // Возвращаем null, если чтение не удалось
        return null;
    }
}

package com.den.shak.pq;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import android.util.Log;

public class ConfigReader {
    private static final String TAG = "ConfigReader";

    // Методы для получения API-ключа из конфигурационного файла
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

    public static String setUserGatewayUrl(Context context) {
        Properties properties = new Properties();
        try {
            // Открываем поток для чтения файла config.properties из ресурсов приложения
            InputStream inputStream = context.getResources().openRawResource(R.raw.config);
            // Загружаем свойства из файла в объект Properties
            properties.load(inputStream);
            // Возвращаем значение свойства "SET_USER_GATEWAY_URL"
            return properties.getProperty("SET_USER_GATEWAY_URL");
        } catch (IOException e) {
            // Если произошла ошибка при чтении файла, логируем её
            Log.e(TAG, "Error reading config file", e);
        }
        // Возвращаем null, если чтение не удалось
        return null;
    }

    public static String setOrderGatewayUrl(Context context) {
        Properties properties = new Properties();
        try {
            // Открываем поток для чтения файла config.properties из ресурсов приложения
            InputStream inputStream = context.getResources().openRawResource(R.raw.config);
            // Загружаем свойства из файла в объект Properties
            properties.load(inputStream);
            // Возвращаем значение свойства "SET_USER_GATEWAY_URL"
            return properties.getProperty("SET_ORDER_GATEWAY_URL");
        } catch (IOException e) {
            // Если произошла ошибка при чтении файла, логируем её
            Log.e(TAG, "Error reading config file", e);
        }
        // Возвращаем null, если чтение не удалось
        return null;
    }

    public static String setMapKitApiKey(Context context) {
        Properties properties = new Properties();
        try {
            // Открываем поток для чтения файла config.properties из ресурсов приложения
            InputStream inputStream = context.getResources().openRawResource(R.raw.config);
            // Загружаем свойства из файла в объект Properties
            properties.load(inputStream);
            // Возвращаем значение свойства "YANDEX_MAPKIT_API_KEY"
            return properties.getProperty("YANDEX_MAPKIT_API_KEY");
        } catch (IOException e) {
            // Если произошла ошибка при чтении файла, логируем её
            Log.e(TAG, "Error reading config file", e);
        }
        // Возвращаем null, если чтение не удалось
        return null;
    }

    public static String setAccessKey(Context context) {
        Properties properties = new Properties();
        try {
            // Открываем поток для чтения файла config.properties из ресурсов приложения
            InputStream inputStream = context.getResources().openRawResource(R.raw.config);
            // Загружаем свойства из файла в объект Properties
            properties.load(inputStream);
            // Возвращаем значение свойства "STORAGE_ACCESS_KEY"
            return properties.getProperty("STORAGE_ACCESS_KEY");
        } catch (IOException e) {
            // Если произошла ошибка при чтении файла, логируем её
            Log.e(TAG, "Error reading config file", e);
        }
        // Возвращаем null, если чтение не удалось
        return null;
    }

    public static String setSecretKey(Context context) {
        Properties properties = new Properties();
        try {
            // Открываем поток для чтения файла config.properties из ресурсов приложения
            InputStream inputStream = context.getResources().openRawResource(R.raw.config);
            // Загружаем свойства из файла в объект Properties
            properties.load(inputStream);
            // Возвращаем значение свойства "STORAGE_SEECRET_KEY"
            return properties.getProperty("STORAGE_SEECRET_KEY");
        } catch (IOException e) {
            // Если произошла ошибка при чтении файла, логируем её
            Log.e(TAG, "Error reading config file", e);
        }
        // Возвращаем null, если чтение не удалось
        return null;
    }

    public static String setBucketName(Context context) {
        Properties properties = new Properties();
        try {
            // Открываем поток для чтения файла config.properties из ресурсов приложения
            InputStream inputStream = context.getResources().openRawResource(R.raw.config);
            // Загружаем свойства из файла в объект Properties
            properties.load(inputStream);
            // Возвращаем значение свойства "BUCKET_NAME"
            return properties.getProperty("BUCKET_NAME");
        } catch (IOException e) {
            // Если произошла ошибка при чтении файла, логируем её
            Log.e(TAG, "Error reading config file", e);
        }
        // Возвращаем null, если чтение не удалось
        return null;
    }
}

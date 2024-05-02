package com.den.shak.pq.cloud;

import java.util.Map;

public class QueryStringBuilder {

    // Метод для построения строки запроса URL на основе переданных параметров
    public static String buildQueryString(Map<String, String> parameters) {
        // Создаём объект StringBuilder для построения строки запроса
        StringBuilder queryString = new StringBuilder();
        // Проходим по элементам Map
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            // Если строка запроса уже содержит элементы, добавляем символ & для разделения параметров.
            if (queryString.length() > 0) {
                queryString.append("&");
            }
            // Добавляем ключ и значение из Map в строку запроса, разделяя их символом =
            queryString.append(entry.getKey()).append("=").append(entry.getValue());
        }
        // Возвращаем построенную строку запроса
        return queryString.toString();
    }
}

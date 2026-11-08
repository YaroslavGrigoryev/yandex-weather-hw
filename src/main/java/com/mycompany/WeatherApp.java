
package com.mycompany;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WeatherApp {
    public static void main(String[] args) throws IOException, InterruptedException {

        int limit = 3;
        String apiKey = System.getenv("YANDEX_WEATHER_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("Ошибка: API-ключ не задан!");
            return;
        }

        if (limit <= 0) {
            System.out.println("Ошибка: limit должен быть больше 0!");
            return;
        }

        String url = "https://api.weather.yandex.ru/v2/forecast?lat=55.75&lon=37.62&limit=" + limit;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-Yandex-Weather-Key", apiKey)
                .GET()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            System.out.printf("Ошибка HTTP: %d%n", response.statusCode());
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());

        JsonNode temp = root.path("fact").path("temp");
        JsonNode forecasts = root.path("forecasts");

        if (!temp.isNumber()) {
            System.out.println("Ошибка: текущая температура отсутствует!");
            return;
        }

        if (!forecasts.isArray() || forecasts.size() < limit) {
            System.out.println("Ошибка: недостаточно данных прогноза!");
            return;
        }

        double sum = 0;

        for (int i = 0; i < limit; i++) {
            JsonNode dayTemp = forecasts.get(i)
                    .path("parts")
                    .path("day")
                    .path("temp_avg");

            if (!dayTemp.isNumber()) {
                System.out.printf("Ошибка: нет температуры за день %d%n", i + 1);
                return;
            }

            sum += dayTemp.asDouble();
        }

        double averageTemperature = sum / limit;

        System.out.printf("""
                
                ===== ЯНДЕКС ПОГОДА =====
                Полный JSON-ответ: %s
                Текущая температура: %d °C
                Средняя температура за %d дня: %.2f °C
                ========================
                %n""",
                response.body(), temp.asInt(), limit, averageTemperature);
    }
}

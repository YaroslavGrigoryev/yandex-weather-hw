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
        String url = "https://api.weather.yandex.ru/v2/forecast?lat=55.75&lon=37.62&limit=3";
        String apiKey = System.getenv("YANDEX_WEATHER_API_KEY");
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();

        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("Переменная окружения не задана!");
            return;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-Yandex-Weather-Key", apiKey)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.out.println("Ответ сервера не равен 200");
            return;
        }

        JsonNode root = objectMapper.readTree(response.body());

        int temperature = root.path("fact").path("temp").asInt();

        JsonNode forecasts = root.path("forecasts");
        double sum = 0;

        for (JsonNode forecast : forecasts) {
            sum += forecast.path("parts").path("day").path("temp_avg").asDouble();

        }

        double averageTemperature = 0;

        if (!forecasts.isEmpty()) {
            averageTemperature = sum / forecasts.size();
        } else {
            System.out.println("Массив прогнозов пуст");
            return;
        }


        System.out.printf("Полный JSON-ответ : %s%nТекущая температура : %d%nСредняя температура прогноза : %.2f",
                response.body(), temperature, averageTemperature);

    }
}

package com.discover.backend.context;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class WeatherClient {

    private final RestClient restClient;
    private final String apiKey;

    public WeatherClient(RestClient.Builder restClientBuilder, @Value("${openweather.api-key}") String apiKey) {
        this.restClient = restClientBuilder.baseUrl("https://api.openweathermap.org").build();
        this.apiKey = apiKey;
    }

    public OpenWeatherResponse getCurrentWeather(double lat, double lng) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/data/2.5/weather")
                        .queryParam("lat", lat)
                        .queryParam("lon", lng)
                        .queryParam("appid", apiKey)
                        .queryParam("units", "metric")
                        .build())
                .retrieve()
                .body(OpenWeatherResponse.class);
    }
}

package com.discover.backend.context;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherRule implements ContextRule {

    private final WeatherClient weatherClient;

    @Override
    public Optional<ContextSuggestion> evaluate(ContextInput input) {
        if (input.getLatitude() == null || input.getLongitude() == null) {
            return Optional.empty();
        }

        OpenWeatherResponse response;
        try {
            response = weatherClient.getCurrentWeather(input.getLatitude(), input.getLongitude());
        } catch (Exception e) {
            // weather is a nice-to-have banner, not core functionality — an OpenWeather
            // outage/timeout should never break the rest of the home feed
            log.warn("Failed to fetch weather, skipping weather-based suggestion", e);
            return Optional.empty();
        }

        if (response == null || response.weather() == null || response.weather().isEmpty()) {
            return Optional.empty();
        }

        String condition = response.weather().get(0).main();
        ContextSuggestion suggestion = new ContextSuggestion();

        switch (condition) {
            case "Rain", "Thunderstorm" -> {
                suggestion.setMessage("Perfect weather for chai and pakoras");
                suggestion.setSuggestedTags(List.of("chai", "pakoras", "hot-snacks"));
            }
            case "Clear" -> {
                if (response.main() != null && response.main().temp() >= 30) {
                    suggestion.setMessage("Hot day — something cool might hit the spot");
                    suggestion.setSuggestedTags(List.of("cold-beverage", "ice-cream"));
                } else {
                    return Optional.empty();
                }
            }
            default -> {
                return Optional.empty();
            }
        }

        return Optional.of(suggestion);
    }
}

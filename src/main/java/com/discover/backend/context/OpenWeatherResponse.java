package com.discover.backend.context;

import java.util.List;

// only the fields we actually need out of OpenWeather's much larger response shape
public record OpenWeatherResponse(List<Weather> weather, Main main) {

    public record Weather(String main, String description) {
    }

    public record Main(double temp) {
    }
}

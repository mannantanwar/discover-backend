package com.discover.backend.place;

import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class PlaceDto {
    private UUID publicId;
    private String name;
    private String category;
    private String description;
    private String address;
    private Double latitude;
    private Double longitude;
    private Integer budgetLevel;
    private Map<String, Object> openingHours;
}

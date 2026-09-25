package com.discover.backend.dishreview;

import lombok.Data;

@Data
public class DishStatsDto {
    private Double averageRating;
    private long reviewCount;
}

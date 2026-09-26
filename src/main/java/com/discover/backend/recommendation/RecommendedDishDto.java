package com.discover.backend.recommendation;

import com.discover.backend.dish.DishDto;
import lombok.Data;

@Data
public class RecommendedDishDto {
    private DishDto dish;
    private String reason;
}

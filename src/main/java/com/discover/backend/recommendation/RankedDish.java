package com.discover.backend.recommendation;

import com.discover.backend.dish.Dish;
import lombok.Data;

@Data
public class RankedDish {
    private Dish dish;
    private String reason ;
}

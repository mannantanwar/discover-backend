package com.discover.backend.dishreview;

import lombok.Data;

@Data
public class DishReviewRequest {
    private Integer rating;
    private String text;
}

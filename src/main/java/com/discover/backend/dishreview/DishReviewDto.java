package com.discover.backend.dishreview;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class DishReviewDto {
    private UUID publicId;
    private UUID dishPublicId;
    private Integer rating;
    private String text;
    private Instant createdAt;
}

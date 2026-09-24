package com.discover.backend.dish;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class DishDto {
    private UUID publicId;
    private UUID placePublicId;
    private String name;
    private String description;
    private BigDecimal price;
    private List<String> tasteTags;
    private String photoUrl;
}

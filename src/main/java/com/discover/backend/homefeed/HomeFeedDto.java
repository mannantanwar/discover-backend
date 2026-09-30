package com.discover.backend.homefeed;

import com.discover.backend.recommendation.RecommendedDishDto;
import lombok.Data;

import java.util.List;

@Data
public class HomeFeedDto {
    private List<RecommendedDishDto> recommendedDishes;
    private List<HomeFeedSection> contextSections;
}

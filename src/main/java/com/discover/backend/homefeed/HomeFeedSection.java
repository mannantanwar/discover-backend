package com.discover.backend.homefeed;

import com.discover.backend.recommendation.RecommendedDishDto;
import lombok.Data;

import java.util.List;

@Data
public class HomeFeedSection {
    private String message;
    private List<RecommendedDishDto> dishes;
}

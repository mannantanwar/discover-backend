package com.discover.backend.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DishRecommendationStrategyFactory {

    private final RuleBasedDishRecommendationStrategy ruleBasedDishRecommendationStrategy;
    private final TasteProfileDishRecommendationStrategy tasteProfileDishRecommendationStrategy;

    public DishRecommendationStrategy getStrategy(RecommendationType type) {
        return switch (type) {
            case RULE_BASED -> ruleBasedDishRecommendationStrategy;
            case TASTE_PROFILE -> tasteProfileDishRecommendationStrategy;
        };
    }
}

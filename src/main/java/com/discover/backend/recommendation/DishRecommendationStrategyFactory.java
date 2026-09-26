package com.discover.backend.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DishRecommendationStrategyFactory {

    private final RuleBasedDishRecommendationStrategy ruleBasedDishRecommendationStrategy;

    public DishRecommendationStrategy getStrategy(RecommendationType type) {
        return switch (type) {
            case RULE_BASED -> ruleBasedDishRecommendationStrategy;
        };
    }
}

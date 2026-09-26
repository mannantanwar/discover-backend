package com.discover.backend.recommendation;

import com.discover.backend.dish.Dish;
import com.discover.backend.dish.DishMapper;
import com.discover.backend.dish.DishService;
import com.discover.backend.recommendation.DishRecommendationStrategy.RankedDish;
import com.discover.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DishRecommendationService {

    private final DishService dishService;
    private final DishRecommendationStrategyFactory strategyFactory;
    private final DishMapper dishMapper;

    // ab isme controller se aaye huye metthods likhne hai
    public List<RecommendedDishDto> getRecommendedDishes(User user, UUID placePublicId, RecommendationType type) {
        List<Dish> candidateDishes = dishService.getDishEntitiesForPlace(placePublicId);
        DishRecommendationStrategy strategy = strategyFactory.getStrategy(type);
        List<RankedDish> ranked = strategy.getRecommendedDishes(candidateDishes, user);

        return ranked.stream()
                .map(r -> {
                    RecommendedDishDto dto = new RecommendedDishDto();
                    dto.setDish(dishMapper.toDto(r.getDish()));
                    dto.setReason(r.getReason());
                    return dto;
                })
                .toList();
    }
}

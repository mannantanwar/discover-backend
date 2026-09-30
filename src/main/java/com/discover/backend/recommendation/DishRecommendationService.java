package com.discover.backend.recommendation;

import com.discover.backend.dish.Dish;
import com.discover.backend.dish.DishMapper;
import com.discover.backend.dish.DishService;
import com.discover.backend.recommendation.DishRecommendationStrategy.RankedDish;
import com.discover.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DishRecommendationService {

    private final DishService dishService;
    private final DishRecommendationStrategyFactory strategyFactory;
    private final DishMapper dishMapper;

    // ab isme controller se aaye huye metthods likhne hai
    public List<RecommendedDishDto> getRecommendedDishes(User user, UUID placePublicId, RecommendationType type) {
        return getRecommendedDishes(user, dishService.getDishEntitiesForPlace(placePublicId), type);
    }

    // the strategies already take any List<Dish> — this lets callers like the home feed pass
    // dishes from many places at once instead of being limited to one restaurant's menu
    public List<RecommendedDishDto> getRecommendedDishes(User user, List<Dish> candidateDishes, RecommendationType type) {
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

    // two-stage: tags narrow the pool to what fits the context, then the strategy ranks
    // that shortlist by what this particular user would like
    public List<RecommendedDishDto> getRecommendedDishesMatchingTags(User user, List<Dish> candidateDishes,
                                                                     List<String> tags, RecommendationType type) {
        List<Dish> matching = filterByTags(candidateDishes, tags);
        if (matching.isEmpty()) {
            return List.of();
        }
        return getRecommendedDishes(user, matching, type);
    }

    private List<Dish> filterByTags(List<Dish> dishes, List<String> tags) {
        Set<String> wanted = new HashSet<>(tags);
        return dishes.stream()
                .filter(dish -> dish.getTasteTags() != null
                        && dish.getTasteTags().stream().anyMatch(wanted::contains))
                .toList();
    }
}

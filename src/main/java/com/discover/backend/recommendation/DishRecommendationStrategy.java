package com.discover.backend.recommendation;

import com.discover.backend.dish.Dish;
import com.discover.backend.user.User;
import lombok.Data;

import java.util.List;

public interface DishRecommendationStrategy {
    // this will contain the list recmomendation interface which will have many strategies to be implemneted
    List<RankedDish> getRecommendedDishes(List<Dish> candidateDishes, User user);

    // internal result type — every strategy returns this shape, never exposed outside the
    // recommendation feature itself. DishRecommendationService maps it to RecommendedDishDto
    // before it ever reaches a controller.
    @Data
    class RankedDish {
        private Dish dish;
        private String reason;
    }
}

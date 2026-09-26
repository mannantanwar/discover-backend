package com.discover.backend.recommendation;

import com.discover.backend.dish.Dish;
import com.discover.backend.user.User;


import java.util.List;

public interface DishRecommendationStrategy {
    // this will contain the list recmomendation interface which will have many strategies to be implemneted
    List<RankedDish> getRecommendedDishes(List<Dish>candidateDishes , User user);
}

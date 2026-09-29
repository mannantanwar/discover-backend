package com.discover.backend.recommendation;

import com.discover.backend.dish.Dish;
import com.discover.backend.dishreview.DishReviewService;
import com.discover.backend.dishreview.DishStatsDto;
import com.discover.backend.recommendation.DishRecommendationStrategy.RankedDish;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

// shared cold-start fallback — used by any strategy that has no real preference signal
// to rank against yet (no reviews, no explicit picks). Extracted once a second strategy
// genuinely needed the exact same logic, not preemptively.
@Component
@RequiredArgsConstructor
public class PopularityFallbackRanker {

    private final DishReviewService dishReviewService;

    public List<RankedDish> rankByPopularity(List<Dish> candidateDishes) {
        return candidateDishes.stream()
                .sorted(Comparator.comparingDouble(this::popularityScore).reversed())
                .map(dish -> {
                    RankedDish rd = new RankedDish();
                    rd.setDish(dish);
                    rd.setReason("Popular choice");
                    return rd;
                })
                .toList();
    }

    private double popularityScore(Dish dish) {
        DishStatsDto stats = dishReviewService.getStatsForDish(dish);
        double avg = stats.getAverageRating() == null ? 0.0 : stats.getAverageRating();
        return avg * stats.getReviewCount();
    }
}

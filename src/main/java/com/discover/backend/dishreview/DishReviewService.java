package com.discover.backend.dishreview;

import com.discover.backend.dish.Dish;
import com.discover.backend.dish.DishService;
import com.discover.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DishReviewService {

    private final DishReviewRepository dishReviewRepository;
    private final DishReviewMapper dishReviewMapper;
    private final DishService dishService;

    public List<DishReviewDto> getReviewsForDish(UUID dishPublicId) {
        Dish dish = dishService.getEntityByPublicId(dishPublicId);
        return dishReviewRepository.findByDish(dish).stream()
                .map(dishReviewMapper::toDto)
                .toList();
    }

    public DishReviewDto addOrUpdateReview(User user, UUID dishPublicId, Integer rating, String text) {
        Dish dish = dishService.getEntityByPublicId(dishPublicId);
        DishReview review = dishReviewRepository.findByUserAndDish(user, dish)
                .orElseGet(() -> DishReview.builder().publicId(UUID.randomUUID()).user(user).dish(dish).build());
        review.setRating(rating);
        review.setText(text);
        return dishReviewMapper.toDto(dishReviewRepository.save(review));
    }

    public DishStatsDto getStatsForDish(UUID dishPublicId) {
        Dish dish = dishService.getEntityByPublicId(dishPublicId);
        return getStatsForDish(dish);
    }

    public DishStatsDto getStatsForDish(Dish dish) {
        DishStatsDto stats = new DishStatsDto();
        stats.setAverageRating(dishReviewRepository.averageRatingByDish(dish));
        stats.setReviewCount(dishReviewRepository.countByDish(dish));
        return stats;
    }

    public List<DishReview> getAllReviewsByUser(User user) {
        return dishReviewRepository.findByUser(user);
    }
}

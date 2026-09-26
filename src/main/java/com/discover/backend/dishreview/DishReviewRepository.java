package com.discover.backend.dishreview;

import com.discover.backend.dish.Dish;
import com.discover.backend.user.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DishReviewRepository extends JpaRepository<DishReview, Long> {

    List<DishReview> findByDish(Dish dish);

    Optional<DishReview> findByUserAndDish(User user, Dish dish);

    long countByDish(Dish dish);

    @Query("SELECT AVG(r.rating) FROM DishReview r WHERE r.dish = :dish")
    Double averageRatingByDish(@Param("dish") Dish dish);

    List<DishReview> findByUser(User user);
}

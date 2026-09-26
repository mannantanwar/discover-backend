package com.discover.backend.recommendation;

import com.discover.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/places/{placePublicId}/recommendation")
public class RecommendationController {
    private final DishRecommendationService dishRecommendationService;

    @GetMapping("/{type}")
    public List<RecommendedDishDto> getRecommendedDishes(@AuthenticationPrincipal User user, @PathVariable UUID placePublicId, @PathVariable RecommendationType type) {
        return dishRecommendationService.getRecommendedDishes(user, placePublicId, type);
    }
}

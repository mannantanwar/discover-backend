package com.discover.backend.dishreview;

import com.discover.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dishes/{dishPublicId}/reviews")
@RequiredArgsConstructor
public class DishReviewController {

    private final DishReviewService dishReviewService;

    @GetMapping
    public List<DishReviewDto> getReviewsForDish(@PathVariable UUID dishPublicId) {
        return dishReviewService.getReviewsForDish(dishPublicId);
    }

    @PostMapping
    public DishReviewDto addReview(@AuthenticationPrincipal User user, @PathVariable UUID dishPublicId,
                                    @RequestBody DishReviewRequest request) {
        return dishReviewService.addOrUpdateReview(user, dishPublicId, request.getRating(), request.getText());
    }

    @GetMapping("/stats")
    public DishStatsDto getStatsForDish(@PathVariable UUID dishPublicId) {
        return dishReviewService.getStatsForDish(dishPublicId);
    }
}
